package com.the_mad_pillow.twitphone.twitter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.the_mad_pillow.twitphone.R;

import java.util.ArrayList;
import java.util.List;

import twitter4j.PagableResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public class MyTwitter {
    //screenName, profileImage, follow, follower
    private static final int TASK_COUNT = 4;
    private Handler handler;
    private Context activity;
    private Twitter twitter;
    private String screenName;
    private String profileImageBigger;
    private String profileImage400;
    private List<User> followList;
    private List<User> followerList;

    @SuppressLint("HandlerLeak")
    public MyTwitter(Context context, final Handler mainHandler) {
        activity = context;
        handler = new Handler() {
            int count = 0;

            @Override
            public void handleMessage(Message msg) {
                if (++count == TASK_COUNT) {
                    mainHandler.sendEmptyMessage(activity.getResources().getInteger(R.integer.TwitterTask));
                }
            }
        };

        twitter = TwitterUtils.getTwitterInstance(context);

        //ScreenNameの取得
        //取得後フォロー・フォロワーの取得を開始する
        initGetScreenName();
        //ProfileImageの取得
        initGetProfileImage();
    }

    public String getScreenName() {
        return screenName;
    }

    public Twitter getTwitter() {
        return twitter;
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
                //フォローリストの取得
                initGetFollow();
                //フォロワーリストの取得
                initGetFollower();

                handler.sendEmptyMessage(0);
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private void initGetProfileImage() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    profileImageBigger = twitter.verifyCredentials().getBiggerProfileImageURLHttps();
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

    @SuppressLint("StaticFieldLeak")
    private void initGetFollow() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                followList = new ArrayList<>();
                try {
                    long cursor = -1;
                    int count = 0;
                    while (cursor != 0 && count < 15) {
                        PagableResponseList<User> pagableResponseList = twitter.getFriendsList(screenName, cursor, 200);
                        followList.addAll(pagableResponseList);

                        cursor = pagableResponseList.getNextCursor();
                        count++;
                    }
                } catch (TwitterException e) {
                    e.printStackTrace();
                    finish();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                handler.sendEmptyMessage(0);
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private void initGetFollower() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                followerList = new ArrayList<>();
                try {
                    long cursor = -1;
                    int count = 0;
                    while (cursor != 0 && count < 15) {
                        PagableResponseList<User> pagableResponseList = twitter.getFollowersList(screenName, cursor, 200);
                        followerList.addAll(pagableResponseList);

                        cursor = pagableResponseList.getNextCursor();
                        count++;
                    }
                } catch (TwitterException e) {
                    e.printStackTrace();
                    finish();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                handler.sendEmptyMessage(0);
            }
        }.execute();
    }

    /**
     * API制限によるアプリの終了
     */
    private void finish() {
        ((Activity) activity).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, "TwitterAPI制限によるエラー\n時間をあけてください", Toast.LENGTH_SHORT)
                        .show();

                ((Activity) activity).finish();
            }
        });
    }

    public String getProfileImageBigger() {
        return profileImageBigger;
    }

    public String getProfileImage400() {
        return profileImage400;
    }

    public List<User> getFollowList() {
        return followList;
    }

    public List<User> getFollowerList() {
        return followList;
    }
}
