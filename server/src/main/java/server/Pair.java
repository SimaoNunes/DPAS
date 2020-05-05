package server;

import java.util.concurrent.atomic.AtomicInteger;

public class Pair {

    private AtomicInteger timestamp;
    private AnnouncementBoard announcementBoard;

    protected Pair(int timestamp, AnnouncementBoard announcementBoard){
        this.timestamp = new AtomicInteger(timestamp);
        this.announcementBoard = announcementBoard;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = new AtomicInteger(timestamp);
    }

    public AtomicInteger getTimestamp() {
        return timestamp;
    }

    public void setAnnouncementBoard(AnnouncementBoard announcementBoard) {
        this.announcementBoard = announcementBoard;
    }

    public AnnouncementBoard getAnnouncementBoard() {
        return announcementBoard;
    }
}