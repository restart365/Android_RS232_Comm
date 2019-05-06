package com.dtiguardian.commport.vculib.struct;

import com.dtiguardian.commport.vculib.util.BufferHandler;

import static com.dtiguardian.commport.vculib.util.BufferHandler.LONG_VAL;

public class LogEntry {
    public long Timestamp;
    public int Message;
    public int Param0;
    public long Param1;
    public long Param2;

    public LogEntry(byte[] array){
        BufferHandler h = new BufferHandler(array);
        Timestamp = h.GetInt32(0,LONG_VAL);
        Message = h.GetInt16(4,LONG_VAL);
        Param0 = h.GetInt16(6,LONG_VAL);
        Param1 = h.GetInt32(8,LONG_VAL);
        Param2 = h.GetInt32(12,LONG_VAL);
    }
}
