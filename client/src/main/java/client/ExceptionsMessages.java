package client;

public class ExceptionsMessages {
	
	// General (for all methods)
	public final static String OPERATION_NOT_POSSIBLE = "The operation was not possible, please try again!";
	
	// Specific depending on method
	public final static String ALREADY_REGISTERED = "User is already registered in the DPAS!";
	public final static String MESSAGE_TOO_BIG = "Message cannot exceed 255 characters!";
	public final static String INVALID_ANNOUNCEMENTS = "Announcements referenced do not exist!";
	public final static String USER_NOT_REGISTERED = "This user is not registered!";
	
	// Can't infer if method was successful
	public final static String CANT_INFER_REGISTER = "There was a problem in the connection we can't infer precisely if the register was successful. Please try to log in";
	public final static String CANT_INFER_POST = "There was a problem in the connection, please do a read operation to confirm your post!";

}
