package library;

import java.io.Serializable;

public class Envelope implements Serializable {

    private Request request = null;
    private Response response = null;
    private byte[] signature = null;

    public Envelope(Request request, byte[] signature){
        this.request = request;
        this.signature = signature;
    }

    public Envelope(Request request){
        this.request = request;
    }

    public Envelope(Response response, byte[] signature){
        this.response = response;
        this.signature = signature;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }
    
}
