package com.the_mad_pillow.twitphone.twitter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.the_mad_pillow.twitphone.R;

import twitter4j.Twitter;
import twitter4j.TwitterException;

public class MyTwitter {
    private Handler handler;
    private Handler MainHandler;
    private Context activity;
    private Twitter twitter;
    private String screenName;
    private String profileImage;

    @SuppressLint("HandlerLeak")
    public MyTwitter(Context context, final Handler mainHandler) {
        activity = context;
        MainHandler = mainHandler;
        handler = new Handler() {
            int count = 0;

            @Override
            public void handleMessage(Message msg) {
                if (++count == 2) {
                    mainHandler.sendEmptyMessage(activity.getResources().getInteger(R.integer.TwitterTask));
                }
            }
        };

        twitter = TwitterUtils.getTwitterInstance(context);
        initGetScreenName();
        initGetProfileImage();
    }

    @SuppressLint("StaticFieldLeak")
    private void initGetScreenName() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    screenName = twitter.getScreenName();
                    return null;
                } catch (TwitterException e) {
                    e.printStackTrace();
                    throw new RuntimeException("ScreenNameの取得に失敗");
                }
            }

            @Override
            protected void onPostExecute(Void result) {
                handler.sendEmptyMessage(0);
            }
        }.execute();
    }

    public String getScreenName() {
        return screenName;
    }

    public Twitter getTwitter() {
        return twitter;
    }

    @SuppressLint("StaticFieldLeak")
    public void initGetProfileImage() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    profileImage = twitter.verifyCredentials().getBiggerProfileImageURL();
                    return null;
                } catch (TwitterException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Profile画像取得に失敗");
                }
            }

            @Override
            protected void onPostExecute(Void result) {
                handler.sendEmptyMessage(0);
            }
        }.execute();
    }

    public String getProfileImage() {
        return profileImage;
    }
}
