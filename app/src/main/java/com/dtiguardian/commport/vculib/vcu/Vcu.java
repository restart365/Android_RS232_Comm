package com.dtiguardian.commport.vculib.vcu;

import android.support.annotation.NonNull;

import com.dtiguardian.commport.Rs232;
import com.dtiguardian.commport.Stopwatch;
import com.dtiguardian.commport.vculib.Enum.VcuCommand;
import com.dtiguardian.commport.vculib.Enum.VcuConnectionState;
import com.dtiguardian.commport.vculib.Enum.VcuDriver;
import com.dtiguardian.commport.vculib.Interface.VcuInterface;
import com.dtiguardian.commport.vculib.struct.VcuData;
import com.dtiguardian.commport.vculib.util.Util;
import com.dtiguardian.commport.vculib.util.XTEA;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class Vcu {
    protected VcuInterface vcuEvent;

    protected volatile VcuData mainData;
    protected VcuData backupData;
    protected volatile boolean connectionAllowed;
    protected XTEA encryptionUnit = new XTEA();
    protected final Object portLock = new Object();
    protected final List<byte[]> cmdQueue = new ArrayList<byte[]>();

    protected ConnectionThread cThread;

    public Rs232 port;
    public VcuDriver driver;
    public boolean rebootAfterDisconnect;
    protected boolean isOnline;


    public Vcu(){}

    /**
     * Constructor
     * @param v This is the structure to save all new style VCU data.
     * @param p This is the port for connection
     */
    public Vcu(VcuData v, Rs232 p){
        encryptionUnit.importKey("0123456789ABCDEF");
        mainData = v;
        port = p;
        driver = VcuDriver.vcu50;
        backupData = new VcuData();
        backupData = mainData.Export();
        driver = VcuDriver.Base;
    }

    /**
     * Method to connect new style VCU
     * @param i This is the listener to update VCU connection status
     */
    public void Connect(@NonNull VcuInterface i){
        vcuEvent = i;
        cThread = new ConnectionThread();
        cThread.start();
    }

    /**
     * Method to disconnect new style VCU
     */
    public void Disconnect(){
        if(isOnline){
            cThread.disconnect();
        }
    }

    protected boolean readStatusPacket(){
        throw new RuntimeException("Not implemented");
    }

    public boolean readParams(){
        throw new RuntimeException("Not implemented");
    }

    public boolean readLabels(){
        throw new RuntimeException("Not implemented");
    }

    public boolean saveData(int techid){
        throw new RuntimeException("Not implemented");
    }

    public boolean saveLabels(){
        throw new RuntimeException("Not implemented");
    }

    public boolean saveAllData(int techid) {
        throw new RuntimeException("Not implemented");
    }

    public boolean saveSysParams(){
        throw new RuntimeException("Not implemented");
    }

    public void rename(String cmd){
        throw new RuntimeException("Not implemented");
    }

    public Date getTime(Date time){
        throw new RuntimeException("Not implemented");
    }

    public String getRunningTime(){
        Calendar c = Calendar.getInstance();
        return c.getTime().toString();
    }

    public boolean setTime(Date time){
        return false;
    }

    public boolean simpleCommand(VcuCommand cmd){
        throw new RuntimeException("Not implemented");
    }

    public Object command(VcuCommand cmd, Object[] args){
        throw new RuntimeException("Not implemented");
    }



    protected boolean labelsChanged(){
        for(int i=0;i<40;i++){
            if(!mainData.Labels[i].equals(backupData.Labels[i]))
                return true;
        }
        return false;
    }

    public void backupParams(){
        backupData = (VcuData) Util.deepClone(mainData);
    }

    public void restoreParams(){
        mainData = (VcuData)Util.deepClone(backupData);
    }

    public void pushUpdate(){
        if(vcuEvent != null){
            vcuEvent.OnUpdateAvailable();
        }
    }

    public void backupMainData(){
        backupData = (VcuData) Util.deepClone(mainData);
    }

    private void connectionEnd(){
        if(vcuEvent != null){
            vcuEvent.OnConnectionChanged(VcuConnectionState.Disconnected);
        }
    }

    public VcuData getMainData() {
        return mainData;
    }

    public void setMainData(VcuData value){
        mainData = value;
    }

    public VcuData getBackupData(){
        return backupData;
    }

    public void setBackupData(VcuData value){
        backupData = value;
    }

    public void connectionStatechanged(VcuConnectionState state){
        if(vcuEvent != null)
            vcuEvent.OnConnectionChanged(state);
    }

    public void reboot(){
        Disconnect();
        connectionStatechanged(VcuConnectionState.Reboot);
        vcuEvent = null;
    }

    class ConnectionThread extends Thread{
        Stopwatch stopwatch = new Stopwatch();
        int retries = 0;

        public void disconnect(){
            connectionAllowed = false;
        }

        @Override
        public void run() {
            isOnline = true;
            connectionAllowed = true;
            stopwatch.Start();
            while(connectionAllowed){
                if(stopwatch.Read() >= 80){
                    if(readStatusPacket()){
                        retries = 0;
                        pushUpdate();
                    } else {
                        if(retries > 9){
                            Disconnect();
                            connectionStatechanged(VcuConnectionState.ConnectionLost);
                            vcuEvent = null;
                        } else
                            retries++;
                    }

                    stopwatch.Restart();
                } else {
                    if(cmdQueue.size() > 0){
                        byte[] cmd = cmdQueue.get(0);
                        synchronized (cmdQueue){
                            cmdQueue.remove(0);
                        }
                        synchronized (portLock){
                            port.write(cmd);
                            try{
                                Thread.sleep(4);
                            } catch (InterruptedException e){
                                e.printStackTrace();
                            }
                        }
                    }
                    try{
                        Thread.sleep(1);
                    } catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
            isOnline = false;
            connectionEnd();
        }
    }
}
