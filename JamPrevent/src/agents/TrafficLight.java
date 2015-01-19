/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.Date;

/**
 *
 * @author SiB
 */
public class TrafficLight extends Agent {

    private String location;
    private String direction;
    private String state;
    private int carCount;

    @Override
    public void setup() {

        Object[] arguments = getArguments();
        state = "red";

        if (arguments.length > 0) {
            location = arguments[0].toString();
            direction = arguments[1].toString();
        }
        addBehaviour(new TellLocationBehaviour());
        addBehaviour(new ReceiveAndSetStateBehaviour());
        addBehaviour(new AddCarsBehaviour());

        //  addBehaviour(new DumpPropertiesBehaviour(this, 1000));


        registerAgent();
    }
    
    private class AddCarsBehaviour extends CyclicBehaviour {

        @Override
        public void action() {
            MessageTemplate tmp = MessageTemplate
                    .MatchPerformative(ACLMessage.INFORM);
            ACLMessage m = receive(tmp);

            if (m != null) {
                //ACLMessage reply = m.createReply();
                String message = m.getContent();

                if (message.equals("registerAdditionalCars")) {
                    addAddditionalCars(Integer.parseInt(m.getUserDefinedParameter("additionalCars")));
                }
            }
        }
        
    }

    private class TellLocationBehaviour extends CyclicBehaviour {

        private static final long serialVersionUID = -5018397038252984135L;

        public void action() {
            MessageTemplate tmp = MessageTemplate
                    .MatchPerformative(ACLMessage.REQUEST);
            ACLMessage m = receive(tmp);

            if (m != null) {
                ACLMessage reply = m.createReply();
                String message = m.getContent();

                if (message.equals("getLocationAndDirection")) {
//                    System.out.println("SENDING LOCATION");
                    reply.setContent("getLocationAndDirection");
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.addUserDefinedParameter("location", getLocation());
                    reply.addUserDefinedParameter("direction", getDirection());
                } else if (message.equals("dumpProperties")) {
                    // System.out.println("DUMPING");
                    reply.setContent("dumpProperties");
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.addUserDefinedParameter("location", getLocation());
                    reply.addUserDefinedParameter("direction", getDirection());
                    reply.addUserDefinedParameter("state", getTrafficState());
                    reply.addUserDefinedParameter("carCount", getCarCount());
                    reply.addUserDefinedParameter("index", m.getUserDefinedParameter("index"));
                }
                
                /* else {
                 System.out.println("not understood");
                 reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                 // Reply with not-understood
                 }*/
                this.myAgent.send(reply);
            }
        }

    }

    private class ReceiveAndSetStateBehaviour extends CyclicBehaviour {

        private static final long serialVersionUID = -5018397038252983135L;

        public void action() {
            MessageTemplate tmp = MessageTemplate
                    .MatchPerformative(ACLMessage.PROPOSE);
            ACLMessage m = receive(tmp);

            if (m != null) {
                String message = m.getContent();

                if (message.equals("setState")) {
//                    System.out.println("setting state");

                    Date nextUpdate = new Date(Long.parseLong(m.getUserDefinedParameter("nextUpdate")));
                    addBehaviour(new WakerBehaviour(myAgent, nextUpdate) {

                        @Override
                        protected void onWake() {
                            super.onWake(); //To change body of generated methods, choose Tools | Templates.
                            setState(m.getUserDefinedParameter("state"));
                        }

                    });
                }
                

            }
        }
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

    public void setState(String state) {
        this.state = state;
        if (state.equalsIgnoreCase("green")){
            this.carCount = this.carCount - 3 > 0? this.carCount -3 : 0;
            
        }
//        System.out.println(this.getLocalName() + " new state is: " + state);
    }

    public String getLocation() {
        return location;
    }
    
    public String getCarCount(){
        return String.valueOf(carCount);
    }
    
    public void addAddditionalCars(int carCount){
        this.carCount += carCount;
    }

    public String getTrafficState() {
        return state;
    }

    private String getDirection() {
        return direction;
    }

}
