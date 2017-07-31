/*
 * Tencent is pleased to support the open source community by making TENCENT SOTER available.
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * https://opensource.org/licenses/BSD-3-Clause
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package com.tencent.soter.wrapper.wrap_callback;

import android.support.annotation.NonNull;

/**
 * Created by henryye on 2017/4/18.
 * The process callback.
 */

public interface SoterProcessCallback<T extends SoterProcessResultBase> {
    /**
     * Called when the process come to the end, regardless it's done, there's a failure or user cancelled it.
     * @param result The result of the process
     */
    void onResult(@NonNull T result);
}
