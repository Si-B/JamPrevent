/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package messages;

import java.io.Serializable;

/**
 *
 * @author SiB
 */
public class TrafficLightLocationAndDirection implements Serializable {

    private String location = "";
    private String direction = "";

    /**
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * @return the directon
     */
    public String getDirection() {
        return direction;
    }

    /**
     * @param direction the directon to set
     */
    public void setDirection(String direction) {
        this.direction = direction;
    }
}
