/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author SiB
 */
public class Auctioneer extends Agent {
    
    private final List<AID> trafficLightAgents = new ArrayList<>();

    @Override
    public void setup() {                
        addBehaviour(new WakerBehaviour(this, 1000) {

            @Override
            protected void onWake() {
                super.onWake(); //To change body of generated methods, choose Tools | Templates.
                findAndAddTrafficLights();
                trafficLightAgents.stream().forEach((agent) -> {
                    System.out.println("Found Trafficlight: " + agent.getLocalName());
                });
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
                for(DFAgentDescription trafficLightAgentDescription :  dfds){
                    AID trafficLightAgent = trafficLightAgentDescription.getName();
                    trafficLightAgents.add(trafficLightAgent);
                }                                                
            }                      
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

}
