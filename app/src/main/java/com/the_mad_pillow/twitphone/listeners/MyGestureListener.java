package com.the_mad_pillow.twitphone.listeners;

import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

import com.the_mad_pillow.twitphone.MainActivity;
import com.the_mad_pillow.twitphone.R;

import static android.support.constraint.Constraints.TAG;

public class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
    private final int GESTURE_THRESHOLD = 70;
    private final int GESTURE_VELOCITY_THRESHOLD = 70;

    private MainActivity activity;
    private View view;

    public MyGestureListener(MainActivity activity, View view) {
        this.activity = activity;
        this.view = view;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        view.setTag(false);
        return true;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
        try {
            float diffX = event2.getX() - event1.getX();
            float diffY = event2.getY() - event1.getY();

            if (Math.abs(diffX) > Math.abs(diffY)
                    && (Math.abs(diffX) > GESTURE_THRESHOLD && Math.abs(velocityX) > GESTURE_VELOCITY_THRESHOLD)) {
                view.setTag(true);
                if (diffX > 0) {
                    onSwipeRight();
                } else {
                    onSwipeLeft();
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "" + e.getMessage());
        }
        return false;
    }

    private void onSwipeRight() {
        if ((int) activity.findViewById(R.id.switchListMenuButton).getTag()
                != activity.getResources().getInteger(R.integer.CLOSE)) {
            activity.switchingListMenu();
        } else {
            ((DrawerLayout) activity.findViewById(R.id.drawerLayout)).openDrawer(Gravity.START);
        }
    }

    private void onSwipeLeft() {
        activity.switchingListMenu();
    }

    private void onSwipeTop() {
        Log.i(TAG, "Top");
        //Toast.makeText(MyActivity.this, "swipe top", Toast.LENGTH_SHORT).show();
    }

    private void onSwipeBottom() {
        Log.i(TAG, "Bottom");
        //Toast.makeText(MyActivity.this, "swipe bottom", Toast.LENGTH_SHORT).show();
    }

}