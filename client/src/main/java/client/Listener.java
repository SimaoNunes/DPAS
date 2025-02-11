package client;

import library.Envelope;
import library.Pair;
import library.Request;
import library.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.PublicKey;
import java.util.*;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class Listener implements Runnable{

    private ServerSocket endpoint;
    private int nQuorum;
    private CryptoManager cryptoManager = null;
    private Thread listenerThread;
    private PublicKey clientKey;
    private Map<PublicKey, Integer> serversPorts = null;
    
    private Envelope result = null;    
    private Envelope resultGeneral = null;
    private ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Envelope>> answers = null;
    private List<Pair<Integer, Envelope>> answerGeneral;
    private Map<PublicKey , Pair<Integer, Envelope>> answerGeneralExceptions;

    public Listener(ServerSocket ss, int nQuorum, String userName, PublicKey key, Map<PublicKey, Integer> serversPortsFromEndpoint) {

        cryptoManager = new CryptoManager(userName);
        answers = new ConcurrentHashMap<>();
        answerGeneralExceptions = new HashMap<>();
        answerGeneral = Collections.synchronizedList(new ArrayList<Pair<Integer, Envelope>>());
        answerGeneralExceptions = new HashMap<PublicKey, Pair<Integer, Envelope>>();
        endpoint = ss;
        this.nQuorum = nQuorum;
        this.clientKey = key;
        newListener();
        serversPorts = serversPortsFromEndpoint;
    }

    public Envelope getResultGeneral() {
        return resultGeneral;
    }

    public void setResultGeneral(Envelope resultGeneral) {
        this.resultGeneral = resultGeneral;
    }

    public Envelope getResult() {
        return result;
    }

    public void setResult(Envelope result) {
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
                            cryptoManager.generateRandomNonce(envelope.getRequest().getPublicKey());
                            Response response         = new Response(true, cryptoManager.getNonce(envelope.getRequest().getPublicKey()), clientKey);
                            Envelope responseEnvelope = new Envelope(response, cryptoManager.signResponse(response));
                            outputStream.writeObject(responseEnvelope);
            			}
            			break;
            		case "VALUE":
            			if(cryptoManager.verifyRequest(envelope.getRequest(), envelope.getSignature(), envelope.getRequest().getPublicKey())) {
	                        if(cryptoManager.checkNonce(envelope.getRequest().getPublicKey(), envelope.getRequest().getClientNonce())) {
	                            result = checkAnswer(envelope);
	                        }
	                        else{
	                            //old message or attacker
	                        }
            			}
            			break;
                    case "VALUEGENERAL":
                        if(cryptoManager.verifyRequest(envelope.getRequest(), envelope.getSignature(), envelope.getRequest().getPublicKey())) {
                            if (cryptoManager.checkNonce(envelope.getRequest().getPublicKey(), envelope.getRequest().getClientNonce())) {
                                resultGeneral = checkGeneralAnswer(envelope);
                            }
                        }
                        break;

            		default:
            			break;
                }
            } else {
                switch(envelope.getResponse().getOperationType()) {
                    case "READ":
                        result = checkAnswer(envelope);
                        break;
                    case "READGENERAL":
                        resultGeneral = checkGeneralAnswer(envelope);
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

    private Envelope checkAnswer(Envelope envelope) {
        //FALTA CHECKAR INTEGRITY
        //FALTA CHECKAR SE SAO EXCEPTIONS

        int timestamp;
        PublicKey serverPublicKey;

        if(envelope.getResponse() != null){
            Response obj    = envelope.getResponse();
            timestamp       = obj.getTs();
            serverPublicKey = obj.getPublicKey();
        } else {
            Request obj     = envelope.getRequest();
            timestamp       = obj.getTs();
            serverPublicKey = obj.getPublicKey();
        }

        if(answers.containsKey(timestamp)) {
            answers.get(timestamp).put(serversPorts.get(serverPublicKey), envelope);
            if(answers.get(timestamp).size() > nQuorum) {
                Envelope result = checkQuorum(answers.get(timestamp).values());
                if(result != null) {
                    listenerThread.interrupt();
                    return result;
                }
            }
        }
        else {
            answers.put(timestamp, new ConcurrentHashMap<>());
            answers.get(timestamp).put(serversPorts.get(serverPublicKey), envelope);
        }
        
        return null;
    }

    private Envelope checkGeneralAnswer(Envelope envelope){
        if(envelope.getRequest() != null){
            Request request = envelope.getRequest();
            JSONArray array = (JSONArray) request.getJsonObject().get("announcementList");
            for(Object o : array){
                JSONObject json = (JSONObject) o;
                if(!cryptoManager.verifyMessage((JSONObject) json.get("message"), (byte[]) json.get("signature"))){
                    return null; // there is a message with the wrong signature, maybe send integrity exception?
                }
            }
            answerGeneral.add(new Pair<>(request.getTs(), envelope));
            if(answerGeneral.size() > nQuorum){
                int max = 0;
                int index = 0;
                for(int i = 0; i < answerGeneral.size(); i++){
                    if(answerGeneral.get(i).getFirst() > max){
                        max = answerGeneral.get(i).getFirst();
                        index = i;
                    }
                }
                return answerGeneral.get(index).getSecond();
            } 
        } else {
            Response response = envelope.getResponse();
            answerGeneralExceptions.put(response.getPublicKey(), new Pair<>(response.getTs(), envelope));
            if(answerGeneralExceptions.size() > nQuorum) {
                Collection<Envelope> exceptionsCollection = new ArrayList<>();
                for(Map.Entry<PublicKey, Pair<Integer, Envelope>> entry : answerGeneralExceptions.entrySet()){
                    exceptionsCollection.add(entry.getValue().getSecond());
                }
                Envelope result = checkQuorum(exceptionsCollection);
                if(result != null){
                    return result;
                }
            } 
        }
        return null;
    }

    private Envelope checkQuorum(Collection<Envelope> line) {
        HashMap<String, Integer> counter = new HashMap<>();

        for(Envelope entry: line){
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

}