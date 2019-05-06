package com.dtiguardian.commport.vculib.util;

public class STM32CRC {
    public static long CRC32(byte[] data, long poly, long firstCRC){
        long crc = firstCRC;

        for(byte b : data){
            long d = b & 0xff;
            crc ^= d << 24;

            for(int i=0;i<8;i++){
                if((crc & (long)0x80000000) != 0){
                    crc = ((crc << 1) ^ poly);
                    if(crc > 0xffffffff)
                        crc = crc - 0x100000000l;
                } else {
                    crc <<= 1;
                }
            }
        }
        return crc;
    }

    public static long CRC32_Ethernet(byte[] data, long firstCRC){
        return CRC32(data,(long)0x4C11DB7,firstCRC);
    }

    public static short CalcCRC16(byte[] data, short poly, short firstCRC){
        short crc = firstCRC;

        for (byte b : data) {

            crc ^= (short) (b << 8);

            for (int j = 0; j < 8; j++) {
                if ((crc & 0x8000) > 0)
                    crc = (short) ((crc << 1) ^ poly);
                else
                    crc <<= 1;
            }
        }
        return crc;
    }

    public static short CalcCRC16_CCITT(byte[] data, short firstCRC){
        return CalcCRC16(data,(short) 0x1021,firstCRC);
    }
}
