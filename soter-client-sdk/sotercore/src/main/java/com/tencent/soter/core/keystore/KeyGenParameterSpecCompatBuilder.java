/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tencent.soter.core.keystore;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;

import com.tencent.soter.core.SoterCore;
import com.tencent.soter.core.model.SLogger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Date;

import javax.security.auth.x500.X500Principal;

/**
 * Created by henryye on 15/9/29.
 * If need any edit necessary, please contact him by RTX
 *
 * The builder to compat legacy KeyGenParameterSpecBuilder
 */
@SuppressWarnings({"unused", "WeakerAccess", "SameParameterValue"})
public abstract class KeyGenParameterSpecCompatBuilder {
    private static final String TAG = "Soter.KeyGenParameterSpecCompatBuilder";

    public KeyGenParameterSpecCompatBuilder(String keyName, int purpose) {

    }

    public static KeyGenParameterSpecCompatBuilder newInstance(String keyName, int purpose) {
        if(SoterCore.isNativeSupportSoter()) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return new NormalKeyGenParameterSpecCompatBuilder(keyName, purpose);
            }
            else {
                return new ReflectKeyGenParameterSpecCompatBuilder(keyName, purpose);
            }
        }
        else {
            SLogger.e(TAG, "soter: not support soter. return dummy");
            return new DummyKeyGenParameterSpecCompatBuilder(keyName, purpose);
        }
    }

    public abstract AlgorithmParameterSpec build() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException;

    public abstract KeyGenParameterSpecCompatBuilder setKeySize(int keySize);

    public abstract KeyGenParameterSpecCompatBuilder setAlgorithmParameterSpec(AlgorithmParameterSpec spec);

    public abstract KeyGenParameterSpecCompatBuilder setCertificateSubject(X500Principal subject);

    public abstract KeyGenParameterSpecCompatBuilder setCertificateSerialNumber(BigInteger serialNumber);

    public abstract KeyGenParameterSpecCompatBuilder setCertificateNotBefore(Date date);

    public abstract KeyGenParameterSpecCompatBuilder setCertificateNotAfter(Date date);

    public abstract KeyGenParameterSpecCompatBuilder setKeyValidityStart(Date startDate);

    public abstract KeyGenParameterSpecCompatBuilder setKeyValidityEnd(Date endDate);

    public abstract KeyGenParameterSpecCompatBuilder setDigests(String... digests);

    public abstract KeyGenParameterSpecCompatBuilder setSignaturePaddings(
            String... paddings);

    public abstract KeyGenParameterSpecCompatBuilder setEncryptionPaddings(
            String... paddings);

    public abstract KeyGenParameterSpecCompatBuilder setBlockModes(String... blockModes);

    public abstract KeyGenParameterSpecCompatBuilder setRandomizedEncryptionRequired(boolean required);

    public abstract KeyGenParameterSpecCompatBuilder setUserAuthenticationRequired(boolean required);

    public abstract KeyGenParameterSpecCompatBuilder setUserAuthenticationValidityDurationSeconds(
            int seconds);

    @TargetApi(23)
    private static class NormalKeyGenParameterSpecCompatBuilder extends KeyGenParameterSpecCompatBuilder {

        private KeyGenParameterSpec.Builder builder = null;

        public NormalKeyGenParameterSpecCompatBuilder(String keystoreAlias, int purposes) {
            super(keystoreAlias, purposes);
            builder = new KeyGenParameterSpec.Builder(keystoreAlias, purposes);
        }

        @Override
        public AlgorithmParameterSpec build() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
            return builder.build();
        }

        @Override
        public KeyGenParameterSpecCompatBuilder setKeySize(int keySize) {
            builder.setKeySize(keySize);
            return this;
        }

        @Override
        public KeyGenParameterSpecCompatBuilder setAlgorithmParameterSpec(AlgorithmParameterSpec spec) {
            builder.setAlgorithmParameterSpec(spec);
            return this;
        }

        @Override
        public KeyGenParameterSpecCompatBuilder setCertificateSubject(X500Principal subject) {
            builder.setCertificateSubject(subject);
            return this;
        }

        @Override
        public KeyGenParameterSpecCompatBuilder setCertificateSerialNumber(BigInteger serialNumber) {
            builder.setCertificateSerialNumber(serialNumber);
            return this;
        }

        @Override
        public KeyGenParameterSpecCompatBuilder setCertificateNotBefore(Date date) {
            builder.setCertificateNotBefore(date);
            return this;
        }

        @Override
        public KeyGenParameterSpecCompatBuilder setCertificateNotAfter(Date date) {
            builder.setCertificateNotAfter(date);
            return this;
        }

        @Override
        public KeyGenParameterSpecCompatBuilder setKeyValidityStart(Date startDate) {
            builder.setKeyValidityStart(startDate);
            return this;
        }

        @Override
        public KeyGenParameterSpecCompatBuilder setKeyValidityEnd(Date endDate) {
            builder.setKeyValidityEnd(endDate);
            return this;
        }

        @SuppressLint("WrongConstant")
        @Override
        public KeyGenParameterSpecCompatBuilder setDigests(String... digests) {
            builder.setDigests(digests);
            return this;
        }

        @SuppressLint("WrongConstant")
        @Override
        public KeyGenParameterSpecCompatBuilder setSignaturePaddings(String... paddings) {
            builder.setSignaturePaddings(paddings);
            return this;
        }

        @SuppressLint("WrongConstant")
        @Override
        public KeyGenParameterSpecCompatBuilder setEncryptionPaddings(String... paddings) {
            builder.setEncryptionPaddings(paddings);
            return this;
        }

        @SuppressLint("WrongConstant")
        @Override
        public KeyGenParameterSpecCompatBuilder setBlockModes(String... blockModes) {
            builder.setBlockModes(blockModes);
            return this;
        }

        @Override
        public KeyGenParameterSpecCompatBuilder setRandomizedEncryptionRequired(boolean required) {
            builder.setRandomizedEncryptionRequired(required);
            return this;
        }

        @Override
        public KeyGenParameterSpecCompatBuilder setUserAuthenticationRequired(boolean required) {
            builder.setUserAuthenticationRequired(required);
            return this;
        }

        @Override
        public KeyGenParameterSpecCompatBuilder setUserAuthenticationValidityDurationSeconds(int seconds) {
            builder.setUserAuthenticationValidityDurationSeconds(seconds);
            return this;
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    static class ReflectKeyGenParameterSpecCompatBuilder extends KeyGenParameterSpecCompatBuilder {

        private static final String CLASSNAME = "android.security.keystore.KeyGenParameterSpec";


        private final String mKeystoreAlias;
        private int mPurposes;

        private int mKeySize = -1;
        private AlgorithmParameterSpec mSpec;
        private X500Principal mCertificateSubject;
        private BigInteger mCertificateSerialNumber;
        private Date mCertificateNotBefore;
        private Date mCertificateNotAfter;
        private Date mKeyValidityStart;
        private Date mKeyValidityForOriginationEnd;
        private Date mKeyValidityForConsumptionEnd;
        private String[] mDigests;
        private String[] mEncryptionPaddings;
        private String[] mSignaturePaddings;
        private String[] mBlockModes;
        private boolean mRandomizedEncryptionRequired = true;
        private boolean mUserAuthenticationRequired;
        private int mUserAuthenticationValidityDurationSeconds = -1;


        public ReflectKeyGenParameterSpecCompatBuilder(String keystoreAlias, int purposes) {
            super(keystoreAlias,purposes);
            if (keystoreAlias == null) {
                throw new NullPointerException("keystoreAlias == null");
            } else if (keystoreAlias.isEmpty()) {
                throw new IllegalArgumentException("keystoreAlias must not be empty");
            }
            mKeystoreAlias = keystoreAlias;
            mPurposes = purposes;
        }


        public KeyGenParameterSpecCompatBuilder setKeySize(int keySize) {
            if (keySize < 0) {
                throw new IllegalArgumentException("keySize < 0");
            }
            mKeySize = keySize;
            return this;
        }


        public KeyGenParameterSpecCompatBuilder setAlgorithmParameterSpec(AlgorithmParameterSpec spec) {
            if (spec == null) {
                throw new NullPointerException("spec == null");
            }
            mSpec = spec;
            return this;
        }


        public KeyGenParameterSpecCompatBuilder setCertificateSubject(X500Principal subject) {
            if (subject == null) {
                throw new NullPointerException("subject == null");
            }
            mCertificateSubject = subject;
            return this;
        }


        public KeyGenParameterSpecCompatBuilder setCertificateSerialNumber(BigInteger serialNumber) {
            if (serialNumber == null) {
                throw new NullPointerException("serialNumber == null");
            }
            mCertificateSerialNumber = serialNumber;
            return this;
        }


        public KeyGenParameterSpecCompatBuilder setCertificateNotBefore(Date date) {
            if (date == null) {
                throw new NullPointerException("date == null");
            }
            mCertificateNotBefore = cloneIfNotNull(date);
            return this;
        }


        public KeyGenParameterSpecCompatBuilder setCertificateNotAfter(Date date) {
            if (date == null) {
                throw new NullPointerException("date == null");
            }
            mCertificateNotAfter = cloneIfNotNull(date);
            return this;
        }


        public KeyGenParameterSpecCompatBuilder setKeyValidityStart(Date startDate) {
            mKeyValidityStart = cloneIfNotNull(startDate);
            return this;
        }


        public KeyGenParameterSpecCompatBuilder setKeyValidityEnd(Date endDate) {
            setKeyValidityForOriginationEnd(endDate);
            setKeyValidityForConsumptionEnd(endDate);
            return this;
        }


        public KeyGenParameterSpecCompatBuilder setKeyValidityForOriginationEnd(Date endDate) {
            mKeyValidityForOriginationEnd = cloneIfNotNull(endDate);
            return this;
        }


        public KeyGenParameterSpecCompatBuilder setKeyValidityForConsumptionEnd(Date endDate) {
            mKeyValidityForConsumptionEnd = cloneIfNotNull(endDate);
            return this;
        }


        public KeyGenParameterSpecCompatBuilder setDigests(String... digests) {
            mDigests = cloneIfNotEmpty(digests);
            return this;
        }


        public KeyGenParameterSpecCompatBuilder setEncryptionPaddings(
                String... paddings) {
            mEncryptionPaddings = cloneIfNotEmpty(paddings);
            return this;
        }


        public KeyGenParameterSpecCompatBuilder setSignaturePaddings(
                String... paddings) {
            mSignaturePaddings = cloneIfNotEmpty(paddings);
            return this;
        }


        public KeyGenParameterSpecCompatBuilder setBlockModes(String... blockModes) {
            mBlockModes = cloneIfNotEmpty(blockModes);
            return this;
        }


        public KeyGenParameterSpecCompatBuilder setRandomizedEncryptionRequired(boolean required) {
            mRandomizedEncryptionRequired = required;
            return this;
        }


        public KeyGenParameterSpecCompatBuilder setUserAuthenticationRequired(boolean required) {
            mUserAuthenticationRequired = required;
            return this;
        }

        public KeyGenParameterSpecCompatBuilder setUserAuthenticationValidityDurationSeconds(
                int seconds) {
            if (seconds < -1) {
                throw new IllegalArgumentException("seconds must be -1 or larger");
            }
            mUserAuthenticationValidityDurationSeconds = seconds;
            return this;
        }


        public AlgorithmParameterSpec build() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
            Class<?> clazz = Class.forName(CLASSNAME);
            Constructor<?> ctor = clazz.getConstructor(String.class,
                    Integer.TYPE,
                    AlgorithmParameterSpec.class,
                    X500Principal.class,
                    BigInteger.class,
                    Date.class,
                    Date.class,
                    Date.class,
                    Date.class,
                    Date.class,
                    Integer.TYPE,
                    String[].class,
                    String[].class,
                    String[].class,
                    String[].class,
                    Boolean.TYPE,
                    Boolean.TYPE,
                    Integer.TYPE
            );
            return (AlgorithmParameterSpec) ctor.newInstance(mKeystoreAlias,
                    mKeySize,
                    mSpec,
                    mCertificateSubject,
                    mCertificateSerialNumber,
                    mCertificateNotBefore,
                    mCertificateNotAfter,
                    mKeyValidityStart,
                    mKeyValidityForOriginationEnd,
                    mKeyValidityForConsumptionEnd,
                    mPurposes,
                    mDigests,
                    mEncryptionPaddings,
                    mSignaturePaddings,
                    mBlockModes,
                    mRandomizedEncryptionRequired,
                    mUserAuthenticationRequired,
                    mUserAuthenticationValidityDurationSeconds);
        }

    }


    static class DummyKeyGenParameterSpecCompatBuilder extends KeyGenParameterSpecCompatBuilder {

        public DummyKeyGenParameterSpecCompatBuilder(String keyName, int purpose) {
            super(keyName, purpose);
        }

        @Override
        public AlgorithmParameterSpec build() {
            return null;
        }

        @Override
        public KeyGenParameterSpecCompatBuilder setKeySize(int keySize) {
            return null;
        }

        @Override
        public KeyGenParameterSpecCompatBuilder setAlgorithmParameterSpec(AlgorithmParameterSpec spec) {
            return null;
        }

        @Override
        public KeyGenParameterSpecCompatBuilder setCertificateSubject(X500Principal subject) {
            return null;
        }

        @Override
        public KeyGenParameterSpecCompatBuilder setCertificateSerialNumber(BigInteger serialNumber) {
            return null;
        }

        @Override
        public KeyGenParameterSpecCompatBuilder setCertificateNotBefore(Date date) {
            return null;
        }

        @Override
        public KeyGenParameterSpecCompatBuilder setCertificateNotAfter(Date date) {
            return null;
        }

        @Override
        public KeyGenParameterSpecCompatBuilder setKeyValidityStart(Date startDate) {
            return null;
        }

        @Override
        public KeyGenParameterSpecCompatBuilder setKeyValidityEnd(Date endDate) {
            return null;
        }

        @Override
        public KeyGenParameterSpecCompatBuilder setDigests(String... digests) {
            return null;
        }

        @Override
        public KeyGenParameterSpecCompatBuilder setSignaturePaddings(String... paddings) {
            return null;
        }

        @Override
        public KeyGenParameterSpecCompatBuilder setEncryptionPaddings(String... paddings) {
            return null;
        }

        @Override
        public KeyGenParameterSpecCompatBuilder setBlockModes(String... blockModes) {
            return null;
        }

        @Override
        public KeyGenParameterSpecCompatBuilder setRandomizedEncryptionRequired(boolean required) {
            return null;
        }

        @Override
        public KeyGenParameterSpecCompatBuilder setUserAuthenticationRequired(boolean required) {
            return null;
        }

        @Override
        public KeyGenParameterSpecCompatBuilder setUserAuthenticationValidityDurationSeconds(int seconds) {
            return null;
        }
    }

    static Date cloneIfNotNull(Date value) {
        return (value != null) ? (Date) value.clone() : null;
    }

    public static String[] cloneIfNotEmpty(String[] array) {
        return ((array != null) && (array.length > 0)) ? array.clone() : array;
    }

    public static byte[] cloneIfNotEmpty(byte[] array) {
        return ((array != null) && (array.length > 0)) ? array.clone() : array;
    }
}