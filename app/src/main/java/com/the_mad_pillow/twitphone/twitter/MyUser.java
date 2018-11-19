package com.the_mad_pillow.twitphone.twitter;


import com.twitter.sdk.android.core.models.User;

import lombok.Data;

@Data
public class MyUser {
    private User user;
    private boolean online = false;
    private boolean favorite;

    MyUser(User user) {
        this.user = user;
        initFavorite();
    }

    private void initFavorite(){
        //TODO
        //get Favorite Users & set
    }

    public MyUser setOnline(boolean isOnline) {
        online = isOnline;

        return this;
    }
}
