package Library;

import java.util.ArrayList;

public class AnnouncementBoard extends Board {
    private User user;

    public AnnouncementBoard(User user) {
        super();
        this.user = user;
    }

    public AnnouncementBoard(ArrayList<Announcement> announcements, User user){
        super(announcements);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
