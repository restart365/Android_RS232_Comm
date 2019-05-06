package com.dtiguardian.commport;

import java.util.concurrent.TimeoutException;

public class Rs232  extends SerialHelper {

    private Buffer buffer;
    private final int size = 2048;

    private final int VCU_BAUD_RATE = 115200;

    private final Object portLock;


    public Rs232(String sPort) {
        super(sPort);
        buffer = new Buffer();
        portLock = new Object();
    }

    public void connect(){
        setBaudRate(VCU_BAUD_RATE);

        try{
            open(false);

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public String readLine(){
        String temp = "";
        try {
            temp = buffer.readLine();
        } catch (TimeoutException e){
            e.printStackTrace();
        }
        return temp;
    }

    public void write(byte[] data){
        try{
            interlockedTransaction(data, false, 100);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public byte[] readPacket(byte[] data, int timeout){
        try{
            return interlockedTransaction(data, true, timeout);
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private byte[] interlockedTransaction(byte[] data, boolean read, int timeout) {
        byte[] retByte = new byte[0];
        synchronized (portLock){
            try{
                buffer.setTimeout(timeout);
                buffer.clear();
                send(data);
                if(read)
                    retByte = buffer.readLineBytes();
            } catch (Exception e){
                e.printStackTrace();
            } finally {
                buffer.restoreTimeout();
                restoreTimeout();
            }
        }
        return retByte;
    }

//    public byte[] readLineBytes(){
//        return buffer.readLineBytes();
//    }

    public void setTimeout(int mili){
        buffer.setTimeout(mili);
    }

    public void restoreTimeout(){
        buffer.restoreTimeout();
    }

    public void discardBuffer(){
        buffer.clear();
    }

    @Override
    protected void onDataReceived(byte[] ComRecData) {
        buffer.add(ComRecData,ComRecData.length);
    }
}
