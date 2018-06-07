// ISoterService.aidl
package com.qualcomm.qti.soterserver;

// Declare any non-default types here with import statements

import com.qualcomm.qti.soterserver.SoterExportResult;
import com.qualcomm.qti.soterserver.SoterSessionResult;
import com.qualcomm.qti.soterserver.SoterSignResult;
import com.qualcomm.qti.soterserver.SoterDeviceResult;

interface ISoterService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */

     int generateAppSecureKey(int uid);

     SoterExportResult getAppSecureKey(int uid);

     boolean hasAskAlready(int uid);

     int generateAuthKey(int uid, String kname);

     int removeAuthKey(int uid, String kname);

     SoterExportResult getAuthKey(int uid, String kname);

     int removeAllAuthKey(int uid);

     boolean hasAuthKey(int uid, String kname);

     SoterSessionResult initSigh(int uid, String kname, String challenge);

     SoterSignResult finishSign(long signSession) ;

     SoterDeviceResult getDeviceId() ;

     int getVersion();

     String aidlAuth();


 }
