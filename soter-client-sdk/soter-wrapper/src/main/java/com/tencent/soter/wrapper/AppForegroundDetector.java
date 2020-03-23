package com.tencent.soter.wrapper;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;

import com.tencent.soter.core.model.SLogger;
import com.tencent.soter.wrapper.wrap_task.SoterTaskThread;

/**
 * Created by tofuliu on 2020/03/03.
 *
 * A simple foreground detector to ensure soter service connectivity
 */
@RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
class AppForegroundDetector implements Application.ActivityLifecycleCallbacks {

    int foregroundActivityCount = 0;

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
        foregroundActivityCount++;
        if (foregroundActivityCount == 1) {
            SLogger.i("Soter.AppForegroundDetector", "app enter foreground, check the connection of the soter service");
            SoterTaskThread.getInstance().postToWorker(new Runnable() {
                @Override
                public void run() {
                    SoterWrapperApi.ensureConnection();
                }
            });
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        foregroundActivityCount--;
        if (foregroundActivityCount < 0) {
            foregroundActivityCount = 0;
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
