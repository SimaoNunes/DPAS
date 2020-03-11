package Library;

import java.util.Date;

public class Announcement {
    private char[] message;
    private Date date;
    private String title;
    private User user;

    public Announcement(char[] message, Date date, String title, User user) {
        this.message = message;
        this.date = date;
        this.title = title;
        this.user = user;
    }

    public char[] getMessage() {
        return message;
    }

    public void setMessage(char[] message) {
        this.message = message;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
