package com.qinggan.canbus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import com.qinggan.canbus.ICanBusService;
import com.qinggan.common.OnInitListener;
import com.qinggan.os.ServiceManager;
import com.qinggan.util.VehicleHelper;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/* loaded from: classes.dex */
public class CanBusManager {
    public static final int BRAKE_FLUID_STATUS_UNKNOWN = Integer.MIN_VALUE;
    public static final int BRAKE_PAD_STATUS_UNKNOWN = Integer.MIN_VALUE;
    public static final int BRAKE_WIPER_STATUS_UNKNOWN = Integer.MIN_VALUE;
    public static final String CANBUS_SERVICE = "com.qinggan.canbus.CanBusService";
    public static final String CANBUS_SERVICE_PACKAGE = "com.qinggan.canbus.service";
    public static final int CAN_STATE_UNKNOWN = -1;
    public static final int CONNECT_ERROR = -1;
    public static final int ENGINE_SPEED_UNKNOWN = Integer.MIN_VALUE;
    public static final int ENGINE_STATUS_UNKNOWN = Integer.MIN_VALUE;
    public static final int FUEL_CONSUMPTION_UNKNOWN = Integer.MIN_VALUE;
    public static final float FUEL_CONSUMPTION_UNKNOWN_F = Float.MIN_VALUE;
    public static final int GEAR_UNKNOWN = Integer.MIN_VALUE;
    public static final int HAVE_SET_AIR_CONDITION_STATE = 3;
    public static final int HAVE_SET_VEHICLE_STATE = 3;
    public static final int ILLUMINATION_UNKNOWN = Integer.MIN_VALUE;
    public static final int INVALID_VALUE = -9999;
    public static final long ODOMETER_UNKNOWN = -2147483648000L;
    public static final String QG_CANBUS_SERVICE = "qg.canbus";
    public static final int SET_AIR_CONDITION_FUNCTION_ERROR = 2;
    public static final int SET_AIR_CONDITION_STATE_FAILED = 0;
    public static final int SET_AIR_CONDITION_STATE_MAX = 4;
    public static final int SET_AIR_CONDITION_STATE_MIN = 6;
    public static final int SET_AIR_CONDITION_STATE_SUCCEED = 1;
    public static final int SET_VEHICLE_FUNCTION_ERROR = 2;
    public static final int SET_VEHICLE_STATE_FAILED = 0;
    public static final int SET_VEHICLE_STATE_SUCCEED = 1;
    public static final int STATE_UNKNOWN = Integer.MIN_VALUE;
    private static final String TAG = "CanBusManager";
    public static final int TEMPERATURE_UNKNOWN = Integer.MIN_VALUE;
    public static final int VEHICLE_KEY_UNKNOWN = Integer.MIN_VALUE;
    public static final int VEHICLE_SPEED_UNKNOWN = Integer.MIN_VALUE;
    public static final int VEHICLE_STATE_ACTIVATE = 1;
    public static final int VEHICLE_STATE_OFF = 0;
    public static final int VEHICLE_STATE_ON = 1;
    private static Handler mHandler;
    private static HandlerThread mHandlerThread;
    private static volatile CanBusManager mInstance;
    private Context mContext;
    private ICanBusService mService;
    private static boolean isConnected = false;
    private static Object mLock = new Object();
    private static final Set<OnInitListener> mGetIntanceList = Collections.synchronizedSet(new HashSet());
    private boolean mEmulator = false;
    private HashMap<Context, OnInitListener> mInitListenerList = new HashMap<>();
    private Runnable mObtainICanBusServiceBinderRunnable = new Runnable() { // from class: com.qinggan.canbus.CanBusManager.1
        @Override // java.lang.Runnable
        public void run() throws InterruptedException, RemoteException {
            try {
                Thread.sleep(50L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int time = 100;
            while (!CanBusManager.isConnected) {
                IBinder binder = ServiceManager.getService("qg.canbus");
                if (binder == null) {
                    if (time % 1000 == 0) {
                        Log.e(CanBusManager.TAG, "mObtain can not get service qg.canbus");
                    }
                    try {
                        Thread.sleep(100L);
                        time += 100;
                    } catch (InterruptedException e2) {
                        e2.printStackTrace();
                    }
                } else {
                    try {
                        binder.linkToDeath(CanBusManager.this.deathRecipient, 0);
                    } catch (RemoteException e1) {
                        e1.printStackTrace();
                    }
                    CanBusManager.this.mService = ICanBusService.Stub.asInterface(binder);
                    boolean unused = CanBusManager.isConnected = true;
                    if (CanBusManager.this.mInitListenerList != null && CanBusManager.this.mInitListenerList.size() > 0) {
                        ((OnInitListener) CanBusManager.this.mInitListenerList.get(CanBusManager.this.mContext)).onConnectStatusChange(CanBusManager.isConnected);
                    }
                    for (OnInitListener listener : CanBusManager.mGetIntanceList) {
                        if (listener != null) {
                            listener.onConnectStatusChange(CanBusManager.isConnected);
                        }
                    }
                }
            }
        }
    };
    private Runnable mReObtainICanBusServiceBinderRunnable = new Runnable() { // from class: com.qinggan.canbus.CanBusManager.2
        @Override // java.lang.Runnable
        public void run() throws InterruptedException, RemoteException {
            int time = 100;
            while (!CanBusManager.isConnected) {
                IBinder binder = ServiceManager.getService("qg.canbus");
                if (binder == null) {
                    if (time % 1000 == 0) {
                        Log.e(CanBusManager.TAG, "mReObtain can not get service qg.canbus");
                    }
                    try {
                        Thread.sleep(100L);
                        time += 100;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        binder.linkToDeath(CanBusManager.this.deathRecipient, 0);
                    } catch (RemoteException e1) {
                        e1.printStackTrace();
                    }
                    CanBusManager.this.mService = ICanBusService.Stub.asInterface(binder);
                    boolean unused = CanBusManager.isConnected = true;
                    CanBusManager.this.dispatchConnectStatus(CanBusManager.isConnected);
                }
            }
        }
    };
    IBinder.DeathRecipient deathRecipient = new IBinder.DeathRecipient() { // from class: com.qinggan.canbus.CanBusManager.3
        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            CanBusManager.this.mService = null;
            boolean unused = CanBusManager.isConnected = false;
            new Thread(CanBusManager.this.mReObtainICanBusServiceBinderRunnable).start();
            Log.d(CanBusManager.TAG, "CanBusManager is recreate ============================================= ");
        }
    };

    private CanBusManager(Context context, OnInitListener listener) throws RemoteException {
        this.mContext = context;
        this.mInitListenerList.put(context, listener);
        mHandlerThread = new HandlerThread("callbackcanbusinit");
        mHandlerThread.start();
        mHandler = new GetInstanceHandler(mHandlerThread.getLooper());
        initCanBusService();
    }

    private boolean initCanBusService() throws RemoteException {
        IBinder binder = ServiceManager.getService("qg.canbus");
        if (binder == null) {
            Log.e(TAG, "initCanBusService can not get service qg.canbus");
            Intent intent = new Intent();
            intent.setPackage("com.qinggan.canbus.service");
            intent.setAction("qg.canbus");
            this.mContext.startService(intent);
            new Thread(this.mObtainICanBusServiceBinderRunnable).start();
            return false;
        }
        this.mService = ICanBusService.Stub.asInterface(binder);
        isConnected = true;
        new Thread(new Runnable() { // from class: com.qinggan.canbus.CanBusManager.4
            @Override // java.lang.Runnable
            public void run() throws InterruptedException {
                if (CanBusManager.this.mInitListenerList != null && CanBusManager.this.mInitListenerList.size() > 0) {
                    OnInitListener callback = (OnInitListener) CanBusManager.this.mInitListenerList.get(CanBusManager.this.mContext);
                    try {
                        Thread.sleep(50L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (callback != null) {
                        callback.onConnectStatusChange(CanBusManager.isConnected);
                    }
                }
            }
        }).start();
        try {
            binder.linkToDeath(this.deathRecipient, 0);
        } catch (RemoteException e1) {
            e1.printStackTrace();
        }
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized void dispatchConnectStatus(boolean isConnected2) {
        if (this.mInitListenerList != null && this.mInitListenerList.size() > 0) {
            this.mInitListenerList.get(this.mContext).onConnectStatusChange(isConnected2);
        }
        for (OnInitListener listener : mGetIntanceList) {
            if (listener != null) {
                listener.onConnectStatusChange(isConnected2);
            }
        }
    }

    public static CanBusManager getInstance() throws IllegalStateException {
        if (isConnected) {
            return mInstance;
        }
        throw new IllegalStateException("please init in onCreate of  applicatiion,or server process is invaild,now it is resarting");
    }

    public static synchronized CanBusManager getInstance(Context context, OnInitListener listener) {
        synchronized (mLock) {
            if (mInstance == null) {
                if (context != null && listener != null) {
                    mInstance = new CanBusManager(context, listener);
                    return mInstance;
                }
                throw new IllegalArgumentException("params illegal!");
            }
            if (listener != null) {
                Message message = mHandler.obtainMessage();
                message.obj = listener;
                mHandler.sendMessageDelayed(message, 50L);
            }
            return mInstance;
        }
    }

    public void registerCanBusListener(CanBusListener listener) {
        ICanBusService iCanBusService = this.mService;
        if (iCanBusService != null) {
            try {
                iCanBusService.addCallback(listener);
            } catch (RemoteException e) {
                Log.e(TAG, "addCallback exception:" + e.getMessage());
                e.printStackTrace();
            } catch (Exception ex) {
                Log.e(TAG, "addCallback exception:" + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    @Deprecated
    public boolean destroy() {
        Log.d(TAG, "disConnect");
        return true;
    }

    public void unregisterCanBusListener(CanBusListener listener) {
        ICanBusService iCanBusService = this.mService;
        if (iCanBusService != null) {
            try {
                iCanBusService.removeCallback(listener);
            } catch (RemoteException ex) {
                Log.e(TAG, "removeCallback exception:" + ex.getMessage());
                ex.printStackTrace();
            } catch (Exception ex2) {
                Log.e(TAG, "removeCallback exception:" + ex2.getMessage());
                ex2.printStackTrace();
            }
        }
    }

    public Odometer getOdometer() {
        if (this.mEmulator || !isConnected) {
            return null;
        }
        try {
            return this.mService.getOdometer();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return null;
        } catch (Exception ex) {
            Log.e(TAG, "getOdometer exception:" + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    public String getCanBoxVersion() {
        if (this.mEmulator || !isConnected) {
            return null;
        }
        try {
            return this.mService.getCanBoxVersion();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return null;
        } catch (Exception ex) {
            Log.e(TAG, "getCanBoxVersion exception:" + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    public DoorStatus getDoorStatus() {
        if (this.mEmulator || !isConnected) {
            return null;
        }
        try {
            return this.mService.getDoorStatus();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return null;
        } catch (Exception ex) {
            Log.e(TAG, "getDoorStatus exception:" + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    public LightStatus getLightStatus() {
        if (this.mEmulator || !isConnected) {
            return null;
        }
        try {
            return this.mService.getLightStatus();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return null;
        } catch (Exception ex) {
            Log.e(TAG, "getLightStatus exception:" + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    public GearState getGearStatus() {
        if (this.mEmulator) {
            return GearState.Unknown;
        }
        if (!isConnected) {
            return GearState.Unknown;
        }
        try {
            return this.mService.getGearStatus();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return GearState.Unknown;
        } catch (Exception ex) {
            Log.e(TAG, "getGearStatus exception:" + ex.getMessage());
            ex.printStackTrace();
            return GearState.Unknown;
        }
    }

    public int getAmbientTemperatureValue() {
        if (this.mEmulator) {
            return Integer.MIN_VALUE;
        }
        if (!isConnected) {
            return -1;
        }
        try {
            return this.mService.getAmbientTemperature();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return Integer.MIN_VALUE;
        } catch (Exception ex) {
            Log.e(TAG, "getAmbientTemperatureValue exception:" + ex.getMessage());
            ex.printStackTrace();
            return Integer.MIN_VALUE;
        }
    }

    public WheelSpeed getWheelSpeed() {
        if (this.mEmulator || !isConnected) {
            return null;
        }
        try {
            return this.mService.getWheelSpeed();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return null;
        } catch (Exception ex) {
            Log.e(TAG, "getWheelSpeed exception:" + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    public FuelLevel getFuelLevelValue() {
        if (this.mEmulator || !isConnected) {
            return null;
        }
        try {
            return this.mService.getFuelLevel();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return null;
        } catch (Exception ex) {
            Log.e(TAG, "getFuelLevelValue exception:" + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    public PowerLevel getPowerLevelValue() {
        if (this.mEmulator || !isConnected) {
            return null;
        }
        try {
            return this.mService.getPowerlLevel();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return null;
        } catch (Exception ex) {
            Log.e(TAG, "getFuelLevelValue exception:" + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    public WheelCount getWheelCount() {
        if (this.mEmulator || !isConnected) {
            return null;
        }
        try {
            return this.mService.getWheelCount();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return null;
        } catch (Exception ex) {
            Log.e(TAG, "getWheelCount exception:" + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    public int getEngineTemperature() {
        if (this.mEmulator) {
            return Integer.MIN_VALUE;
        }
        if (!isConnected) {
            return -1;
        }
        try {
            return this.mService.getEngineTemperature();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return Integer.MIN_VALUE;
        } catch (Exception ex) {
            Log.e(TAG, "getEngineTemperature exception:" + ex.getMessage());
            ex.printStackTrace();
            return Integer.MIN_VALUE;
        }
    }

    public BrakePadStatus getBrakePadStatus() {
        if (this.mEmulator || !isConnected) {
            return null;
        }
        try {
            return this.mService.getBrakePadStatus();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return null;
        } catch (Exception ex) {
            Log.e(TAG, "getBrakePadStatus exception:" + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    public int getBrakeFluidStatus() {
        if (this.mEmulator) {
            return Integer.MIN_VALUE;
        }
        if (!isConnected) {
            return -1;
        }
        try {
            return this.mService.getBrakeFluidStatus();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return Integer.MIN_VALUE;
        } catch (Exception ex) {
            Log.e(TAG, "getBrakeFluidStatus exception:" + ex.getMessage());
            ex.printStackTrace();
            return Integer.MIN_VALUE;
        }
    }

    public int getWiperFluidStatus() {
        if (this.mEmulator) {
            return Integer.MIN_VALUE;
        }
        if (!isConnected) {
            return -1;
        }
        try {
            return this.mService.getWiperFluidStatus();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return Integer.MIN_VALUE;
        } catch (Exception ex) {
            Log.e(TAG, "getWiperFluidStatus exception:" + ex.getMessage());
            ex.printStackTrace();
            return Integer.MIN_VALUE;
        }
    }

    public int getComprehensiveFuelConsumption() {
        if (this.mEmulator) {
            return Integer.MIN_VALUE;
        }
        if (!isConnected) {
            return -1;
        }
        try {
            return this.mService.getComprehensiveFuelConsumption();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return Integer.MIN_VALUE;
        } catch (Exception ex) {
            Log.e(TAG, "getComprehensiveFuelConsumption exception:" + ex.getMessage());
            ex.printStackTrace();
            return Integer.MIN_VALUE;
        }
    }

    public float getComprehensiveFuelConsumptionEX() {
        if (this.mEmulator) {
            return Float.MIN_VALUE;
        }
        if (!isConnected) {
            return -1.0f;
        }
        try {
            return this.mService.getComprehensiveFuelConsumptionEX();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return Float.MIN_VALUE;
        } catch (Exception ex) {
            Log.e(TAG, "getComprehensiveFuelConsumption exception:" + ex.getMessage());
            ex.printStackTrace();
            return Float.MIN_VALUE;
        }
    }

    public int getInstantFuelConsumption() {
        if (this.mEmulator) {
            return Integer.MIN_VALUE;
        }
        if (!isConnected) {
            return -1;
        }
        try {
            return this.mService.getInstantFuelConsumption();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return Integer.MIN_VALUE;
        } catch (Exception ex) {
            Log.e(TAG, "getInstantFuelConsumption exception:" + ex.getMessage());
            ex.printStackTrace();
            return Integer.MIN_VALUE;
        }
    }

    public int getFuelConsumption() {
        return getComprehensiveFuelConsumption();
    }

    public int getEngineStatus() {
        if (this.mEmulator) {
            return Integer.MIN_VALUE;
        }
        if (!isConnected) {
            return -1;
        }
        try {
            return this.mService.getEngineStatus();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return Integer.MIN_VALUE;
        } catch (Exception ex) {
            Log.e(TAG, "getEngineStatus exception:" + ex.getMessage());
            ex.printStackTrace();
            return Integer.MIN_VALUE;
        }
    }

    public BatteryState getBatteryState() {
        if (this.mEmulator || !isConnected) {
            return null;
        }
        try {
            return this.mService.getBatteryState();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return null;
        } catch (Exception ex) {
            Log.e(TAG, "getBatteryState exception:" + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    public void queryVehicleState() {
        if (this.mEmulator || !isConnected) {
            return;
        }
        try {
            this.mService.queryVehicleState();
        } catch (RemoteException e) {
            Log.e(TAG, "queryVehicleState died, relaunch!");
        } catch (Exception ex) {
            Log.e(TAG, "queryVehicleState exception:" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public int getHevSysMode() {
        if (this.mEmulator || !isConnected) {
            return -1;
        }
        try {
            return this.mService.getHevSysMode();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return -1;
        } catch (Exception ex) {
            Log.e(TAG, "getHevSysMode exception:" + ex.getMessage());
            ex.printStackTrace();
            return -1;
        }
    }

    public SeatBelt getSeatBeltStatus() {
        if (this.mEmulator || !isConnected) {
            return null;
        }
        try {
            return this.mService.getSeatBeltStatus();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return null;
        } catch (Exception ex) {
            Log.e(TAG, "getSeatBeltStatus exception:" + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    public int getIlluminationValue() {
        if (this.mEmulator) {
            return Integer.MIN_VALUE;
        }
        if (!isConnected) {
            return -1;
        }
        try {
            return this.mService.getIllumination();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return Integer.MIN_VALUE;
        } catch (Exception ex) {
            Log.e(TAG, "getIlluminationValue exception:" + ex.getMessage());
            ex.printStackTrace();
            return Integer.MIN_VALUE;
        }
    }

    public VehicleIO getVehicleIO() {
        if (this.mEmulator || !isConnected) {
            return null;
        }
        try {
            return this.mService.getVehicleIO();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return null;
        } catch (Exception ex) {
            Log.e(TAG, "getVehicleIO exception:" + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    public long getSecondaryOdometerLong() {
        if (this.mEmulator) {
            return ODOMETER_UNKNOWN;
        }
        if (!isConnected) {
            return -1L;
        }
        try {
            return this.mService.getSecondaryOdometer();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return ODOMETER_UNKNOWN;
        } catch (Exception ex) {
            Log.e(TAG, "getSecondaryOdometerLong exception:" + ex.getMessage());
            ex.printStackTrace();
            return ODOMETER_UNKNOWN;
        }
    }

    public int getSecondaryOdometer() {
        return (int) getSecondaryOdometerLong();
    }

    public int getVehicleKeyStatus() {
        if (this.mEmulator) {
            return Integer.MIN_VALUE;
        }
        if (!isConnected) {
            return -1;
        }
        try {
            return this.mService.getVehicleKeyState();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return Integer.MIN_VALUE;
        } catch (Exception ex) {
            Log.e(TAG, "getVehicleKeyStatus exception:" + ex.getMessage());
            ex.printStackTrace();
            return Integer.MIN_VALUE;
        }
    }

    public int getVehicleSpeed() {
        if (this.mEmulator) {
            return Integer.MIN_VALUE;
        }
        if (!isConnected) {
            return -1;
        }
        try {
            return this.mService.getVehicleSpeed();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return Integer.MIN_VALUE;
        } catch (Exception ex) {
            Log.e(TAG, "getVehicleSpeed exception:" + ex.getMessage());
            ex.printStackTrace();
            return Integer.MIN_VALUE;
        }
    }

    public int getEngineSpeed() {
        if (this.mEmulator) {
            return Integer.MIN_VALUE;
        }
        if (!isConnected) {
            return -1;
        }
        try {
            return this.mService.getEngineSpeed();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return Integer.MIN_VALUE;
        } catch (Exception ex) {
            Log.e(TAG, "getEngineSpeed exception:" + ex.getMessage());
            ex.printStackTrace();
            return Integer.MIN_VALUE;
        }
    }

    public AirCondition getAirCondition() {
        if (this.mEmulator || !isConnected) {
            return null;
        }
        try {
            return this.mService.getAirCondition();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return null;
        } catch (Exception ex) {
            Log.e(TAG, "getAirCondition exception:" + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    public int setAirConditionState(AirConditionState airCondition, int state) {
        if (this.mEmulator || !isConnected) {
            return -1;
        }
        try {
            this.mService.setAirConditionState(airCondition, state);
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return -1;
        } catch (Exception ex) {
            Log.e(TAG, "setAirConditionState exception:" + ex.getMessage());
            ex.printStackTrace();
            return -1;
        }
    }

    public int setAirConditionBundleState(Bundle bundle) {
        if (this.mEmulator || !isConnected) {
            return -1;
        }
        try {
            this.mService.setAirConditionBundleState(bundle);
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return -1;
        } catch (Exception ex) {
            Log.e(TAG, "setAirConditionBundleState exception:" + ex.getMessage());
            ex.printStackTrace();
            return -1;
        }
    }

    public int setAirConditionStateEx(AirConditionState airCondition, float state) {
        if (this.mEmulator || !isConnected) {
            return -1;
        }
        try {
            this.mService.setAirConditionState(airCondition, (int) (10.0f * state));
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return -1;
        } catch (Exception ex) {
            Log.e(TAG, "setAirConditionState exception:" + ex.getMessage());
            ex.printStackTrace();
            return -1;
        }
    }

    public int getAirConditionState(AirConditionState airCondition) {
        if (this.mEmulator || !isConnected) {
            return -1;
        }
        try {
            int state = this.mService.getAirConditionState(airCondition);
            return state;
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return -1;
        } catch (Exception ex) {
            Log.e(TAG, "getAirConditionState exception:" + ex.getMessage());
            ex.printStackTrace();
            return -1;
        }
    }

    public RadarData getFrontRadarData() {
        if (this.mEmulator || !isConnected) {
            return null;
        }
        try {
            return this.mService.getFrontRadarData();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return null;
        } catch (Exception ex) {
            Log.e(TAG, "getFrontRadarData exception:" + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    public RadarData getRearRadarData() {
        if (this.mEmulator || !isConnected) {
            return null;
        }
        try {
            return this.mService.getRearRadarData();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return null;
        } catch (Exception ex) {
            Log.e(TAG, "getRearRadarData exception:" + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    public RadarData getLeftRadarData() {
        if (this.mEmulator || !isConnected) {
            return null;
        }
        try {
            return this.mService.getLeftRadarData();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return null;
        } catch (Exception ex) {
            Log.e(TAG, "getLeftRadarData exception:" + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    public RadarData getRightRadarData() {
        if (this.mEmulator || !isConnected) {
            return null;
        }
        try {
            return this.mService.getRightRadarData();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return null;
        } catch (Exception ex) {
            Log.e(TAG, "getRightRadarData exception:" + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    public int getReTracking() {
        if (this.mEmulator) {
            return Integer.MIN_VALUE;
        }
        if (!isConnected) {
            return -1;
        }
        try {
            return this.mService.getReTracking();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return Integer.MIN_VALUE;
        } catch (Exception ex) {
            Log.e(TAG, "getReTracking exception:" + ex.getMessage());
            ex.printStackTrace();
            return Integer.MIN_VALUE;
        }
    }

    public int getHandBrakeStatus() {
        if (this.mEmulator) {
            return Integer.MIN_VALUE;
        }
        if (!isConnected) {
            return -1;
        }
        try {
            return this.mService.getHandBrakeStatus();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return Integer.MIN_VALUE;
        } catch (Exception ex) {
            Log.e(TAG, "getHandBrakeStatus exception:" + ex.getMessage());
            ex.printStackTrace();
            return Integer.MIN_VALUE;
        }
    }

    public int getEngineFluidStatus() {
        if (this.mEmulator) {
            return Integer.MIN_VALUE;
        }
        if (!isConnected) {
            return -1;
        }
        try {
            return this.mService.getEngineFluidStatus();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return Integer.MIN_VALUE;
        } catch (Exception ex) {
            Log.e(TAG, "getEngineFluidStatus exception:" + ex.getMessage());
            ex.printStackTrace();
            return Integer.MIN_VALUE;
        }
    }

    public int getAlarmData() {
        if (this.mEmulator) {
            return Integer.MIN_VALUE;
        }
        if (!isConnected) {
            return -1;
        }
        try {
            return this.mService.getAlarmData();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return Integer.MIN_VALUE;
        } catch (Exception ex) {
            Log.e(TAG, "getAlarmData exception:" + ex.getMessage());
            ex.printStackTrace();
            return Integer.MIN_VALUE;
        }
    }

    public SWCAngle getSWCAngle() {
        if (this.mEmulator || !isConnected) {
            return null;
        }
        try {
            return this.mService.getSWCAngle();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return null;
        } catch (Exception ex) {
            Log.e(TAG, "getSWCAngle exception:" + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    public int getAccStatus() {
        if (this.mEmulator) {
            return Integer.MIN_VALUE;
        }
        if (!isConnected) {
            return -1;
        }
        try {
            return this.mService.getAccStatus();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return Integer.MIN_VALUE;
        } catch (Exception ex) {
            Log.e(TAG, "getAccStatus exception:" + ex.getMessage());
            ex.printStackTrace();
            return Integer.MIN_VALUE;
        }
    }

    public WindowStatus getWindowStatus() {
        if (this.mEmulator || !isConnected) {
            return null;
        }
        try {
            return this.mService.getWindowStatus();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return null;
        } catch (Exception ex) {
            Log.e(TAG, "getWindowStatus exception:" + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    public int updateMediaSrcInfo(MediaSrcInfo info) {
        Log.d(TAG, "updateMediaSrcInfo:" + info);
        if (this.mEmulator) {
            return 0;
        }
        if (!isConnected) {
            return -1;
        }
        try {
            this.mService.updateMediaSrcInfo(info);
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return -1;
        } catch (Exception ex) {
            Log.e(TAG, "updateMediaSrcInfo exception:" + ex.getMessage());
            ex.printStackTrace();
            return -1;
        }
    }

    public int updateMediaPlayInfo(MediaPlayInfo info) {
        Log.d(TAG, "updateMediaPlayInfo:" + info);
        if (this.mEmulator) {
            return 0;
        }
        if (!isConnected) {
            return -1;
        }
        try {
            this.mService.updateMediaPlayInfo(info);
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return -1;
        } catch (Exception ex) {
            Log.e(TAG, "updateMediaPlayInfo exception:" + ex.getMessage());
            ex.printStackTrace();
            return -1;
        }
    }

    public int updateMediaPlayProgressInfo(MediaPlayProgressInfo info) {
        Log.d(TAG, "updateMediaPlayInfo:" + info);
        if (this.mEmulator) {
            return 0;
        }
        if (!isConnected) {
            return -1;
        }
        try {
            this.mService.updateMediaPlayProgressInfo(info);
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return -1;
        } catch (Exception ex) {
            Log.e(TAG, "updateMediaPlayInfo exception:" + ex.getMessage());
            ex.printStackTrace();
            return -1;
        }
    }

    public int updateMediaSeqType(int seqtype) {
        Log.d(TAG, "updateMediaSeqType:" + seqtype);
        if (this.mEmulator) {
            return 0;
        }
        if (!isConnected) {
            return -1;
        }
        try {
            this.mService.updateMediaSeqType(seqtype);
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return -1;
        } catch (Exception ex) {
            Log.e(TAG, "updateMediaPlayInfo exception:" + ex.getMessage());
            ex.printStackTrace();
            return -1;
        }
    }

    public int updateMediaPlayMode(MediaPlayMode mode) {
        Log.d(TAG, "updateMediaPlayMode:" + mode);
        if (this.mEmulator) {
            return 0;
        }
        if (!isConnected) {
            return -1;
        }
        try {
            this.mService.updateMediaPlayMode(mode);
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return -1;
        } catch (Exception ex) {
            Log.e(TAG, "updateMediaPlayMode exception:" + ex.getMessage());
            ex.printStackTrace();
            return -1;
        }
    }

    public int updateMediaVolume(int volume) {
        Log.d(TAG, "updateMediaVolume:" + volume);
        if (this.mEmulator) {
            return 0;
        }
        if (!isConnected) {
            return -1;
        }
        try {
            this.mService.updateMediaVolume(volume);
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return -1;
        } catch (Exception ex) {
            Log.e(TAG, "updateMediaVolume exception:" + ex.getMessage());
            ex.printStackTrace();
            return -1;
        }
    }

    public int updateNaviInfo(NaviInfo info) {
        if (this.mEmulator) {
            return 0;
        }
        if (!isConnected) {
            return -1;
        }
        try {
            this.mService.updateNaviInfo(info);
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return -1;
        } catch (Exception ex) {
            Log.e(TAG, "updateNaviInfo exception:" + ex.getMessage());
            ex.printStackTrace();
            return -1;
        }
    }

    public void updateNaviLaneInfo(boolean showLane, Bundle laneData) {
        if (this.mEmulator || !isConnected) {
            return;
        }
        try {
            this.mService.updateNaviLaneInfo(showLane, laneData);
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
        } catch (Exception ex) {
            Log.e(TAG, "updateNaviLaneInfo exception:" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void updateCallListInfo(Bundle callData) {
        if (this.mEmulator || !isConnected) {
            return;
        }
        try {
            this.mService.updateCallListInfo(callData);
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
        } catch (Exception ex) {
            Log.e(TAG, "updateNaviLaneInfo exception:" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public int updatePhoneInfo(PhoneInfo info) {
        Log.d(TAG, "updatePhoneInfo:" + info);
        if (this.mEmulator) {
            return 0;
        }
        if (!isConnected) {
            return -1;
        }
        try {
            this.mService.updatePhoneInfo(info);
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return -1;
        } catch (Exception ex) {
            Log.e(TAG, "updatePhoneInfo exception:" + ex.getMessage());
            ex.printStackTrace();
            return -1;
        }
    }

    public int getVehicleState(VehicleState vehicle) {
        if (this.mEmulator || !isConnected) {
            return -1;
        }
        try {
            int state = this.mService.getVehicleState(vehicle);
            return state;
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return -1;
        } catch (Exception ex) {
            Log.e(TAG, "getVehicleState exception:" + ex.getMessage());
            ex.printStackTrace();
            return -1;
        }
    }

    public int setVehicleState(VehicleState vehicle, int state) {
        if (this.mEmulator || !isConnected) {
            return -1;
        }
        try {
            this.mService.setVehicleState(vehicle, state);
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return -1;
        } catch (Exception ex) {
            Log.e(TAG, "setVehicleState exception:" + ex.getMessage());
            ex.printStackTrace();
            return -1;
        }
    }

    public int setCanRawData(int canId, int[] data) {
        if (this.mEmulator || !isConnected) {
            return -1;
        }
        try {
            this.mService.setCanRawData(canId, data);
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return -1;
        } catch (Exception ex) {
            Log.e(TAG, "setCanRawData exception:" + ex.getMessage());
            ex.printStackTrace();
            return -1;
        }
    }

    public int setVehicleBundleState(Bundle bundle) {
        if (this.mEmulator || !isConnected) {
            return -1;
        }
        try {
            this.mService.setVehicleBundleState(bundle);
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return -1;
        } catch (Exception ex) {
            Log.e(TAG, "setVehicleBundleState exception:" + ex.getMessage());
            ex.printStackTrace();
            return -1;
        }
    }

    public int getVehicleCenterControlEnabled(VehicleCenterControlEnabled vehicle) {
        if (this.mEmulator || !isConnected) {
            return -1;
        }
        try {
            int state = this.mService.getVehicleCenterControlEnabled(vehicle);
            return state;
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return -1;
        } catch (Exception ex) {
            Log.e(TAG, "getVehicleCenterControlEnabled exception:" + ex.getMessage());
            ex.printStackTrace();
            return -1;
        }
    }

    public int requestVehicleCenterControlEnabledInfo() {
        if (this.mEmulator || !isConnected) {
            return -1;
        }
        try {
            return this.mService.requestVehicleCenterControlEnabledInfo();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return -1;
        } catch (Exception ex) {
            Log.e(TAG, "requestVehicleCenterControlEnabledInfo exception:" + ex.getMessage());
            ex.printStackTrace();
            return -1;
        }
    }

    public int requestTravellingInfo(TravellingInfoType type) {
        if (this.mEmulator || !isConnected) {
            return -1;
        }
        try {
            return this.mService.requestTravellingInfo(type);
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return -1;
        } catch (Exception ex) {
            Log.e(TAG, "requestTravellingInfo exception:" + ex.getMessage());
            ex.printStackTrace();
            return -1;
        }
    }

    public boolean isSupportFunction(FunctionType type) {
        if (this.mEmulator || !isConnected) {
            return false;
        }
        try {
            return this.mService.isSupportFunction(type);
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return false;
        } catch (Exception ex) {
            Log.e(TAG, "isSupportFunction exception:" + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    public int setGearBoxType(GearBoxType type) {
        if (this.mEmulator || !isConnected) {
            return -1;
        }
        try {
            this.mService.setGearBoxType(type);
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return -1;
        } catch (Exception ex) {
            Log.e(TAG, "setGearBoxType exception:" + ex.getMessage());
            ex.printStackTrace();
            return -1;
        }
    }

    public void setFactoryModeState(VehicleStateAutoTest vehicle, String result, int length) {
        if (this.mEmulator || !isConnected) {
            return;
        }
        try {
            this.mService.setFactoryModeState(vehicle, result, length);
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
        }
    }

    public void setFactoryModeResult(int length, String factoryResult) {
        if (this.mEmulator || !isConnected) {
            return;
        }
        try {
            this.mService.setFactoryModeResult(length, factoryResult);
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
        }
    }

    public TPMSInfo getTPMSInfo() throws RemoteException {
        if (this.mEmulator || !isConnected) {
            return null;
        }
        try {
            TPMSInfo tmInfo = this.mService.getTPMSInfo();
            return tmInfo;
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return null;
        } catch (Exception ex) {
            Log.e(TAG, "getVehicleState exception:" + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    public DVRState getDVRState() {
        if (this.mEmulator || !isConnected) {
            return null;
        }
        try {
            DVRState dvrState = this.mService.getDVRState();
            return dvrState;
        } catch (RemoteException e) {
            Log.e(TAG, "getDVRState died, relaunch!");
            return null;
        } catch (Exception ex) {
            Log.e(TAG, "getDVRState exception:" + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    public float getBatteryRemainingCapacity() {
        if (this.mEmulator || !isConnected) {
            return -1.0f;
        }
        try {
            float ret = this.mService.getBatteryRemainingCapacity();
            return ret;
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return -1.0f;
        } catch (Exception ex) {
            Log.e(TAG, "getVehicleState exception:" + ex.getMessage());
            ex.printStackTrace();
            return -1.0f;
        }
    }

    public int getRainfallLevel() {
        if (this.mEmulator || !isConnected) {
            return -1;
        }
        try {
            int level = this.mService.getRainfallLevel();
            return level;
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return -1;
        } catch (Exception ex) {
            Log.e(TAG, "getRainfallLevel exception:" + ex.getMessage());
            ex.printStackTrace();
            return -1;
        }
    }

    public boolean setVehicleCenterControlEnabled(VehicleCenterControlEnabled vehicleCenterControlEnabled, int state) throws RemoteException {
        if (this.mEmulator || !isConnected) {
            return false;
        }
        try {
            this.mService.setVehicleCenterControlEnabled(vehicleCenterControlEnabled, state);
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
        }
        return false;
    }

    class GetInstanceHandler extends Handler {
        public GetInstanceHandler() {
        }

        public GetInstanceHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            OnInitListener listener = (OnInitListener) msg.obj;
            if (listener != null) {
                if (CanBusManager.isConnected) {
                    listener.onConnectStatusChange(true);
                }
                CanBusManager.mGetIntanceList.add(listener);
            }
        }
    }

    public static float getAirConditionMaxTemp() {
        int vehicleId = VehicleHelper.getVehicleTypeID();
        if (vehicleId == 37) {
            return 32.0f;
        }
        if (vehicleId == 41) {
            return 33.0f;
        }
        if (vehicleId != 40) {
            return 1.0f;
        }
        return 32.0f;
    }

    public static float getAirConditionMixTemp() {
        int vehicleId = VehicleHelper.getVehicleTypeID();
        if (vehicleId == 37) {
            return 16.0f;
        }
        if (vehicleId == 41) {
            return 17.0f;
        }
        if (vehicleId != 40) {
            return 1.0f;
        }
        return 16.0f;
    }

    public static int getAirConditionMaxBlowerLevel() {
        int vehicleId = VehicleHelper.getVehicleTypeID();
        if (vehicleId != 37 && vehicleId != 41 && vehicleId != 40) {
            return 1;
        }
        return 8;
    }

    public static int getAirConditionMixBlowerLevel() {
        int vehicleId = VehicleHelper.getVehicleTypeID();
        if (vehicleId == 37) {
            return 0;
        }
        if (vehicleId == 41 || vehicleId != 40) {
            return 1;
        }
        return 1;
    }

    public int getVehicleSceneMode() {
        if (this.mEmulator || !isConnected) {
            return -1;
        }
        try {
            int state = this.mService.getVehicleSceneMode();
            return state;
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return -1;
        } catch (Exception ex) {
            Log.e(TAG, "getVehicleState exception:" + ex.getMessage());
            ex.printStackTrace();
            return -1;
        }
    }

    public int setVehicleSceneMode(int mode) {
        if (this.mEmulator || !isConnected) {
            return -1;
        }
        try {
            this.mService.setVehicleSceneMode(mode);
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return -1;
        } catch (Exception ex) {
            Log.e(TAG, "setVehicleState exception:" + ex.getMessage());
            ex.printStackTrace();
            return -1;
        }
    }

    public RealityWarningInfo getRealityWarningInfo() {
        if (this.mEmulator || !isConnected) {
            return null;
        }
        try {
            return this.mService.getRealityWarningInfo();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return null;
        } catch (Exception ex) {
            Log.e(TAG, "getOdometer exception:" + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    public int setVehicleAndAirConditionBundleState(Bundle airBundle, Bundle vehicleBundle) {
        if (this.mEmulator || !isConnected) {
            return -1;
        }
        try {
            this.mService.setVehicleAndAirConditionBundleState(airBundle, vehicleBundle);
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return -1;
        } catch (Exception ex) {
            Log.e(TAG, "setVehicleAndAirConditionBundleState exception:" + ex.getMessage());
            ex.printStackTrace();
            return -1;
        }
    }

    public int[] getCanRawData(int canId) {
        if (this.mEmulator || !isConnected) {
            return null;
        }
        try {
            int[] data = this.mService.getCanRawData(canId);
            return data;
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return null;
        } catch (Exception ex) {
            Log.e(TAG, "getCanRawData exception:" + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    public CameraState getCameraState() {
        if (this.mEmulator || !isConnected) {
            return null;
        }
        try {
            return this.mService.getCameraState();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return null;
        } catch (Exception ex) {
            Log.e(TAG, "getCameraState exception:" + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    public EnergyConsumptionPercent getEnergyConsumptionPercent() {
        if (this.mEmulator || !isConnected) {
            return null;
        }
        try {
            return this.mService.getEnergyConsumptionPercent();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return null;
        } catch (Exception ex) {
            Log.e(TAG, "getEnergyConsumptionPercent exception:" + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    public EnergyConsumptionInfo getEnergyConsumptionInfo() {
        if (this.mEmulator || !isConnected) {
            return null;
        }
        try {
            return this.mService.getEnergyConsumptionInfo();
        } catch (RemoteException e) {
            Log.e(TAG, "CanBusManagerService died, relaunch!");
            return null;
        } catch (Exception ex) {
            Log.e(TAG, "getEnergyConsumptionInfo exception:" + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }
}
