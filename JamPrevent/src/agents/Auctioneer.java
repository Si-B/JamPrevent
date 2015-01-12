/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
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
        addBehaviour(new WakerBehaviour(this, 1000) {
            @Override
            protected void onWake() {
                super.onWake(); //To change body of generated methods, choose Tools | Templates.
                findAndAddTrafficLights();
                trafficLightAgents.stream().forEach((agent) -> {
                    trafficLightsMetadata.put(agent, new HashMap<String, String>());
                    System.out.println("Found Trafficlight: " + agent.getLocalName());
                    AskTrafficLightForPosition(agent);
                    //System.out.println("which is: " + agent.get());

                });
            }
        });
        
        addBehaviour(new WakerBehaviour(this, 2500) {

            @Override
            protected void onWake() {
                super.onWake(); //To change body of generated methods, choose Tools | Templates.
                
            }                            
        });

        //receiving
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);

                ACLMessage msg = receive(mt);
                if (msg != null) {
                    System.out.println(msg.getSender().getLocalName() + " sent to " + myAgent.getLocalName() + " -> " + msg.getContent());

                    if (msg.getContent().equalsIgnoreCase("getLocation")) {
                        if (msg.getAllUserDefinedParameters().size() > 0) {
                            if (trafficLightsMetadata.containsKey(msg.getSender())) {
                                trafficLightsMetadata.get(msg.getSender()).put("location", msg.getAllUserDefinedParameters().getProperty("location"));
                            }
                            System.out.println(msg.getAllUserDefinedParameters().getProperty("location"));
                        }
                    }
                }
            }
        });
    }

    private void AskTrafficLightForPosition(AID trafficLight) {
        jade.lang.acl.ACLMessage message = new jade.lang.acl.ACLMessage(
                jade.lang.acl.ACLMessage.REQUEST);
        message.addReceiver(trafficLight);
        message.setContent("getLocation");
        this.send(message);
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

}
