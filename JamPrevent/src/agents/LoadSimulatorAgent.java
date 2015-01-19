/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 *
 * @author knut
 */
public class LoadSimulatorAgent extends Agent {
    
    
        private final List<AID> trafficLightAgents = new ArrayList<>();
        private final HashMap<AID, HashMap<String, String>> trafficLightsMetadata = new HashMap<>();
        
    @Override
    public void setup() {
        addBehaviour(new DefaultExecutionBehaviour());
    }
        
        public class FindTrafficLightsAndGetMetaDataBehaviour extends SequentialBehaviour {

        public FindTrafficLightsAndGetMetaDataBehaviour() {
            addSubBehaviour(new WakerBehaviour(this.myAgent, 1000) {
                @Override
                protected void onWake() {
                    super.onWake(); //To change body of generated methods, choose Tools | Templates.
                    findAndAddTrafficLights();
                    trafficLightAgents.stream().forEach((agent) -> {
                        trafficLightsMetadata.put(agent, new HashMap<String, String>());
                        System.out.println("Found Trafficlight: " + agent.getLocalName());
                        askTrafficLightForLocationAndDirection(agent);
                    });
                }
            });
            
            addSubBehaviour(new SimpleBehaviour(this.myAgent) {

                @Override
                public boolean done() {
                    for (AID agent : trafficLightAgents) {
                        if (trafficLightsMetadata.containsKey(agent)) {
                            if (trafficLightsMetadata.get(agent).containsKey("location") && trafficLightsMetadata.get(agent).containsKey("direction")) {
                                if (trafficLightsMetadata.get(agent).get("location").isEmpty() && trafficLightsMetadata.get(agent).get("direction").isEmpty()) {
                                    return false;
                                }
                            } else {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    }

                    return true;
                }

                @Override
                public void action() {
                    MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);

                    ACLMessage msg = receive(mt);
                    if (msg != null) {
                        System.out.println(msg.getSender().getLocalName() + " sent to " + myAgent.getLocalName() + " -> " + msg.getContent());

                        if (msg.getContent().equalsIgnoreCase("getLocationAndDirection")) {
                            if (msg.getAllUserDefinedParameters().size() > 0) {
                                if (trafficLightsMetadata.containsKey(msg.getSender())) {
                                    trafficLightsMetadata.get(msg.getSender()).put("location", msg.getUserDefinedParameter("location"));
                                    trafficLightsMetadata.get(msg.getSender()).put("direction", msg.getUserDefinedParameter("direction"));
                                }
                                System.out.println(msg.getUserDefinedParameter("location") + " " + msg.getUserDefinedParameter("direction"));
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

        private void askTrafficLightForLocationAndDirection(AID trafficLight) {
            jade.lang.acl.ACLMessage message = new jade.lang.acl.ACLMessage(
                    jade.lang.acl.ACLMessage.REQUEST);
            message.addReceiver(trafficLight);
            message.setContent("getLocationAndDirection");
            this.myAgent.send(message);
        }

    }
        
        public class DefaultExecutionBehaviour extends SequentialBehaviour {

        public DefaultExecutionBehaviour() {
            addSubBehaviour(new FindTrafficLightsAndGetMetaDataBehaviour());
            addSubBehaviour(new SetStateBehaviour(this.myAgent, 2500));
        }

        private class SetStateBehaviour extends TickerBehaviour {
            private String activeDirection = "SE";

            public SetStateBehaviour(Agent a, long period) {
                super(a, period);
            }

            @Override
            public void onTick() {

                long t=new Date().getTime();
                Date nextUpdate = new Date(t + 1000);
                
                for (AID trafficLight : trafficLightAgents) {
                    SendTrafficLightAdditionalCars(trafficLight, randInt(0, 5));
                    }

                }
            }
        
        

            private void SendTrafficLightAdditionalCars(AID trafficLight, int count) {
                jade.lang.acl.ACLMessage message = new jade.lang.acl.ACLMessage(
                        jade.lang.acl.ACLMessage.INFORM);
                message.addReceiver(trafficLight);
                message.addUserDefinedParameter("additionalCars", String.valueOf(count));
                message.setContent("registerAdditionalCars");
                this.myAgent.send(message);
            }

    }

        public static int randInt(int min, int max) {

    // NOTE: Usually this should be a field rather than a method
    // variable so that it is not re-seeded every call.
    Random rand = new Random();

    // nextInt is normally exclusive of the top value,
    // so add 1 to make it inclusive
    int randomNum = rand.nextInt((max - min) + 1) + min;

    return randomNum;
}
        
}



