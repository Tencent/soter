package com.tencent.soter.serverdemo.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileUtil {

	/**
	 * The function is used to read public key from file.
	 */
	public static String readFromFile(String fileName, boolean isAppendLineSeparator) {
		String lineSeparator = System.getProperty("line.separator");
		File file = new File(fileName);
		if (!file.exists()) {
			System.err.println("Error: file " + fileName + " not found!");
			return null;
		}

		BufferedReader reader = null;
		InputStream in = null;
		try {
			in = new FileInputStream(new File(fileName));
			reader = new BufferedReader(new InputStreamReader(in));
			String line = null;
			StringBuilder stringBuilder = new StringBuilder();
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("-")) {
					continue;
				} else {
					stringBuilder.append(line);
					if (isAppendLineSeparator) {
						stringBuilder.append(lineSeparator);
					}
				}

			}
			return stringBuilder.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	public static byte[] readByteArrayFromFile(String fileName) {
		BufferedInputStream in = null;
		try {
			File file = new File(fileName);

			ByteArrayOutputStream out = new ByteArrayOutputStream((int) file.length());
			in = new BufferedInputStream(new FileInputStream(file));
			byte[] buffer = new byte[1024];
			int len = -1;
			while ((len = in.read(buffer, 0, 1024)) != -1) {
				out.write(buffer, 0, len);
			}
			byte[] data = out.toByteArray();
			return data;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}


}
