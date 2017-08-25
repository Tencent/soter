/*
 * Tencent is pleased to support the open source community by making TENCENT SOTER available.
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * https://opensource.org/licenses/BSD-3-Clause
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package com.tencent.soter.core.model;

/**
 * Created by henryye on 2017/4/6.
 *
 * The result of single SOTER core operation. The {@link SoterCoreResult#errCode} is defined in {@link SoterErrCode}
 */

@SuppressWarnings("unused")
public class SoterCoreResult implements SoterErrCode {
    public int errCode;
    public String errMsg;

    public SoterCoreResult(int errCode, String errMsg) {
        this(errCode);
        if(!SoterCoreUtil.isNullOrNil(errMsg)) {
            this.errMsg = errMsg;
        }
    }

    public SoterCoreResult(int errCode) {
        this.errCode = errCode;
        switch (this.errCode) {
            case ERR_OK:
                this.errMsg = "ok";
                break;
            case ERR_SOTER_NOT_SUPPORTED:
                this.errMsg = "device not support soter";
                break;
            case ERR_UNKNOWN:
            default:
                this.errMsg = "errmsg not specified";
        }
    }

    public boolean isSuccess() {
        return errCode == ERR_OK;
    }


    @Override
    public boolean equals(Object obj) {
        return obj instanceof SoterCoreResult && ((SoterCoreResult) obj).errCode == errCode;
    }

    public int getErrCode() {
        return errCode;
    }

    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    @Override
    public String toString() {
        return "SoterCoreResult{" +
                "errCode=" + errCode +
                ", errMsg='" + errMsg + '\'' +
                '}';
    }
}
