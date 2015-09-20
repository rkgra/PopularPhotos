package com.instagramclient.sushmanayak.instagramclient.data;

/**
 * Created by SushmaNayak on 9/16/2015.
 */
public class User {
    public String username;
    public String userProfileUrl;
    public String userId;

    public User(String id, String username, String userProfileUrl) {
        this.userId = id;
        this.userProfileUrl = userProfileUrl;
        this.username = username;
    }
}
