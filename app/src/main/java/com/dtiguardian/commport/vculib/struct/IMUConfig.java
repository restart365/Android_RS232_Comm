package com.dtiguardian.commport.vculib.struct;

import java.io.Serializable;

public class IMUConfig implements Serializable {
    public int ShutdownAngle;
    public int Holdoff;
    public int AccelConfig0;
    public int AccelConfig1;
    public int GyroConfig0;
    public int GyroConfig1;

    public IMUConfig() {}
}
