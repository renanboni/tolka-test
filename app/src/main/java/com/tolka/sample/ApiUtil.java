package com.tolka.sample;

import com.tolka.sample.model.TvAnyWhereChannel;

public class ApiUtil
{
    public static class ApiConstants
    {
        public static final int API_PORT = 8080;
        public static final String PREFIX_HTTP = "http://";
        static final String PREFIX_ATSC1 = "atsc1://";
        static final String PREFIX_ATSC3 = "atsc3://";
        public static final String URI_TV_ANYWHERE_CHANNEL_LIST_V2 = "/tolka/api/tv-anywhere/v2/service";
        public static final String URI_TV_ANYWHERE_EPG_LIST = "/tolka/api/v1/tv-anywhere/epg-list";
        public static final String URI_TV_ANYWHERE_EPG_NOW = "/tolka/api/v1/tv-anywhere/epg-now";
    }

    public static class TunerType
    {
        public static final int ISDBT = 0;
        public static final int DVBT = 1;
        public static final int DVBT2 = 2;
        public static final int ATSC1 = 3;
        public static final int DVBC = 4;
        public static final int DVBS = 5;
        public static final int ATSC3 = 6;

        public static String getTunerSystemName( int type )
        {
            switch ( type )
            {
            case ISDBT:
                return "ISDB-T";
            case DVBT:
                return "DVB-T";
            case DVBT2:
                return "DVB-T2";
            case ATSC1:
                return "ATSC1";
            case DVBC:
                return "DVBC";
            case DVBS:
                return "DVBS";
            case ATSC3:
                return "ATSC3";
            }
            return "Unknown";
        }
    }

    public static String buildGetChannelApiV2( String ip )
    {
        return getBasicUrl( ip ) + ApiConstants.URI_TV_ANYWHERE_CHANNEL_LIST_V2;
    }

    public static String buildPlayUrl( String ip, TvAnyWhereChannel channel )
    {
        String httpUrl = getBasicUrl(ip)+channel.getUrl();
        if ( channel.getTunerType() == TunerType.ATSC1 )
        {
            httpUrl = httpUrl.replace( ApiConstants.PREFIX_HTTP, ApiConstants.PREFIX_ATSC1 );
        }
        else if ( channel.getTunerType() == TunerType.ATSC3 )
        {
            httpUrl = httpUrl.replace( ApiConstants.PREFIX_HTTP, ApiConstants.PREFIX_ATSC3 );
        }
        return httpUrl;
    }

    public static String buildEpgListApi( String ip )
    {
        return getBasicUrl( ip ) + ApiConstants.URI_TV_ANYWHERE_EPG_LIST;
    }

    public static String buildEpgNowApi( String ip )
    {
        return getBasicUrl( ip ) + ApiConstants.URI_TV_ANYWHERE_EPG_NOW;
    }

    private static String getBasicUrl( String ip )
    {
        return ApiConstants.PREFIX_HTTP + ip + ":" + ApiConstants.API_PORT;
    }
}