package com.the_mad_pillow.twitphone.twitter;

import android.content.Context;

import twitter4j.Twitter;
import twitter4j.TwitterException;

public class MyTwitter {
    private Twitter twitter;
    private String screenName;

    public MyTwitter(Context context) {
        twitter = TwitterUtils.getTwitterInstance(context);
        screenName = initGetScreenName();
    }

    private String initGetScreenName() {
        try {
            return twitter.getScreenName();
        } catch (TwitterException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("ScreenNameの取得に失敗");
    }

    public String getScreenName() {
        return screenName;
    }

    public Twitter getTwitter() {
        return twitter;
    }
}
