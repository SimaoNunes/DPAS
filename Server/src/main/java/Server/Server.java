package Server;

import Library.Request;
import Library.Response;

import org.apache.commons.io.FileUtils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64;

public class Server implements Runnable{

    private ServerSocket server = null;
    private Map<String, Integer> userIdMap = new HashMap<String, Integer>();

    protected Server(ServerSocket ss){

        server = ss;
        newListener();
    }

    public void run(){

        Socket socket = null;
        ObjectOutputStream outStream;
        ObjectInputStream inStream;

        try{
            socket = server.accept();

        } catch (IOException e) {
            e.printStackTrace();
        }

        newListener();

        try {
            inStream = new ObjectInputStream(socket.getInputStream());
            outStream = new ObjectOutputStream(socket.getOutputStream());
            try {
                System.out.println("User connected.");

                //aqui decifra-se

                Request request = (Request) inStream.readObject();
                switch(request.getOperation()) {
                    case "REGISTER":
                        register(request, outStream);
                        break;
                    case "POST":
                        post(request, false, outStream);
                        break;
                    case "POSTGENERAL":
                        post(request, true, outStream);
                        break;
                    case "READ":
                        read(request, outStream);
                        break;
                    case "READGENERAL":
                        readGeneral();
                        break;
                    case "DELETEALL":
                        deleteUsers();
                        break;
                }
                socket.close();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int checkPostsNumber(PublicKey key)

    public boolean checkKey(PublicKey publicKey){ //checks if a key exists in the server keystore

        char[] passphrase = "changeit".toCharArray();
        KeyStore ks = null;
        try {
            ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream("Keystores/keystore"), passphrase);

            Enumeration aliases = ks.aliases();

            for (; aliases.hasMoreElements(); ) {

                String alias = (String) aliases.nextElement();


                if (ks.isKeyEntry(alias)) {
                    PublicKey key = ks.getCertificate(alias).getPublicKey();
                    if (key.equals(publicKey)) {
                        return true;
                    }

                }

            }
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void register(Request request, ObjectOutputStream outStream){
        System.out.println("REGISTER Method. Registering user: " + request.getName());
        // Check if key is "trustful" (aka is in Server's keystore)
        if(!checkKey(request.getPublicKey())) {
            send(new Response(false, -7), outStream);
            return;
        }
        // Get key from request to open respective file
        String key = Base64.getEncoder().encodeToString(request.getPublicKey().getEncoded());
        // Check if user is already registered
        synchronized (userIdMap){
            if(userIdMap.containsKey(key)) {
                send(new Response(false, -2), outStream);
            }
            // Register new user
            else {
                int userId = userIdMap.size();
                String path = "./storage/AnnouncementBoards/" + userId;
                File file = new File(path);
                file.mkdirs();
                userIdMap.put(key, userId);
                System.out.println("User " + request.getName() + " successfully registered!");
                send(new Response("User successfully registered!", true), outStream);
            }

        }
    }

    public void send(Response response, ObjectOutputStream outputStream){
        try {
            outputStream.writeObject(response);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void saveFile(String completePath, String announcement) throws IOException {
        byte[] bytesToStore = announcement.getBytes();
        try{
            File file = new File(completePath);
            FileOutputStream fos = new FileOutputStream(file);

            fos.write(bytesToStore);
            fos.close();

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    
    private void read(Request request, ObjectOutputStream outStream) {
        if(request.getPublicKey().getEncoded().length != 294){
            send(new Response(false, -3), outStream);  //InvalidPublicKey
        }
        else if(request.getNumber() < 0){
            send(new Response(false, -11), outStream);  //InvalidAnnouncementsNumber
        }
        else if(request.getNumber() > )



    	// FALTA FAZER VERIFICACOES DE EXCEPTIONS.... 
        // TIPO QUANDO SE PEDEM MAIS ANNOUNCEMENTS DO QUE OS QUE EXISTEM E ASSIM
        System.out.println("READ method");
        // Get key from request to open respective file
        String key = Base64.getEncoder().encodeToString(request.getPublicKey().getEncoded());
        // Check if user is registered
        if(userIdMap.containsKey(key)) {
        	int userId = userIdMap.get(key);
        	String path = "./storage/AnnouncementBoards/" + userId + "/";
            File file = new File(path);
            int n_announcements = file.list().length;
            JSONParser parser = new JSONParser();
            try{
                JSONArray annoucementsList = new JSONArray();
                JSONObject announcement;
                for (int i=0; i<request.getNumber(); i++) {
                    announcement = (JSONObject) parser.parse(new FileReader(path + Integer.toString(n_announcements)));
                    n_announcements--;
                    annoucementsList.add(announcement);
                }
                JSONObject announcementsToSend =  new JSONObject();
                announcementsToSend.put("announcementList", annoucementsList);
                // AQUI FALTA ENVIAR O OBJETO announcementsToSend NA RESPOSTA
                send(new Response(true), outStream);
            } catch(Exception e){
                e.printStackTrace();
                send(new Response(false, -8), outStream);
            }
        }
        else {
            send(new Response(false, -1), outStream);
        }
    }

    private void readGeneral(){

    }

    private void post(Request request, Boolean general, ObjectOutputStream outStream) throws IOException{
        System.out.println("POST method");
        // Check if message length exceeds 255 characters
        if(request.getMessage().length() > 255){
            send(new Response(false, -4), outStream);  //MessageTooBigException
        }
        // Checks if key has proper length
        else if(request.getPublicKey().getEncoded().length != 294){
            send(new Response(false, -3), outStream);  //InvalidPublicKey
        }
        else{
        	// Get key from request to open respective file
            String key = Base64.getEncoder().encodeToString(request.getPublicKey().getEncoded());
            // Check if user is registered
            if(userIdMap.containsKey(key)) {
            	String path;
            	File file;
            	// Check wether post is general or not, to get the proper file location
                if(general){
                    path = "./storage/GeneralBoard";
                    file = new File(path);
                }
                else {
                    int userId = userIdMap.get(key);
                    path = "./storage/AnnouncementBoards/" + userId;
                    file = new File(path);
                }
                // Write to file
                int totalAnnouncements = file.list().length;
                JSONObject announcementObject =  new JSONObject();
                announcementObject.put("user", key);
                announcementObject.put("message", request.getMessage());
                saveFile(path + "/" + Integer.toString(totalAnnouncements), announcementObject.toJSONString());
                send(new Response(true), outStream);
            } else {
            	// This user is not registered
                send(new Response(false, -1), outStream);
            }
        }
    }

    public void deleteUsers() throws IOException {

        System.out.println("Delete operation");

        userIdMap.clear();
        String path = "./storage/AnnouncementBoards";
        FileUtils.deleteDirectory(new File(path));
        File files = new File(path);
        files.mkdirs();
        path = "./storage/GeneralBoard";
        FileUtils.deleteDirectory(new File(path));
        files = new File(path);
        files.mkdirs();
    }



    private void newListener() {
        (new Thread(this)).start();
    }


}
