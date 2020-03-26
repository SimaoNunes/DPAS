package Client;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.util.Scanner;
public class App {
	
	// Client_API
	//private static Client_API clientAPI;
	// Scanner to get user input
	private static Scanner scanner;
	// Keystore with the client keyPair and Server publicKey (FIXME the keystore has all users keyPairs)
	private static KeyStore keyStore;
	// User public Key
	private static PublicKey myPublicKey;
	// Variable used to store user input
	private static String userInput;
	// Variable used to sanitize user input (only set to true if we accept user input)
	private static Boolean goodInput;
	
    public static void main(String[] args) { 
    	System.out.println("\n=================  DPAS Application =================");
    	// Initialize necessary objects
    	scanner = new Scanner(System.in);
    	myPublicKey = null;
        try {
        	keyStore = KeyStore.getInstance("JKS");
			keyStore.load(new FileInputStream("Keystores/keystore"), "changeit".toCharArray());
		} catch (NoSuchAlgorithmException | CertificateException | IOException | KeyStoreException e) {
			e.printStackTrace();
		}
    	// Ask user if he is registered or not
    	goodInput = false;
    	while(!goodInput) {
			System.out.print("\nAre you a registered user? (Y = Yes, N = No)\n>> ");
			userInput = scanner.nextLine();
			switch(userInput) {
				case "y":
				case "Y":
				case "yes":
				case "Yes":
				case "YES":
					goodInput = true;
					runApp();
					break;
				case "n":
				case "N":
				case "no":
				case "No":
				case "NO":
					goodInput = true;
					registerUser();
					break;
				default:
					System.out.println("\nYou should answer \"Yes/No\" only!");
					break;
			}
    	}
    	System.out.println("\n=======================  End  =======================");
    }

	private static void registerUser() {
    	// Ask user if he is registered or not
    	goodInput = false;
    	while(!goodInput) {
    		// Get publicKey from keystore based on user name (alias)
			System.out.print("\nWhat's your username\n>> ");
			userInput = scanner.nextLine();						//FIXME Not sanitizing user input
			try {
				myPublicKey = keyStore.getCertificate(userInput).getPublicKey();
			} catch (KeyStoreException e) {
				e.printStackTrace();
			}
			if(myPublicKey == null) {
				System.out.println("\nUnknown username in the keyStore! Must enter valid username!");
			} else {
				goodInput = true;
			}
    	}
    	// Register user
    	//clientAPI.register(myPublicKey, userInput);
    	System.out.println("\nHi " + userInput + "! You're now registered on DPAS!  ");
	}

	private static void runApp() {
		System.out.println("\nGOING TO RUN APP");
	} 
	
}

