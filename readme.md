TicketChecker
=============
The aim of this project is to provide an ticket ckecking system based on Android for your own events, for example proms. The tickets must have QR-Codes which are checked during entrance with an Android app. There is a little server to allow synchronisation between several Android devices.

Both the Android app and the server are written in Scala. The server uses the Play Framework.


Developing
----------

### Running the ticket server
The ticket server is written in Scala using the Play Framework. All you should need a recent version of [sbt](http://www.scala-sbt.org/release/docs/Getting-Started/Setup.html). I'm using v0.13.0. To run the server open a terminal in the server directory an execute `sbt ~run`. You can access the server frontend in your browser from `localhost:9000`.

Make sure your Android device has access to the server (is in the same LAN) and enter the IP address of the computer running the server in the app settings..

### Developing the Android app with sbt
- get the latest Android SDK
- install sbt 0.12 or newer
- open a terminal in clients/android-sbt and execute "sbt"
	- you should now have an sbt console, to compile, package and install the app on your (connected) Android device type "run"
	- "~run" will recompile and update the app everytime you make changes to the source
	- for more commands look at the [android-sdk-plugin](https://github.com/pfn/android-sdk-plugin) or read the [sbt documentation](http://www.scala-sbt.org/release/docs/index.html)
