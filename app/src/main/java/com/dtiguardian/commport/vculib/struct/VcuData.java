package com.dtiguardian.commport.vculib.struct;

import com.dtiguardian.commport.vculib.util.StringUtils;
import com.google.gson.Gson;

public class VcuData {
    public Signature signature;

    public SysParams SysParams;
    public Status Status;

    public String[] Labels;
    public int VcuVersion;

    public RpmConfig Rpm = new RpmConfig();
    public IMUConfig IMU = new IMUConfig();
    public RadioConfig Radio = new RadioConfig();
    public IOConfig[] IO = new IOConfig[4];
    public ValveConfig Valve = new ValveConfig();

    private String serialNumber;
    private String valveSerial;

    public boolean[] auxIsInput = new boolean[8];
    public boolean[] auxIsOutput = new boolean[8];



    public void setVcuSerial(String value){
        serialNumber = value;
        if(serialNumber.length() > 20)
            serialNumber = serialNumber.substring(0,20);
        else
            serialNumber = StringUtils.PadRight(serialNumber,'\0',20);
        byte[] tmp = serialNumber.getBytes();
        if(SysParams.serial != null)
            System.arraycopy(tmp,0,SysParams.serial,0,20);
    }

    public String getVcuSerial(){
        return serialNumber;
    }

    public void setValveSerial(String value){
        valveSerial = value;
        if(valveSerial.length() > 20)
            valveSerial = valveSerial.substring(0,20);
        else
            valveSerial = StringUtils.PadRight(valveSerial,'\0',20);
        byte[] tmp = valveSerial.getBytes();
        if(SysParams.valveSerial != null){
            System.arraycopy(tmp,0,SysParams.valveSerial,0,20);
        }
    }

    public void FromBitStream(byte[] bitstream){
        switch ((int)signature.paramsVersion){
            case 1:
                VcuParamsV1 p1 = new VcuParamsV1(bitstream);
                Rpm = p1.rpm;
                IMU = p1.imu;
                Radio = p1.radio;
                IO = p1.io;
                break;
            case 2:
                VcuParamsV2 p2 = new VcuParamsV2(bitstream);
                Rpm = p2.rpm;
                IMU = p2.imu;
                Radio = p2.radio;
                IO = p2.io;
                Valve = p2.valve;
                break;
        }
    }

    public byte[] ToBitStream(){
        byte[] ans = new byte[0];
        switch ((int)signature.paramsVersion){
            case 1:
                VcuParamsV1 p1 = new VcuParamsV1();
                p1.rpm = Rpm;
                p1.imu = IMU;
                p1.radio = Radio;
                p1.io = IO;
                ans = p1.toArray();
                break;
            case 2:
                VcuParamsV2 p2 = new VcuParamsV2();
                p2.rpm = Rpm;
                p2.imu = IMU;
                p2.radio = Radio;
                p2.io = IO;
                p2.valve = Valve;
                ans = p2.toArray();
                break;
        }
        return ans;
    }

    public static VcuData NewVcuData(byte[] data){
        Signature sig = new Signature(data);
        VcuData d = new VcuData(sig);
        switch((int)sig.paramsVersion){
            case 1:
            case 2:
                d.VcuVersion = ((int)sig.hardwareVersion[0] * 10) + (int)sig.hardwareVersion[1];
                return d;
            default:
                d.VcuVersion = -1;
                return d;
        }
    }

    protected VcuData(Signature s){
        Init();
        signature = s;
    }

    public VcuData(){
        Init();
    }

    public VcuData Export(){
        VcuData data;
        Gson gson = new Gson();
        String serialized = gson.toJson(this);
        data = gson.fromJson(serialized,VcuData.class);
        data.signature = this.signature;
        data.SysParams = this.SysParams;
        return data;
    }

    protected void Init(){
        serialNumber = "";
        valveSerial = "";
        Labels = new String[40];
        for(int i=0;i<40;i++){
            Labels[i] = "";
        }
        for(int i=0;i<8;i++)
            auxIsInput[i] = true;
        auxIsOutput = auxIsInput;
    }

    @Override
    public boolean equals(Object o) {
        boolean cmp = false;

        if(!(o instanceof VcuData))
            return cmp;
        else
            cmp = true;

        VcuData tmp = (VcuData) o;

        cmp &= Rpm.equals(tmp.Rpm);

        return cmp;
    }
}
