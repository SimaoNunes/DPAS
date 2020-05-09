package client;

import library.Envelope;
import library.Request;
import library.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PublicKey;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class Listener implements Runnable{

    private ServerSocket endpoint;
    private int nQuorum;
    private Request result = null;
    private CryptoManager cryptoManager = null;
    private Thread listenerThread;
    private PublicKey clientKey;
    private ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Request>> answers = null;

    public Listener(ServerSocket ss, int nQuorum, String userName, PublicKey key){

        cryptoManager = new CryptoManager(userName);
        answers = new ConcurrentHashMap<>();
        endpoint = ss;
        this.nQuorum = nQuorum;
        this.clientKey = key;
        newListener();

    }

    public Request getResult() {
        return result;
    }

    public void setResult(Request result) {
        this.result = result;
    }

    public void run() {

        Socket socket = null;
        ObjectInputStream inStream;
        ObjectOutputStream outputStream;

        try {
            socket = endpoint.accept();
        } catch(IOException e){
            return;
        }

        newListener();

        try {
            // inStream receives objects
            inStream = new ObjectInputStream(socket.getInputStream());
            // outStream sends objects
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            // Receive an envelope
            Envelope envelope = (Envelope) inStream.readObject();
            if(envelope.getRequest() != null) {
            	// Switch between two possible operations
                switch(envelope.getRequest().getOperation()) {
            		case "NONCE":
            			if(cryptoManager.verifyRequest(envelope.getRequest(), envelope.getSignature(), envelope.getRequest().getPublicKey())) {
                            byte[] clientNonce = cryptoManager.generateRandomNonce(envelope.getRequest().getPublicKey());
                            Response response         = new Response(true, clientNonce, clientKey);
                            Envelope responseEnvelope = new Envelope(response, cryptoManager.signResponse(response));
                            outputStream.writeObject(responseEnvelope);
            			}
            			break;
            		case "VALUE":
                        if(cryptoManager.checkNonce(envelope.getRequest().getPublicKey(), envelope.getRequest().getClientNonce())) {
                            result = checkAnswer(envelope);
                        }
                        else{
                            //old message or attacker
                        }
            			break;
            		default:
            			break;
                }
                
            }
            outputStream.close();
            inStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void newListener() {
        listenerThread = new Thread(this);
        listenerThread.start();
    }

    private Request checkAnswer(Envelope envelope) { //do not forget that VALUE message is a request
        Request request = envelope.getRequest();
        //FALTA CHECKAR INTEGRITY
        //FALTA CHECKAR SE SAO EXCEPTIONS
        if(answers.containsKey(request.getTs())) {
            answers.get(request.getTs()).put(request.getPort(), request);
            if(answers.get(request.getTs()).size() > nQuorum){
                Request result = checkQuorum(answers.get(request.getTs()).values());
                if(result != null){
                    listenerThread.interrupt();
                    return request;
                }
            }
        }
        else {
            answers.put(request.getTs(), new ConcurrentHashMap<>());
            answers.get(request.getTs()).put(request.getPort(), request);
        }
        return null;

    }

    private Request checkQuorum(Collection<Request> line) {
        HashMap<String, Integer> counter = new HashMap<>();

        for(Request entry: line){
            if(counter.containsKey(entry.toString())){
                counter.put(entry.toString(), counter.get(entry.toString()) + 1);
                if(counter.get(entry.toString()) > nQuorum){
                    return entry;
                }
            }
            else{
                counter.put(entry.toString(), 1);
            }

        }
        return null;
    }

    /*private boolean containsResponse(Set<Response> responses, Response response){
        for(Response r : responses){
            if(equalsResponses(r, response)){
                return true;
            }
        }
        return false;
    }

    private boolean equalsResponses(Response response1, Response response2){
        if(response1.getSuccess() && response2.getSuccess()){
            if(response1.getJsonObject().toJSONString().equals(response2.getJsonObject().toJSONString())){
                System.out.println("equal responses true");
                return true;
            }
        }
        else{
            if(!response1.getSuccess() && !response2.getSuccess()){
                if(response1.getErrorCode() == response2.getErrorCode()){
                    return true;
                }
            }
        }
        return false;
    }*/

}
