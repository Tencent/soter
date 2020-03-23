package com.tencent.soter.wrapper.wrap_core;

import com.tencent.soter.core.model.SLogger;
import com.tencent.soter.core.model.SoterCoreResult;

import java.util.HashMap;

/**
 * Created by tofuliu on 2019/06/27.
 */
public class RemoveASKStrategy {
    private static final String TAG = "Soter.RemoveASKStrategy";

    private static final int MAX_EXCEPTION_COUNT = 2;

    private static HashMap<Class, ErrorModel> exceptionMap = new HashMap<>();

    public static boolean shouldRemoveAllKey(Class clazz, SoterCoreResult result) {
        if (result.isSuccess()) {
            return false;
        }

        ErrorModel model = exceptionMap.get(clazz);
        if (model == null) {
            model = new ErrorModel();
            model.fillResult(result);
            exceptionMap.put(clazz, model);
        } else {
            if (result.getErrCode() == model.errCode && result.getErrMsg().equals(model.errMsg)) {
                model.counter++;
                if (model.counter >= MAX_EXCEPTION_COUNT) {
                    return true;
                }
            } else {
                model.fillResult(result);
            }
        }
        SLogger.d(TAG, "error counter: %s", model.counter);
        return false;
    }

    private static class ErrorModel {
        int counter;
        int errCode;
        String errMsg;

        public void fillResult(SoterCoreResult result) {
            errCode = result.errCode;
            errMsg = result.errMsg;
            this.counter = 1;
        }
    }
}
