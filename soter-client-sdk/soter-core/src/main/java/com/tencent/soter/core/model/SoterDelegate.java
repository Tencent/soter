package com.tencent.soter.core.model;

import android.support.annotation.NonNull;

import junit.framework.Assert;

/**
 * Created by henryye on 2018/1/10.
 */

public class SoterDelegate {
    private static final String TAG = "Soter.SoterDelegate";

    public interface ISoterDelegate {
        void onTriggeredOOM();
        boolean isTriggeredOOM();
    }

    @NonNull
    private static volatile ISoterDelegate sSoterDelegateImp = new ISoterDelegate() {
        private boolean isTriggeredOOM = false; // once triggered OOM, we regard it as no attk or error stack. mark as not native support.

        @Override
        public void onTriggeredOOM() {
            SLogger.e(TAG, "soter: triggered OOM. using default imp, just record the flag");
            this.isTriggeredOOM = true;
        }

        @Override
        public boolean isTriggeredOOM() {
            return isTriggeredOOM;
        }
    };

    public static void setImplement(@NonNull ISoterDelegate instance) {
        Assert.assertNotNull(instance);
        sSoterDelegateImp = instance;
    }

    public static void onTriggerOOM() {
        sSoterDelegateImp.onTriggeredOOM();
    }

    public static boolean isTriggeredOOM() {
        return sSoterDelegateImp.isTriggeredOOM();
    }
}
