package Library;

import org.json.simple.JSONObject;

import java.io.Serializable;

public class Response implements Serializable {

    private boolean success;
    private JSONObject jsonObject;

    private int errorCode;

    public Response(boolean success, int errorCode){

        this.success = success;
        this.errorCode = errorCode;
    }

    public Response(boolean success, JSONObject object){
        this.success = success;
        this.jsonObject = object;
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }

    public void setJsonObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public Response(boolean success){
        this.success = success;
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
