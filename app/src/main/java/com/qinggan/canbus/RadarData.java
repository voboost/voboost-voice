package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;

public final class RadarData implements Parcelable {
    private static final int GRAY = 1;
    private static final int RADAR_CLEAN_HINT = 13;
    private static final int RADAR_FUNCTION_EXCEPTION = 15;
    private static final int RED = 3;
    private static final int YELLOW = 2;
    int leftOrFrontDistance;
    int leftOrFrontDistanceLevel;
    int middleLeftOrFrontDistance;
    int middleLeftOrFrontDistanceLevel;
    int middleRightOrRearDistance;
    int middleRightOrRearDistanceLevel;
    int radarFunctionStatus;
    int radarShowDistanceColor;
    RadarType radarType;
    int recentObstaclesDistance;
    int rightOrRearDistance;
    int rightOrRearDistanceLevel;
    public static int RADAR_LEVEL_ACCESSIBILITY = 0;
    public static int RADAR_LEVEL_UNKNOWN = -1;
    public static int RADAR_DISTANCE_UNKNOWN = -1;
    public static int RADAR_LEVEL_MAX = 8;
    public static int RADAR_SHOW_DISTANCE_COLOR = -1;
    public static int RADAR_FUNCTION_STATUS = -1;
    public static final Parcelable.Creator<RadarData> CREATOR = new Parcelable.Creator<RadarData>() { // from class: com.qinggan.canbus.RadarData.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public RadarData[] newArray(int size) {
            return new RadarData[size];
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public RadarData createFromParcel(Parcel source) {
            RadarData radar = new RadarData();
            radar.radarType = (RadarType) source.readParcelable(RadarType.class.getClassLoader());
            radar.leftOrFrontDistanceLevel = source.readInt();
            radar.middleLeftOrFrontDistanceLevel = source.readInt();
            radar.middleRightOrRearDistanceLevel = source.readInt();
            radar.rightOrRearDistanceLevel = source.readInt();
            radar.leftOrFrontDistance = source.readInt();
            radar.middleLeftOrFrontDistance = source.readInt();
            radar.middleRightOrRearDistance = source.readInt();
            radar.rightOrRearDistance = source.readInt();
            radar.radarShowDistanceColor = source.readInt();
            radar.radarFunctionStatus = source.readInt();
            radar.recentObstaclesDistance = source.readInt();
            return radar;
        }
    };

    public RadarData() {
        int i = RADAR_LEVEL_UNKNOWN;
        this.leftOrFrontDistanceLevel = i;
        this.middleLeftOrFrontDistanceLevel = i;
        this.middleRightOrRearDistanceLevel = i;
        this.rightOrRearDistanceLevel = i;
        this.recentObstaclesDistance = -1;
        int i2 = RADAR_DISTANCE_UNKNOWN;
        this.leftOrFrontDistance = i2;
        this.middleLeftOrFrontDistance = i2;
        this.middleRightOrRearDistance = i2;
        this.rightOrRearDistance = i2;
        this.radarShowDistanceColor = -1;
        this.radarFunctionStatus = -1;
    }

    public RadarData(RadarType radarType) {
        int i = RADAR_LEVEL_UNKNOWN;
        this.leftOrFrontDistanceLevel = i;
        this.middleLeftOrFrontDistanceLevel = i;
        this.middleRightOrRearDistanceLevel = i;
        this.rightOrRearDistanceLevel = i;
        this.recentObstaclesDistance = -1;
        int i2 = RADAR_DISTANCE_UNKNOWN;
        this.leftOrFrontDistance = i2;
        this.middleLeftOrFrontDistance = i2;
        this.middleRightOrRearDistance = i2;
        this.rightOrRearDistance = i2;
        this.radarShowDistanceColor = -1;
        this.radarFunctionStatus = -1;
        this.radarType = radarType;
    }

    public RadarType getRadarType() {
        return this.radarType;
    }

    public void setRadarType(RadarType radarType) {
        this.radarType = radarType;
    }

    public int getLeftOrFrontDistanceLevel() {
        return this.leftOrFrontDistanceLevel;
    }

    public void setLeftOrFrontDistanceLevel(int leftOrFrontDistanceLevel) {
        this.leftOrFrontDistanceLevel = leftOrFrontDistanceLevel;
    }

    public int getMiddleLeftOrFrontDistanceLevel() {
        return this.middleLeftOrFrontDistanceLevel;
    }

    public void setMiddleLeftOrFrontDistanceLevel(int middleLeftOrFrontDistanceLevel) {
        this.middleLeftOrFrontDistanceLevel = middleLeftOrFrontDistanceLevel;
    }

    public int getMiddleRightOrRearDistanceLevel() {
        return this.middleRightOrRearDistanceLevel;
    }

    public void setMiddleRightOrRearDistanceLevel(int middleRightOrRearDistanceLevel) {
        this.middleRightOrRearDistanceLevel = middleRightOrRearDistanceLevel;
    }

    public int getRightOrRearDistanceLevel() {
        return this.rightOrRearDistanceLevel;
    }

    public void setRightOrRearDistanceLevel(int rightOrRearDistanceLevel) {
        this.rightOrRearDistanceLevel = rightOrRearDistanceLevel;
    }

    public int getLeftOrFrontDistance() {
        return this.leftOrFrontDistance;
    }

    public void setLeftOrFrontDistance(int leftOrFrontDistance) {
        this.leftOrFrontDistance = leftOrFrontDistance;
    }

    public int getMiddleLeftOrFrontDistance() {
        return this.middleLeftOrFrontDistance;
    }

    public void setMiddleLeftOrFrontDistance(int middleLeftOrFrontDistance) {
        this.middleLeftOrFrontDistance = middleLeftOrFrontDistance;
    }

    public int getMiddleRightOrRearDistance() {
        return this.middleRightOrRearDistance;
    }

    public void setMiddleRightOrRearDistance(int middleRightOrRearDistance) {
        this.middleRightOrRearDistance = middleRightOrRearDistance;
    }

    public int getRightOrRearDistance() {
        return this.rightOrRearDistance;
    }

    public void setRightOrRearDistance(int rightOrRearDistance) {
        this.rightOrRearDistance = rightOrRearDistance;
    }

    public int getRecentObstaclesDistance() {
        return this.recentObstaclesDistance;
    }

    public void setRecentObstaclesDistance(int recentObstaclesDistance) {
        this.recentObstaclesDistance = recentObstaclesDistance;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.radarType, flags);
        dest.writeInt(this.leftOrFrontDistanceLevel);
        dest.writeInt(this.middleLeftOrFrontDistanceLevel);
        dest.writeInt(this.middleRightOrRearDistanceLevel);
        dest.writeInt(this.rightOrRearDistanceLevel);
        dest.writeInt(this.leftOrFrontDistance);
        dest.writeInt(this.middleLeftOrFrontDistance);
        dest.writeInt(this.middleRightOrRearDistance);
        dest.writeInt(this.rightOrRearDistance);
        dest.writeInt(this.radarShowDistanceColor);
        dest.writeInt(this.radarFunctionStatus);
        dest.writeInt(this.recentObstaclesDistance);
    }

    public String toString() {
        return "RadarData{radarType=" + this.radarType + ", leftOrFrontDistanceLevel=" + this.leftOrFrontDistanceLevel + ", middleLeftOrFrontDistanceLevel=" + this.middleLeftOrFrontDistanceLevel + ", middleRightOrRearDistanceLevel=" + this.middleRightOrRearDistanceLevel + ", rightOrRearDistanceLevel=" + this.rightOrRearDistanceLevel + ", leftOrFrontDistance=" + this.leftOrFrontDistance + ", middleLeftOrFrontDistance=" + this.middleLeftOrFrontDistance + ", middleRightOrRearDistance=" + this.middleRightOrRearDistance + ", rightOrRearDistance=" + this.rightOrRearDistance + ", radarShowDistanceColor=" + this.radarShowDistanceColor + ", radarFunctionStatus=" + this.radarFunctionStatus + ", recentObstaclesDistance=" + this.recentObstaclesDistance + '}';
    }
}
