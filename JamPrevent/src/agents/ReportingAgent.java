/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import messages.TrafficLightLoadSimulation;
import messages.TrafficLightProperties;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author sib
 */
public class ReportingAgent extends BaseAgent {

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
            dumpFile = new File(pathToDump, "state.json");
        }

        //Find all known TrafficLights
        addBehaviour(new FindAndAddTrafficLightsBehaviour(this, 1000));

        //requesting known TrafficLights to dump their properies to me
        addBehaviour(new RequestTrafficLightsToDumpPropertiesBehaviour(this, 100));

        //listening to messages of TrafficLights
        addBehaviour(new ReceiveMessagesBehaviour());
    }

    private class HandleTrafficLightPropertiesInform extends OneShotBehaviour {

        private final ACLMessage msg;

        public HandleTrafficLightPropertiesInform(Agent myAgent, ACLMessage msg) {
            super(myAgent);
            this.msg = msg;
        }

        @Override
        public void action() {

            TrafficLightProperties tlp;
            try {
                ContentElement content = getContentManager().extractContent(msg);
                Concept action = ((Action)content).getAction();                
                tlp = (TrafficLightProperties) action;

                if (tlp.getIndex() == requestIndex) {
                    JSONObject currentTrafficLight = new JSONObject();
                    currentTrafficLight.put("location", tlp.getLocation().toLowerCase());
                    currentTrafficLight.put("direction", tlp.getDirection().toLowerCase());
                    currentTrafficLight.put("state", tlp.getTrafficState().toLowerCase());
                    currentTrafficLight.put("load", tlp.getCarCount());
                    trafficLightStates.add(currentTrafficLight);
                    receivedAnswersPerIndex++;
                }

                if (receivedAnswersPerIndex == trafficLightAgents.size()) {
                    addBehaviour(new OneShotBehaviour() {

                        @Override
                        public void action() {
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
                    });
                }            
            } catch (Codec.CodecException ex) {
                Logger.getLogger(ReportingAgent.class.getName()).log(Level.SEVERE, null, ex);
            } catch (OntologyException ex) {
                Logger.getLogger(ReportingAgent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private class ReceiveMessagesBehaviour extends CyclicBehaviour {

        private static final long serialVersionUID = -5018397038252984135L;

        @Override
        public void action() {

            ACLMessage msg = receive();
            if (msg == null) {
                block();
                return;
            }
            try {
                ContentElement content = getContentManager().extractContent(msg);
                Concept action = ((Action)content).getAction();

                switch (msg.getPerformative()) {

                    case (ACLMessage.INFORM):

                        System.out.println("Request from " + msg.getSender().getLocalName());

                        if (action instanceof TrafficLightProperties) {
                            addBehaviour(new HandleTrafficLightPropertiesInform(myAgent, msg));
                        } else {
                            replyNotUnderstood(msg);
                        }
                        break;

                    default:
                        replyNotUnderstood(msg);
                }
            } catch (Exception ex) {
            }
        }
    }

    private class FindAndAddTrafficLightsBehaviour extends WakerBehaviour {

        public FindAndAddTrafficLightsBehaviour(Agent a, long timeout) {
            super(a, timeout);
        }

        @Override
        protected void onWake() {
            super.onWake(); //To change body of generated methods, choose Tools | Templates.
            findAndAddTrafficLights();
        }

        private void findAndAddTrafficLights() {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("TrafficLight-Service");
            template.addServices(sd);

            try {
                DFAgentDescription[] dfds = DFService.search(this.myAgent, template);

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
    }

    private class RequestTrafficLightsToDumpPropertiesBehaviour extends TickerBehaviour {

        public RequestTrafficLightsToDumpPropertiesBehaviour(Agent a, long period) {
            super(a, period);
        }

        @Override
        public void onTick() {
            requestTrafficLightsToDumpProperties();
        }

        private void requestTrafficLightsToDumpProperties() {

            TrafficLightProperties tlp = new TrafficLightProperties();
            tlp.setIndex(requestIndex);
            jade.lang.acl.ACLMessage message = new jade.lang.acl.ACLMessage(
                    jade.lang.acl.ACLMessage.REQUEST);
            
            message.setLanguage(codec.getName());
            message.setOntology(ontology.getName());
            
            trafficLightAgents.stream().forEach((trafficLight) -> {
                try {
                    getContentManager().fillContent(message, new Action(trafficLight, tlp));                
                    message.addReceiver(trafficLight);
                } catch (Codec.CodecException ex) {
                    Logger.getLogger(ReportingAgent.class.getName()).log(Level.SEVERE, null, ex);
                } catch (OntologyException ex) {
                    Logger.getLogger(ReportingAgent.class.getName()).log(Level.SEVERE, null, ex);
                }
            });

                        
            
//            try {
//                message.setContentObject(tlp);
//            } catch (IOException ex) {
//                Logger.getLogger(ReportingAgent.class.getName()).log(Level.SEVERE, null, ex);
//            }

            this.myAgent.send(message);
        }
    }

}
