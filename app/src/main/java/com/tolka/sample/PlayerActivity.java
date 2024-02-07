package com.tolka.sample;

import android.os.Bundle;
import android.util.Log;

import com.tolka.tvanywhere.PlayerStateChangeListener;
import com.tolka.tvanywhere.TvAnyWherePlayer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class PlayerActivity extends AppCompatActivity
{
    public static final String TAG = "anywhere:Player";
    public static final String PARAM_PLAY_URL = "PARAM_PLAY_URL";

    private TvAnyWherePlayer mPlayer;
//    private String testUrl = "http://192.168.16.209:3000/api/atsc1/v1/service/2";
    //    private String url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";

    @Override
    protected void onCreate( @Nullable Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_player );

        String url = getIntent().getStringExtra( PARAM_PLAY_URL );

        mPlayer = findViewById( R.id.player_tv_any_where );
        mPlayer.setPlayStateChangedListener( new PlayerStateChangeListener()
        {
            @Override
            public void onStateChanged( int playbackState, String name )
            {
                Log.d( TAG, "onStateChanged: "+name );
            }

            @Override
            public void onError( String msg )
            {
                Log.d( TAG, "onError: "+msg );
            }
        } );
        mPlayer.play( url );
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mPlayer.release();
    }

}
