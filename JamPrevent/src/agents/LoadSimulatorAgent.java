/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import messages.TrafficLightLoadSimulation;

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

    public class FindTrafficLightsBehaviour extends SequentialBehaviour {

        public FindTrafficLightsBehaviour() {
            addSubBehaviour(new WakerBehaviour(this.myAgent, 1000) {
                @Override
                protected void onWake() {
                    super.onWake(); //To change body of generated methods, choose Tools | Templates.
                    findAndAddTrafficLights();
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
    }

    public class DefaultExecutionBehaviour extends SequentialBehaviour {

        public DefaultExecutionBehaviour() {
            addSubBehaviour(new FindTrafficLightsBehaviour());
            addSubBehaviour(new SetStateBehaviour(this.myAgent, 2500));
        }

        private class SetStateBehaviour extends TickerBehaviour {

            public SetStateBehaviour(Agent a, long period) {
                super(a, period);
            }

            @Override
            public void onTick() {
                for (AID trafficLight : trafficLightAgents) {
                    sendTrafficLightAdditionalCars(trafficLight, randInt(0, 5));
                }
            }
        }

        private void sendTrafficLightAdditionalCars(AID trafficLight, int count) {

            TrafficLightLoadSimulation tlls = new TrafficLightLoadSimulation();
            tlls.setAdditionalCars(count);

            jade.lang.acl.ACLMessage message = new jade.lang.acl.ACLMessage(
                    jade.lang.acl.ACLMessage.PROPOSE);
            message.addReceiver(trafficLight);

            try {
                message.setContentObject(tlls);
            } catch (IOException ex) {
                Logger.getLogger(LoadSimulatorAgent.class.getName()).log(Level.SEVERE, null, ex);
            }

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
