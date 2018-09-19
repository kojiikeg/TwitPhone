package com.the_mad_pillow.twitphone;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.the_mad_pillow.twitphone.twitter.MyTwitter;
import com.the_mad_pillow.twitphone.twitter.TwitterOAuthActivity;
import com.the_mad_pillow.twitphone.twitter.TwitterUtils;

import java.util.ArrayList;
import java.util.List;

import io.skyway.Peer.Browser.Navigator;
import io.skyway.Peer.OnCallback;
import io.skyway.Peer.Peer;
import io.skyway.Peer.PeerOption;

public class MainActivity extends AppCompatActivity {
    private static final int RECORD_AUDIO_REQUEST_ID = 1;
    private final String TAG = getClass().getSimpleName();

    private MyPeer peer;
    //debug peerID表示
    private String currentId;

    private SwipeRefreshLayout swipeRefreshLayout;
    private MyAdapter adapter;
    private List<String> idList = new ArrayList<>();

    private MyTwitter myTwitter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ActionBar設定
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        //TwitterAccessTokenCheck
        //TODO ボタンでのログインに変更する
        if (!TwitterUtils.hasAccessToken(this)) {
            Intent intent = new Intent(getApplication(), TwitterOAuthActivity.class);
            startActivity(intent);
            finish();
        }

        //非同期TaskのデータをUIThreadで処理するHandler
        @SuppressLint("HandlerLeak")
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0: //GetTwitterTask
                        showUI();
                        break;
                }
            }
        };
        myTwitter = new MyTwitter(this, handler);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (peer != null) {
            peer.getPeer().disconnect();
        }
    }

    /**
     * ActionBarのItemクリック時の処理
     *
     * @param item ActionBarのそれぞれのItem
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Log.d("debug", "" + item.getItemId());
        if (id == android.R.id.home) {
            ((DrawerLayout) findViewById(R.id.drawerLayout)).openDrawer(Gravity.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showUI() {
        if (peer == null) {
            createPeerId();
        } else {
            peer.getPeer().reconnect();
        }

        // アイコンを指定
        if (getSupportActionBar() != null) {
            RequestOptions requestOptions = new RequestOptions()
                    .override(getSupportActionBar().getHeight() * 3 / 4)
                    .circleCrop();
            Glide.with(this)
                    .load(myTwitter.getProfileImage400())
                    .apply(requestOptions)
                    .into(new SimpleTarget<Drawable>() {
                        @Override
                        public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                            getSupportActionBar().setHomeAsUpIndicator(resource);
                        }
                    });
        }

        //peerID List
        createSwipeRefreshLayout();

        //debug 自分のpeerID表示
        showCurrentPeerId();
    }

    public void createPeerId() {
        //PeerID取得
        PeerOption options = new PeerOption();
        options.key = BuildConfig.SKYWAY_API_KEY;
        options.domain = BuildConfig.SKYWAY_HOST;
        options.turn = true;
        peer = new MyPeer(this, myTwitter.getScreenName(), options);
        Navigator.initialize(peer.getPeer());
    }

    /**
     * debug
     */
    private void showCurrentPeerId() {
        peer.on(Peer.PeerEventEnum.OPEN, new OnCallback() {
            @Override
            public void onCallback(Object object) {
                Log.d(TAG, "[On/Open]");

                if (object instanceof String) {
                    Log.d(TAG, "ID:" + object);
                    currentId = (String) object;
                    ((TextView) findViewById(R.id.debugTextView)).setText(currentId);
                }
            }
        });
    }

    /**
     * ListViewの設定
     */
    public void createSwipeRefreshLayout() {
        //ListAdapter設定
        ListView listView = findViewById(R.id.listView);
        adapter = new MyAdapter(this, 0, idList);
        listView.setAdapter(adapter);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        // 色指定
        swipeRefreshLayout.setColorSchemeResources(R.color.blue, R.color.lightBlue);

        //スワイプ時の動作設定
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                peer.refreshPeerList();
            }
        });

        //Listクリック時の動作設定
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedPeerId = idList.get(i);
                if (selectedPeerId == null) {
                    Log.d(TAG, "Selected PeerId == null");
                    return;
                }
                Log.d(TAG, "SelectedPeerId: " + selectedPeerId);
                peer.call(selectedPeerId);
            }
        });
    }

    /**
     * API23以降必要 音声権限
     */
    private void checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Manifest.permission.RECORD_AUDIO is not GRANTED");
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                Log.d(TAG, "shouldShowRequestPermissionRationale = false");
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                Log.d(TAG, "request Permission RECORD_AUDIO");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        RECORD_AUDIO_REQUEST_ID);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    /**
     * API23以降は追加の権限要求が必要
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RECORD_AUDIO_REQUEST_ID: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "request Permission RECORD_AUDIO GRANTED!");
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    Log.d(TAG, "request Permission RECORD_AUDIO DENIED!");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                break;
            }
        }
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }

    public MyAdapter getAdapter() {
        return adapter;
    }

    public List<String> getIdList() {
        return idList;
    }

    public MyTwitter getMyTwitter() {
        return myTwitter;
    }
}
