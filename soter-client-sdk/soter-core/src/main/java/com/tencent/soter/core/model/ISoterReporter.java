package com.tencent.soter.core.model;

/**
 * Created by qingcuilu on 2024/2/18.
 */
public interface ISoterReporter {
    void reportError(int errCode, String errMsg);
}
