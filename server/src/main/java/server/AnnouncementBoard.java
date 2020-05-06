package server;

import java.io.Serializable;
import org.json.simple.JSONArray;

public class AnnouncementBoard implements Serializable{
    
    private String user;
    private JSONArray annoucements;

    protected AnnouncementBoard(String user, JSONArray list){
        this.annoucements = list;
        this.user = user;
    }

    public void setAnnouncementBoard(JSONArray list) {
        this.annoucements = list;
    }

    public JSONArray getAnnouncementBoard() {
        return annoucements;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUser() {
        return user;
    }
}