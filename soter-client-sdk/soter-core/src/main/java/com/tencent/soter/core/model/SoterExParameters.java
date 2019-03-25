package com.tencent.soter.core.model;

/**
 * Device settings about biometrics.
 */
public class SoterExParameters {
    private static final String TAG = "SoterExParameters";

    private static final String SOTEREX_PROVIDER_CLASS_NAME = "com.tencent.soter.core.model.SoterExParameterProvider";

    private static SoterExParameters instance = null;

    private ISoterExParameters impl = null;

    private SoterExParameters() {
        try {
            impl = (ISoterExParameters) Class.forName(SOTEREX_PROVIDER_CLASS_NAME).getDeclaredMethod("getInstance").invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
            SLogger.printErrStackTrace(TAG, e, "soter: init ext param failed.");
        }
    }

    public static SoterExParameters getInstance() {
        if(instance == null) {
            synchronized (SoterExParameters.class) {
                if(instance == null) {
                    instance = new SoterExParameters();
                }
                return instance;
            }
        } else {
            return instance;
        }
    }

    public Object getParam(String key, Object defVal) {
        if (impl != null) {
            return impl.getParam(key, defVal);
        }
        return null;
    }

    public int getFingerprintType() {
        Object result = getParam(ISoterExParameters.FINGERPRINT_TYPE, ISoterExParameters.FINGERPRINT_TYPE_UNDEFINE);
        if (result instanceof Integer) {
            return (int) result;
        } else {
            return ISoterExParameters.FINGERPRINT_TYPE_UNDEFINE;
        }
    }

    public int[] getFingerprintHardwarePosition() {
        Object result = getParam(ISoterExParameters.FINGERPRINT_HARDWARE_POSITION, null);
        if (result instanceof int[]) {
            return (int[]) result;
        } else {
            return null;
        }
    }
}
