package com.dtiguardian.commport.vculib.struct;

import com.dtiguardian.commport.vculib.Enum.BitDef;

import java.io.Serializable;

public class IOConfig implements Serializable {
    public int config0;
    public int config1;
    public int delay;
    public int width;
    public byte ovr;
    public byte tie;

    public IOConfig(){
        config0 = 0;
        config1 = 0;
        delay = 0;
        width = 0;
        ovr = 0;
        tie = 0;
    }

    public void SetMode(short mode){
        config0 &= 0xff00;
        config0 |= (short)(mode & 0x00ff);
    }

    public short GetMode(){
        return (short)(config0 & 0x00ff);
    }

    public void SetTrigger(short trg){
        config0 &= 0x00ff;
        config0 |= (short)(trg << 8);
    }

    public short GetTrigger()
    {
        return (short)(config0 >> 8);
    }

    public void SetFunctionBit(BitDef def, boolean state)
    {
        short shift = (short)def.ordinal();
        config1 &= (short)(~(1 << shift));
        if(state)
            config1 |= (short)(1 << shift);
    }

    public boolean GetFunctionBit(BitDef def)
    {
        short shift = (short)def.ordinal();
        return toBoolean(config1 & (1<< shift));
    }

    public void SetDelay(short del)
    {
        delay = del;
    }

    public void SetInputDefaults()
    {
        SetMode((short) 1);
        SetFunctionBit(BitDef.OpenValve, false);
        SetFunctionBit(BitDef.PullHigh, false);
        SetFunctionBit(BitDef.NC_Switch, false);
        SetFunctionBit(BitDef.PTO, false);
        SetFunctionBit(BitDef.ESTOP, false);
        SetDelay((byte)0);
    }

    public void SetOutputDefaults()
    {
        SetMode((short)2);
        SetFunctionBit(BitDef.OpenValve, false);
        SetFunctionBit(BitDef.PullHigh, false);
        SetFunctionBit(BitDef.NC_Switch, false);
        SetFunctionBit(BitDef.PTO, false);
        SetFunctionBit(BitDef.ESTOP, false);
        SetDelay((short)0);
        SetTrigger((short)0);
        width = 0;
    }

    public void SetDisabledDefaults()
    {
        SetMode((short)0);
        SetFunctionBit(BitDef.OpenValve, false);
        SetFunctionBit(BitDef.PullHigh, false);
        SetFunctionBit(BitDef.NC_Switch, false);
        SetFunctionBit(BitDef.PTO, false);
        SetFunctionBit(BitDef.ESTOP, false);
        SetDelay((short)0);
        SetTrigger((short)0);
        width = 0;
    }

    public static boolean toBoolean(int val)
    {
        return(val!=0);
    }
}
