/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package messages;

import jade.content.AgentAction;
import java.util.Date;

/**
 *
 * @author SiB
 */
public class TrafficLightState implements AgentAction {

    private Date nextUpdate;
    private String trafficState;

    /**
     * @return the nextUpdate
     */
    public Date getNextUpdate() {
        return nextUpdate;
    }

    /**
     * @param nextUpdate the nextUpdate to set
     */
    public void setNextUpdate(Date nextUpdate) {
        this.nextUpdate = nextUpdate;
    }

    /**
     * @return the trafficState
     */
    public String getTrafficState() {
        return trafficState;
    }

    /**
     * @param trafficState the trafficState to set
     */
    public void setTrafficState(String trafficState) {
        this.trafficState = trafficState;
    }

}
