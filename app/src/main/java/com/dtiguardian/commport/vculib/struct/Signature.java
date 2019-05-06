package com.dtiguardian.commport.vculib.struct;

import com.dtiguardian.commport.vculib.util.BufferHandler;

import java.io.Serializable;

public class Signature implements Serializable {
    public long paramsVersion;
    public long[] firmwareVersion;
    public long[] hardwareVersion;
    public byte numberOfValves;
    public byte rpmSources;

    public Signature(byte hw, byte fw){
        hardwareVersion = new long[4];
        hardwareVersion[0] = hw;
        firmwareVersion = new long[4];
        firmwareVersion[0] = fw;
        numberOfValves = 1;
        paramsVersion = 0;
        rpmSources = 0;
    }

    public Signature(byte[] array){
        firmwareVersion = new long[4];
        hardwareVersion = new long[4];

        BufferHandler h = new BufferHandler(array);

        paramsVersion = h.GetUInt8(0);
        int i;
        for(i=0;i<4;i++){
            firmwareVersion[i] = h.GetUInt8(4 + 4 * i);
            hardwareVersion[i] = h.GetUInt8(20 + 4 * i);
        }
        numberOfValves = h.GetInt8(36);
        rpmSources = h.GetInt8(37);
    }

    public String GetFirwareString(){
        StringBuffer buffer = new StringBuffer();
        buffer.append(firmwareVersion[0]).append(".")
                .append(firmwareVersion[1]).append(".")
                .append(firmwareVersion[2]).append(" Build ")
                .append(firmwareVersion[3]);
        return buffer.toString();
    }

    public String GetHardwareString(){
        StringBuffer buffer = new StringBuffer();
        buffer.append(hardwareVersion[0]).append(".")
                .append(hardwareVersion[1]).append(".")
                .append(hardwareVersion[2]).append(" Ver ")
                .append(hardwareVersion[3]);
        return buffer.toString();
    }
}
