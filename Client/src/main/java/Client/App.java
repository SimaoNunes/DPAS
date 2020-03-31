package Client;

import Client.ClientEndpoint;

import Library.Exceptions.AlreadyRegisteredException;
import Library.Exceptions.InvalidAnnouncementException;
import Library.Exceptions.InvalidPostsNumberException;
import Library.Exceptions.InvalidPublicKeyException;
import Library.Exceptions.MessageTooBigException;
import Library.Exceptions.TooMuchAnnouncementsException;
import Library.Exceptions.UnknownPublicKeyException;
import Library.Exceptions.UserNotRegisteredException;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class App {
	// ClientEndpoint
	private static ClientEndpoint clientEndpoint;
	// Scanner to get user input
	private static Scanner scanner = new Scanner(System.in);
	// Keystore with the client keyPair and Server publicKey
	private static KeyStore keyStore;
	// Username
	private static String userName;
	
	
    public static void main(String[] args) {
    	// Check if arguments are being wrongly used (should only receive username, or no arguments at all)
    	if(args.length > 1) {
    		System.out.println("\nWrong way of running app. Either give a single argument with the user name or don't provide arguments and register a new user");
    	}
    	System.out.println("\n======================  DPAS Application ======================");
    	// Load user's keystore
        try {
        	keyStore = KeyStore.getInstance("JKS");
			keyStore.load(new FileInputStream("Keystores/keystore"), "changeit".toCharArray());
		} catch (NoSuchAlgorithmException | CertificateException | IOException | KeyStoreException e) {
			e.printStackTrace();
		}
    	// Check if user name is provided. Otherwise register a new user
    	if(args.length == 1) {
    		userName = args[0]; 																						//FIXME not sanitizing user input
    		// Check if username is in keystore and if this user is the owner of the account
			try {
				if(keyStore.containsAlias(userName)) {
					if(keyStore.entryInstanceOf(userName, KeyStore.PrivateKeyEntry.class)) {
						clientEndpoint = new ClientEndpoint(userName);
						runApp();
					} else {
						System.out.println("\nYou're not the owner of this account!");
					}
				}
				else {
					System.out.println("\nUnknown username in the keyStore! Must enter valid username!");
				}
			} catch (KeyStoreException e) {
				System.out.println("\nThere's a problem with the application.\n Error related with Keystore (problably badly loaded).");
			}
    	} 
    	else {
			if(registerUser()) {
				clientEndpoint = new ClientEndpoint(userName);
				runApp();
			}
    	}
    	System.out.println("\n============================  End  ============================");
    }
    
    
    
	private static Boolean registerUser() {
		System.out.println("\nPlease register yourself in the DPAS.");
    	// Ask user if he is registered or not
    	Boolean goodInput = false;
    	String inputUserName = null;
    	while(!goodInput) {
    		// Check if username is trusted (aka if username alias is in keyStore)
    		System.out.print("\nInsert a username:\n>> ");
			inputUserName = scanner.nextLine();																			//FIXME Not sanitizing user input
			/*try {*/
			try {
				if(keyStore.containsAlias(inputUserName)) {
					if(keyStore.entryInstanceOf(inputUserName, KeyStore.PrivateKeyEntry.class)) {
					goodInput = true;
					userName = inputUserName;
					clientEndpoint.register();
					} else {
						System.out.println("\nYou're not the owner of this account!");
						return false;
					}
				} else {
					System.out.println("\nUnknown username in the keyStore! Must enter valid username!");
				}
			} catch (KeyStoreException e) {
				System.out.println("\nThere's a problem with the application.\n Error related with Keystore (problably badly loaded).");
				return false;
			} catch (AlreadyRegisteredException e) {
				System.out.println("\nUser with such username is already registered in DPAS!");
				return false;
			} catch (UnknownPublicKeyException | InvalidPublicKeyException e) {
				System.out.println("\nThere seems to be a problem with your authentication. Make sure you have the app properly installed with your CC public key.");
				return false;
			} 
    	}
    	System.out.println("\nHi " + userName + "! You're now registered on DPAS!");
    	return true;
	}

	
	
	private static void runApp(){
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
			} else {
				goodInput = true;
			}
		}
		// Post announcement
		try{
			if(isGeneral){
				clientEndpoint.postGeneral(message, null);
			} else {
				clientEndpoint.post(message, null);
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
		// Ask for JSONObject with announcements to the Server
		JSONObject jsonAnnouncs = null;
		try {
			if(isGeneral) {
				jsonAnnouncs = clientEndpoint.readGeneral(Integer.parseInt(numberOfPosts));
				printAnnouncements(jsonAnnouncs, true);
			} else {
				System.out.println("\nWhich User's Announcement Board you want to read from?\n>> ");
				String userName = scanner.nextLine();																	//FIXME NOT SANITIZING USER INPUT
				if(keyStore.containsAlias(userName)) {
					jsonAnnouncs = clientEndpoint.read(userName, Integer.parseInt(numberOfPosts));
					printAnnouncements(jsonAnnouncs, false);
				} else {
					System.out.println("\nUnknown username in the keyStore! Must enter valid username!");
				}
			}
		} catch (KeyStoreException e) {
			System.out.println("\nThere's a problem with the application.\n Error related with Keystore (problably badly loaded).");
		} catch (InvalidPostsNumberException e) {
			System.out.println("\nERROR: You've inserted and invalid number of announcements to read.");
		} catch (TooMuchAnnouncementsException e) {
			System.out.println("\nERROR: The number of announcements you've asked for exceeds the number of announcements existing in such board");
		} catch (UserNotRegisteredException e) {
			System.out.println("\nERROR: User is not registered in DPAS System.");
		} catch (InvalidPublicKeyException e) {
			System.out.println("\nERROR: Make sure you have the app properly installed with your CC public key.");
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
                System.out.println("Message: " + msg);
            }
        // Read
        } else {
            for (Object object : array) {
                JSONObject obj = (JSONObject) object;

                String msg = (String) obj.get("message");
                String announcId = (String) obj.get("id");

                System.out.println("\n" + i++ + ")");
                System.out.println("Id: " + announcId);
                System.out.println("Message: " + msg);
            }	
        }
	}

}

