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
public class TrafficLightProperties implements Concept  {

    private String location;
    private String direction;
    private String trafficState;
    private int carCount;
    private int index;
    private String crossLocation;

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
     * @return the direction
     */
    public String getDirection() {
        return direction;
    }

    /**
     * @param direction the direction to set
     */
    public void setDirection(String direction) {
        this.direction = direction;
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

    /**
     * @return the carCount
     */
    public int getCarCount() {
        return carCount;
    }

    /**
     * @param carCount the carCount to set
     */
    public void setCarCount(int carCount) {
        this.carCount = carCount;
    }

    /**
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * @param index the index to set
     */
    public void setIndex(int index) {
        this.index = index;
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
