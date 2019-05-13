# EasyModel v1.0 RC2
Official public repository

EasyModel is a web application for kinetic modeling of biological systems.

Project is written in Java EE using the Eclipse IDE.
Dependencies: Wolfram Mathematica, MySQL, Java JDK, Vaadin 8 and Apache Tomcat.

HOW TO IMPORT PROJECT INTO ECLIPSE

1. Download the "easymodel" Java project directory it into your workspace.
2. In Eclipse: File>Import...>Maven>Existing Maven Projects>select the "easymodel" folder.
3. Right click on the project>Maven>Update Project...
4. Right click on the project>Properties>Java Build Path>Libraries>Add JARs>add the jars located in $project.basedir/src/main/webapp/WEB-INF/lib
5. Right click on easymodel project>Properties>Java Build Path>Libraries>Add External JARs>select the file $ProgramsDirectory$/Wolfram Research/Mathematica/$Version$/SystemFiles/Links/JLink/JLink.jar

HOW TO RUN PROJECT IN ECLIPSE

1. Add an Apache Tomcat Server to Eclipse: Window>Preferences>Server>Runtime Environments>Add...
2. Right click on project>Run As>Run configurations...>Create a new Apache Tomcat configuration and add an External JAR to the Classpath: $ProgramsDirectory$/Wolfram Research/Mathematica/$Version$/SystemFiles/Links/JLink/JLink.jar
3. Right click on project>Run As>Run on Server
4. Check console messages to see if project has been sucessfully deployed and edit properties file if necessary (easymodel/easymodel.properties).

HOW TO RUN PROJECT ON A STANDALONE APACHE TOMCAT SERVER

1. Export project to WAR file in $TomcatDir$/webapps: Right click project>Export>WAR file
2. Copy the file TomcatConfig/setenv.sh (linux) or setenv.bat (windows) to $TomcatDir$/bin and edit the file to fit your system.
3. Run tomcat by executing $TomcatDir$/bin/startup.sh (linux) or startup.bat (windows).
(NOTE: in UNIX server systems you may need to create a virtual screen with the command "Xvfb :0 -screen 0 1280x1024x24 &" in order to allow Mathematica generate image files)

HOW TO SET UP THE MYSQL SERVER

1. Create a database called "easymodel" in the MySQL server.
2. Execute the file MySQL_DB_easymodel/easymodel.sql in the MySQL server to set up the database tables etc. (in phpmyadmin: select the easymodel database and import the .sql file).
3. Edit MySQL configuration in EasyModel properties file (easymodel/easymodel.properties) to match your MySQL server configuration and restart Tomcat.
