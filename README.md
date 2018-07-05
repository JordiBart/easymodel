# easymodel
Public repository for EasyModel (kinetic modeling of biological systems)

Project is written in Java EE for Eclipse IDE

HOW TO IMPORT PROJECT INTO ECLIPSE

1. Download project in zip file
2. File>Import...>General>Existing Projects into Workspace>Select archive file
3. Right click on project>Ivy>Resolve

HOW TO RUN PROJECT IN ECLIPSE

1. Add an Apache Tomcat Server to Eclipse: Window>Preferences>Server>Runtime Environments>Add...
2. Right click on project>Run As>Run configurations...>Create a new Apache Tomcat configuration and add an External JAR to the Classpath: $ProgramsDirectory$/Wolfram Research/Mathematica/$Version$/SystemFiles/Links/JLink/JLink.jar
3. Right click on project>Run As>Run on Server
4. Check console messages to see if project has been sucessfully deployed and edit properties file if necessary (easymodel/easymodel.properties)

HOW TO RUN PROJECT ON A STANDALONE APACHE TOMCAT SERVER

1. Export project to WAR file in $TomcatDir$/webapps: Right click project>Export>WAR file
2. Create a file setenv.sh (linux) / setenv.bat (windows) with this content:

TODO

3. Run tomcat by executing $TomcatDir$/bin/startup.sh / startup.bat
