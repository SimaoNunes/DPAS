package Server;

import Library.Request;
import Library.Response;
import org.apache.commons.io.FileUtils;

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
                        read(outStream);
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
        if(file.exists()){ //already registered
            send(new Response(false, -2), outputStream);
        }
        else{
            System.out.println("tenho de entrar aqui");
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
                int totalAnnouncements = file.list().length;
                System.out.println("Total announcements " + Integer.toString(totalAnnouncements));
                if(general){
                    path = "./storage/GeneralBoard/";
                }
                saveFile(path + Integer.toString(totalAnnouncements), request.getMessage());
                send(new Response(true), outstream);
            } else {
            	// This user is not registered
                send(new Response(false, -1), outstream);
            }
        }
    }


    private void read(ObjectOutputStream outStream) {
        System.out.println("SEND method");

        Path fileLocation = Paths.get("./storage");
        if(!Files.exists(fileLocation)){
            System.out.println("Maninho essa merda nao existe");
        } else{
            try {
                byte[] data = Files.readAllBytes(fileLocation);
                outStream.write(data);
                outStream.flush();
            } catch (Exception e) {
                e.printStackTrace();
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
