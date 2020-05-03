# Dependable Public Announcement Server
High Dependable Systems Project: DPAS.
This is Java API that wants to solve the problems concerned with the emergence of fake news and the need for trusted sources of information. We want to make sure that relevant public information and facts can be posted, tracked and verified.
	The main specifications are:
- Every user of this system has his/her own Announcement Board where only he/she can post announcements.
- There's a General Board where everyone is able to post announcements.
- Users are accountable for the announcements they post
- Users can read all the announcements of other users and obtain their cronological order
- When posting announcements, users can refer to previous announcements posted by them or other users.

## exceptions Guide

Each error code sent by the server is translated into an exception client side.

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

(Exception from endpoint to app)

- -11 -> NonceTimeout
- -12 -> OperationTimeout
- -13 -> Freshness
- -14 -> Integrity
## How to test 

To test our system and simulate possible attacks, we provided a set of tests inside the Client's module. 

#### Before testing our system, your machine should have:
- Java 13 installed
- Maven installed (at least version 3.6)
- A copy of this repository
- Works 100% on Linux, on Windows might not be consistent

#### Testing Process

- You will need to open two consoles to execute our tests. 

- In console number 1, inside the root directory of this repository, you must install all the different components with Maven. Since the tests will only work when the server is running, you should skip the test phase (for now):

```bash
mvn -DskipTests clean install
```

- Once the installation process is complete, you should start the execution of the server in console number 2, after entering inside the Server's directory. To do this, you must execute the following commands: 

```bash
cd Server
mvn exec:java
```

- Now, that the server is ready to receive requests, you can go back to console number 1 and test the whole system by executing:


```bash
mvn test
```

### Warning

During the test process, the test modules will ask you to reboot the server to simulate one possible situation that would compromise our system: 
- If the Server maintains persistent information, even if it crashes

To reboot the server, you must stop the execution on console number 2, and execute again:

```bash
ctrl+c  #interrupt the execution
mvn exec:java
```

## Contributors
- Simão Nunes
- Miguel Grilo
- Miguel Francisco