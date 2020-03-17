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
 * Error codes in SOTER core operations.
 */
public interface SoterErrCode {
    /**
     * Undefined result
     */
    int ERR_UNKNOWN = -1;
    /**
     * The operation is OK
     */
    int ERR_OK = 0;
    /**
     * The parameter is not valid. Check the errmsg for more information
     */
    int ERR_PARAMERROR = 1;
    /**
     * Not suopport SOTER. You should check the support state before calling this method
     */
    int ERR_SOTER_NOT_SUPPORTED = 2;
    /**
     * App Secure Key does not exist
     */
    int ERR_ASK_NOT_EXIST = 3;
    /**
     * Failed to generate App Secure Key
     */
    int ERR_ASK_GEN_FAILED = 4;
    /**
     * Failed to delete App Secure Key
     */
    int ERR_REMOVE_ASK = 5;
    /**
     * Failed to generate Auth Key
     */
    int ERR_AUTH_KEY_GEN_FAILED = 6;
    /**
     * Failed to remove Auth Key
     */
    int ERR_REMOVE_AUTH_KEY = 7;
}
