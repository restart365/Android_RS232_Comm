package com.dtiguardian.commport.vculib.util;

import android.util.Base64;

public class XTEA {
    long[] key = new long[4];
    int rounds=32;
    long I = 0xFFFFFFFFL;

    public XTEA()
    {
        importKey("0123456789ABCDEF");
    }

    public void importKey(String newkey)
    {
        if (newkey.length() == 16)
        {
            byte[] tmp = newkey.getBytes();
            for (int i = 0; i < key.length; i++)
            {
                key[i] = tmp[(i * 4)] << 24;
                key[i] += tmp[(i * 4) + 1] << 16;
                key[i] += tmp[(i * 4) + 2] << 8;
                key[i] += tmp[(i * 4) + 3];
            }
        }
    }

    public void importKey(byte[] newkey)
    {
        if (newkey.length == 16)
        {
            for (int i = 0; i < key.length; i++)
            {
                key[i] = newkey[(i * 4)] << 24;
                key[i] += newkey[(i * 4) + 1] << 16;
                key[i] += newkey[(i * 4) + 2] << 8;
                key[i] += newkey[(i * 4) + 3];
            }
        }
    }

    private int[] padData(int[] data)
    {
        int remainder = data.length%8;
        if(remainder>0)
        {
            int[] newData = new int[data.length+(8-remainder)];
            for(int i=0;i<data.length;i++)
                newData[i] = data[i];
            return newData;
        }
        else
            return data;
    }

    private long[] toLongArray(int[] data)
    {
        int length=0;
        int[] tmp;

        if(data.length%4 > 0)
            length = data.length/4 + 1;
        else
            length = data.length/4;

        tmp = new int[length*4];

        for(int i=0;i<data.length;i++)
            tmp[i] = data[i];

        long[] longArray = new long[length];

        for(int i=0;i<longArray.length;i++)
        {
            longArray[i] = (long)(tmp[(i*4)]);
            longArray[i] += (long)(tmp[(i*4)+1])<<8;
            longArray[i] += (long)(tmp[(i*4)+2])<<16;
            longArray[i] += (long)(tmp[(i*4)+3])<<24;
        }

        return longArray;
    }

    private int[] toIntArray(long[] longArray)
    {
        int[] returnArray = new int[longArray.length * 4];
        for (int i = 0; i < longArray.length; i++)
        {
            returnArray[(i * 4) + 3] = (int) (longArray[i] >> 24);
            returnArray[(i * 4) + 2] = (int)((longArray[i] >> 16) & 0xff);
            returnArray[(i * 4) + 1] = (int)((longArray[i] >> 8) & 0xff);
            returnArray[i * 4] = (int)(longArray[i] & 0xff);
        }
        return returnArray;
    }

    private long[] encryptBlock(long vx, long vy, long[] key)
    {
        long v0 = vx, v1 = vy, sum = 0, delta = 0x9E3779B9;
        long[] returnLong = new long[2];

        for (int i = 0; i < rounds; i++)
        {
            v0 += (((((v1 << 4)&I) ^ (v1 >> 5)) + v1)&I) ^ (sum + key[(int) (sum & 3)]);
            v0&=I;
            sum += delta;
            v1 += (((((v0 << 4)&I) ^ (v0 >> 5)) + v0)&I) ^ (sum + key[(int) ((sum >> 11) & 3)]);
            v1&=I;
        }
        returnLong[0] = v0; returnLong[1] = v1;

        return returnLong;
    }

    private long[] decryptBlock(long vx, long vy, long[] key)
    {
        long v0 = vx, v1 = vy, delta = 0x9E3779B9, sum = delta * rounds;
        long[] returnLong = new long[2];

        for (int i = 0; i < rounds; i++)
        {
            v1 -= (((((v0 << 4)&I) ^ (v0 >> 5)) + v0)&I) ^ (sum + key[(int) ((sum >> 11) & 3)]);
            v1&=I;
            sum -= delta;
            v0 -= (((((v1 << 4)&I) ^ (v1 >> 5)) + v1)&I) ^ (sum + key[(int) (sum & 3)]);
            v0&=I;
        }
        returnLong[0] = v0; returnLong[1] = v1;

        return returnLong;
    }

    public byte[] Encrypt(byte[] data){
        return fromIntToByteArray(Encrypt(fromByteToIntArray(data)));
    }

    public int[] Encrypt(int[] data)
    {
        data = padData(data);

        long[] longArray = toLongArray(data);
        long[] returnLongArray = new long[2];
        int[] returnIntArray = null;

        for(int i=0;i<longArray.length;i+=2)
        {
            returnLongArray = encryptBlock(longArray[i], longArray[i+1], key);
            longArray[i]= returnLongArray[0];
            longArray[i+1]= returnLongArray[1];
        }

        returnIntArray = toIntArray(longArray);

        return returnIntArray;
    }

    public int[] Encrypt(String str)
    {
        byte[] tmp = str.getBytes();
        int[] tmpInt = new int[tmp.length];

        for(int i=0;i<tmp.length;i++)
            tmpInt[i] = tmp[i];

        tmpInt = Encrypt(tmpInt);

        return tmpInt;
    }

    public int[] Decrypt(int[] data)
    {
        long[] longArray = toLongArray(data);
        long[] returnLongArray = new long[2];
        int[] returnIntArray = null;

        for(int i=0;i<longArray.length;i+=2)
        {
            returnLongArray = decryptBlock(longArray[i], longArray[i+1], key);
            longArray[i]= returnLongArray[0];
            longArray[i+1]= returnLongArray[1];
        }
        returnIntArray = toIntArray(longArray);
        return returnIntArray;
    }

    public byte[] Decrypt(byte[] data)
    {
        int[] tmpInt = fromByteToIntArray(data);
        return fromIntToByteArray(Decrypt(tmpInt));
    }

    public static byte[] fromIntToByteArray(int[] intArray)
    {
        byte[] byteArray = new byte[intArray.length];

        for(int i=0;i<intArray.length;i++)
            byteArray[i] = (byte) (intArray[i]&0xFF);

        return byteArray;
    }

    public static int[] fromByteToIntArray(byte[] byteArray)
    {
        int[] intArray = new int[byteArray.length];

        for(int i=0;i<byteArray.length;i++)
            intArray[i] = (int) (byteArray[i]&0xFF);

        return intArray;
    }

    public String encodeB64(int[] data)
    {
        return Base64.encodeToString(fromIntToByteArray(data), Base64.NO_WRAP);
    }

    public String encodeB64(byte[] data)
    {
        return Base64.encodeToString(data, Base64.NO_WRAP);
    }

    public int[] decodeB64(String data)
    {
        try
        {
            return fromByteToIntArray(Base64.decode(data, Base64.DEFAULT));
        }
        catch(Exception e)
        {
            return null;
        }
    }
}
