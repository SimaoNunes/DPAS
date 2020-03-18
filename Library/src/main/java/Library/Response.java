package Library;

import java.io.Serializable;

public class Response implements Serializable {

    private String message = null;
    private boolean success;

    private int errorCode;

    
    public Response(String message, Boolean success){ 
        this.message = message;
        this.success = success;
    }

    public Response(boolean success, int errorCode){

        this.success = success;
        this.errorCode = errorCode;
    }

    public Response(boolean success){
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}
