package com.dtiguardian.commport.vculib.struct;


import com.dtiguardian.commport.vculib.util.BufferHandler;

public class VcuParamsV2 {
    public RpmConfig rpm;
    public IMUConfig imu;
    public RadioConfig radio;
    public IOConfig[] io;
    public ValveConfig valve;

    public VcuParamsV2(){}

    public VcuParamsV2(byte[] array) {
        BufferHandler h = new BufferHandler(array);

        rpm = new RpmConfig();
        imu = new IMUConfig();
        radio = new RadioConfig();
        io = new IOConfig[4];
        valve = new ValveConfig();

        rpm.FullScale = h.GetInt16(0,BufferHandler.LONG_VAL);
        rpm.Shutdown = h.GetInt16(2,BufferHandler.LONG_VAL);
        rpm.PTO = h.GetInt16(4,BufferHandler.LONG_VAL);
        rpm.Test = h.GetInt16(6,BufferHandler.LONG_VAL);
        rpm.Idle = h.GetInt16(8,BufferHandler.LONG_VAL);
        rpm.Pulses = h.GetInt16(10,BufferHandler.LONG_VAL);
        rpm.Source = h.GetInt8(12);
        rpm.Flags = h.GetInt8(13);

        imu.ShutdownAngle = h.GetInt16(14,BufferHandler.LONG_VAL);
        imu.Holdoff = h.GetInt16(16,BufferHandler.LONG_VAL);
        imu.AccelConfig0 = h.GetInt16(18,BufferHandler.LONG_VAL);
        imu.AccelConfig1 = h.GetInt16(20,BufferHandler.LONG_VAL);
        imu.GyroConfig0 = h.GetInt16(22,BufferHandler.LONG_VAL);
        imu.GyroConfig1 = h.GetInt16(24,BufferHandler.LONG_VAL);

        radio.Id = h.GetInt32(26,BufferHandler.LONG_VAL);
        radio.MasterId = h.GetInt32(30,BufferHandler.LONG_VAL);
        radio.MasterDelay = h.GetInt16(34,BufferHandler.LONG_VAL);
        radio.Config = h.GetInt16(36,BufferHandler.LONG_VAL);

        for (int i=0;i<4;i++){
            io[i] = new IOConfig();
            io[i].config0 = h.GetInt16(40+10*i,BufferHandler.LONG_VAL);
            io[i].config1 = h.GetInt16(42+10*i,BufferHandler.LONG_VAL);
            io[i].delay = h.GetInt16(44+10*i,BufferHandler.LONG_VAL);
            io[i].width = h.GetInt16(46+10*i,BufferHandler.LONG_VAL);
            io[i].ovr = h.GetInt8(48+10*i);
            io[i].tie = h.GetInt8(49+10*i);
        }

        valve.Config0 = h.GetInt16(80,BufferHandler.LONG_VAL);
        valve.Config1 = h.GetInt16(82, BufferHandler.LONG_VAL);
        valve.Delay = h.GetInt16(84,BufferHandler.LONG_VAL);
        valve.Reset = h.GetInt16(86,BufferHandler.LONG_VAL);
        valve.Extra0 = h.GetInt16(88,BufferHandler.LONG_VAL);
        valve.Extra1 = h.GetInt16(90,BufferHandler.LONG_VAL);
    }

    public byte[] toArray(){
        BufferHandler h = new BufferHandler();
        h.AddInt16((long)rpm.FullScale);
        h.AddInt16((long)rpm.Shutdown);
        h.AddInt16((long)rpm.PTO);
        h.AddInt16((long)rpm.Test);
        h.AddInt16((long)rpm.Idle);
        h.AddInt16((long)rpm.Pulses);
        h.AddInt8(rpm.Source);
        h.AddInt8(rpm.Flags);

        h.AddInt16((long)imu.ShutdownAngle);
        h.AddInt16((long)imu.Holdoff);
        h.AddInt16((long)imu.AccelConfig0);
        h.AddInt16((long)imu.AccelConfig1);
        h.AddInt16((long)imu.GyroConfig0);
        h.AddInt16((long)imu.GyroConfig1);

        h.AddInt32((long)radio.Id);
        h.AddInt32((long)radio.MasterId);
        h.AddInt16((long)radio.MasterDelay);
        h.AddInt16((long)radio.Config);

        h.AddInt16(0);

        for(int i=0;i<4;i++){
            h.AddInt16((long)io[i].config0);
            h.AddInt16((long)io[i].config1);
            h.AddInt16((long)io[i].delay);
            h.AddInt16((long)io[i].width);
            h.AddInt8(io[i].ovr);
            h.AddInt8(io[i].tie);
        }

        h.AddInt16((long)valve.Config0);
        h.AddInt16((long)valve.Config1);
        h.AddInt16((long)valve.Delay);
        h.AddInt16((long)valve.Reset);
        h.AddInt16((long)valve.Extra0);
        h.AddInt16((long)valve.Extra1);

        return h.ToByteArray();
    }
}
