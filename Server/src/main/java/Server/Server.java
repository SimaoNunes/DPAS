package Server;

import Exceptions.MessageTooBigException;
import Library.Request;
import Library.Response;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
                }
                socket.close();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send(Response response, ObjectOutputStream outputStream){
        try {
            outputStream.writeObject(response);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private int checkDirectory(String path){
        int totalAnnouncements = 0;
        File files = new File(path);

        if (!files.exists()) {
            files.mkdirs();
            System.out.println("Directories created!");
        } else {
            totalAnnouncements = files.list().length;
        }

        System.out.println("Total announcements " + Integer.toString(totalAnnouncements));
        return totalAnnouncements;
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
        if(request.getMessage().length() > 255){
            send(new Response(false, -4), outstream);
        }
        else if(request.getPublicKey().getEncoded().length != 256){
            send(new Response(false, -3), outstream);
        }
        else{
            send(new Response(true), outstream);

        }

        String path;
        if(general){
            path = "./storage/general/";
        } else {
            path = "./storage/username/";
            // change this username ^ later *****
        }

        int totalAnnouncements = checkDirectory(path);
        saveFile(path + Integer.toString(totalAnnouncements), request.getMessage());
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

    private void quitUser() {
        System.out.println("User disconnected.");
    }



    private void newListener() {
        (new Thread(this)).start();
    }


}
