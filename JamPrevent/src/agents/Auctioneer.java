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
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import messages.TrafficLightLocationAndDirection;
import messages.TrafficLightOffer;
import messages.TrafficLightProperties;
import messages.TrafficLightState;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author SiB
 */
public class Auctioneer extends BaseAgent {

    private final List<AID> trafficLightAgents = new ArrayList<>();
    private final HashMap<AID, HashMap<String, String>> trafficLightsMetadata = new HashMap<>();
    private final HashMap<String, HashMap<String, AID>> trafficLightsByDirection = new HashMap<>();
    private String lastDirection = "WE";
    private int requestIndex = 0;
    private int receivedAnswersPerIndex = 0;
    
    @Override
    public void setup() {
        super.setup();
        addBehaviour(new DefaultExecutionBehaviour());
        addBehaviour(new ReceiveMessagesBehaviour());
    }

    private class SetStateBehaviour extends OneShotBehaviour {

        public SetStateBehaviour() {           
        }

        @Override
        public void action() {
            
            AID trafficLightHighestCarCount = trafficLightAgents.get(0);
            
            for(AID trafficLight : trafficLightsMetadata.keySet()){
                if(Integer.valueOf(trafficLightsMetadata.get(trafficLight).get("carCount")) > Integer.valueOf(trafficLightsMetadata.get(trafficLightHighestCarCount).get("carCount"))){
                    trafficLightHighestCarCount = trafficLight;
                }
            }
            
            List<AID> trafficLightsThatShouldBeGreen = new ArrayList<AID>();
            trafficLightsThatShouldBeGreen.add(trafficLightHighestCarCount);
            
            switch (trafficLightsMetadata.get(trafficLightHighestCarCount).get("location")){
                case ("W"):
                    trafficLightsThatShouldBeGreen.add(trafficLightsByDirection.get("E").get("W"));
                    break;
                    
                case ("E"):
                    AID southToEastTrafficLight = trafficLightsByDirection.get("S").get("E");
                    AID westToEastTrafficLight  = trafficLightsByDirection.get("W").get("E");
                    
                    int southToEastCarCount = Integer.valueOf(trafficLightsMetadata.get(southToEastTrafficLight).get("carCount"));
                    int westToEastCarCount = Integer.valueOf(trafficLightsMetadata.get(westToEastTrafficLight).get("carCount"));
                    if(southToEastCarCount > westToEastCarCount){
                        trafficLightsThatShouldBeGreen.add(southToEastTrafficLight);
                    }
                    else{
                        trafficLightsThatShouldBeGreen.add(westToEastTrafficLight);
                    }
                    break;
                    
                case ("S"):
                    trafficLightsThatShouldBeGreen.add(trafficLightsByDirection.get("E").get("W"));
                    break;
            }
            
                
            long t = new Date().getTime();
            Date nextUpdate = new Date(t + 1000);
            
//            sendTrafficLightNewState(trafficLightHighestCarCount, "green", nextUpdate);
            
            for(AID trafficLight : trafficLightsMetadata.keySet()){
                if(trafficLightsThatShouldBeGreen.contains(trafficLight)){
                    sendTrafficLightNewState(trafficLight, "green", nextUpdate);    
                }                
                else {
                    sendTrafficLightNewState(trafficLight, "red", nextUpdate);    
                }
            }                        
        }
        
        private void sendTrafficLightNewState(AID trafficLight, String state, Date nextUpdate) {

            TrafficLightState tls = new TrafficLightState();
            tls.setTrafficState(state);
            tls.setNextUpdate(nextUpdate);

            jade.lang.acl.ACLMessage message = new jade.lang.acl.ACLMessage(
                    jade.lang.acl.ACLMessage.PROPOSE);

            message.setLanguage(codec.getName());
            message.setOntology(ontology.getName());

            message.addReceiver(trafficLight);

            try {
                getContentManager().fillContent(message, new Action(trafficLight, tls));
            } catch (Codec.CodecException ex) {
                Logger.getLogger(Auctioneer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (OntologyException ex) {
                Logger.getLogger(Auctioneer.class.getName()).log(Level.SEVERE, null, ex);
            }

            this.myAgent.send(message);
        }        
    }

    private class RequestTrafficLightOfferBehaviour extends TickerBehaviour {

        public RequestTrafficLightOfferBehaviour(Agent a, long period) {
            super(a, period);
        }        

        @Override
        protected void onTick() {
            TrafficLightOffer tlo = new TrafficLightOffer();

            jade.lang.acl.ACLMessage message = new jade.lang.acl.ACLMessage(
                    jade.lang.acl.ACLMessage.CFP);

            message.setLanguage(codec.getName());
            message.setOntology(ontology.getName());
            tlo.setIndex(requestIndex);
            for(AID trafficLight :  trafficLightAgents){
                try {
                    getContentManager().fillContent(message, new Action(trafficLight, tlo));
                    message.addReceiver(trafficLight);
                } catch (Codec.CodecException ex) {
                    Logger.getLogger(Auctioneer.class.getName()).log(Level.SEVERE, null, ex);
                } catch (OntologyException ex) {
                    Logger.getLogger(Auctioneer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            this.myAgent.send(message);
        }
    }

    private class ReceiveMessagesBehaviour extends CyclicBehaviour {

        private static final long serialVersionUID = -5018397038252984135L;

        @Override
        public void action() {

            ACLMessage msg = receive();
            if (msg == null) {
//                block();
                return;
            }
            try {
                ContentElement content = getContentManager().extractContent(msg);
                Concept action = ((Action)content).getAction();

                switch (msg.getPerformative()) {

                    case (ACLMessage.PROPOSE):

                        System.out.println("Request from " + msg.getSender().getLocalName());

                        if (action instanceof TrafficLightOffer) {
                            addBehaviour(new HandleTrafficLightOfferPropose(myAgent, msg));
                        } else {
                            replyNotUnderstood(msg);
                        }
                        break;

                    case (ACLMessage.INFORM):

                        System.out.println("Request from " + msg.getSender().getLocalName());

                        if (action instanceof TrafficLightLocationAndDirection) {
                            addBehaviour(new HandleTrafficLightLocationAndDirectionInform(myAgent, msg));
                        } else {
                            replyNotUnderstood(msg);
                        }
                        break;
                                                                                
                    default:
                        replyNotUnderstood(msg);
                }
            } catch (Exception ex) {
            }
        }
    }    
    
    private class HandleTrafficLightOfferPropose extends OneShotBehaviour {

        private final ACLMessage msg;

        public HandleTrafficLightOfferPropose(Agent myAgent, ACLMessage msg) {
            super(myAgent);
            this.msg = msg;
        }

        @Override
        public void action() {

            TrafficLightOffer tlo;
            try {
                ContentElement content = getContentManager().extractContent(msg);
                Concept action = ((Action)content).getAction();                
                tlo = (TrafficLightOffer) action;

                if (tlo.getIndex() == requestIndex) {
                    trafficLightsMetadata.get(msg.getSender()).put(TRAFFIC_LIGHT_OFFER_CARCOUNT, String.valueOf(tlo.getCarCount()));
                    trafficLightsMetadata.get(msg.getSender()).put(TRAFFIC_LIGHT_OFFER_LAST_GREEN_TIME, String.valueOf(tlo.getLastGreenTime()));
                    receivedAnswersPerIndex++;
                }

                if (receivedAnswersPerIndex == trafficLightAgents.size()) {
                    addBehaviour(new SetStateBehaviour());
                    receivedAnswersPerIndex = 0;
                    requestIndex++;
                }            
            } catch (Codec.CodecException ex) {
                Logger.getLogger(ReportingAgent.class.getName()).log(Level.SEVERE, null, ex);
            } catch (OntologyException ex) {
                Logger.getLogger(ReportingAgent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }   
    
    public class HandleTrafficLightLocationAndDirectionInform extends OneShotBehaviour {

        private final ACLMessage msg;

        public HandleTrafficLightLocationAndDirectionInform(Agent myAgent, ACLMessage msg) {
            super(myAgent);
            this.msg = msg;
        }

        @Override
        public void action() {

            TrafficLightLocationAndDirection tllad;
            try {

                ContentElement content = getContentManager().extractContent(msg);
                Concept action = ((Action) content).getAction();
                tllad = (TrafficLightLocationAndDirection) action;

                if (trafficLightsMetadata.containsKey(msg.getSender())) {
                    trafficLightsMetadata.get(msg.getSender()).put("location", tllad.getLocation());
                    trafficLightsMetadata.get(msg.getSender()).put("direction", tllad.getDirection());
                    System.out.println(tllad.getLocation() + " " + tllad.getDirection());
                }
                
                trafficLightsByDirection.put(tllad.getLocation(), new HashMap<>());
                trafficLightsByDirection.get(tllad.getLocation()).put(tllad.getDirection(), msg.getSender());

                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.CONFIRM);
                send(reply);
                System.out.println("TrafficLightLocationAndDirection received!");
            } catch (Codec.CodecException ex) {
                Logger.getLogger(Auctioneer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (OntologyException ex) {
                Logger.getLogger(Auctioneer.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    public class DefaultExecutionBehaviour extends SequentialBehaviour {

        public DefaultExecutionBehaviour() {
            addSubBehaviour(new FindExistingTrafficLightsBehaviour(this.myAgent, 1000));
            addSubBehaviour(new RequestTrafficLightOfferBehaviour(this.myAgent, 500));
//            addSubBehaviour(new SetStateBehaviour(this.myAgent, 1000));
        }
        
        private class FindExistingTrafficLightsBehaviour extends WakerBehaviour {

            public FindExistingTrafficLightsBehaviour(Agent a, long timeout) {
                super(a, timeout);
            }

            @Override
            protected void onWake() {
                super.onWake(); //To change body of generated methods, choose Tools | Templates.
                findAndAddTrafficLights();
                trafficLightAgents.stream().forEach((agent) -> {
                    trafficLightsMetadata.put(agent, new HashMap<>());
                    System.out.println("Found Trafficlight: " + agent.getLocalName());
                    askTrafficLightForLocationAndDirection(agent);
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
                }
            }

            private void askTrafficLightForLocationAndDirection(AID trafficLight) {

                try {
                    TrafficLightLocationAndDirection tllaa = new TrafficLightLocationAndDirection();

                    jade.lang.acl.ACLMessage message = new jade.lang.acl.ACLMessage(
                            jade.lang.acl.ACLMessage.REQUEST);

                    message.setLanguage(codec.getName());
                    message.setOntology(ontology.getName());

                    getContentManager().fillContent(message, new Action(trafficLight, tllaa));
                    message.addReceiver(trafficLight);

                    this.myAgent.send(message);
                } catch (Codec.CodecException ex) {
                    Logger.getLogger(Auctioneer.class.getName()).log(Level.SEVERE, null, ex);
                } catch (OntologyException ex) {
                    Logger.getLogger(Auctioneer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        private class SetStateBehaviour extends TickerBehaviour {

            private String activeDirection = "SE";

            public SetStateBehaviour(Agent a, long period) {
                super(a, period);
            }

            @Override
            public void onTick() {

                long t = new Date().getTime();
                Date nextUpdate = new Date(t + 1000);

                for (AID trafficLight : trafficLightAgents) {
                    if (activeDirection.equalsIgnoreCase("SE")) {

                        if (trafficLightsMetadata.get(trafficLight).get("location").equalsIgnoreCase("W") && trafficLightsMetadata.get(trafficLight).get("direction").equalsIgnoreCase("E")) {
                            sendTrafficLightNewState(trafficLight, "green", nextUpdate);
                        }

                        if (trafficLightsMetadata.get(trafficLight).get("location").equalsIgnoreCase("E") && trafficLightsMetadata.get(trafficLight).get("direction").equalsIgnoreCase("W")) {
                            sendTrafficLightNewState(trafficLight, "green", nextUpdate);
                        }

                        if (trafficLightsMetadata.get(trafficLight).get("location").equalsIgnoreCase("S") && trafficLightsMetadata.get(trafficLight).get("direction").equalsIgnoreCase("E")) {
                            sendTrafficLightNewState(trafficLight, "red", nextUpdate);
                        }

                        lastDirection = "WE";
                    } else {
                        if (trafficLightsMetadata.get(trafficLight).get("location").equalsIgnoreCase("W") && trafficLightsMetadata.get(trafficLight).get("direction").equalsIgnoreCase("E")) {
                            sendTrafficLightNewState(trafficLight, "red", nextUpdate);
                        }

                        if (trafficLightsMetadata.get(trafficLight).get("location").equalsIgnoreCase("E") && trafficLightsMetadata.get(trafficLight).get("direction").equalsIgnoreCase("W")) {
                            sendTrafficLightNewState(trafficLight, "red", nextUpdate);
                        }

                        if (trafficLightsMetadata.get(trafficLight).get("location").equalsIgnoreCase("S") && trafficLightsMetadata.get(trafficLight).get("direction").equalsIgnoreCase("E")) {
                            sendTrafficLightNewState(trafficLight, "green", nextUpdate);
                        }

                        lastDirection = "SE";
                    }

                }
                activeDirection = lastDirection;
            }

            private void sendTrafficLightNewState(AID trafficLight, String state, Date nextUpdate) {

                TrafficLightState tls = new TrafficLightState();
                tls.setTrafficState(state);
                tls.setNextUpdate(nextUpdate);

                jade.lang.acl.ACLMessage message = new jade.lang.acl.ACLMessage(
                        jade.lang.acl.ACLMessage.PROPOSE);

                message.setLanguage(codec.getName());
                message.setOntology(ontology.getName());

                message.addReceiver(trafficLight);

                try {
                    getContentManager().fillContent(message, new Action(trafficLight, tls));
                } catch (Codec.CodecException ex) {
                    Logger.getLogger(Auctioneer.class.getName()).log(Level.SEVERE, null, ex);
                } catch (OntologyException ex) {
                    Logger.getLogger(Auctioneer.class.getName()).log(Level.SEVERE, null, ex);
                }

                this.myAgent.send(message);
            }

        }

    }

}
