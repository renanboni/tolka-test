package com.tolka.sample.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class ChKey
{
    private int frequency;
    private int serviceId;
    private String showName;

    public ChKey( JSONObject json )
    {
        this.frequency = json.optInt( "frequency" );
        this.serviceId = json.optInt( "serviceId" );
        this.showName = json.optString( "showName" );
    }

    public ChKey( int frequency, int serviceId, String showName )
    {
        this.frequency = frequency;
        this.serviceId = serviceId;
        this.showName = showName;
    }

    public int getFrequency()
    {
        return frequency;
    }

    public int getServiceId()
    {
        return serviceId;
    }

    public String getShowName()
    {
        return showName;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }
        ChKey chKey = (ChKey) o;
        return frequency == chKey.frequency && serviceId == chKey.serviceId && Objects.equals( showName, chKey.showName );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( frequency, serviceId, showName );
    }

    public JSONObject toJson()
    {
        JSONObject json = new JSONObject();
        try
        {
            json.put( "frequency", frequency );
            json.put( "serviceId", serviceId );
            json.put( "showName", showName );
        }
        catch ( JSONException e )
        {
            e.printStackTrace();
        }
        return json;
    }
}
