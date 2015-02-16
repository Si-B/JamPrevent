# JamPrevent

## Execution
In order to start the project you need to change your run configuration and add pass these arguments:

```
-gui -agents "WestSouthLightRandomPredefined:agents.TrafficLight(W,S,RandomPredefined);WestEastLightRandomPredefined:agents.TrafficLight(W,E,RandomPredefined);EastWestLightRandomPredefined:agents.TrafficLight(E,W,RandomPredefined);EastSouthLightRandomPredefined:agents.TrafficLight(E,S,RandomPredefined);SouthEastLightRandomPredefined:agents.TrafficLight(S,E,RandomPredefined);SouthWestLightRandomPredefined:agents.TrafficLight(S,W,RandomPredefined);RandomPredefinedAuctioneer:agents.Auctioneer(RandomPredefined); WestSouthLight:agents.TrafficLight(W,S,Random);WestEastLight:agents.TrafficLight(W,E,Random);EastWestLight:agents.TrafficLight(E,W,Random);EastSouthLight:agents.TrafficLight(E,S,Random);SouthEastLight:agents.TrafficLight(S,E,Random);SouthWestLight:agents.TrafficLight(S,W,Random);Auctioneer:agents.Auctioneer(Random);WestSouthLightSingleHeighest:agents.TrafficLight(W,S,SingleHeighest);WestEastLightSingleHeighest:agents.TrafficLight(W,E,SingleHeighest);EastWestLightSingleHeighest:agents.TrafficLight(E,W,SingleHeighest);EastSouthLightSingleHeighest:agents.TrafficLight(E,S,SingleHeighest);SouthEastLightSingleHeighest:agents.TrafficLight(S,E,SingleHeighest);SouthWestLightSingleHeighest:agents.TrafficLight(S,W,SingleHeighest);AuctioneerSingleHeighest:agents.Auctioneer(SingleHeighest);WestSouthLightPredefined:agents.TrafficLight(W,S,Predefined);WestEastLightPredefined:agents.TrafficLight(W,E,Predefined);EastWestLightPredefined:agents.TrafficLight(E,W,Predefined);EastSouthLightPredefined:agents.TrafficLight(E,S,Predefined);SouthEastLightPredefined:agents.TrafficLight(S,E,Predefined);SouthWestLightPredefined:agents.TrafficLight(S,W,Predefined);AuctioneerPredefined:agents.Auctioneer(Predefined);ReportProvider:agents.ReportingAgent(_____PATH_______);Simulator:agents.LoadSimulatorAgent"

```
where `_____PATH_______` should be an absolute path to the frontend folder that comes with this project.

You of course need to also set `jade.Boot` as the Main Class.

After you start the JADE-Application you should open the `index.html` and the `stats.html` in the `frontend` folder (preferably with firefox if you do not want to start a local webserver) to see the statitics and visualisation.
