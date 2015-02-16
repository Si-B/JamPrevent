/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ontologies;

import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.schema.AgentActionSchema;
import jade.content.schema.ConceptSchema;
import jade.content.schema.ObjectSchema;
import jade.content.schema.PrimitiveSchema;
import messages.TrafficLightLoadSimulation;
import messages.TrafficLightLocationAndDirection;
import messages.TrafficLightOffer;
import messages.TrafficLightProperties;
import messages.TrafficLightState;

/**
 *
 * @author SiB
 */
public class JamPreventOntology extends Ontology implements JamPreventVocabulary{
    
   // ----------> The name identifying this ontology
   public static final String ONTOLOGY_NAME = "JamPreventOntology";

   // ----------> The singleton instance of this ontology
   private static Ontology instance = new JamPreventOntology();

   // ----------> Method to access the singleton ontology object
   public static Ontology getInstance() { return instance; }    
 
// Private constructor
   private JamPreventOntology() {

      super(ONTOLOGY_NAME, BasicOntology.getInstance());

      try {
         // ------- Add Concepts
         ConceptSchema cs = new ConceptSchema(TRAFFIC_LIGHT_LOCATION_AND_DIRECTION);
         add(cs, TrafficLightLocationAndDirection.class);
         cs.add(TRAFFIC_LIGHT_LOCATION_AND_DIRECTION_DIRECTION, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
         cs.add(TRAFFIC_LIGHT_LOCATION_AND_DIRECTION_LOCATION, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);         
         cs.add(TRAFFIC_LIGHT_LOCATION_AND_DIRECTION_CROSSLOCATION, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);         

         cs = new ConceptSchema(TRAFFIC_LIGHT_PROPERTIES);
         add(cs, TrafficLightProperties.class);
         cs.add(TRAFFIC_LIGHT_PROPERTIES_CARCOUNT, (PrimitiveSchema) getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
         cs.add(TRAFFIC_LIGHT_PROPERTIES_INDEX, (PrimitiveSchema) getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);         
         cs.add(TRAFFIC_LIGHT_PROPERTIES_LOCATION, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);         
         cs.add(TRAFFIC_LIGHT_PROPERTIES_TRAFFICSTATE, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);         
         cs.add(TRAFFIC_LIGHT_PROPERTIES_DIRECTION, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);         
         cs.add(TRAFFIC_LIGHT_PROPERTIES_CROSSLOCATION, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);         

         cs =  new ConceptSchema(TRAFFIC_LIGHT_OFFER);         
         add(cs, TrafficLightOffer.class);
         cs.add(TRAFFIC_LIGHT_OFFER_CARCOUNT, (PrimitiveSchema) getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
         cs.add(TRAFFIC_LIGHT_OFFER_INDEX, (PrimitiveSchema) getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
         cs.add(TRAFFIC_LIGHT_OFFER_LAST_GREEN_TIME, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
         
         // ------- Add AgentActions
         AgentActionSchema as = new AgentActionSchema(TRAFFIC_LIGHT_STATE);
         add(as, TrafficLightState.class);
         as.add(TRAFFIC_LIGHT_STATE_NEXTUPDATE, (PrimitiveSchema) getSchema(BasicOntology.DATE), ObjectSchema.OPTIONAL);
         as.add(TRAFFIC_LIGHT_STATE_TRAFFICSTATE, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
                  
         as = new AgentActionSchema(TRAFFIC_LIGHT_LOAD_SIMULATION);
         add(as, TrafficLightLoadSimulation.class);
         as.add(TRAFFIC_LIGHT_LOAD_SIMULATION_ADDITIONALCARS, (PrimitiveSchema) getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
      }
      catch (OntologyException oe) {
      }
   }      
}
