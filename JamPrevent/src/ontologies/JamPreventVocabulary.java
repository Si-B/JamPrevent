/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ontologies;

/**
 *
 * @author SiB
 */
public interface JamPreventVocabulary {
    public static final String TRAFFIC_LIGHT_LOCATION_AND_DIRECTION = "TrafficLightLocationAndDirection";
    public static final String TRAFFIC_LIGHT_LOCATION_AND_DIRECTION_LOCATION = "location";
    public static final String TRAFFIC_LIGHT_LOCATION_AND_DIRECTION_DIRECTION = "direction";
    
    public static final String TRAFFIC_LIGHT_PROPERTIES = "TrafficLightProperties";
    public static final String TRAFFIC_LIGHT_PROPERTIES_CARCOUNT = "carCount";
    public static final String TRAFFIC_LIGHT_PROPERTIES_INDEX = "index";
    public static final String TRAFFIC_LIGHT_PROPERTIES_LOCATION = "location";
    public static final String TRAFFIC_LIGHT_PROPERTIES_TRAFFICSTATE = "trafficState";
    public static final String TRAFFIC_LIGHT_PROPERTIES_DIRECTION = "direction";
    
    public static final String TRAFFIC_LIGHT_STATE = "TrafficLightState";
    public static final String TRAFFIC_LIGHT_STATE_NEXTUPDATE = "nextUpdate";
    public static final String TRAFFIC_LIGHT_STATE_TRAFFICSTATE = "trafficState";
    
    public static final String TRAFFIC_LIGHT_LOAD_SIMULATION = "TrafficLightLoadSimulation";
    public static final String TRAFFIC_LIGHT_LOAD_SIMULATION_ADDITIONALCARS = "additionalCars";
}
