package com.the_mad_pillow.twitphone.others;

import android.content.Intent;
import android.util.Log;

import com.the_mad_pillow.twitphone.BuildConfig;
import com.the_mad_pillow.twitphone.activities.AnswerActivity;
import com.the_mad_pillow.twitphone.activities.MainActivity;
import com.the_mad_pillow.twitphone.twitter.MyUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import io.skyway.Peer.Browser.MediaConstraints;
import io.skyway.Peer.Browser.MediaStream;
import io.skyway.Peer.Browser.Navigator;
import io.skyway.Peer.CallOption;
import io.skyway.Peer.MediaConnection;
import io.skyway.Peer.Peer;
import io.skyway.Peer.PeerOption;
import lombok.Getter;
import lombok.Setter;

public class MyPeer {
    //debug
    private final String TAG = getClass().getSimpleName();

    private MainActivity activity;
    @Getter
    private static MyPeer myPeer;
    @Getter
    private Peer peer;
    @Getter
    @Setter
    private MediaConnection connection;

    public MyPeer(MainActivity activity, String peerId, PeerOption options) {
        peer = new Peer(activity, peerId, options);
        this.activity = activity;

        init();
        myPeer = this;
    }

    /**
     * 着信時の処理設定
     */
    private void init() {
        peer.on(Peer.PeerEventEnum.CALL, object -> {
            Log.d("debug", "testtest");
            if (object instanceof MediaConnection) {
                MediaConnection connection = (MediaConnection) object;
                if (this.connection != null) {
                    connection.close();
                    return;
                }

                this.connection = connection;
                // 着信画面へ遷移
                Intent intent = new Intent(activity.getApplication(), AnswerActivity.class);
                intent.putExtra("peerImageUrl", activity.getMyTwitter().getMyUser(connection.peer()).getUser().profileImageUrlHttps);
                activity.startActivity(intent);
            }
        });
    }

    /**
     * Listのオンライン状態のリロード
     */
    public void refreshPeerList() {
        peer.listAllPeers(object -> {
            List<String> peerList = new ArrayList<>();
            for (int i = 0; i < ((JSONArray) object).length(); i++) {
                try {
                    peerList.add(((JSONArray) object).getString(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            for (MyUser user : activity.getMyTwitter().getFFList()) {
                user.setOnline(peerList.contains(user.getUser().screenName));
            }
            //Listの更新
            activity.getMyTwitter().getOnlineList(true);
            activity.getMyTwitter().getOtherList(true);
            activity.runOnUiThread(() -> {
                activity.getAdapter().notifyDataSetChanged();
                activity.getSwipeRefreshLayout().setRefreshing(false);
            });
        });
    }

    /**
     * 通話処理
     *
     * @param peerId 通話相手のPeerID
     */
    public void call(String peerId) {
        if (peer == null) {
            return;
        }

        if (peer.isDestroyed() || peer.isDisconnected()) {
            return;
        }

        if (connection != null) {
            return;
        }

        MediaStream stream = getMediaStream();

        if (stream == null) {
            return;
        }
        CallOption option = new CallOption();
        option.metadata = BuildConfig.SKYWAY_HOST;
        MediaConnection mediaConnection = peer.call(peerId, stream, option);

        if (mediaConnection == null) {
            return;
        }

        setConnectionCallback(mediaConnection);
        connection = mediaConnection;
    }

    public MediaStream getMediaStream() {
        MediaConstraints constraints = new MediaConstraints();
        constraints.videoFlag = false;
        constraints.audioFlag = true;
        return Navigator.getUserMedia(constraints);
    }

    public void closeConnection() {
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }

    /**
     * 通話が切れた場合の処理
     *
     * @param connection 現在のConnection
     */
    public void setConnectionCallback(MediaConnection connection) {
        connection.on(MediaConnection.MediaEventEnum.CLOSE, o -> closeConnection());
    }
}
