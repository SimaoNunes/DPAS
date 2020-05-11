package server;

import java.io.Serializable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class AnnouncementBoard implements Serializable{
    
    private String user;
    private JSONArray announcements;

    protected AnnouncementBoard(String user, JSONArray list) {
        this.announcements = list;
        this.user = user;
    }

    protected AnnouncementBoard(String user) {
        this.user = user;
        this.announcements = new JSONArray();
    }

    public void addAnnouncement(JSONObject object) {
        this.announcements.add(object);
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
        this.announcements = annoucements;
    }

    public JSONArray getAnnoucements() {
        return announcements;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUser() {
        return user;
    }
    
    public int size() {
    	return this.announcements.size();
    }
}