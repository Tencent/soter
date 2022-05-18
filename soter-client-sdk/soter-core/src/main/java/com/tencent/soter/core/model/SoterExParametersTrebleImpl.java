package com.tencent.soter.core.model;

import android.support.annotation.NonNull;

import com.tencent.soter.core.model.ISoterExParameters;

public class SoterExParametersTrebleImpl implements ISoterExParameters {

    private static int fingerprintType = FINGERPRINT_TYPE_UNDEFINE;

    private static int[] fingerprintPosition = null;

    @Override
    public Object getParam(@NonNull String key, Object defVal) {
        synchronized (SoterExParametersTrebleImpl.class) {
            if (FINGERPRINT_TYPE.equals(key)) {
                return fingerprintType != FINGERPRINT_TYPE_UNDEFINE ? fingerprintType : defVal;
            } else if (FINGERPRINT_HARDWARE_POSITION.equals(key)) {
                return fingerprintPosition != null ? fingerprintPosition : defVal;
            }
            return null;
        }
    }

    public static void setParam(@NonNull String key, Object value) {
        synchronized (SoterExParametersTrebleImpl.class) {
            if (FINGERPRINT_TYPE.equals(key)) {
                fingerprintType = (int) value;
            } else if (FINGERPRINT_HARDWARE_POSITION.equals(key)) {
                fingerprintPosition = (int[]) value;
            }
        }
    }
}
