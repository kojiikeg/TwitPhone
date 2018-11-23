package com.the_mad_pillow.twitphone.activities;

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
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.daasuu.ei.Ease;
import com.daasuu.ei.EasingInterpolator;
import com.the_mad_pillow.twitphone.BuildConfig;
import com.the_mad_pillow.twitphone.R;
import com.the_mad_pillow.twitphone.adapters.ExpandableAdapter;
import com.the_mad_pillow.twitphone.others.FButton;
import com.the_mad_pillow.twitphone.others.MyPeer;
import com.the_mad_pillow.twitphone.twitter.MyTwitter;
import com.the_mad_pillow.twitphone.twitter.MyUser;
import com.the_mad_pillow.twitphone.views.CallDialog;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.models.User;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.skyway.Peer.Browser.Navigator;
import io.skyway.Peer.PeerOption;
import lombok.Getter;

public class MainActivity extends AppCompatActivity {
    private static final int RECORD_AUDIO_REQUEST_ID = 1;
    private final String TAG = getClass().getSimpleName();

    private MyPeer peer;

    @Getter
    private ExpandableAdapter adapter;
    @Getter
    private SwipeRefreshLayout swipeRefreshLayout;
    @Getter
    private MyTwitter myTwitter;

    //ListMenu開閉用のButtonの初期座標
    private float defaultMenuSwitchingButtonX;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig(BuildConfig.TWITTER_API_KEY, BuildConfig.TWITTER_API_SECRET))
                .debug(true)
                .build();
        Twitter.initialize(config);

        //Twitter認証チェック
        if (TwitterCore.getInstance().getSessionManager().getActiveSession() == null) {
            Intent intent = new Intent(getApplication(), HomeActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        //API23以降のAndroidに必要な権限認証
        checkAudioPermission();

        //ActionBar設定
        setSupportActionBar(findViewById(R.id.toolbar));

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

        createSwitchListMenu();
        initSortButton();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void createSwitchListMenu() {
        final CircleImageView switchListMenuButton = findViewById(R.id.switchListMenuButton);
        switchListMenuButton.setTag(getResources().getInteger(R.integer.CLOSE));
        switchListMenuButton.setOnTouchListener((view, motionEvent) -> {
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
        });
    }

    private void createLeftMenuUserProfile() {
        final RelativeLayout header = findViewById(R.id.leftDrawerHeader);
        final CircleImageView profileImage = findViewById(R.id.leftMenuImageView);
        final TextView profileName = findViewById(R.id.leftMenuName);
        final TextView profileID = findViewById(R.id.leftMenuID);

        Glide.with(this)
                .load(myTwitter.getUser().profileBannerUrl)
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                        header.setBackground(resource);
                    }
                });

        Glide.with(this)
                .load(myTwitter.getUser().profileImageUrlHttps.replace("_normal", ""))
                .apply(RequestOptions.circleCropTransform())
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                        profileImage.setImageDrawable(resource);
                    }
                });

        profileName.setText(myTwitter.getUser().name);
        profileID.setText(getString(R.string.screenName, myTwitter.getUser().screenName));
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

        if (!favoriteButton.isShown()) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> favoriteButton.setVisibility(View.VISIBLE), 100);
            new Handler(Looper.getMainLooper()).postDelayed(() -> FFButton.setVisibility(View.VISIBLE), 200);
            new Handler(Looper.getMainLooper()).postDelayed(() -> offlineButton.setVisibility(View.VISIBLE), 300);
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> switchingButton.setTag(-tempState), 1000);
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
        peer = new MyPeer(this, myTwitter.getUser().screenName, options);
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
                    .load(myTwitter.getUser().profileImageUrlHttps.replace("normal", "bigger"))
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

    public void createExpandableListView() {
        List<String> groups = new ArrayList<>();
        groups.add("お気に入り");
        groups.add("オンライン");
        groups.add("全て");
        SparseArray<List<MyUser>> children = new SparseArray<>();
        children.put(0, myTwitter.getFavoriteList(true));
        children.put(1, myTwitter.getOnlineList(true));
        children.put(2, myTwitter.getFFList());

        ExpandableListView expandableListView = findViewById(R.id.FFExpandableListView);
        adapter = new ExpandableAdapter(this, groups, children);
        expandableListView.setAdapter(adapter);
        expandableListView.expandGroup(0);
        expandableListView.expandGroup(1);
        expandableListView.expandGroup(2);

        //Listクリック時の動作設定
        expandableListView.setOnChildClickListener((ExpandableListView, view, groupPosition, childPosition, id) -> {
            showPopup(children.get(groupPosition).get(childPosition).getUser());
            return false;
        });
    }

    public void showPopup(User user) {
        CallDialog callDialog = new CallDialog(this, user);
        callDialog.show();
    }


    /**
     * ListViewの設定
     */
    @SuppressLint("ClickableViewAccessibility")
    public void createSwipeRefreshLayout() {
        createExpandableListView();

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        // 色指定
        swipeRefreshLayout.setColorSchemeResources(R.color.blue, R.color.lightBlue);

        //スワイプ時の動作設定
        swipeRefreshLayout.setOnRefreshListener(() -> peer.refreshPeerList());

        peer.refreshPeerList();
    }

    /**
     * Sortの設定を保存するFButtonのクリック処理の初期設定
     */
    private void initSortButton() {
        //FButtonの取得
        FButton favoriteBtn = findViewById(R.id.favoriteButton);
        FButton FFBtn = findViewById(R.id.FFButton);
        FButton offlineBtn = findViewById(R.id.offlineButton);

        //初期状態はtrue
        //TODO 内部データから読み込み
        favoriteBtn.setTag(true);
        FFBtn.setTag(true);
        offlineBtn.setTag(true);

        //ClickListenerの設定
        favoriteBtn.setOnClickListener(sortButtonClick);
        FFBtn.setOnClickListener(sortButtonClick);
        offlineBtn.setOnClickListener(sortButtonClick);
    }

    //sort用のFButtonのクリック時の処理
    View.OnClickListener sortButtonClick = view -> {
        view.setTag(!(boolean) view.getTag());

        //TODO
        //change color

    };

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
                // Show an explanation to the user *asynchronously* -- don't block
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
}
