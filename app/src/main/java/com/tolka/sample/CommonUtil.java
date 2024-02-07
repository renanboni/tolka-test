package com.tolka.sample;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class CommonUtil
{
    public static final int REQUEST_PERMISSION_CODE = 0;

    public static String getStringFromInputStream( InputStream is )
    {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try
        {
            br = new BufferedReader( new InputStreamReader( is ) );
            while ( (line = br.readLine()) != null )
            {
                sb.append( line );
            }
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        finally
        {
            if ( br != null )
            {
                try
                {
                    br.close();
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    public static boolean hasCameraPermission( Activity activity) {
        if ( ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CODE );
            return false;
        }
        return true;
    }
}
