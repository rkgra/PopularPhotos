package com.instagramclient.sushmanayak.instagramclient.data;

import com.instagramclient.sushmanayak.instagramclient.data.Comment;
import com.instagramclient.sushmanayak.instagramclient.data.User;

import java.util.ArrayList;

/**
 * Created by SushmaNayak on 9/15/2015.
 */
public class InstagramPhoto {
    public InstagramPhoto() {
    }

    public User user;
    public String caption;
    public String mediaType;
    public String videoUrl;
    public String imageUrl;
    public String imageId;
    public String location;
    public String locationID;
    public int likesCount;
    public int commentsCount;
    public int timestamp;
    public String instagramLink;
    public ArrayList<Comment> comments;
}

