package server;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.Serializable;

public class GeneralBoard implements Serializable {

    private JSONArray annoucements;

    protected GeneralBoard(JSONArray list) {
        this.annoucements = list;
    }

    protected GeneralBoard() {
        this.annoucements = new JSONArray();
    }

    public void addAnnouncement(JSONObject object) {
        this.annoucements.add(object);
    }

    public JSONObject getAnnouncements(int number){
        JSONArray annoucementsList = new JSONArray();

        if(number == 0){
            annoucementsList = getAnnoucements();
        }
        else{
            int i = 0;
            while (i < number){
                annoucementsList.add(annoucementsList.get(annoucementsList.size() - i));
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
        return annoucements;
    }
}
