package Client_API;

import javax.net.ssl.KeyManagerFactory;
import java.io.*;
import java.net.Socket;

import java.security.*;
import java.security.cert.CertificateException;
import java.util.Scanner;


public class App {

	// private static Scanner myObj;
	// private static ObjectOutputStream outStream;
	// private static ObjectInputStream inStream;

    public static void main( String[] args ) {
        System.out.println( "Hello World from the Client_API!" );        

        KeyStore ks;
        PublicKey publicKey = null;
        char[] passphrase = "changeit".toCharArray();

        try {

            ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream("Keystores/keystore"), passphrase);
            Key key = ks.getKey("user1", passphrase);

            publicKey = ks.getCertificate("user1").getPublicKey();


        } catch (Exception e) {
            e.printStackTrace();
        }

        /*PublicKey key = null;
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();
            key = kp.getPublic();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }*/
        ClientAPI c = new ClientAPI();
        c.post(publicKey, "tem 256 caracteressssssssssss" +
                "sssssssssssssssssssssssssssssssssssssssssssssssss" +
                "ssssssssssssssssssssssssssssssssssssssssssssssssss" +
                "ssssssssssssssssssssssssssssssssssssssssssssssssss" +
                "sssssssssssssssssssssssssssssssssssss" +
                "sssssssssssssssssssssssssssssssssssssssss", null);
        //c.postGeneral(publicKey, "Este anuncio vai para o general", null);
    }

}
