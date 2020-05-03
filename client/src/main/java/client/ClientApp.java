package client;

import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import exceptions.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class ClientApp {
	// ClientEndpoint
	private static ClientEndpoint clientEndpoint = null;
	// Scanner to get user input
	private static Scanner scanner = new Scanner(System.in);
	// Keystore with the client keyPair and Server publicKey
	private static KeyStore keyStore = null;
	// Username
	private static String userName = null;
	
	private static final String INVALID = "\nInvalid instruction!";
	
    public static void main(String[] args) {
    	System.out.println("\n======================  DPAS Application ======================");
    	// Check if arguments are being wrongly used (should only receive username, or no arguments at all)
    	if(args.length > 2 || args.length == 0) {
    		System.out.println("\nWrong way of running app. Either give the number of faults the system supports" +
									   " or provide faults and user in this order or don't provide at all and register");
    	}
    	// Check if user name is provided. Otherwise register a new user
    	int faults = Integer.parseInt(args[0]);
    	String server = getServerAddress();
		if(args.length == 2) {
    		userName = args[1]; 																						//FIXME not sanitizing user input
    		// Try to load user's keystore and if this user is the owner of the account
			try(FileInputStream fis = new FileInputStream("keystores/" + userName + "_keystore")) {
		    	// Try to load user's keystore
	        	keyStore = KeyStore.getInstance("JKS");
				keyStore.load(fis, "changeit".toCharArray());
				clientEndpoint = new ClientEndpoint(userName, server, faults);
				runApp();
			} catch (KeyStoreException e) {
				System.out.println("\nThere's a problem with the application.\n Error related with Keystore (problably badly loaded). You sure you typed your name right?");
			} catch (
				NoSuchAlgorithmException | 
				CertificateException	 | 
				IOException e) {
				System.out.println("\nThere's a problem with the application.\n Error related with Keystore (problably badly loaded).");
			}
    	} 
    	else {
			if(registerUser(server, faults)) {
				runApp();
			}
    	}
    	System.out.println("\n============================  End  ============================");
    }
    
    
///////////////
//			 //
//	Run App  //
//			 //
///////////////
    
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
					System.out.println(INVALID);
			}
		}
	}
    
	
//////////////////////
//					//
//	Register User	//
//					//
//////////////////////
	
	private static Boolean registerUser(String server, int faults) {
		System.out.println("\nPlease register yourself in the DPAS.");
		String inputUserName = null;
		// Check if username is trusted (aka if username alias is in keyStore)
		System.out.print("\nInsert a username:\n>> ");
		inputUserName = scanner.nextLine();																			//FIXME Not sanitizing user input
		try(FileInputStream fis = new FileInputStream("keystores/" + inputUserName + "_keystore")) {
	    	// Try to load user's keystore
	    	keyStore = KeyStore.getInstance("JKS");
			keyStore.load(fis, "changeit".toCharArray());
			userName = inputUserName;
			clientEndpoint = new ClientEndpoint(userName, server, faults);
			clientEndpoint.register();
		} catch (
			KeyStoreException		 | 
			NoSuchAlgorithmException | 
			CertificateException	 | 
			IOException e) {
			System.out.println("\nThere's a problem with the application.\n Error related with Keystore (problably badly loaded)");
			return false;
		} catch (UnknownPublicKeyException e) {
			System.out.println("\nThere seems to be a problem with your authentication. Make sure you have the app properly installed with your CC public key.");
			return false;
		} catch (
			AlreadyRegisteredException |
			NonceTimeoutException	   | 
			OperationTimeoutException e) {
			System.out.println("\n"+e.getMessage());
			return false;
		} catch (
			IntegrityException | 
			FreshnessException e) {
			System.out.println("\n" + e.getMessage());
		}
		System.out.println("\nHi " + userName + "! You're now registered on DPAS!");
		return true;
	}

	
///////////////////
//				 //
//	Post Method	 //
//				 //
///////////////////	

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
					System.out.println(INVALID);
			}
		}
	}	
	
	
	private static void postMethod(Boolean isGeneral) {
		// Ask for message
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
		// Ask if user wants to reference other Announcements
		Boolean end = false;
		String announcId;
		List<Integer> announcsList  = new ArrayList<Integer>();
		System.out.print("\nType the Ids of the Announcements you want to reference, and press Enter in between them."
						 + "When you're finished, press Enter again."
						 + "If you don't want to reference any Announcement, just press Enter.\n>>");
		while(!end) {
			System.out.print(" ");
			announcId = scanner.nextLine();
			if(announcId.isEmpty()) {
				end = true;
			}
			else if(announcId.matches("^[0-9]+$")) { 
				announcsList.add(Integer.parseInt(announcId));
			}
			else {
				System.out.println("\nPlease insert a valid number");
			}
		}
		int[] announcsArray = toIntArray(announcsList);
		// Post announcement
		try{
			if(isGeneral){
				clientEndpoint.broadcastPost(message, announcsArray, true);
			} else {
				clientEndpoint.broadcastPost(message, announcsArray, false);
			}
		} catch (
			UserNotRegisteredException 	 | 
			MessageTooBigException 		 | 
			InvalidAnnouncementException | 
			NonceTimeoutException		 | 
			OperationTimeoutException	 |
			IntegrityException			 | 
			FreshnessException e) {
			System.out.println("\n"+e.getMessage());
		}
	}
	
	
///////////////////
//				 //
//	Read Method	 //
//				 //
///////////////////
	
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
					System.out.println(INVALID);
			}
		}
	}
	
	
	private static void readMethod(boolean isGeneral) {
		String numberOfPosts = askForNAnnouncements();
		// Ask for JSONObject with announcements to the Server
		JSONObject jsonAnnouncs = null;
		try {
			if(isGeneral) {
				jsonAnnouncs = clientEndpoint.readGeneral(Integer.parseInt(numberOfPosts));
				if(!(jsonAnnouncs == null))
					printAnnouncements(jsonAnnouncs, true);
			} else {
				System.out.print("\nWhich User's Announcement Board you want to read from?\n>> ");
				String userName = scanner.nextLine();    //CUIDADO!!!-> NOT SANITIZING USER INPUT
				if(keyStore.containsAlias(userName)) {
					jsonAnnouncs = clientEndpoint.read(userName, Integer.parseInt(numberOfPosts));
					if(!(jsonAnnouncs == null))
						printAnnouncements(jsonAnnouncs, false);
				} else {
					System.out.println("\nUnknown username in the keyStore! Must enter valid username!");
				}
			}
		} catch (KeyStoreException e) {
			System.out.println("\nThere's a problem with the application.\n Error related with Keystore (problably badly loaded).");
		} catch (
			InvalidPostsNumberException	  |
			TooMuchAnnouncementsException |
			UserNotRegisteredException 	  |
			NonceTimeoutException 		  | 
			OperationTimeoutException 	  | 
			IntegrityException 			  |
			FreshnessException e) {
			System.out.println("\n" + e.getMessage());
		}
	}	
	
	
//////////////////////////////
//						    //
// 	   Auxiliary Methods    //
//						    //
//////////////////////////////
	
	private static String askForNAnnouncements(){
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
        return numberOfPosts;
    }
	
	
	private static void printAnnouncements(JSONObject jsonAnnouncs, Boolean isGeneral) {
		// Get array of announcements in JSON format, iterate over them and print them
        JSONArray array = (JSONArray) jsonAnnouncs.get("announcementList");
        int i = 1;
        // ReadGeneral
        for (Object object : array) {

            String user = "";
            String result = "";

            JSONObject obj = (JSONObject) object;
            result = "\n" + i++ + ")";
            if(isGeneral){
                user = (String) obj.get("user");
                result += "\nAnnouncement From User: " + user;
            }

            String announcId = (String) obj.get("id");
            String date = (String) obj.get("date");
            String msg = (String) obj.get("message");
            JSONArray refs = (JSONArray) obj.get("ref_announcements");

            result += "\nId: " + announcId + "\nDate: " + date + "\nMessage: " + msg + "\n";

            System.out.println(result);
            if(!(refs == null)) {
                System.out.print("References:");
                for (Object ref : refs) {
                    String refString = (String) ref;
                    System.out.print(" " + refString);
                }
                System.out.println();
            }
        }
	}
	
	
	private static int[] toIntArray(List<Integer> list){
		if(list.size() == 0) {
			return new int[0];
		} else {
			int[] ret = new int[list.size()];
			for(int i = 0;i < ret.length;i++)
				ret[i] = list.get(i);
			return ret;
		}
	}
	
    private static String getServerAddress(){
    	File file = new File("server_address.txt");

		String address = null;

		try(BufferedReader br = new BufferedReader(new FileReader(file))) {

			address = br.readLine();

		} catch (IOException e) {
			e.printStackTrace();
		}
		if(address == null){
			return "localhost";
		}
		else{
			return address;
		}
	}

}

