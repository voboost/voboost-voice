package com.qinggan.canbus.plugs;

public final class SceneID {
    public static final int SCENE_CALL = 516;
    public static final int SCENE_GASOIL = 533;
    public static final int SCENE_HOTEL = 529;
    public static final int SCENE_LOCAL_RADIO = 548;
    public static final int SCENE_LOCAL_VIDEO = 554;
    public static final int SCENE_MOVIE = 530;
    public static final int SCENE_MUSIC = 545;
    public static final int SCENE_NAVI = 544;
    public static final int SCENE_NEWS = 555;
    public static final int SCENE_ONLINE_RADIO = 549;
    public static final int SCENE_ONLINE_VIDEO = 534;
    public static final int SCENE_PARKING = 518;
    public static final int SCENE_PHONE_CALLING = 557;
    public static final int SCENE_POI = 520;
    public static final int SCENE_QGXW = 513;
    public static final int SCENE_QGXW_INPUT = 514;
    public static final int SCENE_QGXW_SELECT = 515;
    public static final int SCENE_RADIO = 517;
    public static final int SCENE_RESTAURANT = 521;
    public static final int SCENE_SCHEDULE = 552;
    public static final int SCENE_SHARE_PIC_SEND_VOICE = 547;
    public static final int SCENE_TAKEOUT = 532;
    public static final int SCENE_TAXI = 536;
    public static final int SCENE_THIRD_VOICE_TO_PATEO = 550;
    public static final int SCENE_THUNDER = 556;
    public static final int SCENE_TOUR = 528;
    public static final int SCENE_TRAIN = 531;
    public static final int SCENE_TRANSPORTATION = 546;
    public static final int SCENE_VEHICLE = 519;
    public static final int SCENE_VIDEO = 553;
    public static final int SCENE_VOICEPRINT_PAY = 551;
    public static final int SCENE_WASHING_CAR = 537;
    public static final int SCENE_WEATHER = 535;
    public static final int SOURCE_VOICE_KEY = 65536;
    public static final int SOURCE_VOICE_SCREEN = 131072;
    public static final int SOURCE_VOICE_WAKEUP = 196608;
    private static final int VUI_FLAG_MASK = -65536;
    public static final int VUI_SCENE_ALL = 1;
    public static final int VUI_SCENE_ANSWER_PHONE_CALL = 15;
    public static final int VUI_SCENE_COMMON_CONFIRM = 17;
    public static final int VUI_SCENE_CONFIRM = 5;
    public static final int VUI_SCENE_CONTACTS = 4;
    public static final int VUI_SCENE_DEFAULT = 1;
    public static final int VUI_SCENE_DICTATION = 7;
    public static final int VUI_SCENE_DUEROS_LISTEN = 18;
    public static final int VUI_SCENE_FACTORY_TEST = 13;
    public static final int VUI_SCENE_FEEDBACK = 10;
    public static final int VUI_SCENE_INPUT = 8;
    public static final int VUI_SCENE_INPUT_NAVI = 32770;
    public static final int VUI_SCENE_INPUT_PHONE = 32769;
    private static final int VUI_SCENE_MASK = 65535;
    public static final int VUI_SCENE_NO_SET = -1;
    public static final int VUI_SCENE_POI = 2;
    public static final int VUI_SCENE_POI_INPUT = 9;
    public static final int VUI_SCENE_QGXW_REPLY_CONFIRM = 5;
    public static final int VUI_SCENE_QGXW_SEND_CONFIRM = 14;
    public static final int VUI_SCENE_QING_LINK_ASR = 20;
    public static final int VUI_SCENE_QING_LINK_DISS = 19;
    public static final int VUI_SCENE_RECORD = 11;
    public static final int VUI_SCENE_SELECT = 6;
    public static final int VUI_SCENE_SELECT_MVW = 12;
    public static final int VUI_SCENE_SMS = 3;
    public static final int VUI_SCENE_TEXT_CORRECTION = 14;
    public static final int VUI_SCENE_TTS_NOTIFY = 16;
    public static final int VUI_SCENE_VOICE_CONTINUOUSDIALOGUE = 22;
    public static final int VUI_SCENE_VOICE_GAME = 21;
    public static final int VUI_SCENE_VOICE_MOCK = 23;
    public static final int VUI_SCENE_WAKEUP = 0;
    private static final int VUI_SOURCE_SHIFTS = 16;
    public static final int VUI_TAG_DEFAULT = 256;
    public static final int VUI_TAG_HICAR = 1024;
    public static final int VUI_TAG_MOCK = 1280;
    public static final int VUI_TAG_OUTCAR = 1536;
    public static final int VUI_TAG_QING_LINK = 512;
    public static final int VUI_TAG_THIRD_VOICE = 768;

    public static int getScene(int all) {
        int scene = 65535 & all;
        return scene;
    }

    public static int getSource(int all) {
        int source = (-65536) & all;
        return source;
    }

    public static String getSceneString(int id) {
        if (id != 517) {
            if (id != 534) {
                if (id == 545) {
                    return "Music";
                }
                if (id != 553) {
                    if (id != 548) {
                        if (id == 549) {
                            return "OnlineRadio";
                        }
                        if (id == 555) {
                            return "News";
                        }
                        if (id != 556) {
                            return "";
                        }
                        return "KSong";
                    }
                }
            }
            return "OnlineVideo";
        }
        return "LocalRadio";
    }
}
