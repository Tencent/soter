/*
 * Tencent is pleased to support the open source community by making TENCENT SOTER available.
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * https://opensource.org/licenses/BSD-3-Clause
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package com.tencent.soter.wrapper.wrap_fingerprint;

/**
 * Created by henryye on 2017/4/24.
 * The callback for fingerprint authentication prpcess. Note that do not directly rely on the callback to do logic, you should only use it
 * to refresh the UI components.
 */

public interface SoterFingerprintStateCallback {
    /**
     * Callback when fingerprint sensor start listening
     */
    void onStartAuthentication();

    /**
     * Callback when sensor indicates this authentication event as not success, neither not error, e.g., user authenticate with
     * wet fingerprint.
     * @param helpCode The help code provided by system
     * @param helpString The hint msg provided by system
     */
    void onAuthenticationHelp(int helpCode, CharSequence helpString);

    /**
     * Callback when sensor indicates this authentication event as success.
     */
    void onAuthenticationSucceed();

    /**
     * Callback when sensor indicates this authentication event as failed, which means user uses a not enrolled fingerprint
     * for authentication
     */
    void onAuthenticationFailed();

    /**
     * Callback when user cancelled the authentication
     */
    void onAuthenticationCancelled();

    /**
     * Callback when sensor indicates this authentication event as an unrecoverable error, e.g., Auth Key is invalid permanently
     * Note that we separate cancellation event from it and move it to {@link SoterFingerprintStateCallback#onAuthenticationFailed()}, which
     * we think is much more reasonable
     * @param errorCode The error code provided by system
     * @param errorString The hint msg provided by system
     */
    void onAuthenticationError(int errorCode, CharSequence errorString);
}
