package com.dtiguardian.commport.vculib.util;

public class StringUtils {
    static public String PadRight(String str, int padVal, int len)
    {
        BufferHandler handler = new BufferHandler(str);
        for (int i = str.length(); i < len; i++)
            handler.Buffer().add((byte)padVal);
        String str2 = new String(handler.ToByteArray());
        return str2;
    }

    static public String TrimRight(String str, int trimVal)
    {
        byte[] bytes = str.getBytes();
        int count = 0; //-1

        for(int i = bytes.length-1;i>=0;i--)
            if(bytes[i]==trimVal) count++;

        byte[] bytes2 = new byte[bytes.length - count];

        for(int i=0;i<bytes2.length;i++)
            bytes2[i] = bytes[i];

        return new String(bytes2);
    }

    static String IntToHexStr(int value)
    {
        return null;
    }
}
