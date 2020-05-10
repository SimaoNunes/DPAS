package server;

import library.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class GeneralBoard implements Serializable {

    private ArrayList<Pair<JSONObject, byte[]>> annoucements;

    protected GeneralBoard(ArrayList<Pair<JSONObject, byte[]>> list) {

        this.annoucements = list;
    }

    protected GeneralBoard() {

        this.annoucements = new ArrayList<>();
    }

    public void addAnnouncement(JSONObject object, byte[] signature) {

        this.annoucements.add(new Pair<>(object, signature));
    }

    public JSONObject getAnnouncements(int number){
        JSONArray annoucementsList = new JSONArray();

        if(number == 0){
            annoucementsList = getAnnoucements();
        }
        else{
            int i = 0;
            while (i < number){
                JSONObject object = new JSONObject();
                object.put("message", annoucements.get(annoucements.size() - 1 - i).getFirst());
                object.put("signature", annoucements.get(annoucements.size() - 1 - i).getSecond());
                annoucementsList.add(object);
                i++;
            }
        }
        JSONObject announcementsToSend =  new JSONObject();
        announcementsToSend.put("announcementList", annoucementsList);
        return announcementsToSend;
    }

    public void setAnnoucements(JSONArray annoucements) {
        this.annoucements = annoucements;
    }

    public JSONArray getAnnoucements() {
        JSONArray array = new JSONArray();
        for(int i = 0; i < annoucements.size(); i++){
            JSONObject object = new JSONObject();
            object.put("message", annoucements.get(i).getFirst());
            object.put("signature", annoucements.get(i).getSecond());
            array.add(object);
        }
        return array;
    }

    public int size(){
        return this.annoucements.size();
    }
}
