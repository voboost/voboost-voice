package com.qinggan.canbus;

/* loaded from: classes.dex */
public interface CanBoxCommType {

    public static final class CAN_RAW_DATA {
        public static final int APAAVM_GENERAL_STATUS = 1427;
        public static final int BCM_GENERAL_STATUS = 1184;
        public static final int CAN_MSG_BMS1 = 912;
        public static final int CAN_MSG_BMS2 = 601;
        public static final int CAN_MSG_BMS6 = 1465;
        public static final int CAN_MSG_BMS7 = 1482;
        public static final int CAN_MSG_MCU04 = 1469;
        public static final int CAN_MSG_MCU_DB = 1483;
        public static final int DVR_System_STATUS_BAIC = 1440;
        public static final int EMS_ENGINE_TEMP = 1472;
        public static final int EMS_NEVER_BREAK_DOWN1_BAIC = 1479;
        public static final int EMS_NEVER_BREAK_DOWN2_BAIC = 1480;
        public static final int EMS_NEVER_BREAK_DOWN3_BAIC = 1481;
        public static final int EMS_NEVER_BREAK_DOWN4 = 1498;
        public static final int EMS_NEVER_BREAK_DOWN5 = 1499;
        public static final int EMS_NEVER_BREAK_DOWN6 = 1500;
        public static final int ESP_GENERAL_STATUS = 792;
        public static final int ESP_GENERAL_STATUS3 = 1349;
        public static final int ESP_GENERAL_STATUS_YAW_SENSOR2E = 1009;
        public static final int ESP_STATUS_FOR_ACC = 777;
        public static final int HUM_VCU_SMART_ENERGY_MANGEMENT = 1526;
        public static final int ICM_GENERAL_STATUS = 1568;
        public static final int ICM_GENERAL_STATUS_2 = 1569;
        public static final int ICM_GENERAL_STATUS_3 = 1777;
        public static final int VCU_COMPONENT_ENERGY1 = 1535;
        public static final int VCU_COMPONENT_ENERGY2 = 1534;
        public static final int VCU_DB_VEHICLE_FAULT = 1524;
        public static final int VCU_DRIVER_COUNTER = 1501;
        public static final int VCU_ESP_TORQUE = 440;
        public static final int VCU_HUM_SMART_ENERGY_MANAGEMENT = 1502;
        public static final int VCU_IP_DISPLAY1 = 1489;
        public static final int VCU_IP_DISPLAY2 = 1490;
        public static final int VCU_ROUTE_SUM = 1450;
        public static final int VCU_TBOX_VEHICLE_STS = 1561;
        public static final int VCU_TCU_GENERAL_STATUS = 992;
        public static final int VCU_VEHICLE_BASIC_STS = 1485;
    }

    public static final class KEY_CODE {
        public static final int ACCCANCEL = 32;
        public static final int ACCDISTANCEDOWN = 33;
        public static final int ACCDISTANCEUP = 34;
        public static final int ACCPOWER = 29;
        public static final int ACCRESET = 30;
        public static final int ACCSET = 31;
        public static final int CANCEL = 26;
        public static final int CCP_KEY_ANTICLOCKWISE = 62;
        public static final int CCP_KEY_BACK = 50;
        public static final int CCP_KEY_CLOCKWISE = 61;
        public static final int CCP_KEY_DOWN_SLIDING = 58;
        public static final int CCP_KEY_ENTER = 56;
        public static final int CCP_KEY_LEFT_SLIDING = 59;
        public static final int CCP_KEY_MEDIA = 53;
        public static final int CCP_KEY_MENU = 49;
        public static final int CCP_KEY_MUTE_OFF = 51;
        public static final int CCP_KEY_NAVIGAITION = 48;
        public static final int CCP_KEY_RADIO = 52;
        public static final int CCP_KEY_RIGHT_SLIDING = 60;
        public static final int CCP_KEY_UP_SLIDING = 57;
        public static final int CCP_KEY_VOLUME_ADD = 55;
        public static final int CCP_KEY_VOLUME_DEC = 54;
        public static final int CRUISE = 24;
        public static final int CRUISE_OFF = 25;
        public static final int ENTER = 19;
        public static final int FLIPPAGE = 35;
        public static final int HANGUP = 9;
        public static final int ICALL = 15;
        public static final int IVOKA = 16;
        public static final int KEYCODE_DOUBLE_CLICK = 106;
        public static final int KEYCODE_DOUBLE_DOUBLECLICK = 107;
        public static final int KEYCODE_DOUBLE_DRAG_ANTICLOCKWISE = 109;
        public static final int KEYCODE_DOUBLE_DRAG_CLOCKWISE = 108;
        public static final int KEYCODE_DOUBLE_DRAG_DOWN = 103;
        public static final int KEYCODE_DOUBLE_DRAG_LEFT = 100;
        public static final int KEYCODE_DOUBLE_DRAG_RIGHT = 101;
        public static final int KEYCODE_DOUBLE_DRAG_UP = 102;
        public static final int KEYCODE_DOUBLE_EDGE_LEFT = 110;
        public static final int KEYCODE_DOUBLE_EDGE_RIGHT = 111;
        public static final int KEYCODE_DOUBLE_ZOOM_IN = 105;
        public static final int KEYCODE_DOUBLE_ZOOM_OUT = 104;
        public static final int KEYCODE_SINGLE_CLICK = 98;
        public static final int KEYCODE_SINGLE_DOUBLECLICK = 99;
        public static final int KEYCODE_SINGLE_DRAG_ANTICLOCKWISE = 91;
        public static final int KEYCODE_SINGLE_DRAG_CLOCKWISE = 90;
        public static final int KEYCODE_SINGLE_DRAG_DOWN = 97;
        public static final int KEYCODE_SINGLE_DRAG_LEFT = 94;
        public static final int KEYCODE_SINGLE_DRAG_RIGHT = 95;
        public static final int KEYCODE_SINGLE_DRAG_UP = 96;
        public static final int KEYCODE_SINGLE_EDGE_LEFT = 92;
        public static final int KEYCODE_SINGLE_EDGE_RIGHT = 93;
        public static final int MALFUNCTION = 254;
        public static final int MEMDOEWN = 11;
        public static final int MEMUP = 10;
        public static final int METERDOWN = 37;
        public static final int METERUP = 36;
        public static final int MODE = 7;
        public static final int MUTE = 6;
        public static final int PHONE = 5;
        public static final int PICKUP = 14;
        public static final int RESUP = 27;
        public static final int RES_DOWN = 28;
        public static final int RETURN = 13;
        public static final int SEEKDOWN = 3;
        public static final int SEEKUP = 4;
        public static final int SHUT_DOWN = 64;
        public static final int SPEECH = 10;
        public static final int SRC = 12;
        public static final int SWS_KEY_AVM = 78;
        public static final int SWS_KEY_NVS = 77;
        public static final int SWS_KEY_PHOTO = 76;
        public static final int SWS_KEY_USER_DEFINED = 65;
        public static final int SWS_L_TOUCH_ANTICLOCKWISE = 73;
        public static final int SWS_L_TOUCH_CLOCKWISE = 72;
        public static final int SWS_R_TOUCH_ANTICLOCKWISE = 75;
        public static final int SWS_R_TOUCH_CLOCKWISE = 74;
        public static final int SWS_SELECT_DOWN = 68;
        public static final int SWS_SELECT_DOWN_WHEEL = 69;
        public static final int SWS_SELECT_LEFT = 70;
        public static final int SWS_SELECT_RIGHT = 71;
        public static final int SWS_SELECT_UP = 66;
        public static final int SWS_SELECT_UP_WHEEL = 67;
        public static final int TAKEPHOTO = 17;
        public static final int TEL = 8;
        public static final int VOLDOWN = 2;
        public static final int VOLUP = 1;
    }

    public static final class KEY_STATUS {
        public static final int KEYCLICKS = 2;
        public static final int KEYDOWN = 1;
        public static final int KEYUP = 0;
        public static final int LONG_PRESSED = 4;
        public static final int SHORT_PRESSED = 3;
    }

    public static final class RADAR_DATA {
        public static final int NO_OBSTACLES = 255;
        public static final int NO_RADAR = 239;
    }
}
