package com.dtiguardian.commport.vculib.struct;

import com.dtiguardian.commport.vculib.util.BufferHandler;

import static com.dtiguardian.commport.vculib.util.BufferHandler.LONG_VAL;

public class LogHeader {
    public long FirstAddress;
    public long NextAddress;
    public long NumberOfEntries;
    public long NewestTimestamp;
    public long OldestTimestamp;
    public int IsInit;

    public LogHeader(){}

    public LogHeader(byte[] array){
        BufferHandler h = new BufferHandler(array);
        FirstAddress = h.GetInt32(0,LONG_VAL);
        NextAddress = h.GetInt32(4,LONG_VAL);
        NumberOfEntries = h.GetInt32(8,LONG_VAL);
        NewestTimestamp = h.GetInt32(12,LONG_VAL);
        OldestTimestamp = h.GetInt32(16,LONG_VAL);
        IsInit = h.GetInt16(20,LONG_VAL);
    }
}
