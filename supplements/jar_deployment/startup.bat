:: create a directory and place this files:
:: -this .bat file
:: -JLinkNativeLibrary.dll
:: -JLinkNativeLibrary.lib
:: -the EasyModel jar created with mvn clean package -Pproduction -Dvaadin.force.production.build=true
::
:: execute this .bat to start the server
start /min "EasyModel-Server" java -jar easymodel-2.4-SNAPSHOT.jar --server.port=8000
