package com.tencent.soter.soterserver;

import android.os.Parcel;
import android.os.Parcelable;

public class SoterDeviceResult implements Parcelable {
    public int resultCode;
    public byte[] exportData;
    public int exportDataLength;


    public SoterDeviceResult(){
        super();
    }

    protected SoterDeviceResult(Parcel in) {
        resultCode = in.readInt();
        exportData = in.createByteArray();
        exportDataLength = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(resultCode);
        dest.writeByteArray(exportData);
        dest.writeInt(exportDataLength);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SoterDeviceResult> CREATOR = new Creator<SoterDeviceResult>() {
        @Override
        public SoterDeviceResult createFromParcel(Parcel in) {
            return new SoterDeviceResult(in);
        }

        @Override
        public SoterDeviceResult[] newArray(int size) {
            return new SoterDeviceResult[size];
        }
    };
}

