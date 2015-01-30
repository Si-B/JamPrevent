/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package messages;

import jade.content.Concept;
import java.util.Date;

/**
 *
 * @author SiB
 */
public class TrafficLightOffer implements Concept{
    private int carCount = 0;
    private Date lastGreenTime;
    private int index;
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
     * @return the lastGreenTime
     */
    public Date getLastGreenTime() {
        return lastGreenTime;
    }

    /**
     * @param lastGreenTime the lastGreenTime to set
     */
    public void setLastGreenTime(Date lastGreenTime) {
        this.lastGreenTime = lastGreenTime;
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
}
