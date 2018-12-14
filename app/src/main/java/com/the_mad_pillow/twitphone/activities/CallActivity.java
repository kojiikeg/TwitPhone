package com.the_mad_pillow.twitphone.activities;

import android.animation.ObjectAnimator;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.daasuu.ei.Ease;
import com.daasuu.ei.EasingInterpolator;
import com.the_mad_pillow.twitphone.R;
import com.the_mad_pillow.twitphone.others.FButton;
import com.the_mad_pillow.twitphone.others.MyPeer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class CallActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.JAPAN);
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
        rotateImage();

        FButton finishButton = findViewById(R.id.finishButton);
        finishButton.setOnClickListener(view -> {
            if (MyPeer.getMyPeer().getConnection() != null
                    && MyPeer.getMyPeer().getConnection().isOpen()) {
                MyPeer.getMyPeer().getConnection().close();
            }
            finish();
        });

        TextView timer = findViewById(R.id.timer);
        new Thread(() -> {
            while (!this.isDestroyed()) {
                if (MyPeer.getMyPeer().getConnection().isOpen()) {
                    runOnUiThread(() -> timer.setText("00:00:00"));
                    new Thread(() -> {
                        while (true) {
                            try {
                                TimeUnit.SECONDS.sleep(1);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                break;
                            }

                            if (MyPeer.getMyPeer().getConnection() == null) {
                                finish();
                                break;
                            } else if (!MyPeer.getMyPeer().getConnection().isOpen()) {
                                MyPeer.getMyPeer().getConnection().close();
                                finish();
                                break;
                            }
                            //start timer

                            try {
                                Date date = simpleDateFormat.parse(timer.getText().toString());
                                date.setSeconds(date.getSeconds() + 1);
                                runOnUiThread(() -> timer.setText(simpleDateFormat.format(date)));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    break;
                }
            }
        }).start();
    }

    private void rotateImage() {
        CircleImageView image = findViewById(R.id.image);
        ObjectAnimator animator = ObjectAnimator.ofFloat(image, "rotation", 0, 360);
        animator.setInterpolator(new EasingInterpolator(Ease.LINEAR));
        animator.setDuration(30000);
        animator.setRepeatCount(Animation.INFINITE);
        animator.start();
    }
}
