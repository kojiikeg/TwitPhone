package com.the_mad_pillow.twitphone;

import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class ImageViewHighLighter implements View.OnTouchListener {
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        v.performClick();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                ((ImageView) v).setColorFilter(Color.argb(100, 255, 255, 255));
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                ((ImageView) v).setColorFilter(null);
                break;
        }
        return false;
    }
}