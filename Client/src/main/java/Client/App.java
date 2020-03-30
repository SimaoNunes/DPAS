package Client;

import Client_API.ClientAPI;

import Exceptions.AlreadyRegisteredException;
import Exceptions.InvalidAnnouncementException;
import Exceptions.InvalidPostsNumberException;
import Exceptions.InvalidPublicKeyException;
import Exceptions.MessageTooBigException;
import Exceptions.TooMuchAnnouncementsException;
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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class App {
	// Client_API
	private static ClientAPI clientAPI;
	// Scanner to get user input
	private static Scanner scanner;
	// Keystore with the client keyPair and Server publicKey (FIXME the keystore has all users keyPairs)
	private static KeyStore keyStore;
	// User public Key
	private static PublicKey myPublicKey;
	// Username
	private static String userName;
	
    public static void main(String[] args) {
    	// Check if arguments are being wrongly used (should only receive username, or no arguments at all)
    	if(args.length > 1) {
    		System.out.println("\nWrong way of running app. Either give a single argument with the user name or don't provide arguments and register a new user");
    	}
    	System.out.println("\n======================  DPAS Application ======================");
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
    	// Check if user name is provided. Otherwise register a new user
    	if(args.length == 1) {
    		@SuppressWarnings("unused")
			String userName = args[0]; //FIXME not sanitizing user input. User can give any username (Miguel) but he will only have the password for his alias.
    		try {
				runApp();
			} catch (KeyStoreException e) {
				System.out.println("\nThere's a problem with the application.\n Error related with Keystore (problably badly loaded).");
			}
    	}
    	else {
			try {
				if(registerUser()) {
					runApp();
				}
			} catch (KeyStoreException e) {
				System.out.println("\nThere's a problem with the application.\n Error related with Keystore (problably badly loaded).");
			}
    	}
    	System.out.println("\n============================  End  ============================");
    }
    
    
    
	private static Boolean registerUser() throws KeyStoreException {
		System.out.println("\nPlease register yourself in the DPAS.");
    	// Ask user if he is registered or not
    	Boolean goodInput = false;
    	String inputUserName = null;
    	while(!goodInput) {
    		// Get publicKey from keystore based on user name (alias)
    		System.out.print("\nInsert a username\n>> ");
			inputUserName = scanner.nextLine();							//FIXME Not sanitizing user input
			if(keyStore.containsAlias(inputUserName)) {
				if(keyStore.entryInstanceOf(inputUserName, KeyStore.PrivateKeyEntry.class)) {
				myPublicKey = keyStore.getCertificate(inputUserName).getPublicKey();
				goodInput = true;
				} else {
					System.out.println("\nYou're not the owner of this account!");
				}
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
    	userName = inputUserName;
    	System.out.println("\nHi " + inputUserName + "! You're now registered on DPAS!");
    	return true;
	}

	
	
	private static void runApp() throws KeyStoreException {
		// Check if username is in keystore. If so, get respective public key
		if(keyStore.containsAlias(userName)) {
			myPublicKey = keyStore.getCertificate(userName).getPublicKey();
		} else {
			System.out.println("\nUnknown username in the keyStore! Must enter valid username!");
			return;
		}
		// Run App
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
	
	
	
	private static void read() {
		Boolean run = true;
		String userInput;
		while(run) {
			System.out.print("\nWhere do you want to read from?\n"
							+ "1) An User's specific Announcement Board\n"
							+ "2) General Board\n"
							+ "0) Back\n"
							+ ">> ");
			userInput = scanner.nextLine();
			switch(userInput) {
				case "1":
					readMethod(false);
					run = false;
					break;
				case "2":
					readMethod(true);
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
		// Post announcement
		if(isGeneral) {
			try {
				clientAPI.postGeneral(myPublicKey, message, null);
			} catch (UserNotRegisteredException e) {
				System.out.println("\nERROR: User is not registered in DPAS System.");
			} catch (InvalidPublicKeyException e) {
				System.out.println("\nERROR: Make sure you have the app properly installed with your CC public key.");
			} catch (MessageTooBigException e) {
				System.out.println("\nERROR: Message size exceeds 255 characters.");
			} catch (InvalidAnnouncementException e) {
				System.out.println("\nERROR: Invalid announcement reference.");
			}
		} else {
			try {
				clientAPI.post(myPublicKey, message, null);
			} catch (UserNotRegisteredException e) {
				System.out.println("\nERROR: User is not registered in DPAS System.");
			} catch (InvalidPublicKeyException e) {
				System.out.println("\nERROR: Make sure you have the app properly installed with your CC public key.");
			} catch (MessageTooBigException e) {
				System.out.println("\nERROR: Message size exceeds 255 characters.");
			} catch (InvalidAnnouncementException e) {
				System.out.println("\nERROR: Invalid announcement reference.");
			}
		}
	}
	
	
	
	private static void readMethod(boolean isGeneral) {
		Boolean goodInput = false;
		String numberOfPosts = null;
		while(!goodInput) {
			System.out.print("\nHow many announcements do you want to read?\n>> ");
			numberOfPosts = scanner.nextLine();
			if(!numberOfPosts.matches("^[0-9]+$")) { 
				System.out.println("\nPlease insert a valid number");
			}
			else {
				goodInput = true;
			}
		}
		// Get JSONObject with announcements from the API
		JSONObject jsonAnnouncs = null;
		if(isGeneral) {
			try {
				jsonAnnouncs = clientAPI.readGeneral(Integer.parseInt(numberOfPosts));
			} catch (InvalidPostsNumberException e) {
				System.out.println("\nERROR: You've inserted and invalid number");
				return;
			} catch (TooMuchAnnouncementsException e) {
				System.out.println("\nERROR: The number of announcements you've asked for exceeds the number of announcements existing in such board");
				return;
			}
			printAnnouncements(jsonAnnouncs);
		} else {
			System.out.println("Cant read from announcement board yet.");
			/*try {
				clientAPI.post(myPublicKey, numberOfPosts, null);
			} catch (UserNotRegisteredException e) {
				System.out.println("\nERROR: user is not registered in DPAS System.");
			} catch (InvalidPublicKeyException e) {
				System.out.println("\nERROR: make sure you have the app properly installed with your CC public key.");
			} catch (MessageTooBigException e) {
				System.out.println("\nERROR: Message size exceeds 255 characters.");
			} catch (InvalidAnnouncementException e) {
				System.out.println("\nERROR: invalid announcement reference.");
			}*/
		}
	}

	
	
	private static void printAnnouncements(JSONObject jsonAnnouncs) {
        JSONArray array = (JSONArray) jsonAnnouncs.get("announcementList");

        for (Object object : array) {
            JSONObject obj = (JSONObject) object;

            String msg = (String) obj.get("message");
            String announcId = (String) obj.get("id");

            System.out.println("Announcement with ID: " + announcId);
            System.out.println("Message: " + msg + "\n");
        }
	}
	
	
}

