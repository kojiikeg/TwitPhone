package com.the_mad_pillow.twitphone.activities;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;

import com.daasuu.ei.Ease;
import com.daasuu.ei.EasingInterpolator;
import com.the_mad_pillow.twitphone.R;
import com.the_mad_pillow.twitphone.others.FButton;

import de.hdodenhof.circleimageview.CircleImageView;

public class CallActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        //TODO start call
        //if can connect
        rotateImage();
        //TODO start time

        FButton finishButton = findViewById(R.id.finishButton);
        finishButton.setOnClickListener(view -> {
            //TODO finish call
            finish();
        });

        //TODO if can't connect
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
