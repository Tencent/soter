package com.tencent.soter.core.sotercore;

/**
 * Created by tofuliu on 2019/06/28.
 */
public interface SoterCoreTrebleServiceListener {

    void onStartServiceConnecting();

    void onServiceConnected();

    void onServiceDisconnected();

    void onServiceBinderDied();

    void onNoServiceWhenCalling();
}
