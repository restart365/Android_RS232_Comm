package com.dtiguardian.commport.vculib.struct;


import com.dtiguardian.commport.vculib.util.BufferHandler;
import static com.dtiguardian.commport.vculib.util.BufferHandler.LONG_VAL;

public class Status {
    private byte[] ValveStates = new byte[4];
    private long Cycles;
    private long Leds;
    private long RunningTime;
    private long Time;
    private int RPM;
    private int Battery;
    private int Flags;
    private byte AuxState;
    private byte Temperature;
    private byte ValveState;

    public Status(byte[] array){
        BufferHandler h = new BufferHandler(array);
        for(int i=0;i<4;i++){
            ValveStates[i] = h.GetInt8(i);
        }
        Cycles = h.GetInt32(4,LONG_VAL);
        Leds = h.GetInt32(8,LONG_VAL);
        RunningTime = h.GetInt32(12,LONG_VAL);
        Time = h.GetInt32(16,LONG_VAL);
        RPM = h.GetInt16(20,LONG_VAL);
        Battery = h.GetInt16(22,LONG_VAL);
        Flags = h.GetInt16(24,LONG_VAL);
        AuxState = h.GetInt8(26);
        Temperature = h.GetInt8(27);
        ValveState = h.GetInt8(28);
    }

    public byte getAuxState() {
        return AuxState;
    }

    public byte getTemperature() {
        return Temperature;
    }

    public byte getValveState() {
        return ValveState;
    }

    public byte[] getValveStates() {
        return ValveStates;
    }

    public int getVoltage() {
        return Battery;
    }

    public int getFlags() {
        return Flags;
    }

    public int getRPM() {
        return RPM;
    }

    public long getCycles() {
        return Cycles;
    }

    public long getLeds() {
        return Leds;
    }

    public long getRunningTime() {
        return RunningTime;
    }

    public long getUnixTime() {
        return Time;
    }
}
