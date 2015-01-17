/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 *
 * @author SiB
 */
public class TrafficLight extends Agent{
    private String location;
    private String state;

    @Override
    public void setup() {
        
        Object[] arguments = getArguments();
        state = "red";
        
        if(arguments.length > 0){
            location = arguments[0].toString();
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

				if (message.equals("getLocation")) {
                                    System.out.println("SENDING LOCATION");
                                    reply.setContent("getLocation");
                                    reply.setPerformative(ACLMessage.INFORM);
                                    reply.addUserDefinedParameter("location", getLocation());                                    
				} else {
					System.out.println("not understood");
					reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
					// Reply with not-understood
				}
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
                                    System.out.println("setting state");
                                    setState(m.getUserDefinedParameter("state"));                                
				} else {
                                    System.out.println("not understood");
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

    public void setState(String state){
        this.state = state;
    }
    
    public String getLocation() {
        return location;
    }
    
}
