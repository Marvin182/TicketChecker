TicketChecker
=============
The aim of this project is to provide an ticket ckecking system based on Android for your own events, for example proms. The tickets must have QR-Codes which are checked during entrance with an Android app. There is a little server to allow synchronisation between several Android devices.

Both the Android app and the server are written in Scala. The server uses the Play Framework.


Usage
-----
Server
- Start up the server
	- you will need to install [sbt](http://www.scala-sbt.org/release/docs/Getting-Started/Setup.html)
	- go server/ directory and execute `sbt run` in your terminal
	- closing the terminal window or pressing Ctrl+D will shut down the server
	- on first run the sbt will download and install all dependency, this might take a minute
	- open localhost:9000 in your favourite browser
	- on first start up there will be an error "Database 'default' needs evolution!". Just apply the listed evolutions, it's a initial database setup.
	- a login window should appear
- Adding users
	- Initially there be a admin user "Admin" with password "me" and to non admin users called "Max" and "Peter" without passwords
	- You should define your own users by creating "~/.ticketchecker/users.csv"
	- write "username;password;isAdmin" in the first line and then one line per user
	- restart the server (all users that are not in the csv file will be deleted)
- Adding tickets
	- Initially there will be some tickets, you can overwrite these by creating  "~/.ticketchecker/tickets.csv"
	- write "order;code;forename;surname;student;table" in the first line and then one line for each ticket
	- if you change the ticket list the server will try to match tickets from the database to the tickets in the csv file (on server start up) based on order and code and only update the table number
	- combinations of order and code that are not in the tickets.csv will be removed from the database

Clients
The current version of the client has been published in the app store. So if you don't need any changes, just install [TicketChecker by Barti](https://play.google.com/store/apps/details?id=de.mritter.ticketchecker.client) on your Android phones.

In the top bar app you will find buttons to turn on the phones flash light (can improve scanning) and connection setting. The address should be the IP address of your computer running the server (has to be in the same LAN) and port 9000. The user doesn't need to be administrator.


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
