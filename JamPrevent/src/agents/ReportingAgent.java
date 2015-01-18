/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author knut
 */
public class ReportingAgent extends Agent{
    private final List<AID> trafficLightAgents = new ArrayList<>();
    private int index = 0;
    private String pathToDump;
    private File dumpFile;
    @Override
    protected void setup() {
        super.setup(); //To change body of generated methods, choose Tools | Templates.
        
        Object[] arguments = getArguments();        
        
        if(arguments.length > 0){
            pathToDump = arguments[0].toString();
            dumpFile = new File(pathToDump + "\\state.json");
        }        
        
        
        addBehaviour(new WakerBehaviour(this, 1000) {

            @Override
            protected void onWake() {
                super.onWake(); //To change body of generated methods, choose Tools | Templates.
                findAndAddTrafficLights();
            }                        
        });
        
        addBehaviour(new TickerBehaviour(this, 600) {

            @Override
            public void onTick() {
                    RequestTrafficLightsToDumpProperties();
            }
        });
        addBehaviour(new TickerBehaviour(this, 100) {

            @Override
            public void onTick() {
                     MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);

                    ACLMessage msg = receive(mt);
                    if (msg != null) {
//                        System.out.println(msg.getSender().getLocalName() + " sent to " + myAgent.getLocalName() + " -> " + msg.getContent());

                        if (msg.getContent().equalsIgnoreCase("dumpProperties")) {
                            if (msg.getAllUserDefinedParameters().size() > 0) {
                               /* if (trafficLightsMetadata.containsKey(msg.getSender())) {
                                    trafficLightsMetadata.get(msg.getSender()).put("location", msg.getUserDefinedParameter("location"));
                                    trafficLightsMetadata.get(msg.getSender()).put("direction", msg.getUserDefinedParameter("direction"));
                                }*/
                                String output = "[\n{\"location\": \"" + msg.getUserDefinedParameter("location").toLowerCase() + "\",\n\"direction\": \"" + msg.getUserDefinedParameter("direction").toLowerCase() + "\",\n\"state\": \"" + msg.getUserDefinedParameter("state").toLowerCase() + "\"\n}\n]";                                
                                try {
                                    try (FileOutputStream file = new FileOutputStream(dumpFile)) {
                                        file.write(output.getBytes());
                                    }
                                } catch (IOException ex) {
                                    Logger.getLogger(ReportingAgent.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                
//                                System.out.println(msg.getUserDefinedParameter("index") 
//                                        + ": " 
//                                        + msg.getUserDefinedParameter("location")
//                                        + " "
//                                        + msg.getUserDefinedParameter("direction")
//                                        + " "
//                                        + msg.getUserDefinedParameter("state")
//                                );
                            }
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
            message.addUserDefinedParameter("index", String.valueOf(index));
            message.setContent("dumpProperties");
            this.send(message);
            index++;
        }
    
}
