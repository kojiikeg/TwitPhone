package com.the_mad_pillow.twitphone.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.the_mad_pillow.twitphone.R;
import com.the_mad_pillow.twitphone.others.FButton;
import com.the_mad_pillow.twitphone.others.MyPeer;

import de.hdodenhof.circleimageview.CircleImageView;
import io.skyway.Peer.Browser.MediaStream;


public class AnswerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer);

        Log.d("debug", "testtesttest");

        CircleImageView imageView = findViewById(R.id.image);
        Glide.with(this)
                .load(getIntent().getStringExtra("peerImageUrl").replace("_normal", ""))
                .apply(RequestOptions.overrideOf(240, 240))
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                        imageView.setImageDrawable(resource);
                    }
                });
        FButton acceptButton = findViewById(R.id.accept);
        FButton refuseButton = findViewById(R.id.refuse);

        acceptButton.setOnClickListener(view -> {
            MediaStream stream = MyPeer.getMyPeer().getMediaStream();
            MyPeer.getMyPeer().getConnection().answer(stream);
            MyPeer.getMyPeer().setConnectionCallback(MyPeer.getMyPeer().getConnection());

            Intent intent = new Intent(getApplication(), CallActivity.class);
            intent.putExtra("peerImageUrl", getIntent().getStringExtra("peerImageUrl"));
            startActivity(intent);

            finish();
        });

        refuseButton.setOnClickListener(view -> {
            MyPeer.getMyPeer().getConnection().close();
            finish();
        });
    }
}
