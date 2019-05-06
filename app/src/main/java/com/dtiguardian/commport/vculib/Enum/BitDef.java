package com.dtiguardian.commport.vculib.Enum;

public enum BitDef {
    OpenValve (0),
    PullHigh (1),
    NC_Switch (2),
    PTO (3),
    ESTOP (4);

    private final int bit;

    private BitDef(int levelCode){
        this.bit = levelCode;
    }
}
