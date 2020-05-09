package client;

import exceptions.AlreadyRegisteredException;
import exceptions.InvalidAnnouncementException;
import exceptions.InvalidPostsNumberException;
import exceptions.MessageTooBigException;
import exceptions.TooMuchAnnouncementsException;
import exceptions.UnknownPublicKeyException;
import exceptions.UserNotRegisteredException;
import library.Response;

public class ResponseChecker {
	
//////////////////////////////////////////////////////////////
//															//
//   Methods that check if Responses must throw exceptions  //
//															//
//////////////////////////////////////////////////////////////
	
    public static void checkRegister(Response response) throws AlreadyRegisteredException, UnknownPublicKeyException {
        if(!response.getSuccess()){
            int error = response.getErrorCode();
            if(error == -7) {
                throw new UnknownPublicKeyException("Such key doesn't exist in the server side!");
            }
            else if(error == -2) {
                throw new AlreadyRegisteredException("User with that public key already registered in the DPAS!");
            }
        }
    }

    public static void checkPost(Response response) throws UserNotRegisteredException,
            MessageTooBigException, InvalidAnnouncementException {
        if(!response.getSuccess()){
            int error = response.getErrorCode();
            if(error == -1) {
                throw new UserNotRegisteredException("This user is not registered!");
            }
            else if(error == -4) {
                throw new MessageTooBigException("Message cannot exceed 255 characters!");
            }
            else if(error == -5) {
                throw new InvalidAnnouncementException("Announcements referenced do not exist!");
            }
        }
    }

    public static void checkRead(Response response) throws UserNotRegisteredException, InvalidPostsNumberException, TooMuchAnnouncementsException {
        if(!response.getSuccess()){
            int error = response.getErrorCode();
            if(error == -1) {
                throw new UserNotRegisteredException("This user is not registered!");
            }
            else if(error == -3) {
                throw new UserNotRegisteredException("The user you're reading from is not registered!");
            }
            else if(error == -6) {
                throw new InvalidPostsNumberException("Invalid announcements number to be read!");
            }
            else if(error == -10) {
                throw new TooMuchAnnouncementsException("The number of announcements you've asked for exceeds the number of announcements existing in such board");
            }
        }
    }
    
    public static void checkReadGeneral(Response response) throws InvalidPostsNumberException, TooMuchAnnouncementsException, UserNotRegisteredException {
        if(!response.getSuccess()){
            int error = response.getErrorCode();
            if(error == -1) {
                throw new UserNotRegisteredException("This user is not registered!");
            }
            else if(error == -6) {
                throw new InvalidPostsNumberException("Invalid announcements number to be read!");
            }
            else if(error == -10) {
                throw new TooMuchAnnouncementsException("The number of announcements you've asked for exceeds the number of announcements existing in such board");
            }
		}
	}
    
    public static void checkAskWts(Response response) throws UserNotRegisteredException {
        if(!response.getSuccess()){
            int error = response.getErrorCode();
            if(error == -1) {
                throw new UserNotRegisteredException("This user is not registered!");
            }
		}
	}

}
