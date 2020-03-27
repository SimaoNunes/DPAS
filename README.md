# Dependable Public Announcement Server
High Dependable Systems Project: DPAS
This is Java API that wants to solve the problems concerned with the emergence of fake news and the need for trusted sources of information. We want to make sure that relevant public information and facts can be posted, tracked and verified.
	The main specifications are:
- Every user of this system has his/her own Announcement Board where only he/she can post announcements.
- There's a General Board where everyone is able to post announcements.
- Users are accountable for the announcements they post
- Users can read all the announcements of other users and obtain their cronological order
- When posting announcements, users can refer to previous announcements posted by them or other users.

## ClientAPI
This is a Java API. All the methods return either Java primitive type objects or JSONs.
There are 5 functions you can use:
```bash
register(PublicKey publicKey, String name)
post(PublicKey key, String message, int[] announcs)
postGeneral(PublicKey key, String message, int[] announcs)
read(PublicKey key, int number)
readGeneral(int number)
```

## Setup

```bash
mvn archetype:generate -DgroupId=SEC -DartifactId=DPAS -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.4 -DinteractiveMode=false
cd DPAS
mvn package
```

## To Run
```bash
java -cp target/DPAS-1.0-SNAPSHOT.jar SEC.App
```
or
```bash
mvn compile
mvn exec:java -DmainClass=SEC.App
```
or (if main class is defined at pom.xml)
```bash
mvn compile
mvn exec:java 



# Exceptions Guide

Each error code sent by the server is translated into an exception client side.

Error Code                     Exception
-1                             UserNotRegistered
-2                             AlreadyRegistered
-3                             InvalidPublicKey
-4                             MessageTooBig
-5                             InvalidAnnouncement
-6                             InvalidPostsNumber
-7                             UnknownPublicKey
-8                             ErrorReadingFile
-9                             ErrorWrittingFile
-10                            TooMuchAnnouncements
```
