// src/main/java/com/qinggan/bus/QGBusEvent.java
package com.qinggan.bus;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Event class for QGBus IPC.
 * MUST match the system class in /system/framework/QGAPI.jar exactly.
 */
public class QGBusEvent implements Parcelable {

    // === Constants (как в системном классе) ===
    public static final int EVENT_LOW_PRIORITY = 0;
    public static final int EVENT_NORMAL_PRIORITY = 1;
    public static final int EVENT_HIGH_PRIORITY = 2;

    // === Public fields (как в декомпилированном классе) ===
    public String mEventType;
    public int mNeedCache;
    public String mSource;
    public String mDestination;
    public int mPriority;
    public Bundle mData;

    // === CREATOR (обязателен для Parcelable) ===
    public static final Creator<QGBusEvent> CREATOR = new Creator<QGBusEvent>() {
        @Override
        public QGBusEvent createFromParcel(Parcel in) {
            return new QGBusEvent(in);
        }

        @Override
        public QGBusEvent[] newArray(int size) {
            return new QGBusEvent[size];
        }
    };

    // === Конструкторы ===
    public QGBusEvent() {
        mEventType = null;
        mNeedCache = 0;
        mSource = null;
        mDestination = null;
        mPriority = EVENT_NORMAL_PRIORITY;
        mData = null;
    }

    protected QGBusEvent(Parcel in) {
        mEventType = in.readString();
        mNeedCache = in.readInt();
        mSource = in.readString();
        mDestination = in.readString();
        mPriority = in.readInt();
        mData = in.readBundle(getClass().getClassLoader());
    }

    // === Parcelable implementation ===
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Порядок записи должен ТОЧНО совпадать с конструктором из Parcel!
        dest.writeString(mEventType);
        dest.writeInt(mNeedCache);
        dest.writeString(mSource);
        dest.writeString(mDestination);
        dest.writeInt(mPriority);
        dest.writeBundle(mData);
    }

    // === Методы-обёртки (как в системном классе) ===
    // Обратите внимание: имена методов БЕЗ префикса 'm'!

    public void setDestination(String str) {
        this.mDestination = str;
    }

    public String getDestination() {
        return this.mDestination;
    }

    public String getSource() {
        return this.mSource;
    }

    // НЕТ setSource() в системном классе — только прямое присваивание полю!

    public String getEventType() {
        return this.mEventType;
    }

    public void setEventType(String str) {
        this.mEventType = str;
    }

    public void setSticky(boolean z) {
        this.mNeedCache = z ? 1 : 0;
    }

    public boolean isSticky() {
        return this.mNeedCache > 0;
    }

    public void setData(Bundle bundle) {
        this.mData = bundle;
    }

    public Bundle getData() {
        return this.mData;
    }
}