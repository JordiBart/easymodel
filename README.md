# easymodel
Public repository for EasyModel (kinetic modeling of biological systems)

Project is written in Java EE for Eclipse IDE

HOW TO IMPORT PROJECT INTO ECLIPSE

1. Download project in a temporal directory called "easymodel"
2. File>Import...>General>Existing Projects into Workspace
3. Right click on project>Ivy>Resolve

HOW TO RUN PROJECT IN ECLIPSE

1. Add an Apache Tomcat Server to Eclipse: Window>Preferences>Server>Runtime Environments>Add...
2. Right click on project>Run As>Run configurations...>Create a new Apache Tomcat configuration and add an External JAR to the Classpath: $ProgramsDirectory$/Wolfram Research/Mathematica/$Version$/SystemFiles/Links/JLink/JLink.jar
3. Right click on project>Run As>Run on Server
4. Check console messages to see if project has been sucessfully deployed and edit properties file if necessary (easymodel/easymodel.properties)

HOW TO RUN PROJECT ON A STANDALONE APACHE TOMCAT SERVER

1. Export project to WAR file in $TomcatDir$/webapps: Right click project>Export>WAR file
2. Create a file setenv.sh (linux) / setenv.bat (windows) with this content:

*CONTENT TO COMPLETE*

3. Run tomcat by executing $TomcatDir$/bin/startup.sh / startup.bat

HOW TO SET UP THE MYSQL SERVER

1. Create a database called "easymodel" in the MySQL server
2. Execute the file MySQLeasymodelDB/easymodel.sql in the MySQL server to set up the database tables etc. (in phpmyadmin: select the easymodel database and import the .sql file)
3. Edit MySQL configuration in application properties file (easymodel/easymodel.properties) if necessary and restart application
