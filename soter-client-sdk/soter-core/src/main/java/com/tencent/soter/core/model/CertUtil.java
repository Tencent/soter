package com.tencent.soter.core.model;

import android.util.Base64;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
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
    private static final int LINE_LENGTH = 64;
    private static final String LINE_SEPARATOR = "\n";

    private static final int ATTESTATION_CHALLENGE_INDEX = 4;
    private static final String KEY_DESCRIPTION_OID = "1.3.6.1.4.1.11129.2.1.17";

    public static final String JSON_KEY_PUBLIC = "pub_key";
    public static final String JSON_KEY_COUNTER = "counter";
    public static final String JSON_KEY_CPU_ID = "cpu_id";
    public static final String JSON_KEY_UID = "uid";
    public static final String JSON_KEY_CERTS = "certs";
    public static final String JSON_KEY_SALTLEN = "rsa_pss_saltlen";

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

        ASN1Sequence decodedSequence;
        try (ASN1InputStream asn1InputStream = new ASN1InputStream(attestationExtensionBytes)) {
            // The extension contains one object, a sequence, in the
            // Distinguished Encoding Rules (DER)-encoded form. Get the DER
            // bytes.
            byte[] derSequenceBytes = ((ASN1OctetString) asn1InputStream.readObject()).getOctets();
            // Decode the bytes as an ASN1 sequence object.
            try (ASN1InputStream seqInputStream = new ASN1InputStream(derSequenceBytes)) {
                decodedSequence = (ASN1Sequence) seqInputStream.readObject();
            }
        }
        // get attestation challenge from attestation key
        byte[] attestationChallengeBytes = ((ASN1OctetString)decodedSequence.getObjectAt(ATTESTATION_CHALLENGE_INDEX)).getOctets();
        if (attestationChallengeBytes != null && attestationChallengeBytes.length > 0 ){
            String challenge = new String(attestationChallengeBytes);
            JSONObject jsonObject = new JSONObject(challenge);

            soterPubKeyModel.setCpu_id(jsonObject.getString(JSON_KEY_CPU_ID));
            soterPubKeyModel.setUid(jsonObject.getInt(JSON_KEY_UID));
            soterPubKeyModel.setCounter(jsonObject.getLong(JSON_KEY_COUNTER));
        }
    }
}