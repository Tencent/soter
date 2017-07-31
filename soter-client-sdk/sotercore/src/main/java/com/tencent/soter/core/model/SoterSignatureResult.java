/*
 * Tencent is pleased to support the open source community by making TENCENT SOTER available.
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * https://opensource.org/licenses/BSD-3-Clause
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package com.tencent.soter.core.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * The signature model generated in TEE after authenticated by user's enrolled fingerprint.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class SoterSignatureResult {
    private static final String TAG = "Soter.SoterSignatureResult";

    private static final String SIGNATURE_KEY_RAW = "raw";
    private static final String SIGNATURE_KEY_FID = "fid";
    private static final String SIGNATURE_KEY_COUNTER = "counter";
    private static final String SIGNATURE_KEY_TEE_NAME = "tee_n";
    private static final String SIGNATURE_KEY_TEE_VERSION = "tee_v";
    private static final String SIGNATURE_KEY_FP_NAME = "fp_n";
    private static final String SIGNATURE_KEY_FP_VERSION = "fp_v";
    private static final String SIGNATURE_KEY_CPU_ID = "cpu_id";
    private static final String SIGNATURE_KEY_SALTLEN = "rsa_pss_saltlen";

    private static final int DEFAULT_SALT_LEN = 20;

    private String rawValue = null;
    private String fid = null;
    private long counter = -1;
    private String TEEName = "";
    private String TEEVersion = "";
    private String FpName = "";
    private String FpVersion = "";
    private String cpuId = "";

    private int saltLen = DEFAULT_SALT_LEN;


    public String getJsonValue() {
        return jsonValue;
    }

    private void setJsonValue(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    private String jsonValue = "";
    //real signature


    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    private String signature = "";

    public void setCpuId(String cpuId) {
        this.cpuId = cpuId;
    }

    public SoterSignatureResult(String rawValue, String fid, long counter, String TEEName, String TEEVersion, String fpName, String fpVersion, String cpuId, String signature, int saltLen) {
        this.rawValue = rawValue;
        this.fid = fid;
        this.counter = counter;
        this.TEEName = TEEName;
        this.TEEVersion = TEEVersion;
        FpName = fpName;
        FpVersion = fpVersion;
        this.cpuId = cpuId;
        this.signature = signature;
        this.saltLen = saltLen;
    }

    @Override
    public String toString() {
        return "SoterSignatureResult{" +
                "rawValue='" + rawValue + '\'' +
                ", fid='" + fid + '\'' +
                ", counter=" + counter +
                ", TEEName='" + TEEName + '\'' +
                ", TEEVersion='" + TEEVersion + '\'' +
                ", FpName='" + FpName + '\'' +
                ", FpVersion='" + FpVersion + '\'' +
                ", cpuId='" + cpuId + '\'' +
                ", saltLen=" + saltLen +
                ", jsonValue='" + jsonValue + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }

    public SoterSignatureResult() {
    }

    public static SoterSignatureResult convertFromJson(String jsonStr) {
//        this.jsonValue = jsonStr;
        try {
            JSONObject jsonObj = new JSONObject(jsonStr);
            SoterSignatureResult result = new SoterSignatureResult();
            result.setJsonValue(jsonStr);
            result.setRawValue(jsonObj.optString(SIGNATURE_KEY_RAW));
            result.setFid(jsonObj.optString(SIGNATURE_KEY_FID));
            result.setCounter(jsonObj.optLong(SIGNATURE_KEY_COUNTER));
            result.setTEEName(jsonObj.optString(SIGNATURE_KEY_TEE_NAME));
            result.setTEEVersion(jsonObj.optString(SIGNATURE_KEY_TEE_VERSION));
            result.setFpName(jsonObj.optString(SIGNATURE_KEY_FP_NAME));
            result.setFpVersion(jsonObj.optString(SIGNATURE_KEY_FP_VERSION));
            result.setCpuId(jsonObj.optString(SIGNATURE_KEY_CPU_ID));
            result.setSaltLen(jsonObj.optInt(SIGNATURE_KEY_SALTLEN, DEFAULT_SALT_LEN));
            return result;
        } catch (JSONException e) {
            SLogger.e(TAG, "soter: convert from json failed." + e.toString());
            return null;
        }
    }

    private void setRawValue(String rawValue) {
        this.rawValue = rawValue;
    }

    private void setFid(String fid) {
        this.fid = fid;
    }

    private void setCounter(long counter) {
        this.counter = counter;
    }

    private void setTEEName(String TEEName) {
        this.TEEName = TEEName;
    }

    private void setTEEVersion(String TEEVersion) {
        this.TEEVersion = TEEVersion;
    }

    private void setFpName(String fpName) {
        FpName = fpName;
    }

    private void setFpVersion(String fpVersion) {
        FpVersion = fpVersion;
    }

    private void setSaltLen(int saltLen) {
        this.saltLen = saltLen;
    }

    public String getRawValue() {

        return rawValue;
    }

    public String getFid() {
        return fid;
    }

    public long getCounter() {
        return counter;
    }

    public String getTEEName() {
        return TEEName;
    }

    public String getTEEVersion() {
        return TEEVersion;
    }

    public String getFpName() {
        return FpName;
    }

    public String getFpVersion() {
        return FpVersion;
    }

    public String getCpuId() {
        return cpuId;
    }

    public int getSaltLen() {
        return saltLen;
    }
}
