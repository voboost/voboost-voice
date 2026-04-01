package com.qinggan.canbus.plugs;

public interface BAPCmdCommType {
    public static final int FUNCTION_INVALID = 0;
    public static final int FUNCTION_VALID = 1;

    public static final class BAPClimateCMD {
        public static final int BAP_CLIMATE_INFO_REQUEST_CMD = 100;
        public static final int BAP_SEAT_VENTILATION_LEVEL_1 = 2;
        public static final int BAP_SEAT_VENTILATION_LEVEL_2 = 2;
        public static final int BAP_SEAT_VENTILATION_LEVEL_3 = 2;
        public static final int BAP_SEAT_VENTILATION_OFF = 0;
        public static final int SET_BAP_CLEAN_AIR_CMD = 104;
        public static final int SET_BAP_LEFT_SEAT_VENTILATION_CMD = 102;
        public static final int SET_BAP_RIGHT_SEAT_VENTILATION_CMD = 103;
        public static final int SET_BAP_STEERING_WHEEL_HEATING_CMD = 101;
    }

    public static final class BAPExteriorLightCMD {
        public static final int BAP_EXTERIOR_LIGHT_INFO_REQUEST_CMD = 500;
        public static final int BAP_EXTERIOR_LIGHT_LEVEL_0 = 0;
        public static final int BAP_EXTERIOR_LIGHT_LEVEL_1 = 1;
        public static final int BAP_EXTERIOR_LIGHT_LEVEL_2 = 2;
        public static final int BAP_EXTERIOR_LIGHT_LEVEL_3 = 3;
        public static final int BAP_EXTERIOR_LIGHT_LEVEL_4 = 4;
        public static final int BAP_EXTERIOR_LIGHT_LEVEL_5 = 5;
        public static final int BAP_EXTERIOR_LIGHT_LEVEL_6 = 6;
        public static final int SET_BAP_EXTERIOR_LIGHT_LEVEL_CMD = 501;
    }

    public static final class BAPOpsCMD {
        public static final int BAP_OPS_INFO_REQUEST_2_CMD = 201;
        public static final int BAP_OPS_INFO_REQUEST_3_CMD = 202;
        public static final int BAP_OPS_INFO_REQUEST_4_CMD = 203;
        public static final int BAP_OPS_INFO_REQUEST_5_CMD = 204;
        public static final int BAP_OPS_REQUEST_1_CMD = 200;
        public static final int SET_BAP_OPS_DRAG_CAR_CONNECTION_CMD = 207;
        public static final int SET_BAP_OPS_MUTE_CMD = 205;
        public static final int SET_BAP_OPS_POP_OPS_SURFACE_CMD = 206;
    }

    public static final class BAPRdkCMD {
        public static final int BAP_RDK_INFO_REQEUST_CMD = 400;
        public static final int SET_BAR_CALIBRATION_TYRE_CMD = 401;
    }

    public static final class BAPRvcCMD {
        public static final int BAP_RVC_INFO_REQEUST_CMD = 300;
    }
}
