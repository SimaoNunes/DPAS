package client;

import library.Envelope;
import library.Response;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Listener implements Runnable{

    private ServerSocket endpoint;
    private int nQuorum;
    private Response result = null;
    private Thread listenerThread;
    private ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Response>> answers = null;

    public Listener(ServerSocket ss, int nQuorum){

        answers = new ConcurrentHashMap<>();
        endpoint = ss;
        this.nQuorum = nQuorum;
        newListener();

    }

    public Response getResult() {
        return result;
    }

    public void setResult(Response result) {
        this.result = result;
    }

    public void run(){

        Socket socket = null;
        ObjectInputStream inStream;

        try{
            socket = endpoint.accept();
        } catch (IOException e) {
            e.printStackTrace();
        }

        newListener();

        try{
            inStream = new ObjectInputStream(socket.getInputStream());
            System.out.println("JUST RECEIVED A NEW MESSAGE");
            Envelope envelope = (Envelope) inStream.readObject();
            result = checkAnswer(envelope);
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

    private Response checkAnswer(Envelope envelope){
        Response response = envelope.getResponse();
        //FALTA CHECKAR INTEGRITY E FRESHNESS
        //FALTA CHECKAR SE SAO EXCEPTIONS

        if(answers.containsKey(response.getTs())){
            answers.get(response.getTs()).put(response.getPort(), response);
            if(answers.get(response.getTs()).size() > nQuorum){

                Response result = checkQuorum(answers.get(response.getTs()).values());
                if(result != null){
                    listenerThread.interrupt();
                    return result;
                }

            }
        }

        else{
            answers.put(response.getTs(), new ConcurrentHashMap<>());
            answers.get(response.getTs()).put(response.getPort(), response);

        }
        return null;

    }

    private Response checkQuorum(Collection<Response> line){
        HashMap<String, Integer> counter = new HashMap<>();

        for(Response entry: line){
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
