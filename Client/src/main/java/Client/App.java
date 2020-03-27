package Client;

import Client_API.ClientAPI;

import Exceptions.AlreadyRegisteredException;
import Exceptions.InvalidAnnouncementException;
import Exceptions.InvalidPublicKeyException;
import Exceptions.MessageTooBigException;
import Exceptions.UnknownPublicKeyException;
import Exceptions.UserNotRegisteredException;

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
		Boolean run = true;
		String userInput;
		while(run) {
			System.out.print("\nPlease choose the number of what you want to perform and press enter:\n"
							+ "1) Post an Announcement\n"
							+ "2) Read Announcements\n"
							+ "0) Quit\n"
							+ ">> ");
			userInput = scanner.nextLine();
			switch(userInput) {
				case "1":
					post();
					break;
				case "2":
					read();
					break;
				case "0":
					System.out.print("\nSure you want to quit? (Y = Yes, N = No)\n>> ");
					userInput = scanner.nextLine();
					switch(userInput) {
						case "y":
						case "Y":
						case "yes":
						case "Yes":
						case "YES":
							run = false;
							break;
						default:
					}				
					break;
				default:
					System.out.println("\nInvalid instruction!");
			}
		}
	}

	private static void post() {
		Boolean run = true;
		String userInput;
		while(run) {
			System.out.print("\nWhere do you want to post the announcement?\n"
							+ "1) My personal Announcement Board\n"
							+ "2) General Board\n"
							+ "0) Back\n"
							+ ">> ");
			userInput = scanner.nextLine();
			switch(userInput) {
				case "1":
					postMethod(false);
					run = false;
					break;
				case "2":
					postMethod(true);
					run = false;
					break;
				case "0":
					run = false;				
					break;
				default:
					System.out.println("\nInvalid instruction!");
			}
		}
	}

	private static void postMethod(Boolean isGeneral) {
		Boolean goodInput = false;
		String message = null;
		while(!goodInput) {
			System.out.print("\nType your message:\n>> ");
			message = scanner.nextLine();
			if(message.length() > 255) { 
				System.out.println("\nMessage size exceeds 255 characters.");
			}
			else {
				goodInput = true;
			}
		}
		if(isGeneral) {
			try {
				clientAPI.postGeneral(myPublicKey, message, null);
			} catch (UserNotRegisteredException e) {
				System.out.println("\nThere was an error: user is not registered in DPAS System.");
			} catch (InvalidPublicKeyException e) {
				System.out.println("\nThere was an error: make sure you have the app properly installed with your CC public key.");
			} catch (MessageTooBigException e) {
				System.out.println("\nThere was an error: Message size exceeds 255 characters.");
			} catch (InvalidAnnouncementException e) {
				System.out.println("\nThere was an error: invalid announcement reference.");
			}
		} else {
			try {
				clientAPI.post(myPublicKey, message, null);
			} catch (UserNotRegisteredException e) {
				System.out.println("\nThere was an error: user is not registered in DPAS System.");
			} catch (InvalidPublicKeyException e) {
				System.out.println("\nThere was an error: make sure you have the app properly installed with your CC public key.");
			} catch (MessageTooBigException e) {
				System.out.println("\nThere was an error: Message size exceeds 255 characters.");
			} catch (InvalidAnnouncementException e) {
				System.out.println("\nThere was an error: invalid announcement reference.");
			}
		}
	}

	private static void read() {
		System.out.println("\nGOING TO READ");
	}
	
}

