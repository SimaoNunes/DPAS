package server;

import java.io.Serializable;
import org.json.simple.JSONArray;

public class AnnouncementBoard implements Serializable{
    
    private String user;
    private JSONArray annoucements;

    protected AnnouncementBoard(String user, JSONArray annoucements){
        this.annoucements = annoucements;
        this.user = user;
    }

    public void setAnnoucements(JSONArray annoucements) {
        this.annoucements = annoucements;
    }

    public JSONArray getAnnoucements() {
        return annoucements;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUser() {
        return user;
    }
}