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
    private Context activity;
    private Twitter twitter;
    private String screenName;

    public MyTwitter(Context context, Handler handler) {
        activity = context;
        this.handler = handler;
        twitter = TwitterUtils.getTwitterInstance(context);
        initGetScreenName();
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
                Message msg = Message.obtain();
                msg.what = activity.getResources().getInteger(R.integer.getScreenNameTask);
                handler.sendMessage(msg);
            }
        }.execute();

    }

    public String getScreenName() {
        return screenName;
    }

    public Twitter getTwitter() {
        return twitter;
    }
}
