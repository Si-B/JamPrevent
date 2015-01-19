/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author sib
 */
public class ReportingAgent extends Agent {

    private final List<AID> trafficLightAgents = new ArrayList<>();
    private final ArrayList<JSONObject> trafficLightStates = new ArrayList<>();
    private int requestIndex = 0;
    private int receivedAnswersPerIndex = 0;
    private String pathToDump;
    private File dumpFile;

    @Override
    protected void setup() {
        super.setup(); //To change body of generated methods, choose Tools | Templates.

        Object[] arguments = getArguments();

        if (arguments.length > 0) {
            pathToDump = arguments[0].toString();
            dumpFile = new File(pathToDump + "\\state.json");
        }

        //Find all known TrafficLights
        addBehaviour(new WakerBehaviour(this, 1000) {

            @Override
            protected void onWake() {
                super.onWake(); //To change body of generated methods, choose Tools | Templates.
                findAndAddTrafficLights();
            }
        });
        //requesting known TrafficLights to dump their properies to me
        addBehaviour(new TickerBehaviour(this, 100) {

            @Override
            public void onTick() {
                RequestTrafficLightsToDumpProperties();
            }
        });
        
        //listening to messages of TrafficLights
        addBehaviour(new CyclicBehaviour(this) {
                       
            @Override
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                ACLMessage msg = receive(mt);
                if (msg != null) {
                    if (msg.getContent().equalsIgnoreCase("dumpProperties")) {
                        if (msg.getAllUserDefinedParameters().size() > 0) {

                            if (Integer.parseInt(msg.getUserDefinedParameter("index")) == requestIndex) {
                                JSONObject currentTrafficLight = new JSONObject();
                                currentTrafficLight.put("location", msg.getUserDefinedParameter("location").toLowerCase());
                                currentTrafficLight.put("direction", msg.getUserDefinedParameter("direction").toLowerCase());
                                currentTrafficLight.put("state", msg.getUserDefinedParameter("state").toLowerCase());
                                trafficLightStates.add(currentTrafficLight);
                                receivedAnswersPerIndex++;
                            }
                        }
                    }

                    if (receivedAnswersPerIndex == trafficLightAgents.size()) {

                        JSONArray outputValues = new JSONArray();

                        for (JSONObject currentTrafficLightState : trafficLightStates) {
                            outputValues.add(currentTrafficLightState);
                        }

                        try {
                            try (FileOutputStream file = new FileOutputStream(dumpFile)) {
                                file.write(outputValues.toJSONString().getBytes());
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(ReportingAgent.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        receivedAnswersPerIndex = 0;
                        trafficLightStates.clear();
                        requestIndex++;
                    }
                }
            }

        });
    }

    private void findAndAddTrafficLights() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("TrafficLight-Service");
        template.addServices(sd);

        try {
            DFAgentDescription[] dfds = DFService.search(this, template);

            if (dfds.length > 0) {
                for (DFAgentDescription trafficLightAgentDescription : dfds) {
                    AID trafficLightAgent = trafficLightAgentDescription.getName();
                    trafficLightAgents.add(trafficLightAgent);
                }
            }
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    private void RequestTrafficLightsToDumpProperties() {
        jade.lang.acl.ACLMessage message = new jade.lang.acl.ACLMessage(
                jade.lang.acl.ACLMessage.REQUEST);
        trafficLightAgents.stream().forEach((trafficLight) -> {
            message.addReceiver(trafficLight);
        });
        message.addUserDefinedParameter("index", String.valueOf(requestIndex));
        message.setContent("dumpProperties");
        this.send(message);
    }

}
