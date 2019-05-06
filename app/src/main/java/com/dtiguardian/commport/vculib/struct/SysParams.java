package com.dtiguardian.commport.vculib.struct;

import com.dtiguardian.commport.vculib.util.BufferHandler;
import com.dtiguardian.commport.vculib.util.StringUtils;

import java.io.Serializable;

public class SysParams implements Serializable {
    public int unused;
    public int accX;
    public int accY;
    public int accZ;
    public byte auxIsInput;
    public byte auxIsOutput;
    public byte[] serial = new byte[20];
    public byte[] valveSerial = new byte[20];

    public SysParams(){
        unused = 0;
        accX = 0;
        accY = 0;
        accZ = 0;
        auxIsOutput = 0;
        auxIsInput = 0;
        serial = new byte[20];
        valveSerial = new byte[20];
    }

    public SysParams(byte[] array){
        BufferHandler h = new BufferHandler(array);
        unused = h.GetInt16(0);
        accX = h.GetInt16(2);
        accY = h.GetInt16(4);
        accZ = h.GetInt16(6);
        auxIsInput = h.GetInt8(8);
        auxIsOutput = h.GetInt8(9);
        for(int i=0;i<20;i++){
            serial[i] = h.GetInt8(10+i);
        }
        for(int i=0;i<20;i++){
            valveSerial[i] = h.GetInt8(30+i);
        }
    }

    public String getSerialString(){
        return StringUtils.TrimRight(new String(serial),'\0');
    }

    public String getValveSerialString(){
        return StringUtils.TrimRight(new String(valveSerial),'\0');
    }

    public byte[] toArray(){
        BufferHandler h = new BufferHandler();
        h.AddInt16(unused);
        h.AddInt16(accX);
        h.AddInt16(accY);
        h.AddInt16(accZ);
        h.AddInt8(auxIsInput);
        h.AddInt8(auxIsOutput);
        h.AddByteArray(serial);
        h.AddByteArray(valveSerial);
        return h.ToByteArray();
    }
}
