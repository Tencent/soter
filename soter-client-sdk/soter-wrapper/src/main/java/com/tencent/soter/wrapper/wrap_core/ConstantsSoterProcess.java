/*
 * Tencent is pleased to support the open source community by making TENCENT SOTER available.
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * https://opensource.org/licenses/BSD-3-Clause
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package com.tencent.soter.wrapper.wrap_core;

/**
 * Created by henryye on 2017/4/28.
 * The key upload and generate status used by library locally
 */

public class ConstantsSoterProcess {

    public interface KeyStatus {
        int KEY_STATUS_NORMAL = 0;
        int KEY_STATUS_GENERATING = 1;
        int KEY_STATUS_GENERATED_BUT_NOT_UPLOADED = 2;
    }

}
