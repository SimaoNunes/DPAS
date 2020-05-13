# Dependable Public Announcement Server

**High Dependable Systems Project: DPAS**.
This is Java API that wants to solve the problems concerned with the emergence of fake news and the need for trusted sources of information. We want to make sure that relevant public information and facts can be posted, tracked and verified.
	The main specifications are:
- Every user of this system has his/her own Announcement Board where only he/she can post announcements.
- There's a General Board where everyone is able to post announcements.
- Users are accountable for the announcements they post
- Users can read all the announcements of other users and obtain their cronological order
- When posting announcements, users can refer to previous announcements posted by them or other users.

## ClientApp

We developed a simple App that runs the API as an example. To run it you must go to the directory *client* and then run the app:

```bash
cd client
mvn exec:java
```

If you don't provide any arguments, the application assumes you are trying to register to the Service. If so you must provide an username that appears on the keystore [user1, user2, user3] because we are assuming the server and clients know all the keys from everyone, and for simplification, we used the keystore alias as their well known usernames.
If you are already registered in the system you must run the app as a registered user:

```bash
cd client
mvn exec:java -Dexev.args="<username>"
```

Now just enjoy the application!

## How to test 

To test our system and simulate possible attacks, we provided a set of tests inside the *client* module. 

#### Before testing our system, your machine should have:
- Java 13 installed
- Maven installed (at least version 3.6)
- A copy of this repository
- Works 100% on Linux, on Windows might not be consistent

#### Testing Process

- You will need to open two consoles to execute our tests. 

- In console number 1, inside the root directory of this repository (DPAS), you must install all the different components with Maven. Since the tests will only work when the server is running, you should skip the test phase (for now):

```bash
mvn -DskipTests clean install
```

- Once the installation process is complete, you should start the execution of the servers in console number 2, after entering inside the directory *server*. To do this, you must execute the following commands (this will run 4 servers):

```bash
cd server
mvn exec:java
```

- Now, that the servers are ready to receive requests, you can go back to console number 1 and test the whole system by executing:

```bash
mvn test
```

- If you want to run a single test you can also run the following command:

```bash
mvn -Dtest=<testname> test
```

### Warnings

During the test process, the test modules will ask you to reboot the server to simulate one possible situation that would compromise our system: 
- If the Server maintains persistent information, even if it crashes

To reboot the server, you must stop the execution on console number 2, and execute again:

It may also be the case that you may need to delete the *storage* files in case of test fails, in order to run them properly in a next round of tests (tests can fail because of concurrence, for example if a machine is much slower than a certain assumption a timeout will be exceed).

```bash
ctrl+c  #interrupt the execution
mvn exec:java
```
## Exceptions Guide

Every time the server must throw an exception, it sends an error code to the client.
This error code is translated into an exception as the following:

- -1 -> UserNotRegistered
- -2 -> AlreadyRegistered
- -3 -> InvalidPublicKey (currently not being used, only UnknownPublicKey)
- -4 -> MessageTooBig
- -5 -> InvalidAnnouncement
- -6 -> InvalidPostsNumber
- -7 -> UnknownPublicKey
- -8 -> ErrorReadingFile
- -9 -> ErrorWrittingFile
- -10 -> TooMuchAnnouncements

This are exceptions that the server doesn't throw explicitly, which means this are exceptios that the endpoint
interprets based on timeouts and non fresh/non integrate messages.

- -11 -> NonceTimeout
- -12 -> OperationTimeout
- -13 -> Freshness
- -14 -> Integrity

## Contributors
- Simão Nunes
- Miguel Grilo
- Miguel Francisco