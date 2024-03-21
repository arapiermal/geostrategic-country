package com.erimali.compute;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CustomStringEncoder {

    public static void main(String[] args) {
        String originalText = "Unë a jam\n mirë";
        String key = "hello there";
        byte[] encodedData = encode(originalText, key);
        String decodedText = decode(encodedData, key);
        System.out.println("Original Text: " + originalText);
        System.out.println("Decoded Text: " + decodedText);

    }
    //get file by bytes -> efficiency and correctness
    public static byte[] encode(String data, String key) {
        return encode(data.getBytes(StandardCharsets.UTF_8), key);
    }

    public static byte[] encode(byte[] data, String key) {
        if (key.isEmpty())
            return data;

        byte[] encodedData = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            int keyIndex = i % key.length();
            int value = (data[i] & 0xFF) + (key.charAt(keyIndex) & 0xFF);
            value = value % 256;
            encodedData[i] = (byte) value;
        }
        return encodedData;
    }

    public static String decode(byte[] encodedData, String key) {
        if (key.isEmpty())
            return new String(encodedData, StandardCharsets.UTF_8);
        
        byte[] decodedData = new byte[encodedData.length];
        for (int i = 0; i < encodedData.length; i++) {
            int keyIndex = i % key.length();
            int value = (encodedData[i] & 0xFF) - (key.charAt(keyIndex) & 0xFF);
            value = (value + 256) % 256;
            decodedData[i] = (byte) value;
        }
        return new String(decodedData, StandardCharsets.UTF_8);
    }
    
    public static void saveAndEncode(String filePath, String data, String key) {
		try {
			// Encoding the file (UTF-8)
			byte[] examBytes = CustomStringEncoder.encode(data, key);
			try (FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
				fileOutputStream.write(examBytes);
			}
		} catch (IOException e) {
			System.err.println("Error saving bytes: " + e.getMessage());
		}
	}
	public static String loadAndDecode(String filePath, String key) {
		try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
            byte[] examBytes = fileInputStream.readAllBytes();
            return CustomStringEncoder.decode(examBytes, key);
        } catch(IOException e) {
        	return null;
        }
	}
}
