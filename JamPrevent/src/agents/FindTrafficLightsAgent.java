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
 *Class to be inherited from by all classes which want to find all TrafficLights in the yellow pages.
 * @author SiB
 */
public class FindTrafficLightsAgent extends BaseAgent {
    
    protected final List<AID> trafficLightAgents = new ArrayList<>();

    @Override
    protected void setup() {
        super.setup(); //To change body of generated methods, choose Tools | Templates.
        addBehaviour(new FindAndAddTrafficLightsBehaviour(this, 1000));
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
}
