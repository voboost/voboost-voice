package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;

/* loaded from: classes.dex */
public final class DoorStatus implements Parcelable {
    public static final int CLOSED = 0;
    public static final Parcelable.Creator<DoorStatus> CREATOR = new Parcelable.Creator<DoorStatus>() { // from class: com.qinggan.canbus.DoorStatus.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public DoorStatus[] newArray(int size) {
            return new DoorStatus[size];
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public DoorStatus createFromParcel(Parcel source) {
            DoorStatus doorStatusValue = new DoorStatus();
            doorStatusValue.bonnetDoor = source.readInt();
            doorStatusValue.fLDoor = source.readInt();
            doorStatusValue.fRDoor = source.readInt();
            doorStatusValue.loadSpace = source.readInt();
            doorStatusValue.rLDoor = source.readInt();
            doorStatusValue.rRDoor = source.readInt();
            doorStatusValue.fLDoorLockStatus = source.readInt();
            doorStatusValue.fRDoorLockStatus = source.readInt();
            doorStatusValue.rLDoorLockStatus = source.readInt();
            doorStatusValue.rRDoorLockStatus = source.readInt();
            return doorStatusValue;
        }
    };
    public static final int DOOR_STATUS_CLOSED = 0;
    public static final int DOOR_STATUS_LOCK = 0;
    public static final int DOOR_STATUS_OPEN = 1;
    public static final int DOOR_STATUS_UNKNOWN = -1;
    public static final int DOOR_STATUS_UNLOCK = 1;
    public static final int OPEN = 1;
    public int bonnetDoor = -1;
    public int fLDoor = -1;
    public int fRDoor = -1;
    public int loadSpace = -1;
    public int rLDoor = -1;
    public int rRDoor = -1;
    public int fLDoorLockStatus = -1;
    public int fRDoorLockStatus = -1;
    public int rLDoorLockStatus = -1;
    public int rRDoorLockStatus = -1;

    public int getBonnetDoor() {
        return this.bonnetDoor;
    }

    public int getFLDoor() {
        return this.fLDoor;
    }

    public int getFRDoor() {
        return this.fRDoor;
    }

    public int getRLDoor() {
        return this.rLDoor;
    }

    public int getRRDoor() {
        return this.rRDoor;
    }

    public int getLoadSpace() {
        return this.loadSpace;
    }

    public void setBonnetDoor(int bonnetDoor) {
        this.bonnetDoor = bonnetDoor;
    }

    public void setFLDoor(int flDoor) {
        this.fLDoor = flDoor;
    }

    public void setFRDoor(int frDoor) {
        this.fRDoor = frDoor;
    }

    public void setRLDoor(int rlDoor) {
        this.rLDoor = rlDoor;
    }

    public void setRRDoor(int rrDoor) {
        this.rRDoor = rrDoor;
    }

    public int getfLDoorLockStatus() {
        return this.fLDoorLockStatus;
    }

    public void setfLDoorLockStatus(int fLDoorLkStatus) {
        this.fLDoorLockStatus = fLDoorLkStatus;
    }

    public int getfRDoorLockStatus() {
        return this.fRDoorLockStatus;
    }

    public void setfRDoorLockStatus(int fRDoorLockStatus) {
        this.fRDoorLockStatus = fRDoorLockStatus;
    }

    public int getrLDoorLockStatus() {
        return this.rLDoorLockStatus;
    }

    public void setrLDoorLockStatus(int rLDoorLockStatus) {
        this.rLDoorLockStatus = rLDoorLockStatus;
    }

    public int getrRDoorLockStatus() {
        return this.rRDoorLockStatus;
    }

    public void setrRDoorLockStatus(int rRDoorLockStatus) {
        this.rRDoorLockStatus = rRDoorLockStatus;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.bonnetDoor);
        dest.writeInt(this.fLDoor);
        dest.writeInt(this.fRDoor);
        dest.writeInt(this.loadSpace);
        dest.writeInt(this.rLDoor);
        dest.writeInt(this.rRDoor);
        dest.writeInt(this.fLDoorLockStatus);
        dest.writeInt(this.fRDoorLockStatus);
        dest.writeInt(this.rLDoorLockStatus);
        dest.writeInt(this.rRDoorLockStatus);
    }

    public String toString() {
        return "DoorStatus{bonnetDoor=" + this.bonnetDoor + ", fLDoor=" + this.fLDoor + ", fRDoor=" + this.fRDoor + ", loadSpace=" + this.loadSpace + ", rLDoor=" + this.rLDoor + ", rRDoor=" + this.rRDoor + ", fLDoorLockStatus=" + this.fLDoorLockStatus + ", fRDoorLockStatus=" + this.fRDoorLockStatus + ", rLDoorLockStatus=" + this.rLDoorLockStatus + ", rRDoorLockStatus=" + this.rRDoorLockStatus + '}';
    }
}
