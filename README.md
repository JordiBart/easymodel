# EasyModel

EasyModel is a web application for modeling and simulation of systems biology models.

This guide is designed to assist in establishing a development environment for EasyModel on a Windows system.
This project has been written mostly in java using the JetBrains IntelliJ IDEA.
Dependencies: Wolfram Mathematica (proprietary), JetBrains IntelliJ IDEA, Java 21 JDK, Node.js, Git, Apache Maven, Laragon, phpmyadmin.

# EasyModel Development Setup Guide (for Windows)

Install JetBrains IntelliJ IDEA Community Edition (free), Amazon Corretto JDK 21, Node.js, Git.
Download and install Maven (unzip to a folder e.g. C:\maven) and set the system var MAVEN_HOME (e.g. C:\maven) and add %MAVEN_HOME%\bin to the PATH system var.
Setup the Maven wrapper: run in terminal: mvn wrapper:wrapper
Import this project folder into IntelliJ IDEA.
(Delete the "node_modules" directory from the project dir if present.)
Right click project folder>Maven>Reload project
Run Maven "clean install"

Wolfram Mathematica setup:
Install Wolfram Mathematica 14.0
Run on terminal the JLink-maven-install-file.bat from the project folder to install the JLink JAR file to the local Maven repository.

Database MySQL:
Install Laragon (WAMP)
Download phpmyadmin zip and extract to C:\laragon\etc\apps\phpmyadmin
Start Laragon services
Right-click on Laragon>MySQL>PHPMyAdmin to open the web MySQL admin panel. User "root"; password "".
Create a new database named "easymodel" with the charset "latin1_general_ci". Then import the easymodel.sql file located in the "supplements" folder to create and populate the database tables.

Edit easymodel-appdata\easymodel.properties according to Mathematica, MySQL etc.

## Running the application

The project is a standard Maven project. To run it from the command line,
type `mvnw` (Windows), or `./mvnw` (Mac & Linux), then open
http://localhost:8080 in your browser.

You can also import the project to your IDE of choice as you would with any
Maven project. Read more on [how to import Vaadin projects to different IDEs](https://vaadin.com/docs/latest/guide/step-by-step/importing) (Eclipse, IntelliJ IDEA, NetBeans, and VS Code).

## Deploying to Production

Check the ./supplements/jar_deployment directory for more specific details on how to deploy EasyModel to production.

To create a production build, call `mvnw clean package -Pproduction` (Windows),
or `./mvnw clean package -Pproduction` (Mac & Linux).
This will build a JAR file with all the dependencies and front-end resources,
ready to be deployed. The file can be found in the `target` folder after the build completes.

Once the JAR file is built, you can run it using
`java -jar target/easymodel-X.X-SNAPSHOT.jar`

## Project structure

- `MainLayout.java` in `src/main/java` contains the navigation setup (i.e., the
  side/top bar and the main menu). This setup uses
  [App Layout](https://vaadin.com/docs/components/app-layout).
- `views` package in `src/main/java` contains the server-side Java views of your application.
- `views` folder in `frontend/` contains the client-side JavaScript views of your application.
- `themes` folder in `frontend/` contains the custom CSS styles.

## Useful links

- Read the documentation at [vaadin.com/docs](https://vaadin.com/docs).
- Follow the tutorial at [vaadin.com/docs/latest/tutorial/overview](https://vaadin.com/docs/latest/tutorial/overview).
- Create new projects at [start.vaadin.com](https://start.vaadin.com/).
- Search UI components and their usage examples at [vaadin.com/docs/latest/components](https://vaadin.com/docs/latest/components).
- View use case applications that demonstrate Vaadin capabilities at [vaadin.com/examples-and-demos](https://vaadin.com/examples-and-demos).
- Build any UI without custom CSS by discovering Vaadin's set of [CSS utility classes](https://vaadin.com/docs/styling/lumo/utility-classes).
- Find a collection of solutions to common use cases at [cookbook.vaadin.com](https://cookbook.vaadin.com/).
- Find add-ons at [vaadin.com/directory](https://vaadin.com/directory).
- Ask questions on [Stack Overflow](https://stackoverflow.com/questions/tagged/vaadin) or join our [Discord channel](https://discord.gg/MYFq5RTbBn).
- Report issues, create pull requests in [GitHub](https://github.com/vaadin).
