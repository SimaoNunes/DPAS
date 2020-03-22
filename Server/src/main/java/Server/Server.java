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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.Base64;

public class Server implements Runnable{

    private ServerSocket server = null;

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

    public boolean checkKey(PublicKey publicKey){

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

    public void register(Request request, ObjectOutputStream outputStream){
        System.out.println("Registering user: " + request.getName());

        if(!checkKey(request.getPublicKey())){
            send(new Response(false, -7), outputStream);
        }

        String key = Base64.getEncoder().encodeToString(request.getPublicKey().getEncoded());

        String path = "./storage/AnnouncementBoards/" + key;
        File file = new File(path);
        // Check if user is already registered
        if(file.exists()){
            send(new Response(false, -2), outputStream);
        }
        else{
            file.mkdirs();
            send(new Response("User successfully registered!", true), outputStream);
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
        System.out.println("READ method");

        String key = Base64.getEncoder().encodeToString(request.getPublicKey().getEncoded());
        String path = "./storage/AnnouncementBoards/" + key + "/";
        File file = new File(path);

        // FALTA FAZER VERIFICACOES DE EXCEPTIONS.... 
        // TIPO QUANDO SE PEDEM MAIS ANNOUNCEMENTS DO QUE OS QUE EXISTEM E ASSIM

        if(file.exists()) {
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
        } else {
            send(new Response(false, -1), outStream);
        }
    }

    private void readGeneral(){

    }

    private void post(Request request, Boolean general, ObjectOutputStream outstream) throws IOException{
        System.out.println("POST method");
        System.out.println(request.getMessage().length());
        System.out.println(request.getPublicKey().getEncoded().length);
        // Check if message length exceeds 255 characters
        if(request.getMessage().length() > 255){
            send(new Response(false, -4), outstream);
        }
        // Checks if key has proper length
        else if(request.getPublicKey().getEncoded().length != 294){
            send(new Response(false, -3), outstream);
        }
        else{
            String key = Base64.getEncoder().encodeToString(request.getPublicKey().getEncoded());
            String path = "./storage/AnnouncementBoards/" + key;
            File file = new File(path);
            if(file.exists()){
                if(general){
                    path = "./storage/GeneralBoard";
                    file = new File(path);
                }
                int totalAnnouncements = file.list().length;

                JSONObject announcementObject =  new JSONObject();
                announcementObject.put("user", key);
                announcementObject.put("message", request.getMessage());

                saveFile(path + "/" + Integer.toString(totalAnnouncements), announcementObject.toJSONString());
                send(new Response(true), outstream);
            } else {
            	// This user is not registered
                send(new Response(false, -1), outstream);
            }
        }
    }

    public void deleteUsers() throws IOException {

        System.out.println("Delete operation");

        String path = "./storage/AnnouncementBoards";
        FileUtils.deleteDirectory(new File(path));
        File files = new File(path);
        files.mkdirs();
    }



    private void newListener() {
        (new Thread(this)).start();
    }


}
