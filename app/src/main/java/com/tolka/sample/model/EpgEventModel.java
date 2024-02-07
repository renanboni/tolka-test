package com.tolka.sample.model;

import org.json.JSONObject;

public class EpgEventModel
{
    /**
    * {
     *   "frequency": 575000,
     *   "serviceId": 5004,
     *   "channelShowName": "2-1 WMAR",
     *   "infoVersion": 0,
     *   "scheduleId": "urn:digicap:schf:002001:20220929000325",
     *   "eventId": 0,
     *   "startTimeInSec": 1664336100,
     *   "durationInSec": 3720,
     *   "programName": "Jimmy Kimmel Live!",
     *   "programDescription": "TV host David Letterman; actor Tracy Morgan.",
     *   "programIcon": "http://tmsimg.com/assets/p184869_b_v12_al.jpg?w=240&amp;h=360",
     *   "ratings": "1,'TV-14-D-L',{0 'TV-14'}{1 'D'}{2 'L'}"
     * }
    * */

    private int frequency;
    private int serviceId;
    private String channelShowName;
    private int infoVersion;
    private String scheduleId;
    private int eventId;
    private long startTimeInSec;
    private long durationInSec;
    private String programName;
    private String programDescription;
    private String programIcon;
    private String ratings;

    public EpgEventModel( JSONObject json )
    {
        this.frequency = json.optInt( "frequency" );
        this.serviceId = json.optInt( "serviceId" );
        this.channelShowName = json.optString( "channelShowName" );
        this.infoVersion = json.optInt( "infoVersion" );
        this.scheduleId = json.optString( "scheduleId" );
        this.eventId = json.optInt( "eventId" );
        this.startTimeInSec = json.optLong( "startTimeInSec" );
        this.durationInSec = json.optLong( "durationInSec" );
        this.programName = json.optString( "programName" );
        this.programDescription = json.optString( "programDescription" );
        this.programIcon = json.optString( "programIcon" );
        this.ratings = json.optString( "ratings" );
    }

    public int getServiceId()
    {
        return serviceId;
    }

    public String getChannelShowName()
    {
        return channelShowName;
    }

    public int getFrequency()
    {
        return frequency;
    }

    public int getInfoVersion()
    {
        return infoVersion;
    }

    public String getScheduleId()
    {
        return scheduleId;
    }

    public int getEventId()
    {
        return eventId;
    }

    public long getStartTimeInSec()
    {
        return startTimeInSec;
    }

    public long getDurationInSec()
    {
        return durationInSec;
    }

    public String getProgramName()
    {
        return programName;
    }

    public String getProgramDescription()
    {
        return programDescription;
    }

    public String getProgramIcon()
    {
        return programIcon;
    }

    public String getRatings()
    {
        return ratings;
    }

    public ChKey getChKey()
    {
        return new ChKey( frequency, serviceId, channelShowName );
    }
}
