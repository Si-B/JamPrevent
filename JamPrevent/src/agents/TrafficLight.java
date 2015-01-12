/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

/**
 *
 * @author SiB
 */
public class TrafficLight extends Agent{

    @Override
    public void setup() {
        registerAgent();
    }
        
    private void registerAgent() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(this.getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType("TrafficLight-Service");
        sd.setName(this.getLocalName() + "-Reply-To-TrafficLight-Service");

        dfd.addServices(sd);

        try {
                DFService.register(this, dfd);
        } catch (FIPAException fe) {
                fe.printStackTrace();
        }
    }
    
}
