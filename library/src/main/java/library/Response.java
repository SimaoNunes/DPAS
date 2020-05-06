package library;

import org.json.simple.JSONObject;

import java.io.Serializable;

public class Response implements Serializable {

    private boolean success;
    private JSONObject jsonObject;
    private byte[] nonce = null;

    private int errorCode;
    private int ts;
    private int rid;


    public Response(byte[] nonce) {
        this.nonce = nonce;
    }

    public Response(boolean success, byte[] nonce, int ts) {
        this.success = success;
        this.nonce = nonce;
        this.ts = ts;
    }

    public Response(boolean success, byte[] nonce) {
        this.success = success;
        this.nonce = nonce;
    }

    public Response(boolean success, int errorCode, byte[] nonce) {

        this.success = success;
        this.errorCode = errorCode;
        this.nonce = nonce;
    }

    public Response(boolean success, JSONObject object, byte[] nonce, int rid) {
        this.success = success;
        this.jsonObject = object;
        this.nonce = nonce;
        this.rid = rid;
    }
    
    

    public int getRid() {
        return rid;
    }

    public void setRid(int rid) {
        this.rid = rid;
    }

    public int getTs() {
        return ts;
    }

    public void setTs(int ts) {
        this.ts = ts;
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

    public byte[] getNonce() {
        return nonce;
    }

    public void setNonce(byte[] nonce) {
        this.nonce = nonce;
    }
}
