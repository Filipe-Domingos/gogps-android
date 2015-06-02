package br.com.sd.go.models;

import android.support.annotation.NonNull;

import java.io.Serializable;

public class VehicleItem implements Comparable, Serializable {

    private Long mId;
    private String mName;
    private String mTime;
    private String speed;
    private String mExtraInfo = "";
    private Double mLongitude = 0.0;
    private Double mLatitude = 0.0;

    public VehicleItem(Long id, String name) {
        mId = id;
        mName = name;
    }

    public Long getId() {
        return mId;
    }

    public void setId(Long id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(String time) {
        mTime = time;
    }

    public void setExtraInfo(String extraInfo) {
        mExtraInfo = extraInfo;
    }

    public boolean getAcc() {
        return mExtraInfo.toLowerCase().contains("<acc>true</acc>");
    }

    public void setLongitude(Double longitude) {
        mLongitude = longitude;
    }

    public Double getLongitude() {
        return mLongitude;
    }

    public void setLatitude(Double latitude) {
        mLatitude = latitude;
    }

    public Double getLatitude() {
        return mLatitude;
    }

    @Override
    public int compareTo(@NonNull Object obj) {
        if (!(obj instanceof VehicleItem)) {
            return -1;
        }
        VehicleItem another = (VehicleItem) obj;
        return getName().compareTo(another.getName());
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }
}
