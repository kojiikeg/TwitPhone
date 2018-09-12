package com.the_mad_pillow.twitphone;

import android.content.Context;
import android.content.SharedPreferences;

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
     * @param context MainActivity context
     * @param token   TwitterAPI AccessToken
     */
    public static void storeAccessToken(Context context, AccessToken token) {
        SharedPreferences preferences = context.getSharedPreferences(ACCESS_TOKEN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(TOKEN, token.getToken());
        editor.putString(TOKEN_SECRET, token.getTokenSecret());

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

    private static boolean hasAccessToken(Context context) {
        return loadAccessToken(context) != null;
    }
}
