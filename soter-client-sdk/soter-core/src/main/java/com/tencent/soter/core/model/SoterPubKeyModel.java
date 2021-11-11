/*
 * Tencent is pleased to support the open source community by making TENCENT SOTER available.
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * https://opensource.org/licenses/BSD-3-Clause
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package com.tencent.soter.core.model;

import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

/**
 * The public key model for App Secure Key and Auth Key. It consists the whole JSON that wrapper in the
 * TEE, and the signature of the JSON generated in TEE. Developers should upload the JSON and signature to server
 */
@SuppressWarnings("unused")
public class SoterPubKeyModel {
    private static final String TAG = "Soter.SoterPubKeyModel";

    public static final String JSON_KEY_PUBLIC = "pub_key";
    public static final String JSON_KEY_COUNTER = "counter";
    public static final String JSON_KEY_CPU_ID = "cpu_id";
    public static final String JSON_KEY_UID = "uid";
    public static final String JSON_KEY_CERTS = "certs";

    private long counter = -1;
    private int uid = -1;
    private String cpu_id = "";
    private String pub_key_in_x509 = "";
    private String rawJson = "";
    private ArrayList<String> certs = null;

    @Override
    public String toString() {
        return "SoterPubKeyModel{" +
                "counter=" + counter +
                ", uid=" + uid +
                ", cpu_id='" + cpu_id + '\'' +
                ", pub_key_in_x509='" + pub_key_in_x509 + '\'' +
                ", rawJson='" + rawJson + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }

    private String signature = "";

    @SuppressWarnings("unused")
    public SoterPubKeyModel(long counter, int uid, String cpu_id, String pub_key_in_x509, String signature) {
        this.counter = counter;
        this.uid = uid;
        this.cpu_id = cpu_id;
        this.pub_key_in_x509 = pub_key_in_x509;
        this.signature = signature;
    }

    public SoterPubKeyModel(String rawJson, String signature) {
        JSONObject jsonObj;
        setRawJson(rawJson);
        try {
            jsonObj = new JSONObject(rawJson);
//            this.rawJson = jsonObj.toString();
            if(jsonObj.has(JSON_KEY_CERTS)){
                JSONArray certJsonArray = jsonObj.optJSONArray(JSON_KEY_CERTS);
                if (certJsonArray.length() < 2) {
                    SLogger.e(TAG,"certificates train not enough");
                }
                SLogger.i(TAG,"certs size: [%d]", certJsonArray.length());
                certs =  new ArrayList<String>();
                for (int i = 0; i < certJsonArray.length(); i++) {
                    String certText = certJsonArray.getString(i);
                    certs.add(certText);
                }
                CertificateFactory factory = CertificateFactory.getInstance("X.509");
                X509Certificate askCertificate = (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(certs.get(0).getBytes()));
                loadDeviceInfo(askCertificate);
                jsonObj.put(JSON_KEY_CPU_ID, cpu_id);
                jsonObj.put(JSON_KEY_UID, uid);
                jsonObj.put(JSON_KEY_COUNTER, counter);
                setRawJson(jsonObj.toString());
            }else{
                this.counter = jsonObj.optLong(JSON_KEY_COUNTER);
                this.uid = jsonObj.optInt(JSON_KEY_UID);
                this.cpu_id = jsonObj.optString(JSON_KEY_CPU_ID);
                this.pub_key_in_x509 = jsonObj.optString(JSON_KEY_PUBLIC);
            }
        } catch (Exception e) {
            SLogger.e(TAG, "soter: pub key model failed");
        }
        this.signature = signature;
    }

    public SoterPubKeyModel(Certificate[] certificates){
        try {
            if(certificates != null){
                ArrayList<String> certTexts = new ArrayList<String>();
                JSONArray jsonArray = new JSONArray();
                for (int i = 0; i < certificates.length; i++) {
                    Certificate certificate = certificates[i];

                    String certText = Base64.encodeToString(certificate.getEncoded(), Base64.NO_WRAP);
                    certText = CertUtil.format(certificate);
                    if (i == 0){
                        loadDeviceInfo((X509Certificate)certificate);
                    }
                    jsonArray.put(certText);
                    certTexts.add(certText);
                }
                certs = certTexts;

                JSONObject jsonObj = new JSONObject();
                jsonObj.put(JSON_KEY_CERTS, jsonArray);
                jsonObj.put(JSON_KEY_CPU_ID, cpu_id);
                jsonObj.put(JSON_KEY_UID, uid);
                jsonObj.put(JSON_KEY_COUNTER, counter);
                setRawJson(jsonObj.toString());
            }
        }catch (Exception e){
            SLogger.e(TAG, "soter: pub key model failed");
        }
    }

    private void loadDeviceInfo(X509Certificate attestationCert) {
        try{
            CertUtil.extractAttestationSequence(attestationCert,this);
        }catch (Exception e){
            SLogger.printErrStackTrace(TAG, e, "soter: loadDeviceInfo from attestationCert failed");
        }

    }


    public void setCounter(long counter) {
        this.counter = counter;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public void setCpu_id(String cpu_id) {
        this.cpu_id = cpu_id;
    }

    public void setPub_key_in_x509(String pub_key_in_x509) {
        this.pub_key_in_x509 = pub_key_in_x509;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public long getCounter() {
        return counter;
    }

    public int getUid() {
        return uid;
    }

    public String getCpu_id() {
        return cpu_id;
    }

    public String getPub_key_in_x509() {
        return pub_key_in_x509;
    }

    public String getSignature() {
        return signature;
    }

    public String getRawJson() {
        return rawJson;
    }

    public void setRawJson(String rawJson) {
        this.rawJson = rawJson;
    }
}
