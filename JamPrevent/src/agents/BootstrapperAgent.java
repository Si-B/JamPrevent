/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import jade.core.AID;
import jade.domain.JADEAgentManagement.CreateAgent;
import static jade.tools.sniffer.Message.r;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author knut
 */
/**
 * This Agent creates one SimulatorAgent and all the TrafficLightAgents and
 * Auctioneers for each auctioning-method set in its types array ("Random",
 * "RandomPredefined", "Predefined", "SingleHeighest").
 */
public class BootstrapperAgent extends BaseAgent {

    /**
     * The setup method gets called automatically and creates the agents.
     */
    @Override
    public void setup() {
        try {
            super.setup();
            Object[] arguments = getArguments();

            ContainerController cc = getContainerController();

            String[] types = new String[]{"Random", "RandomPredefined", "Predefined", "SingleHeighest"};
            AgentController ac;
            ac = cc.createNewAgent("SimulatorAgent", "agents.LoadSimulatorAgent", new String[]{});
            ac.start();

            for (String type : types) {
                ac = cc.createNewAgent("SouthEast" + type, "agents.TrafficLight", new String[]{"S", "E", type});
                ac.start();

                ac = cc.createNewAgent("SouthWest" + type, "agents.TrafficLight", new String[]{"S", "W", type});
                ac.start();

                ac = cc.createNewAgent("EastWest" + type, "agents.TrafficLight", new String[]{"E", "W", type});
                ac.start();

                ac = cc.createNewAgent("EastSouth" + type, "agents.TrafficLight", new String[]{"E", "S", type});
                ac.start();

                ac = cc.createNewAgent("WestEast" + type, "agents.TrafficLight", new String[]{"W", "E", type});
                ac.start();

                ac = cc.createNewAgent("WestSouth" + type, "agents.TrafficLight", new String[]{"W", "S", type});
                ac.start();

                ac = cc.createNewAgent("Auctioneer" + type, "agents.Auctioneer", new String[]{type});
                ac.start();
            }

        } catch (StaleProxyException ex) {
            Logger.getLogger(BootstrapperAgent.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
