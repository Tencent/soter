package com.tencent.soter.soterserver;

import android.os.Parcel;
import android.os.Parcelable;

public class SoterSignResult implements Parcelable {
    public int resultCode;
    public byte[] exportData;
    public int exportDataLength;

    public SoterSignResult(){
        super();
    }

    protected SoterSignResult(Parcel in) {
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

    public static final Creator<SoterSignResult> CREATOR = new Creator<SoterSignResult>() {
        @Override
        public SoterSignResult createFromParcel(Parcel in) {
            return new SoterSignResult(in);
        }

        @Override
        public SoterSignResult[] newArray(int size) {
            return new SoterSignResult[size];
        }
    };
}
