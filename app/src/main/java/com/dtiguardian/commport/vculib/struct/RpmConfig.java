package com.dtiguardian.commport.vculib.struct;

import java.io.Serializable;

public class RpmConfig implements Serializable {
    public int FullScale;
    public int Shutdown;
    public int PTO;
    public int Test;
    public int Idle;
    public int Pulses;
    public byte Source;
    public byte Flags;

    public RpmConfig(){}

    @Override
    public boolean equals(Object obj) {
        boolean cmp = false;

        if(!(obj instanceof RpmConfig))
            return cmp;
        else
            cmp = true;

        RpmConfig tmp = (RpmConfig) obj;

        cmp &= FullScale == tmp.FullScale;
        cmp &= Shutdown == tmp.Shutdown;
        cmp &= PTO == tmp.PTO;
        cmp &= Test == tmp.Test;
        cmp &= Idle == tmp.Idle;
        cmp &= Pulses == tmp.Pulses;
        cmp &= Source == tmp.Source;
        cmp &= Flags == tmp.Flags;

        return cmp;

    }

    public void setCanBusRate(byte mode){
        int tmp = Flags;
        tmp &= ~0x06;
        Flags = (byte)tmp;
        Flags |= (byte)((mode & 0x03) << 1);
    }

    public byte getCanBusRate(){
        return (byte)((Flags >> 1) & 0x03);
    }
}
