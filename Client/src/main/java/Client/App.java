package Client;

import Client_API.ClientAPI;

import Exceptions.AlreadyRegisteredException;
import Exceptions.InvalidPublicKeyException;
import Exceptions.UnknownPublicKeyException;

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
	private static ClientAPI clientAPI;
	// Scanner to get user input
	private static Scanner scanner;
	// Keystore with the client keyPair and Server publicKey (FIXME the keystore has all users keyPairs)
	private static KeyStore keyStore;
	// User public Key
	private static PublicKey myPublicKey;
	
    public static void main(String[] args) { 
    	System.out.println("\n=================  DPAS Application =================");
    	// Initialize necessary objects
    	clientAPI = new ClientAPI();
    	scanner = new Scanner(System.in);
    	myPublicKey = null;
        try {
        	keyStore = KeyStore.getInstance("JKS");
			keyStore.load(new FileInputStream("Keystores/keystore"), "changeit".toCharArray());
		} catch (NoSuchAlgorithmException | CertificateException | IOException | KeyStoreException e) {
			e.printStackTrace();
		}
    	// Ask user if he is registered or not
    	Boolean goodInput = false;
    	String userInput;
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
					try {
						if(registerUser()) {
							runApp();
						}
					} catch (KeyStoreException e) {
						System.out.println("\nThere's a problem with the application.\n Error related with Keystore (problably badly loaded).");
					}
					break;
				default:
					System.out.println("\nYou should answer \"Yes/No\" only!");
					break;
			}
    	}
    	System.out.println("\n=======================  End  =======================");
    }

	private static Boolean registerUser() throws KeyStoreException {
    	// Ask user if he is registered or not
    	Boolean goodInput = false;
    	String inputUserName = null;
    	while(!goodInput) {
    		// Get publicKey from keystore based on user name (alias)
			System.out.print("\nWhat's your username\n>> ");
			inputUserName = scanner.nextLine();							//FIXME Not sanitizing user input
			if(keyStore.containsAlias(inputUserName)) {
				myPublicKey = keyStore.getCertificate(inputUserName).getPublicKey();
				goodInput = true;
			} else {
				System.out.println("\nUnknown username in the keyStore! Must enter valid username!");
			}
    	}
    	// Register user
    	try {
			clientAPI.register(myPublicKey, inputUserName);
		} catch (AlreadyRegisteredException e) {
			System.out.println("\nUser with such username is already registered in DPAS!");
			return false;
		} catch (UnknownPublicKeyException | InvalidPublicKeyException e) {
			System.out.println("\nThere seems to be a problem with your authentication. Make sure you have the app properly installed with your CC public key.");
			return false;
		}
    	System.out.println("\nHi " + inputUserName + "! You're now registered on DPAS!");
    	return true;
	}

	private static void runApp() {
		System.out.println("\nGOING TO RUN APP");
	} 
	
}

