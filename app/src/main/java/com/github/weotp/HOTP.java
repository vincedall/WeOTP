package com.github.weotp;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HOTP {
    private String key;
    private long counter;
    private String hotp;
    private int otpLength;

    public HOTP(String key, long counter, int otpLength){
        this.key = key;
        this.counter = counter;
        this.otpLength = otpLength;
    }

    public void update(){
        byte[] sha1hash = hash(key, counter);
        byte[] trunc = dynamicTruncation(sha1hash);
        ByteBuffer buf = ByteBuffer.wrap(trunc);
        int num = buf.getInt();
        hotp = "" + (num % (int) Math.pow(10, otpLength));
        if (hotp.length() < otpLength)
            hotp = "0" + hotp;
    }

    public String getHOTP(){
        return hotp;
    }

    private byte[] hash(String key, long counter) {
        byte[] counterBytes = ByteBuffer.allocate(8).putLong(counter).array();
        byte[] digest = new byte[]{};
        try {
            Mac hmacSha1;
            hmacSha1 = Mac.getInstance("HmacSHA1");
            SecretKeySpec macKey = new SecretKeySpec(decodeBase32(key), "RAW");
            hmacSha1.init(macKey);
            digest = hmacSha1.doFinal(counterBytes);
        }catch(Exception e){ }
        return digest;
    }

    private byte[] dynamicTruncation(byte [] bytes){
        byte[] trunc = new byte[4];
        try {
            int offset = bytes[bytes.length - 1] & 0x0f;
            trunc[0] = bytes[offset];
            trunc[1] = bytes[offset + 1];
            trunc[2] = bytes[offset + 2];
            trunc[3] = bytes[offset + 3];
            trunc[0] = (byte) (trunc[0] & 0x7F);
        }catch(Exception e){

        }
        return trunc;
    }

    private byte[] decodeBase32(String key){
        int counter = 0;
        long result = 0;
        ArrayList<Byte> bytes = new ArrayList<>();
        Map<String, Integer> base32 = new HashMap<String, Integer>(){{
            put("A", 0); put("a", 0); put("B", 1); put("b", 1); put("C", 2); put("c", 2);
            put("D", 3); put("d", 3); put("E", 4); put("e", 4); put("F", 5); put("f", 5);
            put("G", 6); put("g", 6); put("H", 7); put("h", 7); put("I", 8); put("i", 8);
            put("J", 9); put("j", 9); put("K", 10); put("k", 10); put("L", 11); put("l", 11);
            put("M", 12); put("m", 12); put("N", 13); put("n", 13); put("O", 14); put("o", 14);
            put("P", 15); put("p", 15); put("Q", 16); put("q", 16); put("R", 17); put("r", 17);
            put("S", 18); put("s", 18); put("T", 19); put("t", 19); put("U", 20); put("u", 20);
            put("V", 21); put("v", 21); put("W", 22); put("w", 22); put("X", 23); put("x", 23);
            put("Y", 24); put("y", 24); put("Z", 25); put("z", 25); put("2", 26); put("3", 27);
            put("4", 28); put("5", 29); put("6", 30); put("7", 31);
        }};
        for (int i = 0; i < key.length(); i++){
            counter++;
            long index = base32.get(key.substring(i, i + 1));
            result = result << 5;
            result = result + index;
            if (counter == 8){
                for (int a = 0; a < 5; a++){
                    byte b = (byte) result;
                    result = result >> 8;
                    bytes.add(b);
                }
                counter = 0;
                result = 0;
            }
        }
        byte[] b = new byte[bytes.size()];
        counter = 0;
        for (int i = 0; i < bytes.size(); i+=5) {
            for (int a = i+4; a > i-1; a--) {
                b[counter] = bytes.get(a);
                counter++;
            }
        }
        return b;
    }
}
