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
    private String trafficState;

    @Override
    public void setup() {

        Object[] arguments = getArguments();
        trafficState = "red";

        if (arguments.length > 0) {
            location = arguments[0].toString();
            direction = arguments[1].toString();
        }
        addBehaviour(new TellLocationBehaviour());
        addBehaviour(new ReceiveAndSetStateBehaviour());        

        registerAgent();
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
                    reply.setContent("getLocationAndDirection");
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.addUserDefinedParameter("location", getLocation());
                    reply.addUserDefinedParameter("direction", getDirection());
                } else if (message.equals("dumpProperties")) {
                    reply.setContent("dumpProperties");
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.addUserDefinedParameter("location", getLocation());
                    reply.addUserDefinedParameter("direction", getDirection());
                    reply.addUserDefinedParameter("state", getTrafficState());
                    reply.addUserDefinedParameter("index", m.getUserDefinedParameter("index"));

                }
                
                this.myAgent.send(reply);
            }
        }

    }

    private class ReceiveAndSetStateBehaviour extends CyclicBehaviour {

        private static final long serialVersionUID = -5018397038252983135L;

        @Override
        public void action() {
            MessageTemplate tmp = MessageTemplate
                    .MatchPerformative(ACLMessage.PROPOSE);
            ACLMessage m = receive(tmp);

            if (m != null) {
                String message = m.getContent();
                if (message.equals("setState")) {
                    Date nextUpdate = new Date(Long.parseLong(m.getUserDefinedParameter("nextUpdate")));
                    addBehaviour(new WakerBehaviour(myAgent, nextUpdate) {

                        @Override
                        protected void onWake() {
                            super.onWake(); //To change body of generated methods, choose Tools | Templates.
                            setTrafficState(m.getUserDefinedParameter("state"));
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

    public void setTrafficState(String trafficState) {
        this.trafficState = trafficState;
    }

    public String getLocation() {
        return location;
    }

    public String getTrafficState() {
        return trafficState;
    }

    private String getDirection() {
        return direction;
    }

}
