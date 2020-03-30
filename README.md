# Dependable Public Announcement Server
High Dependable Systems Project: DPAS.
This is Java API that wants to solve the problems concerned with the emergence of fake news and the need for trusted sources of information. We want to make sure that relevant public information and facts can be posted, tracked and verified.
	The main specifications are:
- Every user of this system has his/her own Announcement Board where only he/she can post announcements.
- There's a General Board where everyone is able to post announcements.
- Users are accountable for the announcements they post
- Users can read all the announcements of other users and obtain their cronological order
- When posting announcements, users can refer to previous announcements posted by them or other users.

## ClientAPI
This is a Java API. All the methods return either Java primitive type objects or JSONs.
There are 5 methods you can use:
```bash
register(PublicKey publicKey, String name) : returns int
post(PublicKey key, String message, int[] announcs) : returns int
postGeneral(PublicKey key, String message, int[] announcs) : returns int
read(PublicKey key, int number) : returns JSONObject
readGeneral(int number) : returns JSONObject
```

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

## Application Example

We also developed an example of an application that uses the DPAS ClientAPI. You can find it in the Client module. Hope it helps to better understand how it works!

## Contributors
- Simão Nunes
- Miguel Grilo
- Miguel Francisco


## note to remove later
1) cliente manda mensagem ao server a dizer que quer uma nounce para um pedido x
2) server diz ok e da uma nonce (random com 128bitts)
3) o cliente recebe a nonce, e faz o seu Request com a nonce appended, e manda tambem uma cnonce (client nonce)
4) o server recebe o pedido, verifica a nonce, faz o que é suposto e cria o objeto response, e responde ao cliente com o q ele quer mais a nova cnonce para haver freshness para os dois lados
