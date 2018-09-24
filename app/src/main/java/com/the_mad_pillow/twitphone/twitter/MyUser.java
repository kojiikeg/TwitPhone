package com.the_mad_pillow.twitphone.twitter;

import twitter4j.User;

public class MyUser {
    private User user;
    private boolean online = false;

    private MyUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public MyUser setOnline(boolean isOnline) {
        online = isOnline;

        return this;
    }

    public boolean isOnline() {
        return online;
    }
}
