package Library;

import java.util.ArrayList;

public class Board {
    private ArrayList<Announcement> announcements;

    public Board() {
        this.announcements = new ArrayList<>();
    }

    public Board(ArrayList<Announcement> announcements){
        this.announcements = announcements;
    }

    public ArrayList<Announcement> getAnnouncements() {
        return announcements;
    }

    public void setAnnouncements(ArrayList<Announcement> announcements) {
        this.announcements = announcements;
    }

}
