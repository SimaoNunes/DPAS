package client;

import library.Envelope;
import library.Request;
import library.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.PublicKey;
import java.util.Arrays;
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
    private ConcurrentHashMap<PublicKey, byte[]> nonces;
    private ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Request>> answers = null;

    public Listener(ServerSocket ss, int nQuorum, String userName, PublicKey key){

        cryptoManager = new CryptoManager(userName);
        answers = new ConcurrentHashMap<>();
        nonces = new ConcurrentHashMap<>();
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
                            byte[] nonce = cryptoManager.generateClientNonce();
                            nonces.put(envelope.getRequest().getPublicKey(), nonce);
                            Response response         = new Response(true, nonce, clientKey);
                            Envelope responseEnvelope = new Envelope(response, cryptoManager.signResponse(response));
                            outputStream.writeObject(responseEnvelope);
            			}
            			break;
            		case "VALUE":
                        if(checkNonce(envelope.getRequest().getPublicKey(), envelope.getRequest().getClientNonce())) {
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

    private boolean checkNonce(PublicKey key, byte[] nonce) {
        if(nonces.containsKey(key) && Arrays.equals(nonces.get(key), nonce)) {
            nonces.remove(key);
            return true;
        }
        return false;
    }

}
