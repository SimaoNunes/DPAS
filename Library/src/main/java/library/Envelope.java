package library;

import java.io.Serializable;

public class Envelope implements Serializable {

    private Request request = null;
    private Response response = null;
    private byte[] hash = null;

    public Envelope(Request request, byte[] hash){
        this.request = request;
        this.hash = hash;
    }

    public Envelope(Request request){
        this.request = request;
    }

    public Envelope(Response response, byte[] hash){
        this.response = response;
        this.hash = hash;
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

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }
}
