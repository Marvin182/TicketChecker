TicketChecker
=============
The aim of this project is to provide an ticket ckecking system based on android for events, for example proms. The tickets must have QR-Codes which are checked during entrance with an Android app. There is a light server to allow checking with more than one Android device.


Tutorial
--------
Cooming soon.


Developing
----------

### Running the local ticket server
The ticket server is written in Scala using the play framework. All you should need a recent version of [sbt](http://www.scala-sbt.org/release/docs/Getting-Started/Setup.html). I'm using v0.13.0-RC5. To run the server open a terminal in the server directory an execute `sbt ~run`. You can access the server frontend oo `localhost:9000`.

### Requirements for developing the Android app
- latest Android SDK
- in Eclipse install (Help -> Install New Software)
	- [Scala-IDE](http://scala-ide.org/download/current.html)
	- [Android Proguard Scala](https://github.com/banshee/AndroidProguardScala)
	- [IvyDE](http://ant.apache.org/ivy/ivyde/download.cgi)
- open the project the first time
	- import the project from clients/android/ as normal Eclipse project
	- right click the ivy.xml and choos Add Ivy Library, uncheck the project specify settings in the second tab and click finish