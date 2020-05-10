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
                throw new UnknownPublicKeyException(ExceptionsMessages.UNKNOWN_KEY);
            }
            else if(error == -2) {
                throw new AlreadyRegisteredException(ExceptionsMessages.ALREADY_REGISTERED);
            }
        }
    }

    public static void checkPost(Response response) throws UserNotRegisteredException,
            MessageTooBigException, InvalidAnnouncementException {
        if(!response.getSuccess()){
            int error = response.getErrorCode();
            if(error == -1) {
                throw new UserNotRegisteredException(ExceptionsMessages.USER_NOT_REGISTERED);
            }
            else if(error == -4) {
                throw new MessageTooBigException(ExceptionsMessages.MESSAGE_TOO_BIG);
            }
            else if(error == -5) {
                throw new InvalidAnnouncementException(ExceptionsMessages.INVALID_ANNOUNCEMENTS);
            }
        }
    }

    public static void checkRead(Response response) throws UserNotRegisteredException, InvalidPostsNumberException, TooMuchAnnouncementsException {
        if(!response.getSuccess()){
            int error = response.getErrorCode();
            if(error == -1) {
                throw new UserNotRegisteredException(ExceptionsMessages.USER_NOT_REGISTERED);
            }
            else if(error == -3) {
                throw new UserNotRegisteredException(ExceptionsMessages.USER_TO_READ_FROM_NOT_REGISTERED);
            }
            else if(error == -6) {
                throw new InvalidPostsNumberException(ExceptionsMessages.INVALID_READ_POST);
            }
            else if(error == -10) {
                throw new TooMuchAnnouncementsException(ExceptionsMessages.TOO_MUCH_ANNOUNCEMENTS);
            }
        }
    }
    
    public static void checkReadGeneral(Response response) throws InvalidPostsNumberException, TooMuchAnnouncementsException, UserNotRegisteredException {
        if(!response.getSuccess()){
            int error = response.getErrorCode();
            if(error == -1) {
                throw new UserNotRegisteredException(ExceptionsMessages.USER_NOT_REGISTERED);
            }
            else if(error == -6) {
                throw new InvalidPostsNumberException(ExceptionsMessages.INVALID_READ_POST);
            }
            else if(error == -10) {
                throw new TooMuchAnnouncementsException(ExceptionsMessages.TOO_MUCH_ANNOUNCEMENTS);
            }
		}
	}
    
    public static void checkAskWts(Response response) throws UserNotRegisteredException {
        if(!response.getSuccess()){
            int error = response.getErrorCode();
            if(error == -1) {
                throw new UserNotRegisteredException(ExceptionsMessages.USER_NOT_REGISTERED);
            }
		}
	}

}
