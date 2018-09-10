package com.the_mad_pillow.twitphone;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import io.skyway.Peer.Browser.Navigator;
import io.skyway.Peer.MediaConnection;
import io.skyway.Peer.OnCallback;
import io.skyway.Peer.Peer;
import io.skyway.Peer.PeerError;
import io.skyway.Peer.PeerOption;

public class MainActivity extends AppCompatActivity {

    private Peer peer;

    private String TAG = getClass().getSimpleName();
    private String currentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PeerOption options = new PeerOption();
        options.key = BuildConfig.SKYWAY_API_KEY;
        options.domain = BuildConfig.SKYWAY_HOST;
        options.turn = true;
        peer = new Peer(this, options);
        Navigator.initialize(peer);

        showCurrentPeerId();
    }


    private void showCurrentPeerId() {
        peer.on(Peer.PeerEventEnum.OPEN, new OnCallback()
        {
            @Override
            public void onCallback(Object object)
            {
                Log.d(TAG, "[On/Open]");

                if (object instanceof String)
                {
                    Log.d(TAG, "ID:" + object);
                    currentId = (String)object;
                    ((TextView)findViewById(R.id.debugTextView)).setText(currentId);
                }
            }
        });

        // !!!: Event/Call
        peer.on(Peer.PeerEventEnum.CALL, new OnCallback()
        {
            @Override
            public void onCallback(Object object)
            {
                Log.d(TAG, "[On/Call]");
                if (!(object instanceof MediaConnection))
                {
                    return;
                }
            }
        });

        // !!!: Event/Close
        peer.on(Peer.PeerEventEnum.CLOSE, new OnCallback()
        {
            @Override
            public void onCallback(Object object)
            {
                Log.d(TAG, "[On/Close]");
            }
        });

        // !!!: Event/Disconnected
        peer.on(Peer.PeerEventEnum.DISCONNECTED, new OnCallback()
        {
            @Override
            public void onCallback(Object object)
            {
                Log.d(TAG, "[On/Disconnected]");
            }
        });

        // !!!: Event/Error
        peer.on(Peer.PeerEventEnum.ERROR, new OnCallback()
        {
            @Override
            public void onCallback(Object object)
            {
                PeerError error = (PeerError) object;

                Log.d(TAG, "[On/Error]" + error);
                Log.d(TAG, "[On/ErrorType]" + error.type);
                Log.d(TAG, "[On/ErrorMessage]" + error.message);

                String strMessage = "" + error;
                String strLabel = getString(android.R.string.ok);
            }
        });

    }
}
