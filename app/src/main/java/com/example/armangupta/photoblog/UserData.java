package com.example.armangupta.photoblog;

import java.sql.Time;
import java.util.Date;

/**
 * Created by Arman Gupta on 12-06-2018.
 */

public class UserData {
    String userId;
    String imagethumb;
    String description;
    String imageUrl;
    Date timestamp;



    public UserData(String uid, String imagethumb, String description, String imageFirebaseUrl, Date Timestamp) {
        userId = uid;
        this.imagethumb = imagethumb;
        this.description = description;
        imageUrl = imageFirebaseUrl;
        this.timestamp = Timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
       this.userId = userId;
    }

    public String getImagethumb() {
        return imagethumb;
    }

    public void setImagethumb(String imagethumb) {
        this.imagethumb = imagethumb;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageFirebaseUrl) {
        imageUrl = imageFirebaseUrl;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date date) {
        this.timestamp = date;
    }

}
