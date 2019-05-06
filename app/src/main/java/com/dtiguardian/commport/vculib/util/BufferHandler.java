package com.dtiguardian.commport.vculib.util;

import android.util.Base64;

import java.util.ArrayList;
import java.util.List;

public class BufferHandler {
    public static int LONG_VAL = 1;

    private List<Byte> buffer;

    public List<Byte> Buffer()
    {
        return buffer;
    }

    public BufferHandler()
    {
        buffer = new ArrayList<>(0);
    }

    public BufferHandler(byte[] bytes)
    {
        buffer = new ArrayList<>(0);
        box(bytes);
    }

    public BufferHandler(int[] intArr)
    {
        byte[] tmp = new byte[intArr.length];
        for(int i=0;i<intArr.length;i++)
            tmp[i] = (byte)intArr[i];
        buffer = new ArrayList<>(0);
        box(tmp);
    }

    public BufferHandler(String string)
    {
        buffer = new ArrayList<>(0);
        AddString(string);
    }

    private void box(byte[] bytes)
    {
        for (int i = 0; i < bytes.length; i++)
            buffer.add(bytes[i]);
    }

    private byte[] unbox()
    {
        Byte[] array = new Byte[buffer.size()];
        buffer.toArray(array);
        byte[] tmp = new byte[array.length];
        for(int i=0;i<array.length;i++)
            tmp[i] = array[i];
        return tmp;
    }

    private void removeRange(int fromIndex,int toIndex){
        buffer.subList(fromIndex,toIndex).clear();
    }

    public byte[] ToByteArray()
    {
        return unbox();
    }

    public int[] ToIntArray()
    {
        int[] ret = new int[buffer.size()];
        for(int i=0;i<buffer.size();i++)
            ret[i] = buffer.get(i);
        return  ret;
    }

    public void AddString(String string)
    {
        byte[] strBytes = string.getBytes();
        box(strBytes);
    }

    public void AddByteArray(byte[] array)
    {
        box(array);
    }

    public void AddInt16(int val)
    {
        buffer.add((byte) (val >> 8));
        buffer.add((byte) (val & 0xff));
    }

    public void AddInt16(long longVal){
        buffer.add((byte) (longVal & 0xff));
        buffer.add((byte) (longVal >> 8));//((longVal >> 8) > 0 ? (longVal >> 8) : 0x100 - (longVal >> 8)));
    }

    public void AddInt32(int val)
    {
        AddInt16(val >> 16);
        AddInt16(val & 0xffff);
    }

    public void AddInt32(long longVal){
        AddInt16(longVal & 0xffff);
        AddInt16((longVal >> 16) & 0xffff);

    }

    public void AddInt8(int val)
    {
        buffer.add((byte) val);
    }

    public byte GetInt8(int pos)
    {
        return buffer.get(pos);
    }

    public int GetUInt8(int pos)
    {
        return getUnsignedByte(Buffer().get(pos));
    }

    public int GetInt16(int pos)
    {
        int result = 0;
        result = getUnsignedByte(buffer.get(pos))<<8;
        result += getUnsignedByte(buffer.get(pos + 1));
        return result;
    }

    public int GetInt16(int pos, int flag){
        if(flag == LONG_VAL){
            int result;
            result = getUnsignedByte(buffer.get(pos));
            result += getUnsignedByte(buffer.get(pos+1)) << 8;
            return result;
        } else {
            return GetInt16(pos);
        }
    }

    public long GetInt32(int pos)
    {
        long result = GetInt16(pos)<<16;
        result += GetInt16(pos + 2);
        return result;
    }

    public long GetInt32(int pos, int flag){
        if(flag == LONG_VAL){
            long result = GetInt16(pos,flag);
            result += (long)GetInt16(pos + 2,flag) << 16;
            return result;
        } else {
            return GetInt32(pos);
        }
    }

    // Java crap, even bytes are signed because idiot creator thought
    // i would be too stupid to understand unsigned math.
    public int getUnsignedByte(byte val)
    {
        return (int)val&0xff;
    }

    public int GetChkByte(int begin, int end)
    {
        byte chk = 0;

        for (int i = begin; i < end; i++)
            chk ^= buffer.get(i);

        return getUnsignedByte(chk);
    }

    public void Dump(int index, int count)
    {
        if(buffer.size() > count)
        {
            if ((index+count)<buffer.size())
            {
                for (int i = index; i < count; i++)
                    buffer.remove(index);
            }
        }
    }

    public String GetString(int pos, int length)
    {

        Byte[] src = new Byte[buffer.size()];
        buffer.toArray(src);
        Byte[] dst = new Byte[length];
        System.arraycopy(src,pos,dst,0,length);
        byte[] dstArr = new byte[dst.length];
        for(int i=0;i<dst.length;i++)
            dstArr[i] = dst[i];
        return new String(dstArr);
    }

    public String GetEncodedCommand()
    {
        return Base64.encodeToString(unbox(),Base64.NO_WRAP);
    }

    public String GetEncryptedCommand(XTEA enc)
    {
        int[] tmpInt = enc.fromByteToIntArray(unbox());
        tmpInt = enc.Encrypt(tmpInt);
        return enc.encodeB64(tmpInt);
    }

    public void toSBP(byte parser, byte tag, XTEA enc){
        byte[] backup = unbox();
        buffer.clear();
        AddInt8(parser); //buffer[0] = 65
        AddInt8(tag); //buffer[1] = 0
        AddByteArray(backup); //buffer = 65, 0, 80, 83, 86, 172, 13, 196, 9, 120, 5, 220, 5...
        backup = unbox();
        if(enc != null)
            backup = enc.Encrypt(backup);//buffer = 206, 199, 134, 8, 168, 104, 206, 174
        long crc32 = STM32CRC.CRC32_Ethernet(backup,0xffffffffl); //crc32 = 3982969269
        buffer.clear();
        AddInt32(crc32); //buffer = 181, 73, 103, 237
        AddInt8((enc != null) ? (byte)'#' : (byte)'$'); //buffer = 181, 73, 103, 237, 35
        AddByteArray(backup); //buffer = 181, 73, 103, 237, 35, 223, 16, 1, 187, 130, 183, 5, 188, 14, 37...
        String str = GetEncodedCommand(); // str = "tUln7SPfEAG7grcFvA4lGcpPZ4E6ppG8D9GMb0XBDH4pE+31PMEMfikT7fU8FxHqKskEvqHBDH4pE+31PMEMfikT7fU8wQx+KRPt9TzBDH4pE+31PMEMfikT7fU8wQx+KRPt9TzBDH4pE+31PA=="
        buffer.clear();
        box(str.getBytes()); //buffer = 116, 85, 108, 110, 55, 83, 80, 102, 69, 65, 71, 55, 83, 80, 102, 69, 65, 71, ...
        buffer.add((byte)'\r');
    }

    public int fromSBP(XTEA enc){
        if(buffer.size() < 5)
            return -1;
        String datastr = GetString(0,buffer.size());
        byte[] data = enc.fromIntToByteArray(enc.decodeB64(datastr));
        buffer.clear();
        box(data);
        byte mod = buffer.get(4);
        long crcr = GetInt32(0,LONG_VAL);
        removeRange(0,5);
        long crct = STM32CRC.CRC32_Ethernet(unbox(),0xffffffffl);
        if(crcr != crct)
            return -2;
        if(mod == (byte)'#'){
            byte[] b = enc.Decrypt(unbox());
            buffer.clear();
            box(b);
        }
        removeRange(0,2);
        return 0;
    }
}
