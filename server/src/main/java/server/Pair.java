package server;

public class Pair {

    private int timestamp;
    private AnnouncementBoard announcementBoard;

    protected Pair(int timestamp, AnnouncementBoard announcementBoard){
        this.timestamp = timestamp;
        this.announcementBoard = announcementBoard;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setAnnouncementBoard(AnnouncementBoard announcementBoard) {
        this.announcementBoard = announcementBoard;
    }

    public AnnouncementBoard getAnnouncementBoard() {
        return announcementBoard;
    }
}