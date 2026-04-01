package com.qinggan.canbus.plugs;

import android.os.Parcel;
import android.os.Parcelable;

public final class IVideoConfiguration implements Parcelable {
    public static final Parcelable.Creator<IVideoConfiguration> CREATOR = new Parcelable.Creator<IVideoConfiguration>() { // from class: com.qinggan.qinglink.api.hu.IVideoConfiguration.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public IVideoConfiguration createFromParcel(Parcel in) {
            return new IVideoConfiguration(in);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public IVideoConfiguration[] newArray(int size) {
            return new IVideoConfiguration[size];
        }
    };
    public static final String DEFAULT_CONFIG_JSON = "--invalid--";
    public static final int DEFAULT_FPS = 30;
    public static final int DEFAULT_HEIGHT = 720;
    public static final int DEFAULT_IFI = 2;
    public static final int DEFAULT_MAX_BPS = 1500;
    public static final String DEFAULT_MIME = "video/avc";
    public static final int DEFAULT_MIN_BPS = 400;
    public static final int DEFAULT_WIDTH = 1920;
    public int fps;
    public int height;
    public int ifi;
    public String mConfigJson;
    public int maxBps;
    public String mime;
    public int minBps;
    public int width;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    private IVideoConfiguration(Parcel in) {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        this.height = in.readInt();
        this.width = in.readInt();
        this.minBps = in.readInt();
        this.maxBps = in.readInt();
        this.fps = in.readInt();
        this.ifi = in.readInt();
        this.mime = in.readString();
        this.mConfigJson = in.readString();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.height);
        out.writeInt(this.width);
        out.writeInt(this.minBps);
        out.writeInt(this.maxBps);
        out.writeInt(this.fps);
        out.writeInt(this.ifi);
        out.writeString(this.mime);
        out.writeString(this.mConfigJson);
    }

    public String toString() {
        return "IVideoConfiguration{height=" + this.height + ", width=" + this.width + ", minBps=" + this.minBps + ", maxBps=" + this.maxBps + ", fps=" + this.fps + ", ifi=" + this.ifi + ", mime='" + this.mime + ", mConfigJson='" + this.mConfigJson + "'}";
    }

    private IVideoConfiguration(Builder builder) {
        this.height = builder.height;
        this.width = builder.width;
        this.minBps = builder.minBps;
        this.maxBps = builder.maxBps;
        this.fps = builder.fps;
        this.ifi = builder.ifi;
        this.mime = builder.mime;
        this.mConfigJson = builder.mConfigJson;
    }

    public static IVideoConfiguration createDefault() {
        return new Builder().build();
    }

    public static class Builder {
        private int height = IVideoConfiguration.DEFAULT_HEIGHT;
        private int width = 1920;
        private int minBps = 400;
        private int maxBps = 1500;
        private int fps = 30;
        private int ifi = 2;
        private String mime = IVideoConfiguration.DEFAULT_MIME;
        private String mConfigJson = IVideoConfiguration.DEFAULT_CONFIG_JSON;

        public Builder setSize(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder setBps(int minBps, int maxBps) {
            this.minBps = minBps;
            this.maxBps = maxBps;
            return this;
        }

        public Builder setFps(int fps) {
            this.fps = fps;
            return this;
        }

        public Builder setIfi(int ifi) {
            this.ifi = ifi;
            return this;
        }

        public Builder setMime(String mime) {
            this.mime = mime;
            return this;
        }

        public Builder setConfigJson(String aConfigJson) {
            this.mConfigJson = aConfigJson;
            return this;
        }

        public IVideoConfiguration build() {
            return new IVideoConfiguration(this);
        }
    }
}
