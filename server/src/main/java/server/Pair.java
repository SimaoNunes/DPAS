package server;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public class Pair implements Serializable {

    private AtomicInteger timestamp;
    private AnnouncementBoard announcementBoard;

    protected Pair(int timestamp, AnnouncementBoard announcementBoard){
        this.timestamp = new AtomicInteger(timestamp);
        this.announcementBoard = announcementBoard;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp.set(timestamp);
    }

    public int getTimestamp() {
        return timestamp.get();
    }


    public void setAnnouncementBoard(AnnouncementBoard announcementBoard) {
        this.announcementBoard = announcementBoard;
    }

    public AnnouncementBoard getAnnouncementBoard() {
        return announcementBoard;
    }
}