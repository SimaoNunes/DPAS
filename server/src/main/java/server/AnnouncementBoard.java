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

    public JSONArray getAnnouncements(int number){
        JSONArray announcementsList = new JSONArray();

        if(number == 0){
            announcementsList = getAnnouncements();
        }
        else{
            int i = 0;
            while (i < number){
                announcementsList.add(announcements.get(announcements.size() - 1 - i));
                i++;
            }
        }
        return announcementsList;
    }

    public void setAnnoucements(JSONArray announcements) {
        this.announcements = announcements;
    }

    public JSONArray getAnnouncements() {
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