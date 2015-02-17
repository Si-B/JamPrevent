# JamPrevent

## Execution
In order to start the project you need to change your run configuration and add pass these arguments:

```
-gui -agents "bootstrapper:agents.BootstrapperAgent;ReportingAgent:agents.ReportingAgent(_____PATH_______)"
```

where `_____PATH_______` should be an absolute path to the `frontend` folder that comes with this project.
If you are starting this project from eclipse you may not need the `agents.` namespace in front of the two agents.

You of course need to also set `jade.Boot` as the Main Class.

After you start the JADE-Application you should open the `index.html` and the `stats.html` in the `frontend` folder (preferably with firefox if you do not want to start a local webserver) to see the statitics and visualisation.
