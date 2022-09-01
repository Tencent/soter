package com.tencent.soter.core.model;

import android.support.annotation.NonNull;

/**
 * An interface that defines biometric parameters. The vendor implements this interface to provide biometrics parameters
 */
public interface ISoterExParameters {

    /**
     * The type of fingerprint, will return an int.
     * @see #FINGERPRINT_TYPE_NORMAL
     * @see #FINGERPRINT_TYPE_UNDER_SCREEN
     */
    String FINGERPRINT_TYPE = "fingerprint_type";

    /**
     * The position of the under screen fingerprint, will return an int array of length 4, representing left, top, right,
     * and bottom coordinate in the screen. like [50, 100, 100, 150]
     */
    String FINGERPRINT_HARDWARE_POSITION = "fingerprint_hardware_position";

    /**
     * Vendor undefine the type
     */
    int FINGERPRINT_TYPE_UNDEFINE = 0;
    /**
     * Normal fingerprint type
     */
    int FINGERPRINT_TYPE_NORMAL = 1;
    /**
     * Under screen fingerprint type
     */
    int FINGERPRINT_TYPE_UNDER_SCREEN = 2;

    Object getParam(@NonNull String key, Object defVal);

}
