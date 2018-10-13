package com.the_mad_pillow.twitphone;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.daasuu.ei.Ease;
import com.daasuu.ei.EasingInterpolator;
import com.the_mad_pillow.twitphone.adapters.UserListAdapter;
import com.the_mad_pillow.twitphone.twitter.MyTwitter;
import com.the_mad_pillow.twitphone.twitter.TwitterUtils;

import de.hdodenhof.circleimageview.CircleImageView;
import io.skyway.Peer.Browser.Navigator;
import io.skyway.Peer.PeerOption;

public class MainActivity extends AppCompatActivity {
    private static final int RECORD_AUDIO_REQUEST_ID = 1;
    private final String TAG = getClass().getSimpleName();

    private MyPeer peer;

    private UserListAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    private MyTwitter myTwitter;

    //ListMenu開閉用のButtonの初期座標
    private float defaultMenuSwitchingButtonX;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ActionBar設定
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        //TwitterAccessTokenCheck
        if (!TwitterUtils.hasAccessToken(this)) {
            Intent intent = new Intent(getApplication(), HomeActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        //非同期TaskのデータをUIThreadで処理するHandler
        @SuppressLint("HandlerLeak")
        Handler handler = new Handler() {
            int getListCount = 0;

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0: //getUserTask
                        showUI();
                        createLeftMenuUserProfile();
                        break;
                    case 1: //getListTask
                        if (++getListCount == 2) {//follow & follower Task
                            createSwipeRefreshLayout();
                        }
                        break;
                }
            }
        };
        myTwitter = new MyTwitter(this, handler);

        final CircleImageView switchListMenuButton = findViewById(R.id.switchListMenuButton);
        switchListMenuButton.setTag(getResources().getInteger(R.integer.CLOSE));
        switchListMenuButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.performClick();
                switchingListMenu();

                //HighLight
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        ((ImageView) view).setColorFilter(Color.argb(100, 255, 255, 255));
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        ((ImageView) view).setColorFilter(null);
                        break;
                }
                return true;
            }
        });
    }

    private void createLeftMenuUserProfile() {
        //TODO set Profile's Background Image
        final CircleImageView profileImage = findViewById(R.id.leftMenuImageView);
        final TextView profileName = findViewById(R.id.leftMenuName);
        final TextView profileID = findViewById(R.id.leftMenuID);

        if (getSupportActionBar() != null) {
            RequestOptions requestOptions = new RequestOptions()
                    .override(getSupportActionBar().getHeight() * 3 / 4)
                    .circleCrop();
            Glide.with(this)
                    .load(myTwitter.getUser().get400x400ProfileImageURLHttps())
                    .apply(requestOptions)
                    .into(new SimpleTarget<Drawable>() {
                        @Override
                        public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                            profileImage.setImageDrawable(resource);
                        }
                    });
        }
        profileName.setText(myTwitter.getUser().getName());
        profileID.setText(getString(R.string.screenName, myTwitter.getUser().getScreenName()));
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (defaultMenuSwitchingButtonX == 0) {
            defaultMenuSwitchingButtonX = findViewById(R.id.switchListMenuButton).getX();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (peer != null) {
            peer.getPeer().disconnect();
        }
    }

    /**
     * List並び替えMenuを開閉するAnimation
     */
    public void switchingListMenu() {
        final View switchingButton = findViewById(R.id.switchListMenuButton);
        if ((int) switchingButton.getTag() == getResources().getInteger(R.integer.MOVING)) {
            return;
        }

        final FButton favoriteButton = findViewById(R.id.favoriteButton);
        final FButton FFButton = findViewById(R.id.FFButton);
        final FButton offlineButton = findViewById(R.id.offlineButton);

        final int tempState = (int) switchingButton.getTag();
        switchingButton.setTag(getResources().getInteger(R.integer.MOVING));

        //移動設定
        float SwitchButtonFromX = 0f;
        float SwitchButtonToX = 0f;
        float ButtonsFromX = 0f;
        float ButtonsToX = 0f;
        if (tempState == getResources().getInteger(R.integer.CLOSE)) {
            SwitchButtonToX = -defaultMenuSwitchingButtonX + 30;
            ButtonsFromX = findViewById(R.id.listMenuButtonLayout).getWidth();
        } else {
            SwitchButtonFromX = -defaultMenuSwitchingButtonX + 30;
            ButtonsToX = findViewById(R.id.listMenuButtonLayout).getWidth();
        }

        //横移動
        PropertyValuesHolder translationX = PropertyValuesHolder.ofFloat("translationX", SwitchButtonFromX, SwitchButtonToX);
        PropertyValuesHolder ButtonsTranslationX = PropertyValuesHolder.ofFloat("translationX", ButtonsFromX, ButtonsToX);
        // 回転
        PropertyValuesHolder rotation = PropertyValuesHolder.ofFloat("rotation", 0f, 360f * tempState);

        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(
                switchingButton, translationX, rotation);
        ObjectAnimator objectAnimatorFavorite = ObjectAnimator.ofPropertyValuesHolder(
                favoriteButton, ButtonsTranslationX);
        ObjectAnimator objectAnimatorFF = ObjectAnimator.ofPropertyValuesHolder(
                FFButton, ButtonsTranslationX);
        objectAnimatorFF.setStartDelay(100);
        ObjectAnimator objectAnimatorOffline = ObjectAnimator.ofPropertyValuesHolder(
                offlineButton, ButtonsTranslationX);
        objectAnimatorOffline.setStartDelay(200);

        favoriteButton.setVisibility(View.VISIBLE);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                FFButton.setVisibility(View.VISIBLE);
            }
        }, 100);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                offlineButton.setVisibility(View.VISIBLE);
            }
        }, 200);

        //アニメーション速度操作
        objectAnimator.setInterpolator(new EasingInterpolator(Ease.QUAD_IN_OUT));
        objectAnimator.setDuration(1000);
        objectAnimatorFavorite.setInterpolator(new EasingInterpolator(Ease.QUAD_IN_OUT));
        objectAnimatorFavorite.setDuration(1000);
        objectAnimatorFF.setInterpolator(new EasingInterpolator(Ease.QUAD_IN_OUT));
        objectAnimatorFF.setDuration(1000);
        objectAnimatorOffline.setInterpolator(new EasingInterpolator(Ease.QUAD_IN_OUT));
        objectAnimatorOffline.setDuration(1000);

        // アニメーション実行
        objectAnimator.start();
        objectAnimatorFavorite.start();
        objectAnimatorFF.start();
        objectAnimatorOffline.start();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                switchingButton.setTag(-tempState);
            }
        }, 1000);
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

        // MainToolbar
        createMainToolbar();
    }

    public void createPeerId() {
        //PeerID取得
        PeerOption options = new PeerOption();
        options.key = BuildConfig.SKYWAY_API_KEY;
        options.domain = BuildConfig.SKYWAY_HOST;
        options.turn = true;
        peer = new MyPeer(this, myTwitter.getUser().getScreenName(), options);
        Navigator.initialize(peer.getPeer());
    }

    /**
     * MainToolbarの設定
     */
    public void createMainToolbar() {
        if (getSupportActionBar() != null) {
            RequestOptions requestOptions = new RequestOptions()
                    .override(getSupportActionBar().getHeight() * 3 / 4)
                    .circleCrop();
            Glide.with(this)
                    .load(myTwitter.getUser().get400x400ProfileImageURLHttps())
                    .apply(requestOptions)
                    .into(new SimpleTarget<Drawable>() {
                        @Override
                        public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                            getSupportActionBar().setHomeAsUpIndicator(resource);
                        }
                    });
        }
    }

    /**
     * ListViewの設定
     */
    @SuppressLint("ClickableViewAccessibility")
    public void createSwipeRefreshLayout() {
        //ListAdapter設定
        final ListView listView = findViewById(R.id.listView);
        adapter = new UserListAdapter(this, R.layout.user_list_item, myTwitter.getFFList());
        listView.setAdapter(adapter);
        //listViewのTagをGestureListenerのスワイプされたかどうかのFlagとして利用する

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
                if ((int) findViewById(R.id.switchListMenuButton).getTag() == getResources().getInteger(R.integer.CLOSE)) {
                    String selectedPeerId = myTwitter.getFFList().get(i).getUser().getScreenName();
                    if (selectedPeerId == null) {
                        Log.d(TAG, "Selected PeerId == null");
                        return;
                    }
                    Log.d(TAG, "SelectedPeerId: " + selectedPeerId);
                    peer.call(selectedPeerId);
                }
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

    public MyTwitter getMyTwitter() {
        return myTwitter;
    }

    public UserListAdapter getAdapter() {
        return adapter;
    }
}
