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
    private String profileImageBigger;
    private String profileImageSmaller;
    private String profileImage400;

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
    private void initGetProfileImage() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    profileImageBigger = twitter.verifyCredentials().getBiggerProfileImageURLHttps();
                    profileImageSmaller = twitter.verifyCredentials().getMiniProfileImageURLHttps();
                    profileImage400 = twitter.verifyCredentials().get400x400ProfileImageURLHttps();
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

    public String getProfileImageBigger() {
        return profileImageBigger;
    }

    public String getProfileImageSmaller() {
        return profileImageSmaller;
    }

    public String getProfileImage400() {
        return profileImage400;
    }
}
