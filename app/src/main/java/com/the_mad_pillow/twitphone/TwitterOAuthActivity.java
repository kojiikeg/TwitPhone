package com.the_mad_pillow.twitphone;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class TwitterOAuthActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();
    private Twitter twitter;
    private RequestToken requestToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        twitter = TwitterUtils.getTwitterInstance(this);

        startAuthorize();
    }

    /**
     * 非同期でOAuth認証を行う
     */
    @SuppressLint("StaticFieldLeak")
    private void startAuthorize() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    requestToken = twitter.getOAuthRequestToken(getString(R.string.twitter_callback_url));
                    return requestToken.getAuthorizationURL();
                } catch (TwitterException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(String url) {
                //接続失敗
                if (url == null) {
                    Log.e(TAG, "Can't Connect TwitterOAuthorize");
                    return;
                }

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        }.execute();
    }

    /**
     * CallBackされた時に自動で呼び出される
     *
     * @param intent CallBack時のこのIntent URLデータが格納される
     */
    @SuppressLint("StaticFieldLeak")
    @Override
    public void onNewIntent(Intent intent) {
        if (intent == null
                || intent.getData() == null
                || !intent.getData().toString().startsWith(getString(R.string.twitter_callback_url))) {
            return;
        }

        //Intentに格納されているOauth認証結果
        String verifier = intent.getData().getQueryParameter("oauth_verifier");

        //非同期によるAccessTokenの取得
        new AsyncTask<String, Void, AccessToken>() {
            @Override
            protected AccessToken doInBackground(String... param) {
                try {
                    return twitter.getOAuthAccessToken(requestToken, param[0]);
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(AccessToken accessToken) {
                if (accessToken != null) {
                    showToast("認証しました");
                    successOAuth(accessToken);
                    exitIntent();
                } else {
                    showToast("認証に失敗しました");
                }
            }
        }.execute(verifier);
    }

    /**
     * PreferenceへAccessTokenを格納
     *
     * @param accessToken Twitter AccessToken
     */
    private void successOAuth(AccessToken accessToken) {
        TwitterUtils.storeAccessToken(this, accessToken);
    }

    private void exitIntent() {
        //MainActivityへ遷移
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        //このActivityを終了
        finish();
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
