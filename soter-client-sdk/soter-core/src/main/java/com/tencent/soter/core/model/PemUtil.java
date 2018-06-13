package com.tencent.soter.core.model;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.security.cert.Certificate;

/**
 * A generic PEM writer, based on RFC 1421
 */
public class PemUtil
{
    private static final int LINE_LENGTH = 64;
    private static final String LINE_SEPARATOR = "\n";

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
        writeEncoded(bufferedWriter, certificate.getEncoded());
        writePostEncapsulationBoundary(bufferedWriter, type);
        bufferedWriter.close();

        return stringWriter.toString();
    }
}
