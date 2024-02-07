package com.tolka.sample.model;

import org.json.JSONException;
import org.json.JSONObject;

public class TvAnyWhereChannel
{
    /**
    *   {
     *     "freq": 473000,
     *     "tunerType": 3,
     *     "svcId": 1,
     *     "majorNum": 8,
     *     "minorNum": 1,
     *     "name": "WGTV-DT",
     *     "url": "/tolka/api/tv-anywhere/v2/service?freq=473000&showName=8-1+WGTV-DT&svcId=1"
     *   }
    * */

    private int svcId;
    private int freq;
    private int tunerType;
    private int majorNum;
    private int minorNum;
    private String name;
    private String url;

    public TvAnyWhereChannel( int svcId, int freq, int tunerType, int majorNum, int minorNum, String name, String url )
    {
        this.svcId = svcId;
        this.freq = freq;
        this.tunerType = tunerType;
        this.majorNum = majorNum;
        this.minorNum = minorNum;
        this.name = name;
        this.url = url;
    }

    public int getSvcId()
    {
        return svcId;
    }

    public int getFreq()
    {
        return freq;
    }

    public int getTunerType()
    {
        return tunerType;
    }

    public int getMajorNum()
    {
        return majorNum;
    }

    public int getMinorNum()
    {
        return minorNum;
    }

    public String getName()
    {
        return name;
    }

    public String getUrl()
    {
        return url;
    }

    public String getShowName()
    {
        return getMajorNum()+"-"+getMinorNum()+" "+getName();
    }

    public ChKey getChKey()
    {
        return new ChKey( getFreq(), getSvcId(), getShowName() );
    }

    public JSONObject toJson()
    {
        JSONObject json = new JSONObject();
        try
        {
            json.put( "svcId",svcId );
            json.put( "freq",freq );
            json.put( "tunerType",tunerType );
            json.put( "majorNum",majorNum );
            json.put( "minorNum",minorNum );
            json.put( "name",name );
            json.put( "url",url );
        }
        catch ( JSONException e )
        {
            e.printStackTrace();
        }
        return json;
    }

    static public TvAnyWhereChannel fromJson( JSONObject json )
    {
        try
        {
            int svcId = json.getInt( "svcId" );
            int freq = json.getInt( "freq" );
            int tunerType = json.getInt( "tunerType" );
            int majorNum = json.getInt( "majorNum" );
            int minorNum = json.getInt( "minorNum" );
            String name = json.getString( "name" );
            String url = json.getString( "url" );
            return new TvAnyWhereChannel( svcId, freq, tunerType, majorNum, minorNum, name, url );
        }
        catch ( JSONException e )
        {
            e.printStackTrace();
        }
        return null;
    }
}
