package com.qinggan.canbus.plugs;

public class DLNAManager  {
    private static final int MEDIA_RENDER_ACTION_CMD_BASE = 100;
    public static final int MEDIA_RENDER_ACTION_CMD_NEXT = 109;
    public static final int MEDIA_RENDER_ACTION_CMD_PAUSE = 103;
    public static final int MEDIA_RENDER_ACTION_CMD_PLAY = 102;
    public static final int MEDIA_RENDER_ACTION_CMD_PRE = 108;
    public static final int MEDIA_RENDER_ACTION_CMD_SEEK = 104;
    public static final int MEDIA_RENDER_ACTION_CMD_SETMUTE = 106;
    public static final int MEDIA_RENDER_ACTION_CMD_SETPLAYMODE = 107;
    public static final int MEDIA_RENDER_ACTION_CMD_SETVOLUME = 105;
    public static final int MEDIA_RENDER_ACTION_CMD_SET_AV_URL = 100;
    public static final int MEDIA_RENDER_ACTION_CMD_STOP = 101;
    public static final int MEDIA_RENDER_CTL_MSG_GETMEDIAINFO = 118;
    public static final int MEDIA_RENDER_CTL_MSG_GETPOSITIONINFO = 119;
    public static final int MEDIA_RENDER_CTL_MSG_GETTRANSPORTSETTINGS = 120;
    public static final int MEDIA_RENDER_CTL_MSG_QPLAY_GETMAXTRACKS = 115;
    public static final int MEDIA_RENDER_CTL_MSG_QPLAY_GETTRACKSCOUNT = 116;
    public static final int MEDIA_RENDER_CTL_MSG_QPLAY_GETTRACKSINFO = 113;
    public static final int MEDIA_RENDER_CTL_MSG_QPLAY_INSERT_TRACKS = 111;
    public static final int MEDIA_RENDER_CTL_MSG_QPLAY_QPLAYAUTH = 110;
    public static final int MEDIA_RENDER_CTL_MSG_QPLAY_REMOVETRACKS = 112;
    public static final int MEDIA_RENDER_CTL_MSG_QPLAY_SETNETWORK = 117;
    public static final int MEDIA_RENDER_CTL_MSG_QPLAY_SETTRACKSINFO = 114;
    private static final int MEDIA_RENDER_GENA_EVENT_CMD_BASE = 500;
    public static final int MEDIA_RENDER_GENA_EVENT_CMD_SET_MEDIA_DATA = 504;
    public static final int MEDIA_RENDER_GENA_EVENT_CMD_SET_MEDIA_DURATION = 500;
    public static final int MEDIA_RENDER_GENA_EVENT_CMD_SET_MEDIA_MUTE = 505;
    public static final int MEDIA_RENDER_GENA_EVENT_CMD_SET_MEDIA_PLAYINGSTATE = 502;
    public static final int MEDIA_RENDER_GENA_EVENT_CMD_SET_MEDIA_POSITION = 501;
    public static final int MEDIA_RENDER_GENA_EVENT_CMD_SET_MEDIA_URI = 503;
    public static final int MEDIA_RENDER_GENA_EVENT_CMD_SET_MEDIA_VOLUME = 506;
    public static final String QDLNA_ERROR_CODE_STR = "Q_ErrorCode";
    public static final String QDLNA_ERROR_REASON_STR = "Q_ErrorReason";
    public static final String QDLNA_REMOTE_IP_STR = "Q_RemoteIP";
    public static final String TAG = DLNAManager.class.getSimpleName();
}
