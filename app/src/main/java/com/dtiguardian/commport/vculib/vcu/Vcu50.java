package com.dtiguardian.commport.vculib.vcu;

import com.dtiguardian.commport.Rs232;
import com.dtiguardian.commport.vculib.Enum.VcuCommand;
import com.dtiguardian.commport.vculib.Enum.VcuDriver;
import com.dtiguardian.commport.vculib.struct.LogEntry;
import com.dtiguardian.commport.vculib.struct.LogHeader;
import com.dtiguardian.commport.vculib.struct.Status;
import com.dtiguardian.commport.vculib.struct.SysParams;
import com.dtiguardian.commport.vculib.struct.VcuData;
import com.dtiguardian.commport.vculib.util.BufferHandler;
import com.dtiguardian.commport.vculib.util.StringUtils;

import java.security.InvalidParameterException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Vcu50 extends Vcu{
    /**
     * Constructor
     * @param v This is the structure to save all VCU5 data.
     * @param p This is the port for connection
     */
    public Vcu50(VcuData v, Rs232 p) {
        mainData = v;
        port = p;
        driver = VcuDriver.vcu50;
    }

    /**
     * This method reads system parameters
     * @return Return true if no error found. Otherwise false.
     */
    @Override
    public boolean readParams() {
        try {
            BufferHandler h0 = new BufferHandler("PRS");
            h0.toSBP((byte) 'A', (byte) 0, encryptionUnit);
            byte[] ans = port.readPacket(h0.ToByteArray(), 250);
            h0 = new BufferHandler(ans);
            int err = h0.fromSBP(encryptionUnit);
            if (err == 0) {
                SysParams par = new SysParams(h0.ToByteArray());
                mainData.SysParams = par;
                mainData.setVcuSerial(par.getSerialString());
                mainData.setValveSerial(par.getValveSerialString());
            } else
                return false;
        } catch (Exception e) {
            return false;
        }

        try {
            BufferHandler h0 = new BufferHandler("PRV");
            h0.toSBP((byte) 'A', (byte) 0, encryptionUnit);
            byte[] ans = port.readPacket(h0.ToByteArray(), 300);
            h0 = new BufferHandler(ans);
            int err = h0.fromSBP(encryptionUnit);
            if (err == 0) {
                mainData.FromBitStream(h0.ToByteArray());
            } else
                return false;
        } catch (Exception e) {
            return false;
        }

        backupParams();
        return true;
    }

    /**
     * This method reads labels
     * @return Return true if no error found. Otherwise false.
     */
    @Override
    public boolean readLabels() {
        for (int i = 0; i < mainData.Labels.length; i++) {
            try {
                BufferHandler h0 = new BufferHandler("PRL");
                h0.AddInt16((long)i);
                h0.toSBP((byte) 'A', (byte) 0, encryptionUnit);
                byte[] ans = port.readPacket(h0.ToByteArray(), 150);
                h0 = new BufferHandler(ans);
                int err = h0.fromSBP(encryptionUnit);
                if (err == 0)
                    mainData.Labels[i] = h0.GetString(0, h0.Buffer().size()).trim();
                else
                    mainData.Labels[i] = "Timeout String";
            } catch (Exception e) {
                mainData.Labels[i] = "Invalid String";
            }
        }
        return true;
    }

    /**
     * This method reads latest status from VCU
     * @return Return true if no error found. Otherwise false.
     */
    @Override
    protected boolean readStatusPacket() {
        try {
            BufferHandler h0 = new BufferHandler("PU");
            h0.toSBP((byte) 'A', (byte) 0, encryptionUnit);
            byte[] ans = port.readPacket(h0.ToByteArray(), 102);
            {
                BufferHandler h1 = new BufferHandler(ans);
                int err = h1.fromSBP(encryptionUnit);
                if (err == 0)
                    mainData.Status = new Status(h1.ToByteArray());
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * This method retrieves VCU time.
     * @param time A new Date
     * @return Date format VCU time.
     */
    @Override
    public Date getTime(Date time) {
        if (isOnline) {
            try {
                time = toDate(mainData.Status.getUnixTime());
                return time;
            } catch (Exception e) {
                time = Calendar.getInstance().getTime();
                return time;
            }
        } else {
            time = Calendar.getInstance().getTime();
            return time;
        }
    }

    /**
     * This method reads engine running time
     * @return Engine running time in String format.
     */
    @Override
    public String getRunningTime() {
        Calendar c = Calendar.getInstance();
        if(isOnline){
            long h, m, s;
            s = mainData.Status.getRunningTime();
            h = s/3600;
            s -= h*3600;
            m = s/60;
            s -= m*60;
            StringBuffer buffer = new StringBuffer();
            buffer.append(h).append(":").append(m).append(":").append(s);
            return buffer.toString();
        }

        return "N/A";
    }

    /**
     * This method sets VCU time.
     * @param time Current time that is sent to VCU.
     * @return Return true if no error found. Otherwise false.
     */
    @Override
    public boolean setTime(Date time) {
        if (isOnline) {
            try {
                BufferHandler h0 = new BufferHandler("PT");
                Calendar cal = Calendar.getInstance();
                cal.setTime(time);
                cal.setTimeZone(TimeZone.getTimeZone("UTC"));
                h0.AddInt8(cal.get(Calendar.YEAR) - 2000);
                h0.AddInt8(cal.get(Calendar.MONTH)+1);
                h0.AddInt8(cal.get(Calendar.DAY_OF_MONTH));
                h0.AddInt8(cal.get(Calendar.HOUR_OF_DAY));
                h0.AddInt8(cal.get(Calendar.MINUTE));
                h0.AddInt8(cal.get(Calendar.SECOND));
                h0.toSBP((byte) 'A', (byte) 0, encryptionUnit);
                byte[] ans = port.readPacket(h0.ToByteArray(), 2000);
                h0 = new BufferHandler(ans);
                h0.fromSBP(encryptionUnit);
                if (h0.Buffer().get(0) == 0x06)
                    return true;
                else
                    return false;
            } catch (Exception e) {
                return false;
            }
        } else
            return false;
    }

    /**
     * This method saves local labels to VCU.
     * @return Return true if succeeded, otherwise false.
     */
    @Override
    public boolean saveLabels() {
        if (labelsChanged()) {
            for (int i = 0; i < mainData.Labels.length; i++) {
                BufferHandler h = new BufferHandler("PSL");
                h.AddInt16((long)i);
                h.AddString(StringUtils.PadRight(mainData.Labels[i], ' ', 20));
                h.toSBP((byte) 'A', (byte) 0, encryptionUnit);
                try {
                    byte[] ans = port.readPacket(h.ToByteArray(), 100);
                    h = new BufferHandler(ans);
                    h.fromSBP(encryptionUnit);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    /**
     * This method saves local data and settings to VCU, including rpm settings, auxiliary configurations,
     * key fob settings, serial numbers, versions, etc.
     * @param techid ID of technician that makes this change.
     * @return Return true if succeeded, otherwise false.
     */
    @Override
    public boolean saveData(int techid) {
        BufferHandler h = new BufferHandler("PSI");
        h.AddInt32((long)techid);
        h.toSBP((byte) 'A', (byte) 0, encryptionUnit);
        try {
            byte[] ans = port.readPacket(h.ToByteArray(), 100);
            h = new BufferHandler(ans);
            h.fromSBP(encryptionUnit);

            if (h.Buffer().size() < 1)
                return false;
            if (h.Buffer().get(0) != 6)
                return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        h = new BufferHandler("PSV");
        byte[] array = mainData.ToBitStream();
        h.AddByteArray(array);
        h.toSBP((byte) 'A', (byte) 0, encryptionUnit);
        try {
            byte[] ans = port.readPacket(h.ToByteArray(), 400);
            h = new BufferHandler(ans);
            h.fromSBP(encryptionUnit);
            if (h.Buffer().size() < 1)
                return false;
            if (h.Buffer().get(0) != 6)
                return false;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Save both labels and local data to VCU
     * @param techid ID of technician that makes this change.
     * @return Return true if succeeded, otherwise false.
     */
    @Override
    public boolean saveAllData(int techid) {
        if(saveData(techid)){
            if(saveLabels()){
                if(saveSysParams()){
                    backupParams();
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    /**
     * Save system parameters to VCU
     * @return Return true if succeeded, otherwise false.
     */
    @Override
    public boolean saveSysParams() {
        BufferHandler h = new BufferHandler("PSS");
        byte[] array = mainData.SysParams.toArray();
        h.AddByteArray(array);
        h.toSBP((byte) 'A', (byte) 0, encryptionUnit);
        synchronized (portLock) {
            try {
                byte[] ans = port.readPacket(h.ToByteArray(), 250);
                h = new BufferHandler(ans);
                h.fromSBP(encryptionUnit);
                if (h.Buffer().get(0) != 6)
                    return false;
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    /**
     * This method send command to VCU to open valve.
     */
    private void openValve() {
        BufferHandler h = new BufferHandler("VO");
        h.toSBP((byte) 'A', (byte) 0, encryptionUnit);
        synchronized (cmdQueue) {
            cmdQueue.add(h.ToByteArray());
        }
    }

    /**
     * This method send command to VCU to close valve.
     */
    private void closeValve() {
        BufferHandler h = new BufferHandler("VC");
        h.toSBP((byte) 'A', (byte) 0, encryptionUnit);
        synchronized (cmdQueue) {
            cmdQueue.add(h.ToByteArray());
        }
    }

    /**
     * This method reset VCU to default
     * @return Return true if succeed, otherwise false.
     */
    private boolean setDefaults() {
        try {
            BufferHandler h = new BufferHandler("PD");
            h.toSBP((byte) 'A', (byte) 0, encryptionUnit);
            synchronized (cmdQueue) {
                cmdQueue.add(h.ToByteArray());
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * This method sets trim. Not yet used
     * @param trim
     */
    private void setTrim(int trim) {
        BufferHandler h = new BufferHandler("PE");
        h.AddInt16(trim);
        h.toSBP((byte) 'A', (byte) 0, encryptionUnit);
        synchronized (cmdQueue) {
            cmdQueue.add(h.ToByteArray());
        }
    }

    private void setCycler(int trim) {
        BufferHandler h = new BufferHandler("PC");
        h.AddInt16(trim);
        h.toSBP((byte) 'A', (byte) 0, encryptionUnit);
        synchronized (cmdQueue) {
            cmdQueue.add(h.ToByteArray());
        }
    }

    private boolean clearCycleCount() {
        try {
            BufferHandler h = new BufferHandler("VX");
            h.toSBP((byte) 'A', (byte) 0, encryptionUnit);
            synchronized (cmdQueue) {
                cmdQueue.add(h.ToByteArray());
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void rename(String cmd) {
        BufferHandler h = new BufferHandler(cmd);
        h.toSBP((byte) 'A', (byte) 0, encryptionUnit);
        synchronized (cmdQueue){
            cmdQueue.add(h.ToByteArray());
        }
    }

    private Object clearLog() {
        Object retObject = null;
        try {
            BufferHandler h0 = new BufferHandler("LW~");
            h0.toSBP((byte) 'A', (byte) 0, encryptionUnit);
            byte[] ans = port.readPacket(h0.ToByteArray(), 100);
            h0 = new BufferHandler(ans);
            int err = h0.fromSBP(encryptionUnit);
            if (err == 0)
                retObject = h0.GetInt32(0,BufferHandler.LONG_VAL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retObject;
    }

    private Object getLogHeaderObj() {
        Object retObject = null;
        try {
            BufferHandler h0 = new BufferHandler("LH~");
            h0.toSBP((byte) 'A', (byte) 0, encryptionUnit);
            byte[] ans = port.readPacket(h0.ToByteArray(), 100);
            h0 = new BufferHandler(ans);
            int err = h0.fromSBP(encryptionUnit);
            if (err == 0) {
                LogHeader header = new LogHeader(h0.ToByteArray());
                retObject = (Object) header;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retObject;
    }

    private Object getLogEventObj(int ev) {
        Object retObject = null;
        try {
            BufferHandler h0 = new BufferHandler("LE");
            h0.AddInt32((long)ev);
            h0.toSBP((byte) 'A', (byte) 0, encryptionUnit);
            byte[] ans = port.readPacket(h0.ToByteArray(), 100);
            h0 = new BufferHandler(ans);
            int err = h0.fromSBP(encryptionUnit);
            if (err == 0) {
                LogEntry entry = new LogEntry(h0.ToByteArray());
                retObject = (Object) entry;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retObject;
    }

    private boolean sendFirmwareHeader(byte[] header, int crc) {
        try {
            BufferHandler h = new BufferHandler("H~");
            h.AddByteArray(header);
            h.toSBP((byte) 'F', (byte) 0, null);
            byte[] ans = port.readPacket(h.ToByteArray(), 150);
            h = new BufferHandler(ans);
            h.fromSBP(null);
            int crcc = (int) h.GetInt32(0);
            if (crcc == crc) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean sendFirmwareLine(int line, byte[] data, int crc) {
        try {
            BufferHandler h = new BufferHandler("F~");
            h.AddInt16(line);
            h.AddInt32((int) crc);
            h.AddByteArray(data);
            h.toSBP((byte) 'F', (byte) 0, null);
            byte[] ans = port.readPacket(h.ToByteArray(), 100);
            h = new BufferHandler(ans);
            int crcc = (int) h.GetInt32(0);
            if (crcc == crc)
                return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean verifyFirmware(int header_crc, int flash_crc) {
        try {
            BufferHandler h = new BufferHandler("V~");
            h.toSBP((byte) 'F', (byte) 0, null);
            byte[] ans = port.readPacket(h.ToByteArray(), 500);
            h = new BufferHandler(ans);
            h.fromSBP(null);
            int hcrc = (int) h.GetInt32(0);
            int fcrc = (int) h.GetInt32(4);
            if (hcrc == header_crc && fcrc == flash_crc)
                return true;
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    private boolean jumpToBootloader(){
        try{
            BufferHandler h = new BufferHandler("B~");
            h.toSBP((byte) 'F', (byte) 0, null);
            port.write(h.ToByteArray());
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Object command(VcuCommand cmd, Object[] args) {
        Object retVal = null;
        try{
            switch (cmd){
                case SendFirmwareHeader:
                    retVal = sendFirmwareHeader((byte[])args[0], (int)args[1]);
                    break;
                case SendFirmwareLine:
                    retVal = sendFirmwareLine((int)args[0], (byte[])args[1], (int)args[2]);
                    break;
                case VerifyFirmware:
                    retVal = verifyFirmware((int)args[0],(int)args[1]);
                    break;
                case GetLogHeader:
                    retVal = getLogHeaderObj();
                    break;
                case GetLogEntry:
                    retVal = getLogEventObj((int)args[0]);
                    break;
                case ClearLog:
                    retVal = clearLog();
                    break;
                case SetTrim:
                    setTrim((int)args[0]);
                    break;
                case Cycler:
                    setCycler((int)args[0]);
                    break;
                default:
                    throw new InvalidParameterException(cmd.toString() + " command handling not implemented " + driver.toString());
            }
            Thread.sleep(5);
        } catch (InterruptedException e){
            e.printStackTrace();
        } catch (Exception e){
            throw new NullPointerException("Arguments are null or size unexpected");
        }
        return retVal;
    }

    @Override
    public boolean simpleCommand(VcuCommand cmd) {
        boolean retVal = false;
        try{
            switch (cmd){
                case JumpToBootloader:
                    jumpToBootloader();
                    break;
                case CloseValve:
                    closeValve();
                    break;
                case OpenValve:
                    openValve();
                    break;
                case ClearValveCycleCount:
                    retVal = clearCycleCount();
                    break;
                case SetDefaults:
                    retVal = setDefaults();
                    break;
                default:
                    throw new InvalidParameterException(cmd.toString() + " command handling not implemented " + driver.toString());
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return retVal;
    }

    //======================================================================================= Helper
    public static Date toDate(long unixtime){
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        cal.set(Calendar.YEAR, 1970);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND,0);
        cal.set(Calendar.AM_PM,Calendar.AM);

        cal.add(Calendar.SECOND,(int)unixtime);
        cal.setTimeZone(TimeZone.getDefault());
        return cal.getTime();
    }
}
