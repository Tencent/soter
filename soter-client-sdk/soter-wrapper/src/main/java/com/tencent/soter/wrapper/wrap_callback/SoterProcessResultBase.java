/*
 * Tencent is pleased to support the open source community by making TENCENT SOTER available.
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * https://opensource.org/licenses/BSD-3-Clause
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package com.tencent.soter.wrapper.wrap_callback;

import com.tencent.soter.core.model.SoterCoreResult;
import com.tencent.soter.core.model.SoterCoreUtil;
import com.tencent.soter.wrapper.wrap_core.SoterProcessErrCode;

/**
 * Created by henryye on 2017/4/7.
 * The base of all the processes
 */

abstract public class SoterProcessResultBase<T> extends SoterCoreResult implements SoterProcessErrCode {

    private T extData = null;

    // only for create dummy instance
    SoterProcessResultBase() {
        super(ERR_UNKNOWN);
    }

    @SuppressWarnings("WeakerAccess")
    protected SoterProcessResultBase(int errCode, String errMsg) {
        this(errCode, errMsg, null);
    }

    @SuppressWarnings("WeakerAccess")
    protected SoterProcessResultBase(int errCode, String errMsg, T extData) {
        super(errCode, errMsg);
        switch (errCode) {
            case ERR_GET_SUPPORT_SOTER_REMOTE_FAILED:
                this.errMsg = "get support soter failed remotely";
                break;
            case ERR_UPLOAD_ASK_FAILED:
                this.errMsg = "upload app secure key";
                break;
            case ERR_UPLOAD_AUTH_KEY_FAILED:
                this.errMsg = "upload auth key failed";
                break;
            case ERR_NOT_INIT_WRAPPER:
                this.errMsg = "not initialized yet. please make sure you've already called SoterWrapperApi.init(...) and call backed";
                break;
            case ERR_CONTEXT_INSTANCE_NOT_EXISTS:
                this.errMsg = "context instance already released. should not happen normally, you can try to call again";
                break;
            case ERR_NO_FINGERPRINT_ENROLLED:
                this.errMsg = "there must be at least 1 fingerprint enrolled in system to complete this process. please check it previously";
                break;
            case ERR_GET_CHALLENGE:
                this.errMsg = "get challenge failed";
                break;
            case ERR_UPLOAD_OR_VERIFY_SIGNATURE_FAILED:
                this.errMsg = "upload or verify signature in server side failed";
                break;
        }
        if(!SoterCoreUtil.isNullOrNil(errMsg)) {
            this.errMsg = errMsg;
        }
        this.extData = extData;
    }

    @SuppressWarnings("WeakerAccess")
    protected SoterProcessResultBase(int errCode) {
        this(errCode, "", null);
    }

    @SuppressWarnings("WeakerAccess")
    protected SoterProcessResultBase(int errCode, T extData) {
        this(errCode, "", extData);
    }

    @SuppressWarnings("unused")
    public T getExtData() {
        return extData;
    }

    @Override
    public String toString() {
        if(getExtData() == null) {
            return super.toString();
        } else {
            return String.format("total: %s, extData: %s", super.toString(), getExtData().toString());
        }

    }
}
