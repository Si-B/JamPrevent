/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package messages;

import jade.content.Concept;

/**
 *
 * @author SiB
 */
public class TrafficLightLocationAndDirection implements Concept  {

    private String location = "";
    private String direction = "";
    private String crossLocation = "";

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
}
