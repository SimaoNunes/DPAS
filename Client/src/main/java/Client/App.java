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
	// Keystore with the client keyPair and Server publicKey
	private static KeyStore keyStore;
	// User public Key
	private static PublicKey myPublicKey;
	// Username
	// I wanted the user name global but not being able to do so so I'm passing it to the runApp method...
	
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
				runApp(userName);
			} catch (KeyStoreException e) {
				System.out.println("\nThere's a problem with the application.\n Error related with Keystore (problably badly loaded).");
			}
    	}
    	else {
			try {
				String userName = registerUser();
				if(!(userName == null)) {
					runApp(userName);
				}
			} catch (KeyStoreException e) {
				System.out.println("\nThere's a problem with the application.\n Error related with Keystore (problably badly loaded).");
			}
    	}
    	System.out.println("\n============================  End  ============================");
    }
    
    
    
	private static String registerUser() {
		System.out.println("\nPlease register yourself in the DPAS.");
    	// Ask user if he is registered or not
    	Boolean goodInput = false;
    	String inputUserName = null;
    	while(!goodInput) {
    		// Get publicKey from keystore based on user name (alias)
    		System.out.print("\nInsert a username\n>> ");
			inputUserName = scanner.nextLine();							//FIXME Not sanitizing user input
			try {
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
			} catch (KeyStoreException e) {
				System.out.println("\nThere's a problem with the application.\n Error related with Keystore (problably badly loaded).");
			}
    	}
    	// Register user
    	try {
			clientAPI.register(myPublicKey, inputUserName);
		} catch (AlreadyRegisteredException e) {
			System.out.println("\nUser with such username is already registered in DPAS!");
			return null;
		} catch (UnknownPublicKeyException | InvalidPublicKeyException e) {
			System.out.println("\nThere seems to be a problem with your authentication. Make sure you have the app properly installed with your CC public key.");
			return null;
		}
    	System.out.println("\nHi " + inputUserName + "! You're now registered on DPAS!");
    	return inputUserName;
	}

	
	
	private static void runApp(String userName) throws KeyStoreException {
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
		System.out.println("\nWelcome " + userName + "!");
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
		try{
			if(isGeneral){
				clientAPI.postGeneral(myPublicKey, message, null);
			} else {
				clientAPI.post(myPublicKey, message, null);
			}
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
				printAnnouncements(jsonAnnouncs, true);
			} catch (InvalidPostsNumberException e) {
				System.out.println("\nERROR: You've inserted and invalid number");
			} catch (TooMuchAnnouncementsException e) {
				System.out.println("\nERROR: The number of announcements you've asked for exceeds the number of announcements existing in such board");
			}
		} else {
			System.out.println("\nWhich User's Announcement Board you want to read from?\n>> ");
			String userName = scanner.nextLine();													//FIXME NOT SANITIZING USER INPUT
			try {
				if(keyStore.containsAlias(userName)) {
					PublicKey  publicKey = keyStore.getCertificate(userName).getPublicKey();
					jsonAnnouncs = clientAPI.read(publicKey, Integer.parseInt(numberOfPosts));
					printAnnouncements(jsonAnnouncs, false);
				} else {
					System.out.println("\nUnknown username in the keyStore! Must enter valid username!");
				}
			} catch (NumberFormatException e) {
				System.out.println("\nThere's a problem with the application.\n Error related with Keystore (problably badly loaded).");
			} catch (InvalidPostsNumberException e) {
				System.out.println("\nERROR: You've inserted and invalid number");
			} catch (UserNotRegisteredException e) {
				System.out.println("\nERROR: User is not registered in DPAS System.");
			} catch (InvalidPublicKeyException e) {
				System.out.println("\nERROR: Make sure you have the app properly installed with your CC public key.");
			} catch (TooMuchAnnouncementsException e) {
				System.out.println("\nERROR: The number of announcements you've asked for exceeds the number of announcements existing in such board");
			} catch (KeyStoreException e) {
				System.out.println("\nThere's a problem with the application.\n Error related with Keystore (problably badly loaded).");
			}
		}
	}

	
	
	private static void printAnnouncements(JSONObject jsonAnnouncs, Boolean isGeneral) {
		// Get array of announcements in JSON format, iterate over them and print them
        JSONArray array = (JSONArray) jsonAnnouncs.get("announcementList");
        int i = 0;
        // ReadGeneral
        if(isGeneral) {
            for (Object object : array) {
                JSONObject obj = (JSONObject) object;

                String user = (String) obj.get("user");
                String announcId = (String) obj.get("id");
                String msg = (String) obj.get("message");

                System.out.println("\n" + i++ + ")");
                System.out.println("Announcement From User: " + user);
                System.out.println("Id: " + announcId);
                System.out.println("Message: " + msg + "\n");
            }
        // Read
        } else {
        	System.out.println("\nAnnouncs From User: " + (String) array.get(0));
            for (Object object : array) {
                JSONObject obj = (JSONObject) object;

                String msg = (String) obj.get("message");
                String announcId = (String) obj.get("id");

                System.out.println("\n" + i++ + ")");
                System.out.println("Id: " + announcId);
                System.out.println("Message: " + msg + "\n");
            }	
        }
	}
	
	
}

