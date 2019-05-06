package com.dtiguardian.commport.vculib.struct;

import java.io.Serializable;

public class RadioConfig implements Serializable {
    public long Id;
    public long MasterId;
    public int MasterDelay;
    public int Config;

    public RadioConfig() {}
}
