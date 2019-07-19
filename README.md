# EasyModel v1.0
Official public repository

EasyModel is a web application for kinetic modeling of biological systems.

Project is written in Java EE using the Eclipse IDE.
Dependencies: Wolfram webMathematica, MySQL, Java 8 JDK, Vaadin 8 and Apache Tomcat 9.

HOW TO IMPORT PROJECT INTO ECLIPSE

1. Download the "easymodel" Java project directory it into your workspace.
2. In Eclipse: File>Import...>Maven>Existing Maven Projects>select the "easymodel" folder.
3. Right click on the project>Maven>Update Project...
4. Right click on the project>Properties>Java Build Path>Libraries>Add External JARs>add the jars located in $webMathematicaDir/WEB-INF/lib

HOW TO RUN PROJECT IN ECLIPSE

1. Add an Apache Tomcat Server to Eclipse: Window>Preferences>Server>Runtime Environments>Add...
2. Right click on project>Run As>Run configurations...>Create a new Apache Tomcat configuration and add an External JAR to the Classpath: $webMathematicaDir/WEB-INF/lib/JLink.jar
3. Right click on project>Run As>Run on Server
4. Check console messages to see if project has been sucessfully deployed and edit properties file if necessary (easymodel-appdata/easymodel.properties).

HOW TO RUN PROJECT ON A STANDALONE APACHE TOMCAT SERVER ON LINUX

1. Export project from Eclipse to WAR file in $TomcatDir$/webapps: Right click project>Export>WAR file
2. Copy the file TomcatConfig/setenv.sh to $TomcatDir$/bin and edit the file to fit your system.
3. Run tomcat by executing $TomcatDir$/bin/startup.sh.
(NOTE: in UNIX server systems you may need to create a virtual display with the command "vncserver" in order to allow webMathematica generate image files)

HOW TO SET UP THE MYSQL SERVER

1. Create a database called "easymodel" in the MySQL server.
2. Execute the file MySQL_DB_easymodel/easymodel.sql in the MySQL server to set up the database tables etc. (in phpmyadmin: select the easymodel database and import the .sql file).
3. Edit MySQL configuration in EasyModel properties file (easymodel/easymodel.properties) to match your MySQL server configuration and restart Tomcat.
