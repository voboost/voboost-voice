package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class XmPlayerException extends Exception implements Parcelable {
    public static final Parcelable.Creator<XmPlayerException> CREATOR = new Parcelable.Creator<XmPlayerException>() { // from class: com.ximalaya.ting.android.opensdk.player.service.XmPlayerException.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public XmPlayerException[] newArray(int size) {
            return new XmPlayerException[size];
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public XmPlayerException createFromParcel(Parcel source) {
            return new XmPlayerException(source);
        }
    };
    public static final int ERROR_NO_PLAY_URL = 612;
    private static final long serialVersionUID = 8102305468025663148L;
    private String mCause;
    private int mExtra;
    private int mWhat;

    public XmPlayerException() {
        this("");
    }

    public XmPlayerException(Parcel source) {
        this(source.readString());
        this.mWhat = source.readInt();
        this.mExtra = source.readInt();
    }

    public XmPlayerException(int what, int extra) {
        this("Player Status Exception, what = " + what + ", extra = " + extra);
        this.mWhat = what;
        this.mExtra = extra;
    }

    public XmPlayerException(String cause) {
        super(cause);
        this.mCause = cause;
    }

    @Override // java.lang.Throwable
    public String getMessage() {
        if (!TextUtils.isEmpty(this.mCause)) {
            return this.mCause;
        }
        return super.getMessage();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mCause);
        dest.writeInt(this.mWhat);
        dest.writeInt(this.mExtra);
    }

    public void readFromParcel(Parcel source) {
        this.mCause = source.readString();
        this.mWhat = source.readInt();
        this.mExtra = source.readInt();
    }

    @Override // java.lang.Throwable
    public String toString() {
        return "XmPlayerException{mWhat=" + this.mWhat + ", mExtra=" + this.mExtra + ", mCause='" + this.mCause + "'}";
    }
}
