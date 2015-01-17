/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

/**
 *
 * @author SiB
 */
public class Auctioneer extends Agent {

    private final List<AID> trafficLightAgents = new ArrayList<>();
    private HashMap<AID, HashMap<String, String>> trafficLightsMetadata = new HashMap<>(); 

    @Override
    public void setup() {
        addBehaviour(new DefaultExecutionBehaviour());
    }

    private void SendTrafficLightNewState(AID trafficLight, String state) {
        jade.lang.acl.ACLMessage message = new jade.lang.acl.ACLMessage(
                jade.lang.acl.ACLMessage.PROPOSE);
        message.addReceiver(trafficLight);
        message.addUserDefinedParameter("state", state);
        message.setContent("setState");
        this.send(message);
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
                        AskTrafficLightForLocationAndDirection(agent);
                    });
                }
            });
            addSubBehaviour(new SimpleBehaviour(this.myAgent) {

                @Override
                public boolean done() {                    
                    for(AID agent : trafficLightAgents){                        
                        if(trafficLightsMetadata.containsKey(agent)){
                            if(trafficLightsMetadata.get(agent).containsKey("location") && trafficLightsMetadata.get(agent).containsKey("direction")){                                                    
                                if(trafficLightsMetadata.get(agent).get("location").isEmpty() && trafficLightsMetadata.get(agent).get("direction").isEmpty()){
                                    return false;
                                }
                            }else{
                                return false;
                            }
                        }else{
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

        private void AskTrafficLightForLocationAndDirection(AID trafficLight) {
            jade.lang.acl.ACLMessage message = new jade.lang.acl.ACLMessage(
                    jade.lang.acl.ACLMessage.REQUEST);
            message.addReceiver(trafficLight);
            message.setContent("getLocationAndDirection");
            this.myAgent.send(message);
        }

    }
    
    public class DefaultExecutionBehaviour extends SequentialBehaviour{

        public DefaultExecutionBehaviour() {
            addSubBehaviour(new FindTrafficLightsAndGetMetaDataBehaviour());
            addSubBehaviour(new SetStateBehaviour());
        }
        
        private class SetStateBehaviour extends CyclicBehaviour{
            @Override
            public void action() {
                for (AID trafficLight : trafficLightAgents) {
                    
                    
                    SendTrafficLightNewState(trafficLight, "green");
                }
            }
        }
        
    }

}
