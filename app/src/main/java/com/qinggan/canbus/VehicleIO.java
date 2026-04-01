package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;

public final class VehicleIO implements Parcelable {
    public static final Parcelable.Creator<VehicleIO> CREATOR = new Parcelable.Creator<VehicleIO>() { // from class: com.qinggan.canbus.VehicleIO.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public VehicleIO[] newArray(int size) {
            return new VehicleIO[size];
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public VehicleIO createFromParcel(Parcel source) {
            VehicleIO VehicleIO = new VehicleIO();
            VehicleIO.parkDetectOn = source.readInt() == 1;
            VehicleIO.reverseDetectOn = source.readInt() == 1;
            VehicleIO.AVINDetectOn = source.readInt() == 1;
            VehicleIO.AVIDDetectOn = source.readInt() == 1;
            return VehicleIO;
        }
    };
    public boolean AVIDDetectOn;
    public boolean AVINDetectOn;
    public boolean parkDetectOn;
    public boolean reverseDetectOn;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.parkDetectOn ? 1 : 0);
        parcel.writeInt(this.reverseDetectOn ? 1 : 0);
        parcel.writeInt(this.AVINDetectOn ? 1 : 0);
        parcel.writeInt(this.AVIDDetectOn ? 1 : 0);
    }

    public String toString() {
        return "VehicleIO{parkDetectOn=" + this.parkDetectOn + ", reverseDetectOn=" + this.reverseDetectOn + ", AVINDetectOn=" + this.AVINDetectOn + ", AVIDDetectOn=" + this.AVIDDetectOn + '}';
    }
}
