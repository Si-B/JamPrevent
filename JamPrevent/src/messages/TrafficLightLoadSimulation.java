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
public class TrafficLightLoadSimulation implements Serializable {

    private int additionalCars;

    /**
     * @return the additionalCars
     */
    public int getAdditionalCars() {
        return additionalCars;
    }

    /**
     * @param additionalCars the additionalCars to set
     */
    public void setAdditionalCars(int additionalCars) {
        this.additionalCars = additionalCars;
    }

}
