package com.tencent.soter.soterserver;

import android.os.Parcel;
import android.os.Parcelable;

public class SoterSessionResult implements Parcelable {
    public long session;
    public int resultCode;

    public SoterSessionResult(){
        super();
    }

    protected SoterSessionResult(Parcel in) {
        session = in.readLong();
        resultCode = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(session);
        dest.writeInt(resultCode);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SoterSessionResult> CREATOR = new Creator<SoterSessionResult>() {
        @Override
        public SoterSessionResult createFromParcel(Parcel in) {
            return new SoterSessionResult(in);
        }

        @Override
        public SoterSessionResult[] newArray(int size) {
            return new SoterSessionResult[size];
        }
    };
}
