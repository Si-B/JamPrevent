/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.util.logging.Level;
import java.util.logging.Logger;
import ontologies.JamPreventOntology;
import ontologies.JamPreventVocabulary;
/**
 *Baseclass for all agents to register themselfes at the yellow pages.
 * @author SiB
 */
public class BaseAgent extends Agent implements JamPreventVocabulary{
    
    protected Codec codec = new SLCodec();
    protected Ontology ontology = JamPreventOntology.getInstance();    

    @Override
    protected void setup() {
        super.setup(); //To change body of generated methods, choose Tools | Templates.        
        // Register language and ontology
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);        
    }        
    
    protected void registerAgent(String type) {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(this.getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType(type);
        sd.setName(this.getLocalName() + "-" + type);

        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    protected void replyNotUnderstood(ACLMessage msg) {
        try {
            Action content = (Action) getContentManager().extractContent(msg);
            ACLMessage reply = msg.createReply();
            reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
            getContentManager().fillContent(reply, new Action(msg.getSender(), content));
            send(reply);
        } catch (Codec.CodecException ex) {
            Logger.getLogger(BaseAgent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (OntologyException ex) {
            Logger.getLogger(BaseAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
