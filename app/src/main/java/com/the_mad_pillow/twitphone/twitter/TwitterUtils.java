package com.the_mad_pillow.twitphone.twitter;

import android.content.Context;
import android.content.SharedPreferences;

import com.the_mad_pillow.twitphone.BuildConfig;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

public class TwitterUtils {
    //SharedPreference用
    private static final String TOKEN = "token";
    private static final String TOKEN_SECRET = "token_secret";
    private static final String ACCESS_TOKEN = "twitter_access_token";

    public static Twitter getTwitterInstance(Context context) {
        String consumerKey = BuildConfig.TWITTER_API_KEY;
        String consumerSecret = BuildConfig.TWITTER_API_SECRET;

        TwitterFactory factory = new TwitterFactory();
        Twitter twitter = factory.getInstance();
        twitter.setOAuthConsumer(consumerKey, consumerSecret);

        if (hasAccessToken(context)) {
            twitter.setOAuthAccessToken(loadAccessToken(context));
        }

        return twitter;
    }

    /**
     * TOKENの格納
     *
     * @param context     MainActivity context
     * @param accessToken TwitterAPI AccessToken
     */
    public static void storeAccessToken(Context context, AccessToken accessToken) {
        SharedPreferences preferences = context.getSharedPreferences(ACCESS_TOKEN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(TOKEN, accessToken.getToken());
        editor.putString(TOKEN_SECRET, accessToken.getTokenSecret());

        editor.apply();
    }

    /**
     * PreferenceからTOKENの取得
     * 未保持の場合はreturn null
     *
     * @param context MainActivity context
     * @return AccessToken or null
     */
    private static AccessToken loadAccessToken(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(ACCESS_TOKEN, Context.MODE_PRIVATE);
        String token = preferences.getString(TOKEN, null);
        String tokenSecret = preferences.getString(TOKEN_SECRET, null);

        if (token != null && tokenSecret != null) {
            return new AccessToken(token, tokenSecret);
        } else {
            return null;
        }
    }

    public static boolean hasAccessToken(Context context) {
        return loadAccessToken(context) != null;
    }
}
