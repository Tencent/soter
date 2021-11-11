package com.tencent.soter.core.model;

import android.util.Base64;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

/**
 * A generic PEM writer, based on RFC 1421
 */
public class CertUtil
{

    protected static final String TAG = "Soter.CertUtil";


    private static final int LINE_LENGTH = 64;
    private static final String LINE_SEPARATOR = "\n";
    private static final String KEY_DESCRIPTION_OID = "1.3.6.1.4.1.11129.2.1.17";
    public static final String JSON_KEY_COUNTER = "counter";
    public static final String JSON_KEY_CPU_ID = "cpu_id";
    public static final String JSON_KEY_UID = "uid";

    private static void writeEncoded(BufferedWriter writer, byte[] bytes)  throws IOException
    {
        char[]  buf = new char[LINE_LENGTH];
        for (int i = 0; i < bytes.length; i += buf.length)
        {
            int index = 0;
            while (index != buf.length)
            {
                if ((i + index) >= bytes.length)
                {
                    break;
                }
                buf[index] = (char)bytes[i + index];
                index++;
            }
            writer.write(buf, 0, index);
            writer.write(LINE_SEPARATOR);
        }
    }

    private static void writePreEncapsulationBoundary(BufferedWriter writer, String type) throws IOException
    {
        writer.write("-----BEGIN " + type + "-----");
        writer.write(LINE_SEPARATOR);
    }

    private static void writePostEncapsulationBoundary(BufferedWriter writer, String type) throws IOException
    {
        writer.write("-----END " + type + "-----");
        writer.write(LINE_SEPARATOR);
    }

    public static String format(Certificate certificate) throws Exception{
        String type = "CERTIFICATE";
        StringWriter stringWriter = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);

        writePreEncapsulationBoundary(bufferedWriter, type);
        writeEncoded(bufferedWriter, Base64.encode(certificate.getEncoded(), Base64.NO_WRAP));
        writePostEncapsulationBoundary(bufferedWriter, type);
        bufferedWriter.close();

        return stringWriter.toString();
    }

    public static void extractAttestationSequence(X509Certificate attestationCert, SoterPubKeyModel soterPubKeyModel) throws Exception, IOException {
        byte[] attestationExtensionBytes = attestationCert.getExtensionValue(KEY_DESCRIPTION_OID);
        if (attestationExtensionBytes == null || attestationExtensionBytes.length == 0) {
            throw new Exception("Couldn't find the keystore attestation " + "extension data.");
        }

        int jsonStartOff = 0;
        int jsonEndOff = 0;
        int jsonLength = 0;

        byte jsonStartTag = "{".getBytes()[0];
        byte jsonEndTag = "}".getBytes()[0];

        for (int i = 0; i < attestationExtensionBytes.length; i++) {
            byte b = attestationExtensionBytes[i];
            if (b==jsonStartTag) {
                jsonStartOff = i;
            }else if(b==jsonEndTag){
                jsonEndOff = i;
            }
        }
        if (jsonStartOff > 0 && jsonStartOff < jsonEndOff) {
            if (attestationExtensionBytes[jsonStartOff-1]!=(jsonEndOff-jsonStartOff+1)) {
//                throw new Exception("read extension data error");
                SLogger.w(TAG, "read extension lenght error");
            }
            jsonLength = (jsonEndOff-jsonStartOff+1);

            byte[] jsonBytes = new byte[jsonLength];
            System.arraycopy(attestationExtensionBytes, jsonStartOff, jsonBytes, 0, jsonLength);
            String jsonString = new String(jsonBytes);

            SLogger.i(TAG, "soter: challenge json in attestation certificate " + jsonString);

            JSONObject jsonObject = new JSONObject(jsonString);

            soterPubKeyModel.setCpu_id(jsonObject.getString(JSON_KEY_CPU_ID));
            soterPubKeyModel.setUid(jsonObject.getInt(JSON_KEY_UID));
            soterPubKeyModel.setCounter(jsonObject.getLong(JSON_KEY_COUNTER));
        }
    }
}
