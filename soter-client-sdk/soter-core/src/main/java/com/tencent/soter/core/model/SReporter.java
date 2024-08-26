package com.tencent.soter.core.model;

/**
 * Created by qingcuilu on 2024/2/18.
 */
public class SReporter {
   private static final String TAG = "Soter.SReporter";
   private static ISoterReporter reporter = null;
   
   public static void setReporterImp(ISoterReporter reporterImp) {
      if (reporterImp == null) {
         throw new RuntimeException("logInstance can not be null");
      }
      reporter = reporterImp;
   }
   
   public static void reportError(int errCode, String errMsg) {
      if (reporter != null) {
         SLogger.i(TAG, "reporter errCode:%s errMsg:%s", errCode, errMsg);
         reporter.reportError(errCode, errMsg);
      }
   }
   
   public static void reportError(int errCode, String errMsg, Exception e) {
      if (reporter != null) {
         SLogger.i(TAG, "reporter errCode:%s errMsg:%s exception:%s", errCode, errMsg, e.getMessage());
         String msg = errMsg + " Exception: " + android.util.Log.getStackTraceString(e);
         reporter.reportError(errCode, msg);
      }
   }
}
