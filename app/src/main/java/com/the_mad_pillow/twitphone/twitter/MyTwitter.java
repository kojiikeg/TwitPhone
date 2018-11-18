package com.the_mad_pillow.twitphone.twitter;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.the_mad_pillow.twitphone.R;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.User;

import java.util.ArrayList;
import java.util.List;

public class MyTwitter {
    private Context activity;
    private Handler handler;
    private MyTwitterApiClient myTwitterApiClient;
    private User user;
    private List<MyUser> favoriteList;
    private List<MyUser> onlineList;
    private List<MyUser> FFList;
    private List<User> friendList;
    private List<User> followerList;

    public MyTwitter(Context context, final Handler handler) {
        activity = context;
        this.handler = handler;
        myTwitterApiClient = new MyTwitterApiClient(
                TwitterCore.getInstance().getSessionManager().getActiveSession());

        friendList = new ArrayList<>();
        followerList = new ArrayList<>();

        initGetUser();
        initGetFriendList(-1);
        initGetFollowerList(-1);
    }

    private void initGetUser() {
        myTwitterApiClient.getAccountService().verifyCredentials(true, true, true).enqueue(new Callback<User>() {
            @Override
            public void success(Result<User> result) {
                user = result.data;
                handler.sendEmptyMessage(activity.getResources().getInteger(R.integer.getUserTask));
            }

            @Override
            public void failure(TwitterException exception) {
                finish();
            }
        });
    }

    private void initGetFriendList(long cursor) {
        myTwitterApiClient.getCustomTwitterService()
                .getFriendList(TwitterCore.getInstance().getSessionManager().getActiveSession().getUserId(), cursor, 200)
                .enqueue(new Callback<FriendsResponseModel>() {
                    @Override
                    public void success(Result<FriendsResponseModel> result) {
                        friendList.addAll(result.data.getResults());
                        if (result.data.getNextCursor() != 0) {
                            initGetFriendList(result.data.getNextCursor());
                        } else {
                            handler.sendEmptyMessage(activity.getResources().getInteger(R.integer.getListTask));
                        }
                    }

                    @Override
                    public void failure(TwitterException exception) {
                        finish();
                    }
                });
    }

    private void initGetFollowerList(long cursor) {
        myTwitterApiClient.getCustomTwitterService()
                .getFollowerList(TwitterCore.getInstance().getSessionManager().getActiveSession().getUserId(), cursor, 200)
                .enqueue(new Callback<FriendsResponseModel>() {
                    @Override
                    public void success(Result<FriendsResponseModel> result) {
                        followerList.addAll(result.data.getResults());
                        if (result.data.getNextCursor() != 0) {
                            initGetFollowerList(result.data.getNextCursor());
                        } else {
                            handler.sendEmptyMessage(activity.getResources().getInteger(R.integer.getListTask));
                        }
                    }

                    @Override
                    public void failure(TwitterException exception) {
                        finish();
                    }
                });
    }

    /**
     * API制限によるアプリの終了
     */
    private void finish() {
        ((Activity) activity).runOnUiThread(() -> {
            Toast.makeText(activity, "TwitterAPI制限によるエラー\n時間をあけてください", Toast.LENGTH_SHORT)
                    .show();

            ((Activity) activity).finish();
        });
    }

    public List<MyUser> getFFList() {
        if (FFList == null) {
            FFList = new ArrayList<>();

            for (Object user : overlapList(friendList, followerList)) {
                FFList.add(new MyUser((User) user));
            }
        }
        return FFList;
    }

    public List<MyUser> getFavoriteList(boolean reload) {
        if (favoriteList == null || reload) {
            favoriteList = new ArrayList<>();
            for (MyUser user : FFList) {
                if (user.isFavorite()) {
                    favoriteList.add(user);
                }
            }
        }

        return favoriteList;
    }

    public List<MyUser> getOnlineList(boolean reload) {
        if (onlineList == null || reload) {
            onlineList = new ArrayList<>();
            for (MyUser user : FFList) {
                if (user.isOnline()) {
                    onlineList.add(user);
                }
            }
        }

        return onlineList;
    }

    private List<User> overlapList(List<User> listA, List<User> listB) {
        List<User> list = new ArrayList<>();

        for (User userA : listA) {
            for (User userB : listB) {
                if (userA.id == userB.id) {
                    list.add(userA);
                }
            }
        }

        return list;
    }

    public User getUser() {
        return user;
    }
}