package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;

/* loaded from: classes.dex */
public final class NaviInfo implements Parcelable {
    public static final Parcelable.Creator<NaviInfo> CREATOR = new Parcelable.Creator<NaviInfo>() { // from class: com.qinggan.canbus.NaviInfo.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public NaviInfo[] newArray(int size) {
            return new NaviInfo[size];
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public NaviInfo createFromParcel(Parcel source) {
            NaviInfo info = new NaviInfo();
            info.guideState = (GuideState) source.readParcelable(GuideState.class.getClassLoader());
            info.calcState = (CalcState) source.readParcelable(CalcState.class.getClassLoader());
            info.mTurnByTurnInfo = (TurnByTurnIconInfo) source.readParcelable(TurnByTurnIconInfo.class.getClassLoader());
            info.directionId = source.readInt();
            info.heading = source.readInt();
            info.destDistance = source.readInt();
            info.nextDistance = source.readInt();
            info.remainTime = source.readInt();
            info.param = source.readInt();
            info.currentRoadName = source.readString();
            info.turnRoadName = source.readString();
            info.display = source.readInt() == 1;
            info.mRoundAboutAngle = source.readString();
            info.mRoundAboutNum = source.readInt();
            info.mRoundAboutOut = source.readInt();
            info.destName = source.readString();
            info.is_straight = source.readInt();
            info.tbtIconId = source.readInt();
            return info;
        }
    };
    private CalcState calcState;
    String currentRoadName;
    int destDistance;
    String destName;
    int directionId;
    boolean display = true;
    GuideState guideState;
    int heading;
    private int is_straight;
    private String mRoundAboutAngle;
    private int mRoundAboutNum;
    private int mRoundAboutOut;
    TurnByTurnIconInfo mTurnByTurnInfo;
    int nextDistance;
    int param;
    int remainTime;
    private int tbtIconId;
    String turnRoadName;

    public TurnByTurnIconInfo getTurnByTurnInfo() {
        return this.mTurnByTurnInfo;
    }

    public void setTurnByTurnInfo(TurnByTurnIconInfo turnByTurnInfo) {
        this.mTurnByTurnInfo = turnByTurnInfo;
    }

    public String getRoundAboutAngle() {
        return this.mRoundAboutAngle;
    }

    public void setRoundAboutAngle(String angle) {
        this.mRoundAboutAngle = angle;
    }

    public int getRoundAboutNum() {
        return this.mRoundAboutNum;
    }

    public void setRoundAboutNum(int num) {
        this.mRoundAboutNum = num;
    }

    public int getRoundAboutOut() {
        return this.mRoundAboutOut;
    }

    public void setRoundAboutOut(int num) {
        this.mRoundAboutOut = num;
    }

    public GuideState getGuideState() {
        return this.guideState;
    }

    public void setGuideState(GuideState guideState) {
        this.guideState = guideState;
    }

    public int getDirectionId() {
        return this.directionId;
    }

    public void setDirectionId(int directionId) {
        this.directionId = directionId;
    }

    public int getHeading() {
        return this.heading;
    }

    public void setHeading(int heading) {
        this.heading = heading;
    }

    public int getDestDistance() {
        return this.destDistance;
    }

    public void setDestDistance(int destDistance) {
        this.destDistance = destDistance;
    }

    public int getNextDistance() {
        return this.nextDistance;
    }

    public void setNextDistance(int nextDistance) {
        this.nextDistance = nextDistance;
    }

    public String getCurrentRoadName() {
        return this.currentRoadName;
    }

    public void setCurrentRoadName(String currentRoadName) {
        this.currentRoadName = currentRoadName;
    }

    public String getTurnRoadName() {
        return this.turnRoadName;
    }

    public void setTurnRoadName(String turnRoadName) {
        this.turnRoadName = turnRoadName;
    }

    public int getParam() {
        return this.param;
    }

    public void setParam(int param) {
        this.param = param;
    }

    public int getRemainTime() {
        return this.remainTime;
    }

    public void setRemainTime(int remainTime) {
        this.remainTime = remainTime;
    }

    public boolean isDisplay() {
        return this.display;
    }

    public void setDisplay(boolean display) {
        this.display = display;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(this.guideState, i);
        parcel.writeParcelable(this.calcState, i);
        parcel.writeParcelable(this.mTurnByTurnInfo, i);
        parcel.writeInt(this.directionId);
        parcel.writeInt(this.heading);
        parcel.writeInt(this.destDistance);
        parcel.writeInt(this.nextDistance);
        parcel.writeInt(this.remainTime);
        parcel.writeInt(this.param);
        parcel.writeString(this.currentRoadName);
        parcel.writeString(this.turnRoadName);
        parcel.writeInt(this.display ? 1 : 0);
        parcel.writeString(this.mRoundAboutAngle);
        parcel.writeInt(this.mRoundAboutNum);
        parcel.writeInt(this.mRoundAboutOut);
        parcel.writeString(this.destName);
        parcel.writeInt(this.is_straight);
        parcel.writeInt(this.tbtIconId);
    }

    public String toString() {
        return "NaviInfo{guideState=" + this.guideState + ", calcState=" + getCalcState() + ", directionId=" + this.directionId + ", heading=" + this.heading + ", destDistance=" + this.destDistance + ", nextDistance=" + this.nextDistance + ", remainTime=" + this.remainTime + ", param=" + this.param + ", currentRoadName='" + this.currentRoadName + "', turnRoadName='" + this.turnRoadName + "', display=" + this.display + ", mRoundAboutAngle='" + this.mRoundAboutAngle + "', mRoundAboutNum=" + this.mRoundAboutNum + ", mRoundAboutOut=" + this.mRoundAboutOut + ", mTurnByTurnInfo=" + this.mTurnByTurnInfo + ", destName=" + this.destName + ", is_straight=" + this.is_straight + ", tbtIconId=" + this.tbtIconId + '}';
    }

    public String getDestName() {
        return this.destName;
    }

    public void setDestName(String destName) {
        this.destName = destName;
    }

    public CalcState getCalcState() {
        return this.calcState;
    }

    public void setCalcState(CalcState calcState) {
        this.calcState = calcState;
    }

    public int isstraight() {
        return this.is_straight;
    }

    public void setstraight(int is_straight) {
        this.is_straight = is_straight;
    }

    public int getTbtIconId() {
        return this.tbtIconId;
    }

    public void setTbtIconId(int tbtIconId) {
        this.tbtIconId = tbtIconId;
    }
}
