/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import messages.TrafficLightLoadSimulation;
import messages.TrafficLightLocationAndDirection;
import messages.TrafficLightOffer;
import messages.TrafficLightProperties;
import messages.TrafficLightState;

/**
 *
 * @author SiB
 */
public class TrafficLight extends BaseAgent {

    private String location = "";
    private String direction = "";
    private String trafficState = "";
    private int carCount = 0;
    private Date lastGreenTime;
    private String crossLocation = "";

    @Override
    public void setup() {
        super.setup();
        Object[] arguments = getArguments();
        trafficState = "red";

        if (arguments.length > 0) {
            location = arguments[0].toString();
            direction = arguments[1].toString();
            crossLocation = arguments[2].toString();
        }
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
                MessageTemplate.MatchPerformative(ACLMessage.CFP));

        addBehaviour(new ContractNetResponder(this, template) {
            @Override
            protected ACLMessage prepareResponse(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
                try {
                    System.out.println("Agent " + getLocalName() + ": CFP received from " + cfp.getSender().getName() + ". Action is " + cfp.getContent());

                    ContentElement content = getContentManager().extractContent(cfp);
                    Concept action = ((Action) content).getAction();
                    TrafficLightOffer tlo = (TrafficLightOffer) action;
                    tlo.setCarCount(getCarCount());
                    tlo.setLastGreenTime(getLastGreenTime());

                    ACLMessage reply = cfp.createReply();
                    reply.setPerformative(ACLMessage.PROPOSE);

                    getContentManager().fillContent(reply, new Action(cfp.getSender(), tlo));
                    System.out.println("Agent " + getLocalName() + ": Proposing " + carCount);

                    return reply;
                } catch (Codec.CodecException ex) {
                    Logger.getLogger(TrafficLight.class.getName()).log(Level.SEVERE, null, ex);
                } catch (OntologyException ex) {
                    Logger.getLogger(TrafficLight.class.getName()).log(Level.SEVERE, null, ex);
                }
                return null;
            }

            @Override
            protected ACLMessage prepareResultNotification(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
                System.out.println("Agent " + getLocalName() + ": Proposal accepted");
                System.out.println("Agent " + getLocalName() + ": Action successfully performed");

                ACLMessage inform = accept.createReply();
                inform.setPerformative(ACLMessage.INFORM);
                return inform;
            }                    

            @Override
            protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
                System.out.println("Agent " + getLocalName() + ": Proposal rejected");
                setTrafficState("red");
            }

            @Override
            protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
                setTrafficState("green");
                ACLMessage reply = accept.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                return reply;
            }

        });

        addBehaviour(new ReceiveMessagesBehaviour());

        registerAgent("TrafficLight-Service");
    }

    /**
     * @return the crossLocation
     */
    public String getCrossLocation() {
        return crossLocation;
    }

    /**
     * @param crossLocation the crossLocation to set
     */
    public void setCrossLocation(String crossLocation) {
        this.crossLocation = crossLocation;
    }
    
    private class HandleTrafficLightLoadSimulationPropose extends OneShotBehaviour {

        private final ACLMessage msg;

        public HandleTrafficLightLoadSimulationPropose(Agent myAgent, ACLMessage msg) {
            super(myAgent);
            this.msg = msg;
        }

        @Override
        public void action() {
            try {
                ContentElement content = getContentManager().extractContent(msg);
                Concept action = ((Action) content).getAction();
                TrafficLightLoadSimulation tlls = (TrafficLightLoadSimulation) action;
                addAddditionalCars(tlls.getAdditionalCars());

                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.CONFIRM);

                send(reply);
//                System.out.println("TrafficLightProperties sent!");
            } catch (Codec.CodecException ex) {
                Logger.getLogger(TrafficLight.class.getName()).log(Level.SEVERE, null, ex);
            } catch (OntologyException ex) {
                Logger.getLogger(TrafficLight.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private class ReceiveMessagesBehaviour extends CyclicBehaviour {

        private static final long serialVersionUID = -5018397038252984135L;

        @Override
        public void action() {

            MessageTemplate mt
                    = MessageTemplate.or(
                            MessageTemplate.MatchPerformative(ACLMessage.PROPOSE), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

            ACLMessage msg = receive(mt);
            if (msg == null) {
                block();
                return;
            }
            try {
                ContentElement content = getContentManager().extractContent(msg);
                Concept action = ((Action) content).getAction();

                switch (msg.getPerformative()) {

                    case (ACLMessage.REQUEST):

//                        System.out.println("Request from " + msg.getSender().getLocalName());
                        if (action instanceof TrafficLightLocationAndDirection) {
                            addBehaviour(new HandleTrafficLightLocationAndDirectionRequest(myAgent, msg));
                        } else if (action instanceof TrafficLightProperties) {
                            addBehaviour(new HandleTrafficLightPropertiesRequest(myAgent, msg));
                        } else {
                            replyNotUnderstood(msg);
                        }
                        break;

                    case (ACLMessage.PROPOSE):
                        if (action instanceof TrafficLightState) {
                            addBehaviour(new HandleTrafficLightStatePropose(myAgent, msg));
                        } else if (action instanceof TrafficLightLoadSimulation) {
                            addBehaviour(new HandleTrafficLightLoadSimulationPropose(myAgent, msg));
                        }
                        break;
                }
            } catch (Exception ex) {
            }
        }
    }

    private class HandleTrafficLightStatePropose extends OneShotBehaviour {

        private final ACLMessage msg;

        public HandleTrafficLightStatePropose(Agent myAgent, ACLMessage msg) {
            super(myAgent);
            this.msg = msg;
        }

        @Override
        public void action() {
            try {
                TrafficLightState tlp;

                ContentElement content = getContentManager().extractContent(msg);
                Concept action = ((Action) content).getAction();
                tlp = (TrafficLightState) action;

                addBehaviour(new WakerBehaviour(myAgent, tlp.getNextUpdate()) {

                    @Override
                    protected void onWake() {
                        super.onWake();
                        setTrafficState(tlp.getTrafficState());
                    }
                });

                ACLMessage reply = msg.createReply();

                reply.setPerformative(ACLMessage.CONFIRM);
                send(reply);
//                System.out.println("TrafficLightState received!");
            } catch (Codec.CodecException ex) {
                Logger.getLogger(TrafficLight.class.getName()).log(Level.SEVERE, null, ex);
            } catch (OntologyException ex) {
                Logger.getLogger(TrafficLight.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private class HandleTrafficLightPropertiesRequest extends OneShotBehaviour {

        private final ACLMessage msg;

        public HandleTrafficLightPropertiesRequest(Agent myAgent, ACLMessage msg) {
            super(myAgent);
            this.msg = msg;
        }

        @Override
        public void action() {
            try {
                ContentElement content = getContentManager().extractContent(msg);
                Concept action = ((Action) content).getAction();
                TrafficLightProperties tlp = (TrafficLightProperties) action;
                tlp.setLocation(getLocation());
                tlp.setDirection(getDirection());
                tlp.setCarCount(getCarCount());
                tlp.setTrafficState(getTrafficState());
                tlp.setCrossLocation(getCrossLocation());
                ACLMessage reply = msg.createReply();

                reply.setLanguage(codec.getName());
                reply.setOntology(ontology.getName());

                reply.setPerformative(ACLMessage.PROPAGATE);
                getContentManager().fillContent(reply, new Action(msg.getSender(), tlp));

                send(reply);
//                System.out.println("TrafficLightProperties sent!");
            } catch (Codec.CodecException ex) {
                Logger.getLogger(TrafficLight.class.getName()).log(Level.SEVERE, null, ex);
            } catch (OntologyException ex) {
                Logger.getLogger(TrafficLight.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private class HandleTrafficLightLocationAndDirectionRequest extends OneShotBehaviour {

        private final ACLMessage msg;

        public HandleTrafficLightLocationAndDirectionRequest(Agent agent, ACLMessage msg) {
            super(agent);
            this.msg = msg;
        }

        @Override
        public void action() {

            try {
                ContentElement content = getContentManager().extractContent(msg);
                Concept action = ((Action) content).getAction();
                TrafficLightLocationAndDirection tllad = (TrafficLightLocationAndDirection) action;

                tllad.setCrossLocation(getCrossLocation());
                tllad.setLocation(getLocation());
                tllad.setDirection(getDirection());

                ACLMessage reply = msg.createReply();

                reply.setLanguage(codec.getName());
                reply.setOntology(ontology.getName());

                reply.setPerformative(ACLMessage.PROPAGATE);

                getContentManager().fillContent(reply, new Action(msg.getSender(), tllad));
                send(reply);
//                System.out.println("TrafficLightLocationAndDirection sent!");            
            } catch (Codec.CodecException ex) {
                Logger.getLogger(TrafficLight.class.getName()).log(Level.SEVERE, null, ex);
            } catch (OntologyException ex) {
                Logger.getLogger(TrafficLight.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void setTrafficState(String trafficState) {
        this.trafficState = trafficState;

        if (trafficState.equalsIgnoreCase("green")) {
            this.carCount = this.carCount - 10 > 0 ? this.carCount - 10 : 0;
        }
    }

    public String getLocation() {
        return location;
    }

    public int getCarCount() {
        return carCount;
    }

    public void addAddditionalCars(int carCount) {
        this.carCount += carCount;
    }

    public String getTrafficState() {
        return trafficState;
    }

    private String getDirection() {
        return direction;
    }

    private Date getLastGreenTime() {
        return lastGreenTime;
    }

    public void setLastGreenTime(Date lastGreenTime) {
        this.lastGreenTime = lastGreenTime;
    }
}
