package com.tolka.sample;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.tolka.sample.model.ChKey;
import com.tolka.sample.model.EpgEventModel;
import com.tolka.sample.model.TvAnyWhereChannel;
import com.tolka.tvanywhere.HardwareInspector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "anywhere:Main";
    private static final SimpleDateFormat SDF_YMD = new SimpleDateFormat("yyyyMMdd", Locale.getDefault() );
    private static final SimpleDateFormat SDF_HM = new SimpleDateFormat("HHmm", Locale.getDefault() );
    private static final SimpleDateFormat SDF_ZONE = new SimpleDateFormat("Z", Locale.getDefault() );

    private EditText mEditIp;
    private Button mBtnQR;
    private Button mBtnGetChannel;
    private Button mBtnEpgList;
    private Button mBtnEpgNow;
    private ProgressBar mPbLoading;
    private RecyclerView mRvChannel;
    private ChannelAdapter mAdapter;

    private List<TvAnyWhereChannel> mChannelList = new ArrayList<>();
    private Map<ChKey, List<EpgEventModel>> mEpgMap = new HashMap<>();
    private Map<ChKey, EpgEventModel> mEpgNow = new HashMap<>();

    private String mIp;
    boolean mIsCapableOfAc4 = false;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        mEditIp = findViewById( R.id.ed_ip );
        mBtnQR = findViewById( R.id.btn_qr );
        mBtnGetChannel = findViewById( R.id.btn_get_channel );
        mBtnEpgList = findViewById( R.id.btn_get_epg_list );
        mBtnEpgNow = findViewById( R.id.btn_get_epg_now );
        mPbLoading = findViewById( R.id.pb_loading );
        mRvChannel = findViewById( R.id.rv_channel );

        mAdapter = new ChannelAdapter();
        mRvChannel.setLayoutManager( new LinearLayoutManager( getApplicationContext(), LinearLayoutManager.VERTICAL, false ) );
        mRvChannel.setAdapter( mAdapter );

        mIsCapableOfAc4 = HardwareInspector.hasAc4HwCodecOrCapabilities( MainActivity.this );
        if ( !mIsCapableOfAc4 )
        {
            Log.e( TAG, "GetChannelListTask: device is not capable of decode AC4, will disable playing ATSC3 channels" );
        }

        mEditIp.addTextChangedListener( new TextWatcher()
        {
            @Override
            public void beforeTextChanged( CharSequence s, int start, int count, int after )
            {

            }

            @Override
            public void onTextChanged( CharSequence s, int start, int before, int count )
            {

            }

            @Override
            public void afterTextChanged( Editable s )
            {
                boolean isValid = isValidIp( s.toString() );
                mBtnGetChannel.setEnabled( isValid );
            }
        } );

        mBtnQR.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                if ( !CommonUtil.hasCameraPermission(MainActivity.this) )
                {
                    return;
                }
                new IntentIntegrator(MainActivity.this).initiateScan();
            }
        } );
        mBtnGetChannel.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                String ip = mEditIp.getText().toString();
                if ( isValidIp( ip ) )
                {
                    mIp = ip;
                    new GetChannelListTask().execute( );
                }
            }
        } );

        mBtnEpgList.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                new GetEpgListTask().execute(  );
            }
        } );

        mBtnEpgNow.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                new GetEpgNowTask().execute(  );
            }
        } );
    }

    private boolean isValidIp( String ip )
    {
        if ( ip == null )
        {
            return false;
        }

        String[] split = ip.split( "\\." );
        return split.length == 4;
    }


    class GetChannelListTask extends AsyncTask<String,Integer,Boolean>
    {
        @Override
        protected void onPreExecute()
        {
            toggleLoading( true );
            mChannelList.clear();
        }

        @Override
        protected Boolean doInBackground( String... params)
        {
            String strUrl = ApiUtil.buildGetChannelApiV2( mIp );

            URL url = null;
            HttpURLConnection connection = null;
            try
            {
                url = new URL(strUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                InputStream in = new BufferedInputStream(connection.getInputStream());
                String apiResult = CommonUtil.getStringFromInputStream( in );
                in.close();
                Log.d( TAG, "GetChannelListTask: apiResult="+apiResult );

                return parseResultToChannelList( apiResult );
            }
            catch ( IOException e )
            {
                Log.d( TAG, "GetChannelListTask: error:"+e.getMessage() );
                e.printStackTrace();
                return false;
            }
            finally
            {
                if ( connection != null )
                {
                    connection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute( Boolean bSuccess )
        {
            toggleLoading( false );
            if ( bSuccess )
            {
                mAdapter.notifyDataSetChanged();
                mBtnEpgList.setEnabled( true );
                mBtnEpgNow.setEnabled( true );
            }
        }

        private boolean parseResultToChannelList( String apiResult )
        {
            /*
             * success case:
             * [
             *     {TvAnyWhereChannel},
             *     {TvAnyWhereChannel},
             *     {TvAnyWhereChannel}
             * ]
             * */

            try
            {
                JSONArray channelJArray = new JSONArray(apiResult);
                for( int idx = 0; idx < channelJArray.length(); idx++)
                {
                    JSONObject channelJson = channelJArray.getJSONObject( idx );
                    TvAnyWhereChannel channel = TvAnyWhereChannel.fromJson( channelJson );
                    mChannelList.add( channel );
                }
            }
            catch ( JSONException e )
            {
                e.printStackTrace();
                Log.d( TAG, "GetChannelListTask: error=" + e.getMessage() );
                return false;
            }
            return true;
        }
    }

    class GetEpgListTask extends AsyncTask<Void,Integer,Boolean>
    {
        @Override
        protected Boolean doInBackground( Void... voids )
        {
            Log.d( TAG, "GetEpgListTask: " );
            String strUrl = ApiUtil.buildEpgListApi( mIp );

            URL url = null;
            HttpURLConnection connection = null;
            try
            {
                url = new URL(strUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                InputStream in = new BufferedInputStream(connection.getInputStream());
                String apiResult = CommonUtil.getStringFromInputStream( in );
                in.close();
                Log.d( TAG, "GetEpgListTask: apiResult="+apiResult );

                return parseEpgList(apiResult);
            }
            catch ( IOException e )
            {
                Log.d( TAG, "GetEpgListTask: error:"+e.getMessage() );
                e.printStackTrace();
                return false;
            }
            finally
            {
                if ( connection != null )
                {
                    connection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute( Boolean bSuccess )
        {
            if ( bSuccess )
            {
                mAdapter.notifyDataSetChanged();
            }
        }

        private Boolean parseEpgList( String apiResult )
        {
            /**
             * {
             *   "resultCode": "200",
             *   "resultMsg": [
             *     {
             *       "chKey": {ChKey},
             *       "events": [
             *           {EpgEventModel},
             *           {EpgEventModel},
             *           {EpgEventModel},
             *       ]
             *     },
             *     {
             *       "chKey": {ChKey},
             *       "events": [
             *           {EpgEventModel},
             *           {EpgEventModel},
             *           {EpgEventModel},
             *       ]
             *     },
             *   ]
             * }
             *
             *
             * */
            try
            {
                JSONObject json = new JSONObject( apiResult );
                int resultCode = json.getInt( "resultCode" );
                if ( resultCode != 200 )
                {
                    return false;
                }

                JSONArray epgInfoJArray = json.getJSONArray( "resultMsg" );
                for( int channelIdx = 0; channelIdx < epgInfoJArray.length(); channelIdx++)
                {
                    JSONObject epgInfoJson = epgInfoJArray.getJSONObject( channelIdx );

                    ChKey chKey = new ChKey( epgInfoJson.getJSONObject( "chKey" ) );

                    JSONArray eventJArray = epgInfoJson.getJSONArray( "events" );
                    List<EpgEventModel> events = new ArrayList<>();
                    for ( int eventIdx = 0; eventIdx < eventJArray.length(); eventIdx++ )
                    {
                        JSONObject eventJson = eventJArray.getJSONObject( eventIdx );
                        events.add( new EpgEventModel( eventJson ) );
                    }

                    mEpgMap.put( chKey, events );
                }

            }
            catch ( JSONException e )
            {
                e.printStackTrace();
                Log.d( TAG, "parseEpgList: "+e.getMessage() );
                return false;
            }
            return true;
        }
    }

    class GetEpgNowTask extends AsyncTask<Void,Integer,Boolean>
    {

        @Override
        protected Boolean doInBackground( Void... voids )
        {
            Log.d( TAG, "GetEpgListTask: " );
            String strUrl = ApiUtil.buildEpgNowApi( mIp );

            URL url = null;
            HttpURLConnection connection = null;
            try
            {
                url = new URL(strUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                InputStream in = new BufferedInputStream(connection.getInputStream());
                String apiResult = CommonUtil.getStringFromInputStream( in );
                in.close();
                Log.d( TAG, "GetEpgNowTask: apiResult="+apiResult );

                return parseEpgNow(apiResult);
            }
            catch ( IOException e )
            {
                Log.d( TAG, "GetEpgNowTask: error:"+e.getMessage() );
                e.printStackTrace();
                return false;
            }
            finally
            {
                if ( connection != null )
                {
                    connection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute( Boolean bSuccess )
        {
            if ( bSuccess )
            {
                mAdapter.notifyDataSetChanged();
            }
        }

        private Boolean parseEpgNow( String apiResult )
        {
            /*
             * success case:
             *
             * {
             *   "resultCode": "200",
             *   "resultMsg": [
             *     {EpgEventModel},
             *     {EpgEventModel},
             *     {EpgEventModel}
             *   ]
             * }
             * */

            try
            {
                JSONObject json = new JSONObject( apiResult );
                int resultCode = json.getInt( "resultCode" );
                if ( resultCode != 200 )
                {
                    return false;
                }

                JSONArray epgEventJArray = json.getJSONArray( "resultMsg" );
                for( int channelIdx = 0; channelIdx < epgEventJArray.length(); channelIdx++)
                {
                    JSONObject epgInfoJson = epgEventJArray.getJSONObject( channelIdx );
                    EpgEventModel event = new EpgEventModel( epgInfoJson );
                    mEpgNow.put( event.getChKey(), event );
                }
            }
            catch ( JSONException e )
            {
                e.printStackTrace();
                Log.d( TAG, "parseEpgNow: "+e.getMessage() );
                return false;
            }
            return true;
        }
    }


    public class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.ViewHolder>
    {
        public class ViewHolder extends RecyclerView.ViewHolder
        {
            TextView mTextChType;
            TextView mTextChNum;
            TextView mTextChName;
            Button mButtonPlay;
            Button mButtonEpg;
            TextView mTextNowPlaying;

            public ViewHolder( @NonNull View itemView )
            {
                super( itemView );
                mTextChType = itemView.findViewById( R.id.text_type );
                mTextChNum = itemView.findViewById( R.id.text_ch_num );
                mTextChName = itemView.findViewById( R.id.text_ch_name );
                mButtonPlay = itemView.findViewById( R.id.btn_play );
                mButtonEpg = itemView.findViewById( R.id.btn_epg );
                mTextNowPlaying = itemView.findViewById( R.id.text_epg_now );
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder( @NonNull ViewGroup parent, int viewType )
        {
            View view = LayoutInflater.from(parent.getContext()).inflate( R.layout.item_in_rv_channel, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder( @NonNull ViewHolder holder, int position )
        {
            TvAnyWhereChannel channel = mChannelList.get( position );
            ChKey chKey = channel.getChKey();

            holder.mTextChType.setText( ApiUtil.TunerType.getTunerSystemName(channel.getTunerType()) );

            holder.mTextChNum.setText( channel.getMajorNum()+"-"+channel.getMinorNum() );

            holder.mTextChName.setText( channel.getName() );

            boolean canPlayAtsc3 = mIsCapableOfAc4 | channel.getTunerType() != ApiUtil.TunerType.ATSC3;
            holder.mButtonPlay.setEnabled( canPlayAtsc3 );
            holder.mButtonPlay.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View v )
                {
                    Log.d( TAG, "onClick: "+channel );

                    Intent intent = new Intent( MainActivity.this, PlayerActivity.class );
                    intent.putExtra( PlayerActivity.PARAM_PLAY_URL, ApiUtil.buildPlayUrl( mIp, channel) );
                    startActivity( intent );
                }
            } );


            boolean bEpgMapReady = !(mEpgMap == null || mEpgMap.isEmpty());
            holder.mButtonEpg.setVisibility( bEpgMapReady ? View.VISIBLE : View.GONE );
            holder.mButtonEpg.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View v )
                {
                    List<EpgEventModel> epgEventModels = mEpgMap.get( chKey );
                    List<String> eventListToShow = new ArrayList<>();

                    if ( epgEventModels != null )
                    {
                        for ( EpgEventModel event : epgEventModels )
                        {
                            eventListToShow.add(  toDetailString( event ) );
                        }
                    }
                    SimpleRvAlertDialogFragment.newInstance( channel.getShowName(), eventListToShow ).show( getSupportFragmentManager(),null );
                }
            } );

            boolean mEpgNowReady = !(mEpgNow == null || mEpgNow.isEmpty());
            holder.mTextNowPlaying.setVisibility( mEpgNowReady ? View.VISIBLE : View.GONE );
            if ( mEpgNowReady )
            {
                EpgEventModel eventNow = mEpgNow.get( chKey );
                String simple = eventNow==null? "": toSimpleString(eventNow);
                holder.mTextNowPlaying.setText( "Now Playing: "+simple );
            }
        }

        @Override
        public int getItemCount()
        {
            return mChannelList.size();
        }

        private String toDetailString( EpgEventModel event )
        {
            long startT = event.getStartTimeInSec()*1000;
            long endT =  startT + event.getDurationInSec()*1000;
            Date dateStart = new Date( startT );
            Date dateEnd = new Date( endT );

            return SDF_YMD.format( dateStart )+
                    " " + SDF_HM.format( dateStart ) +
                    "-" + SDF_HM.format( dateEnd ) +
                    " " + "("+ SDF_ZONE.format( dateStart )+")" +
                    "\nname: " + event.getProgramName() +
                    "\nrating: " + event.getRatings() +
                    "\ndescr: " + event.getProgramDescription();
        }

        private String toSimpleString( EpgEventModel event )
        {
            long startT = event.getStartTimeInSec()*1000;
            long endT =  startT + event.getDurationInSec()*1000;
            Date dateStart = new Date( startT );
            Date dateEnd = new Date( endT );

            return SDF_YMD.format( dateStart)+
                    " " + SDF_HM.format( dateStart ) +
                    "-" + SDF_HM.format( dateEnd ) +
                    " " + "("+ SDF_ZONE.format( dateStart )+")" +
                    "\nname: " + event.getProgramName();
        }
    }

    private void toggleLoading( boolean bShow )
    {
        runOnUiThread( new Runnable()
        {
            @Override
            public void run()
            {
                mPbLoading.setVisibility( bShow ? View.VISIBLE:View.GONE );
            }
        } );
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data )
    {
        super.onActivityResult(requestCode,resultCode,data);

        IntentResult result = IntentIntegrator.parseActivityResult( requestCode, resultCode, data );
        if ( result != null )
        {
            String contents = result.getContents();
            if ( contents == null )
            {
                Log.d( TAG, "QR scan cancelled" );
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"QR scan cancelled", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else
            {
                Log.d( TAG, "QR code content = " + contents );
                parseIpFromQr( contents );
            }
        }
    }

    void parseIpFromQr( String contents)
    {
        Pattern pattern = Pattern.compile( "http://(.*):8080" );
        Matcher matcher = pattern.matcher( contents );
        if ( matcher.find() )
        {
            try
            {
                Log.d( TAG, "parseUsSubRatings: group( 1 )=" + matcher.group( 1 ) );
                String ip = matcher.group( 1 );
                if ( isValidIp( ip ) )
                {
                    mEditIp.setText( ip );
                }
            }
            catch ( Exception e )
            {
                Log.d( TAG, "parseUsSubRatings: err="+e.getMessage() );
            }
        }
    }
}