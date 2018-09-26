package com.the_mad_pillow.twitphone.twitter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.Toast;

import com.the_mad_pillow.twitphone.R;

import java.util.ArrayList;
import java.util.List;

import twitter4j.PagableResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public class MyTwitter {
    private Context activity;
    private Handler handler;
    private Twitter twitter;
    private User user;
    private List<MyUser> FFList;
    private List<User> followList;
    private List<User> followerList;

    @SuppressLint("HandlerLeak")
    public MyTwitter(Context context, final Handler handler) {
        activity = context;
        this.handler = handler;
        twitter = TwitterUtils.getTwitterInstance(context);

        //Userの取得
        initGetUser();
    }

    @SuppressLint("StaticFieldLeak")
    private void initGetUser() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    user = twitter.verifyCredentials();
                    return null;
                } catch (TwitterException e) {
                    throw new RuntimeException("Userの取得に失敗");
                }
            }

            @Override
            protected void onPostExecute(Void result) {
                handler.sendEmptyMessage(activity.getResources().getInteger(R.integer.getUserTask));

                initGetFollow();
                initGetFollower();
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
                        PagableResponseList<User> pagableResponseList = twitter.getFriendsList(user.getScreenName(), cursor, 200);
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
                handler.sendEmptyMessage(activity.getResources().getInteger(R.integer.getListTask));
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
                        PagableResponseList<User> pagableResponseList = twitter.getFollowersList(user.getScreenName(), cursor, 200);
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
                handler.sendEmptyMessage(activity.getResources().getInteger(R.integer.getListTask));
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

    public List<User> getFollowList() {
        return followList;
    }

    public List<User> getFollowerList() {
        return followList;
    }

    public List<MyUser> getFFList() {
        if (FFList == null) {
            FFList = new ArrayList<>();

            for (Object user : overlapList(followList, followerList)) {
                FFList.add(new MyUser((User) user));
            }
        }

        return FFList;
    }

    private List<?> overlapList(List<?> listA, List<?> listB) {
        List<Object> list = new ArrayList<>();

        if (listA.size() < listB.size()) {
            for (Object obj : listA) {
                if (listB.contains(obj)) {
                    list.add(obj);
                }
            }
        } else {
            for (Object obj : listB) {
                if (listA.contains(obj)) {
                    list.add(obj);
                }
            }
        }

        return list;
    }

    public User getUser() {
        return user;
    }
}