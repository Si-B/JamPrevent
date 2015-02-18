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
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetInitiator;
import jade.util.leap.Iterator;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import messages.TrafficLightLocationAndDirection;
import messages.TrafficLightOffer;

    /**
     * Auctioneer
     * talks to TrafficLight agents and asks them for offers
     * decides on which offers to accept (set the green state) or
     * refuse based on four different, configurable algorithms
     */
public class Auctioneer extends BaseAgent {

    private final List<AID> trafficLightAgents = new ArrayList<>();
    private final HashMap<AID, HashMap<String, String>> trafficLightsMetadata = new HashMap<>();
    private final HashMap<String, AID> trafficLightsByDirection = new HashMap<>();
    private int requestIndex = 0;
    private String crossLocation = "";

    /**
     *
     */
    @Override
    public void setup() {
        super.setup();
        Object[] arguments = getArguments();

        if (arguments.length > 0) {
            crossLocation = arguments[0].toString();
        }

        addBehaviour(new DefaultExecutionBehaviour());
        addBehaviour(new ReceiveMessagesBehaviour());
    }

    //Prepare and send a call for proposal to all known traffic lights on every tick.
    private class RequestTrafficLightOfferBehaviour extends TickerBehaviour {

        private Random rand = new Random();

        public RequestTrafficLightOfferBehaviour(Agent a, long period) {
            super(a, period);
        }

        @Override
        public void onTick() {
            TrafficLightOffer tlo = new TrafficLightOffer();

            jade.lang.acl.ACLMessage message = new jade.lang.acl.ACLMessage(
                    jade.lang.acl.ACLMessage.CFP);

            message.setLanguage(codec.getName());
            message.setOntology(ontology.getName());
            tlo.setIndex(requestIndex);
            
            for (AID trafficLight : trafficLightAgents) {
                try {
                    getContentManager().fillContent(message, new Action(trafficLight, tlo));
                    message.addReceiver(trafficLight);
                } catch (Codec.CodecException ex) {
                    Logger.getLogger(Auctioneer.class.getName()).log(Level.SEVERE, null, ex);
                } catch (OntologyException ex) {
                    Logger.getLogger(Auctioneer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            message.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
            message.setReplyByDate(new Date(System.currentTimeMillis() + 10000));

            //Using a ContractNetInitiator to initiate the CFP-Protocol.
            addBehaviour(new ContractNetInitiator(this.myAgent, message) {

                @Override
                protected void handlePropose(ACLMessage propose, Vector acceptances) {
                    System.out.println("Agent " + propose.getSender().getName() + " proposed " + propose.getContent());
                }

                @Override
                protected void handleRefuse(ACLMessage refuse) {
                    System.out.println("Agent " + refuse.getSender().getName() + " refused");
                }
                               
                @Override
                protected void handleAllResponses(Vector responses, Vector acceptances) {
                    if (responses.size() < trafficLightAgents.size()) {
                        // Some responder didn't reply within the specified timeout
                        System.out.println("Timeout expired: missing " + (trafficLightAgents.size() - responses.size()) + " responses");                                                                                                
                    }else{
                        // Evaluate proposals.
                        int mostCars = -1;
                        AID bestProposer = null;
                        ACLMessage accept = null;
                        Enumeration e = responses.elements();
                        while (e.hasMoreElements()) {
                            ACLMessage msg = (ACLMessage) e.nextElement();
                            if (msg.getPerformative() == ACLMessage.PROPOSE) {
                                try {
                                    ACLMessage reply = msg.createReply();
                                    reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                                    acceptances.addElement(reply);
                                    ContentElement content = getContentManager().extractContent(msg);
                                    Concept action = ((Action) content).getAction();
                                    TrafficLightOffer tlo = (TrafficLightOffer) action;
                                    int proposal = tlo.getCarCount();

                                    trafficLightsMetadata.get(msg.getSender()).put("carCount", String.valueOf(proposal));
                                    //The TrafficLight with the most cars will win the one round action.
                                    //Note that this TrafficLight is still processed in the auction pattern defined by the crossLocation.
                                    if (proposal > mostCars) {
                                        mostCars = proposal;
                                        bestProposer = msg.getSender();
                                        accept = reply;
                                    }
                                } catch (Codec.CodecException ex) {
                                    Logger.getLogger(Auctioneer.class.getName()).log(Level.SEVERE, null, ex);
                                } catch (OntologyException ex) {
                                    Logger.getLogger(Auctioneer.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }

                        //Depending on crossLocation we use a different aution pattern.
                        if (crossLocation.equalsIgnoreCase("Random")) {
                            setRandom(acceptances);
                        } else if (crossLocation.equalsIgnoreCase("SingleHeighest")) {
                            setSingleHighest(bestProposer, acceptances);
                        } else if (crossLocation.equalsIgnoreCase("RandomPredefined")) {
                            setRandomWithPredefinedStates(acceptances);
                        } else {
                            setHighestWithPredefinedStates(bestProposer, acceptances);
                        }

                        // This is the best proposal using the carCount
                        if (accept != null) {
                            System.out.println("Accepting proposal " + mostCars + " from responder " + bestProposer.getName());
                        }
                    }
                }

                @Override
                protected void handleInform(ACLMessage inform) {
                    System.out.println("Agent " + inform.getSender().getName() + " successfully performed the requested action");
                }

            });
        }
        
        //Pick a random car instead of the one with the most cars. Then find the best matching predefined pattern for setting the TrafficLight states.
        public void setRandomWithPredefinedStates(Vector acceptances) {

            int totalLights = trafficLightAgents.size();

            int randomNum = rand.nextInt(totalLights);

            AID trafficLightWithHighestCarCount = trafficLightAgents.get(randomNum);

            List<AID> trafficLightsThatShouldBeGreen = new ArrayList<AID>();
            trafficLightsThatShouldBeGreen.add(trafficLightWithHighestCarCount);

            String locationDirection = trafficLightsMetadata.get(trafficLightWithHighestCarCount).get("location") + trafficLightsMetadata.get(trafficLightWithHighestCarCount).get("direction");

            AID westToEastTrafficLight = trafficLightsByDirection.get("WE");
            AID westToSouthTrafficLight = trafficLightsByDirection.get("WS");
            AID southToWestTrafficLight = trafficLightsByDirection.get("SW");
            AID southToEastTrafficLight = trafficLightsByDirection.get("SE");
            AID eastToWestTrafficLight = trafficLightsByDirection.get("EW");
            AID eastToSouthTrafficLight = trafficLightsByDirection.get("ES");

            int westToEastCarCount = Integer.valueOf(trafficLightsMetadata.get(westToEastTrafficLight).get("carCount"));
            int westToSouthCarCount = Integer.valueOf(trafficLightsMetadata.get(westToSouthTrafficLight).get("carCount"));
            int southToWestCarCount = Integer.valueOf(trafficLightsMetadata.get(southToWestTrafficLight).get("carCount"));
            int southToEastCarCount = Integer.valueOf(trafficLightsMetadata.get(southToEastTrafficLight).get("carCount"));
            int eastToWestCarCount = Integer.valueOf(trafficLightsMetadata.get(eastToWestTrafficLight).get("carCount"));
            int eastToSouthCarCount = Integer.valueOf(trafficLightsMetadata.get(eastToSouthTrafficLight).get("carCount"));

            switch (locationDirection) {
                case "WS":
                    if (westToEastCarCount > southToWestCarCount && westToEastCarCount > southToEastCarCount) {
                        trafficLightsThatShouldBeGreen.add(westToEastTrafficLight);//1
                        trafficLightsThatShouldBeGreen.add(westToSouthTrafficLight);//1
                        trafficLightsThatShouldBeGreen.add(eastToWestTrafficLight);//1
                    } else if (westToEastCarCount < southToWestCarCount && southToEastCarCount < southToWestCarCount) {
                        trafficLightsThatShouldBeGreen.add(westToSouthTrafficLight);//4
                        trafficLightsThatShouldBeGreen.add(southToEastTrafficLight);//4
                        trafficLightsThatShouldBeGreen.add(southToWestTrafficLight);//4                     
                    } else {
                        if (westToEastCarCount > southToWestCarCount) {
                            trafficLightsThatShouldBeGreen.add(westToSouthTrafficLight);//2
                            trafficLightsThatShouldBeGreen.add(southToEastTrafficLight);//2
                            trafficLightsThatShouldBeGreen.add(eastToWestTrafficLight);//2
                        } else {
                            trafficLightsThatShouldBeGreen.add(westToSouthTrafficLight);//4
                            trafficLightsThatShouldBeGreen.add(southToEastTrafficLight);//4
                            trafficLightsThatShouldBeGreen.add(southToWestTrafficLight);//4
                        }
                    }

                    break;

                case ("EW"):

                    if (westToEastCarCount > southToEastCarCount && westToEastCarCount > westToSouthCarCount && westToEastCarCount > eastToSouthCarCount) {
                        trafficLightsThatShouldBeGreen.add(westToEastTrafficLight);//1
                        trafficLightsThatShouldBeGreen.add(westToSouthTrafficLight);//1
                        trafficLightsThatShouldBeGreen.add(eastToWestTrafficLight);//1
                    } else if (eastToSouthCarCount > southToEastCarCount && eastToSouthCarCount > westToEastCarCount && eastToSouthCarCount > westToSouthCarCount) {
                        trafficLightsThatShouldBeGreen.add(southToEastTrafficLight);//3
                        trafficLightsThatShouldBeGreen.add(eastToSouthTrafficLight);//3
                        trafficLightsThatShouldBeGreen.add(eastToWestTrafficLight);//3
                    } else if (southToEastCarCount > westToEastCarCount && southToEastCarCount > westToSouthCarCount && southToEastCarCount > eastToSouthCarCount) {
                        if (westToSouthCarCount > eastToSouthCarCount) {
                            trafficLightsThatShouldBeGreen.add(southToEastTrafficLight);//3
                            trafficLightsThatShouldBeGreen.add(eastToSouthTrafficLight);//3
                            trafficLightsThatShouldBeGreen.add(eastToWestTrafficLight);//3
                        } else {
                            trafficLightsThatShouldBeGreen.add(westToSouthTrafficLight);//2
                            trafficLightsThatShouldBeGreen.add(southToEastTrafficLight);//2
                            trafficLightsThatShouldBeGreen.add(eastToWestTrafficLight);//2
                        }
                    } else {
                        if (westToEastCarCount > southToEastCarCount) {
                            trafficLightsThatShouldBeGreen.add(westToEastTrafficLight);//1
                            trafficLightsThatShouldBeGreen.add(westToSouthTrafficLight);//1
                            trafficLightsThatShouldBeGreen.add(eastToWestTrafficLight);//1
                        } else {
                            trafficLightsThatShouldBeGreen.add(westToSouthTrafficLight);//2
                            trafficLightsThatShouldBeGreen.add(southToEastTrafficLight);//2
                            trafficLightsThatShouldBeGreen.add(eastToWestTrafficLight);//2
                        }
                    }

                    break;

                case ("SE"):

                    if (southToWestCarCount > westToSouthCarCount && southToWestCarCount > eastToWestCarCount && southToWestCarCount > eastToSouthCarCount) {
                        trafficLightsThatShouldBeGreen.add(westToEastTrafficLight);//1
                        trafficLightsThatShouldBeGreen.add(westToSouthTrafficLight);//1
                        trafficLightsThatShouldBeGreen.add(eastToWestTrafficLight);//1
                    } else if (eastToSouthCarCount > westToSouthCarCount && eastToSouthCarCount > eastToWestCarCount && eastToSouthCarCount > southToWestCarCount) {
                        trafficLightsThatShouldBeGreen.add(southToEastTrafficLight);//3
                        trafficLightsThatShouldBeGreen.add(eastToSouthTrafficLight);//3
                        trafficLightsThatShouldBeGreen.add(eastToWestTrafficLight);//3
                    } else if (westToSouthCarCount > eastToWestCarCount && westToSouthCarCount > eastToSouthCarCount && westToSouthCarCount > southToWestCarCount) {
                        if (southToWestCarCount > eastToWestCarCount) {
                            trafficLightsThatShouldBeGreen.add(westToSouthTrafficLight);//4
                            trafficLightsThatShouldBeGreen.add(southToEastTrafficLight);//4
                            trafficLightsThatShouldBeGreen.add(southToWestTrafficLight);//4
                        } else {
                            trafficLightsThatShouldBeGreen.add(westToSouthTrafficLight);//2
                            trafficLightsThatShouldBeGreen.add(southToEastTrafficLight);//2
                            trafficLightsThatShouldBeGreen.add(eastToWestTrafficLight);//2
                        }
                    } else {
                        if (westToSouthCarCount > eastToSouthCarCount) {
                            trafficLightsThatShouldBeGreen.add(westToSouthTrafficLight);//2
                            trafficLightsThatShouldBeGreen.add(southToEastTrafficLight);//2
                            trafficLightsThatShouldBeGreen.add(eastToWestTrafficLight);//2
                        } else {
                            trafficLightsThatShouldBeGreen.add(westToSouthTrafficLight);//4
                            trafficLightsThatShouldBeGreen.add(southToEastTrafficLight);//4
                            trafficLightsThatShouldBeGreen.add(southToWestTrafficLight);//4
                        }
                    }

                    break;

                case ("SW"):
                    trafficLightsThatShouldBeGreen.add(westToSouthTrafficLight);//4
                    trafficLightsThatShouldBeGreen.add(southToEastTrafficLight);//4
                    trafficLightsThatShouldBeGreen.add(southToWestTrafficLight);//4

                    break;

                case ("ES"):
                    trafficLightsThatShouldBeGreen.add(southToEastTrafficLight);//3
                    trafficLightsThatShouldBeGreen.add(eastToSouthTrafficLight);//3
                    trafficLightsThatShouldBeGreen.add(eastToWestTrafficLight);//3                    

                    break;

                case ("WE"):
                    trafficLightsThatShouldBeGreen.add(westToEastTrafficLight);//1
                    trafficLightsThatShouldBeGreen.add(westToSouthTrafficLight);//1
                    trafficLightsThatShouldBeGreen.add(eastToWestTrafficLight);//1

                    break;
            }

            Enumeration e = acceptances.elements();
            while (e.hasMoreElements()) {
                ACLMessage msg = (ACLMessage) e.nextElement();
                Iterator bla = msg.getAllReceiver();
                while (bla.hasNext()) {
                    Object bla2 = bla.next();

                    if (trafficLightsThatShouldBeGreen.contains(bla2)) {
                        msg.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    } else {
                        msg.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    }
                }
            }

        }

        //Take the trafficLightWithHighestCarCount. Then find the best matching predefined pattern for setting the TrafficLight states.
        public void setHighestWithPredefinedStates(AID trafficLightWithHighestCarCount, Vector acceptances) {

            List<AID> trafficLightsThatShouldBeGreen = new ArrayList<AID>();
            trafficLightsThatShouldBeGreen.add(trafficLightWithHighestCarCount);

            String locationDirection = trafficLightsMetadata.get(trafficLightWithHighestCarCount).get("location") + trafficLightsMetadata.get(trafficLightWithHighestCarCount).get("direction");

            AID westToEastTrafficLight = trafficLightsByDirection.get("WE");
            AID westToSouthTrafficLight = trafficLightsByDirection.get("WS");
            AID southToWestTrafficLight = trafficLightsByDirection.get("SW");
            AID southToEastTrafficLight = trafficLightsByDirection.get("SE");
            AID eastToWestTrafficLight = trafficLightsByDirection.get("EW");
            AID eastToSouthTrafficLight = trafficLightsByDirection.get("ES");

            int westToEastCarCount = Integer.valueOf(trafficLightsMetadata.get(westToEastTrafficLight).get("carCount"));
            int westToSouthCarCount = Integer.valueOf(trafficLightsMetadata.get(westToSouthTrafficLight).get("carCount"));
            int southToWestCarCount = Integer.valueOf(trafficLightsMetadata.get(southToWestTrafficLight).get("carCount"));
            int southToEastCarCount = Integer.valueOf(trafficLightsMetadata.get(southToEastTrafficLight).get("carCount"));
            int eastToWestCarCount = Integer.valueOf(trafficLightsMetadata.get(eastToWestTrafficLight).get("carCount"));
            int eastToSouthCarCount = Integer.valueOf(trafficLightsMetadata.get(eastToSouthTrafficLight).get("carCount"));

            switch (locationDirection) {
                case "WS":
                    if (westToEastCarCount > southToWestCarCount && westToEastCarCount > southToEastCarCount) {
                        trafficLightsThatShouldBeGreen.add(westToEastTrafficLight);//1
                        trafficLightsThatShouldBeGreen.add(westToSouthTrafficLight);//1
                        trafficLightsThatShouldBeGreen.add(eastToWestTrafficLight);//1
                    } else if (westToEastCarCount < southToWestCarCount && southToEastCarCount < southToWestCarCount) {
                        trafficLightsThatShouldBeGreen.add(westToSouthTrafficLight);//4
                        trafficLightsThatShouldBeGreen.add(southToEastTrafficLight);//4
                        trafficLightsThatShouldBeGreen.add(southToWestTrafficLight);//4                     
                    } else {
                        if (westToEastCarCount > southToWestCarCount) {
                            trafficLightsThatShouldBeGreen.add(westToSouthTrafficLight);//2
                            trafficLightsThatShouldBeGreen.add(southToEastTrafficLight);//2
                            trafficLightsThatShouldBeGreen.add(eastToWestTrafficLight);//2
                        } else {
                            trafficLightsThatShouldBeGreen.add(westToSouthTrafficLight);//4
                            trafficLightsThatShouldBeGreen.add(southToEastTrafficLight);//4
                            trafficLightsThatShouldBeGreen.add(southToWestTrafficLight);//4
                        }
                    }

                    break;

                case ("EW"):

                    if (westToEastCarCount > southToEastCarCount && westToEastCarCount > westToSouthCarCount && westToEastCarCount > eastToSouthCarCount) {
                        trafficLightsThatShouldBeGreen.add(westToEastTrafficLight);//1
                        trafficLightsThatShouldBeGreen.add(westToSouthTrafficLight);//1
                        trafficLightsThatShouldBeGreen.add(eastToWestTrafficLight);//1
                    } else if (eastToSouthCarCount > southToEastCarCount && eastToSouthCarCount > westToEastCarCount && eastToSouthCarCount > westToSouthCarCount) {
                        trafficLightsThatShouldBeGreen.add(southToEastTrafficLight);//3
                        trafficLightsThatShouldBeGreen.add(eastToSouthTrafficLight);//3
                        trafficLightsThatShouldBeGreen.add(eastToWestTrafficLight);//3
                    } else if (southToEastCarCount > westToEastCarCount && southToEastCarCount > westToSouthCarCount && southToEastCarCount > eastToSouthCarCount) {
                        if (westToSouthCarCount > eastToSouthCarCount) {
                            trafficLightsThatShouldBeGreen.add(southToEastTrafficLight);//3
                            trafficLightsThatShouldBeGreen.add(eastToSouthTrafficLight);//3
                            trafficLightsThatShouldBeGreen.add(eastToWestTrafficLight);//3
                        } else {
                            trafficLightsThatShouldBeGreen.add(westToSouthTrafficLight);//2
                            trafficLightsThatShouldBeGreen.add(southToEastTrafficLight);//2
                            trafficLightsThatShouldBeGreen.add(eastToWestTrafficLight);//2
                        }
                    } else {
                        if (westToEastCarCount > southToEastCarCount) {
                            trafficLightsThatShouldBeGreen.add(westToEastTrafficLight);//1
                            trafficLightsThatShouldBeGreen.add(westToSouthTrafficLight);//1
                            trafficLightsThatShouldBeGreen.add(eastToWestTrafficLight);//1
                        } else {
                            trafficLightsThatShouldBeGreen.add(westToSouthTrafficLight);//2
                            trafficLightsThatShouldBeGreen.add(southToEastTrafficLight);//2
                            trafficLightsThatShouldBeGreen.add(eastToWestTrafficLight);//2
                        }
                    }

                    break;

                case ("SE"):

                    if (southToWestCarCount > westToSouthCarCount && southToWestCarCount > eastToWestCarCount && southToWestCarCount > eastToSouthCarCount) {
                        trafficLightsThatShouldBeGreen.add(westToEastTrafficLight);//1
                        trafficLightsThatShouldBeGreen.add(westToSouthTrafficLight);//1
                        trafficLightsThatShouldBeGreen.add(eastToWestTrafficLight);//1
                    } else if (eastToSouthCarCount > westToSouthCarCount && eastToSouthCarCount > eastToWestCarCount && eastToSouthCarCount > southToWestCarCount) {
                        trafficLightsThatShouldBeGreen.add(southToEastTrafficLight);//3
                        trafficLightsThatShouldBeGreen.add(eastToSouthTrafficLight);//3
                        trafficLightsThatShouldBeGreen.add(eastToWestTrafficLight);//3
                    } else if (westToSouthCarCount > eastToWestCarCount && westToSouthCarCount > eastToSouthCarCount && westToSouthCarCount > southToWestCarCount) {
                        if (southToWestCarCount > eastToWestCarCount) {
                            trafficLightsThatShouldBeGreen.add(westToSouthTrafficLight);//4
                            trafficLightsThatShouldBeGreen.add(southToEastTrafficLight);//4
                            trafficLightsThatShouldBeGreen.add(southToWestTrafficLight);//4
                        } else {
                            trafficLightsThatShouldBeGreen.add(westToSouthTrafficLight);//2
                            trafficLightsThatShouldBeGreen.add(southToEastTrafficLight);//2
                            trafficLightsThatShouldBeGreen.add(eastToWestTrafficLight);//2
                        }
                    } else {
                        if (westToSouthCarCount > eastToSouthCarCount) {
                            trafficLightsThatShouldBeGreen.add(westToSouthTrafficLight);//2
                            trafficLightsThatShouldBeGreen.add(southToEastTrafficLight);//2
                            trafficLightsThatShouldBeGreen.add(eastToWestTrafficLight);//2
                        } else {
                            trafficLightsThatShouldBeGreen.add(westToSouthTrafficLight);//4
                            trafficLightsThatShouldBeGreen.add(southToEastTrafficLight);//4
                            trafficLightsThatShouldBeGreen.add(southToWestTrafficLight);//4
                        }
                    }

                    break;

                case ("SW"):
                    trafficLightsThatShouldBeGreen.add(westToSouthTrafficLight);//4
                    trafficLightsThatShouldBeGreen.add(southToEastTrafficLight);//4
                    trafficLightsThatShouldBeGreen.add(southToWestTrafficLight);//4

                    break;

                case ("ES"):
                    trafficLightsThatShouldBeGreen.add(southToEastTrafficLight);//3
                    trafficLightsThatShouldBeGreen.add(eastToSouthTrafficLight);//3
                    trafficLightsThatShouldBeGreen.add(eastToWestTrafficLight);//3                    

                    break;

                case ("WE"):
                    trafficLightsThatShouldBeGreen.add(westToEastTrafficLight);//1
                    trafficLightsThatShouldBeGreen.add(westToSouthTrafficLight);//1
                    trafficLightsThatShouldBeGreen.add(eastToWestTrafficLight);//1

                    break;

            }

            Enumeration e = acceptances.elements();
            while (e.hasMoreElements()) {
                ACLMessage msg = (ACLMessage) e.nextElement();
                Iterator bla = msg.getAllReceiver();
                while (bla.hasNext()) {
                    Object bla2 = bla.next();

                    if (trafficLightsThatShouldBeGreen.contains(bla2)) {
                        msg.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    } else {
                        msg.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    }
                }
            }
        }

        //Accept only the proposal of the TrafficLight with the most carCount.
        public void setSingleHighest(AID trafficLightWithHighestCarCount, Vector acceptances) {
            Enumeration e = acceptances.elements();
            while (e.hasMoreElements()) {
                ACLMessage msg = (ACLMessage) e.nextElement();
                Iterator bla = msg.getAllReceiver();
                while (bla.hasNext()) {
                    Object bla2 = bla.next();
                    if (bla2.hashCode() == trafficLightWithHighestCarCount.hashCode()) {
                        msg.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    } else {
                        msg.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    }
                }
            }
        }

        //Pick a random TrafficLight and accept proposal.
        public void setRandom(Vector acceptances) {
            int totalLights = trafficLightAgents.size();
            int randomNum = rand.nextInt(totalLights);
            AID randomlySelectedLight = trafficLightAgents.get(randomNum);
            Enumeration e = acceptances.elements();
            while (e.hasMoreElements()) {
                ACLMessage msg = (ACLMessage) e.nextElement();
                Iterator bla = msg.getAllReceiver();
                while (bla.hasNext()) {
                    Object bla2 = bla.next();
                    if (bla2.hashCode() == randomlySelectedLight.hashCode()) {
                        msg.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    } else {
                        msg.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    }
                }
            }
        }

    }

    //Handle receiving of messages with are not part of the contract net.
    private class ReceiveMessagesBehaviour extends CyclicBehaviour {

        private static final long serialVersionUID = -5018397038252984135L;

        @Override
        public void action() {

            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE);

            ACLMessage msg = receive(mt);
            if (msg == null) {
                block();
                return;
            }
            try {
                ContentElement content = getContentManager().extractContent(msg);
                Concept action = ((Action) content).getAction();

                switch (msg.getPerformative()) {

                    case (ACLMessage.PROPAGATE):
                        if (action instanceof TrafficLightLocationAndDirection) {
                            addBehaviour(new HandleTrafficLightLocationAndDirectionInform(myAgent, msg));
                        }
                        break;
                }
            } catch (Exception ex) {
            }
        }
    }

    //Creating meta data of all answering and know TrafficLights.
    public class HandleTrafficLightLocationAndDirectionInform extends OneShotBehaviour {

        private final ACLMessage msg;

        /**
         *
         * @param myAgent
         * @param msg
         */
        public HandleTrafficLightLocationAndDirectionInform(Agent myAgent, ACLMessage msg) {
            super(myAgent);
            this.msg = msg;
        }

        /**
         *
         */
        @Override
        public void action() {

            TrafficLightLocationAndDirection tllad;
            try {

                ContentElement content = getContentManager().extractContent(msg);
                Concept action = ((Action) content).getAction();
                tllad = (TrafficLightLocationAndDirection) action;

                if (trafficLightsMetadata.containsKey(msg.getSender()) && crossLocation.equalsIgnoreCase(tllad.getCrossLocation())) {
                    trafficLightsMetadata.get(msg.getSender()).put("location", tllad.getLocation());
                    trafficLightsMetadata.get(msg.getSender()).put("direction", tllad.getDirection());
                    System.out.println(tllad.getLocation() + " " + tllad.getDirection());
                    trafficLightsByDirection.put(tllad.getLocation() + tllad.getDirection(), msg.getSender());
                } else {
                    trafficLightsMetadata.remove(msg.getSender());
                    trafficLightAgents.remove(msg.getSender());
                }

                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.CONFIRM);
                send(reply);
            } catch (Codec.CodecException ex) {
                Logger.getLogger(Auctioneer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (OntologyException ex) {
                Logger.getLogger(Auctioneer.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    /**
     *Default execution which finds all TrafficLights then waits so all TrafficLights can be found
     * and then starts requesting by using the call for proposal protocol.
     */
    public class DefaultExecutionBehaviour extends SequentialBehaviour {

        /**
         *
         */
        public DefaultExecutionBehaviour() {
            addSubBehaviour(new FindExistingTrafficLightsBehaviour(this.myAgent, 3000));
            addSubBehaviour(new WakerBehaviour(myAgent, 3000) {

                @Override
                protected void onWake() {
                    super.onWake(); //To change body of generated methods, choose Tools | Templates.
                    addBehaviour(new RequestTrafficLightOfferBehaviour(myAgent, 1000));
                }
            });
        }

        //Using the so known yellow pages to find all TrafficLights.
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
    }

}
