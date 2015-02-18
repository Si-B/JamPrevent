/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.TickerBehaviour;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import messages.TrafficLightLoadSimulation;

/**
 *Simulates arriving cars at all known TrafficLights. Uses a random number from 0 to 10.
 * @author knut
 */
public class LoadSimulatorAgent extends FindTrafficLightsAgent {

    @Override
    public void setup() {
        super.setup();
        addBehaviour(new DefaultExecutionBehaviour());
    }

    public class DefaultExecutionBehaviour extends SequentialBehaviour {

        public DefaultExecutionBehaviour() {
            addSubBehaviour(new SimulateLoadBehaviour(this.myAgent, 1000));
        }

        private class SimulateLoadBehaviour extends TickerBehaviour {

            public SimulateLoadBehaviour(Agent a, long period) {
                super(a, period);
            }

            @Override
            public void onTick() {
                for (AID trafficLight : trafficLightAgents) {
                    sendTrafficLightAdditionalCars(trafficLight, randInt(0, 10));
                }
            }
        } 
        
        private void sendTrafficLightAdditionalCars(AID trafficLight, int count) {

            try {
                TrafficLightLoadSimulation tlls = new TrafficLightLoadSimulation();
                tlls.setAdditionalCars(count);
                
                jade.lang.acl.ACLMessage message = new jade.lang.acl.ACLMessage(
                        jade.lang.acl.ACLMessage.PROPOSE);
                
                message.setLanguage(codec.getName());
                message.setOntology(ontology.getName());
                
                getContentManager().fillContent(message, new Action(trafficLight, tlls));
                
                message.addReceiver(trafficLight);               
                
                this.myAgent.send(message);
            } catch (Codec.CodecException ex) {
                Logger.getLogger(LoadSimulatorAgent.class.getName()).log(Level.SEVERE, null, ex);
            } catch (OntologyException ex) {
                Logger.getLogger(LoadSimulatorAgent.class.getName()).log(Level.SEVERE, null, ex);
            }
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
