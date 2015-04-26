package br.com.sd.go.models;

import android.support.annotation.NonNull;

public class VehicleItem implements Comparable {

    private Long mId;
    private String mName;
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

    @Override
    public int compareTo(@NonNull Object obj) {
        if (!(obj instanceof VehicleItem)) {
            return -1;
        }
        VehicleItem another = (VehicleItem) obj;
        return getName().compareTo(another.getName());
    }
}
