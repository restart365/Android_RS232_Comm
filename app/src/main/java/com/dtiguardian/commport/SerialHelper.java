package com.dtiguardian.commport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import android_serial_api.SerialPort;

public abstract class SerialHelper {
    private SerialPort mSerialPort;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;
    private SendThread mSendThread;
    private String sPort="/dev/ttyHSL0";
    private byte nPort = 0;//HSL0->0 HS0->1
    private int iBaudRate=115200;
    private boolean _isOpen=false;
    private byte[] _bLoopData=new byte[]{0x30};
    private int iDelay=500;
    //----------------------------------------------------
    public SerialHelper(String sPort, int iBaudRate){
        this.sPort = sPort;
        this.iBaudRate=iBaudRate;
    }
    public SerialHelper(){
        this("/dev/ttyHSL0",115200);
    }
    public SerialHelper(String sPort){
        this(sPort,115200);
    }
    public SerialHelper(String sPort, String sBaudRate){
        this(sPort,Integer.parseInt(sBaudRate));
    }
    //----------------------------------------------------
    public void open(boolean openFlow) throws SecurityException, IOException,InvalidParameterException{
        mSerialPort =  new SerialPort(new File(sPort), iBaudRate, openFlow);
        mOutputStream = mSerialPort.getOutputStream();
        mInputStream = mSerialPort.getInputStream();
        if(mReadThread == null){
            mReadThread = new ReadThread();
            mReadThread.setName("mReadThread");
            mReadThread.start();
        }
        if(mSendThread == null){
            mSendThread = new SendThread();
            mSendThread.setName("mSenddThread");
            mSendThread.setSuspendFlag();
            mSendThread.start();
        }
        _isOpen=true;
    }
    //----------------------------------------------------
    public void close(){
        if (mReadThread != null){
            mReadThread.setreadFlag();
            mReadThread = null;
        }

        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
        _isOpen=false;
    }
    //---------------------------------------------------- Backup method
//    public void writeMsg(String msg){
//        try{
//            FileOutputStream outputStream = new FileOutputStream("/dev/ttyHSL0");
//            outputStream.write((msg + "\r\n").getBytes());
//            outputStream.close();
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//    }

    //----------------------------------------------------
    public void send(byte[] bOutArray){
        try
        {
            mOutputStream.write(bOutArray);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    //----------------------------------------------------
    public void sendHex(String sHex){
        byte[] bOutArray = MyFunc.HexToByteArr(sHex);
        send(bOutArray);
    }
    //----------------------------------------------------
    public void sendTxt(String sTxt){
        byte[] bOutArray =sTxt.getBytes();
        send(bOutArray);
    }
    //----------------------------------------------------
    public void sendData(char[] sTxt,boolean isTxt){
        if(isTxt){
            byte[] bOutArray = (String.valueOf(sTxt)).getBytes();//MyFunc.getBytes(sTxt);//sTxt.getBytes();
            send(bOutArray);
        }else{
            byte[] bOutArray =MyFunc.HexToByteArr(new String(sTxt));
            send(bOutArray);
        }
    }
    //----------------------------------------------------
    private class ReadThread extends Thread {
        public boolean readFlag = true;// 控制线程的执行
        @Override
        public void run() {
            super.run();
            while(!isInterrupted()) {
                try
                {
                    if (!readFlag)
                        break;
                    if (mInputStream == null) return;
                    byte[] buffer=new byte[1024];
                    int size = mInputStream.read(buffer);
                    if (size > 0){
//                        byte[] bRec =new byte[size+1];
//                        bRec[0]=nPort;
//                        System.arraycopy(buffer, 0, bRec, 1, size);
                        onDataReceived(buffer);
                    }
                    try
                    {
                        Thread.sleep(50);//延时50ms
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                } catch (Throwable e)
                {
                    e.printStackTrace();
                    return;
                }
            }
        }
        //线程停止
        public void setreadFlag() {
            this.readFlag = false;
        }
    }
    //----------------------------------------------------
    private class SendThread extends Thread{
        public boolean suspendFlag = true;// 控制线程的执行
        private boolean killThread = false;
        @Override
        public void run() {
            super.run();
            while(!isInterrupted()) {
                synchronized (this)
                {
                    if (suspendFlag)
                    {
                        try
                        {
                            wait();
                        } catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
                if(killThread)
                    break;
                send(getbLoopData());
                try
                {
                    Thread.sleep(iDelay);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }

        //线程暂停
        public void setSuspendFlag() {
            this.suspendFlag = true;
        }

        //唤醒线程
        public synchronized void setResume() {
            this.suspendFlag = false;
            notify();
        }

        //终止线程
        public void setkillThread(){
            killThread = true;
        }
    }
    //----------------------------------------------------
    public int getBaudRate()
    {
        return iBaudRate;
    }
    public boolean setBaudRate(int iBaud)
    {
        if (_isOpen)
        {
            return false;
        } else
        {
            iBaudRate = iBaud;
            return true;
        }
    }
    public boolean setBaudRate(String sBaud)
    {
        int iBaud = Integer.parseInt(sBaud);
        return setBaudRate(iBaud);
    }
    //----------------------------------------------------
    public String getPort()
    {
        return sPort;
    }
    public boolean setPort(String sPort)
    {
        if (_isOpen)
        {
            return false;
        } else
        {
            this.sPort = sPort;
            return true;
        }
    }
    //----------------------------------------------------
    public int getnPort()
    {
        return nPort;
    }
    public boolean setnPort(int nPort)
    {
        if (_isOpen)
        {
            return false;
        } else
        {
            this.nPort = (byte)nPort;
            return true;
        }
    }
    //----------------------------------------------------
    public boolean isOpen()
    {
        return _isOpen;
    }
    //----------------------------------------------------
    public byte[] getbLoopData()
    {
        return _bLoopData;
    }
    //----------------------------------------------------
    public void setbLoopData(byte[] bLoopData)
    {
        this._bLoopData = bLoopData;
    }
    //----------------------------------------------------
    public void setTxtLoopData(char[] sTxt){

        this._bLoopData = MyFunc.getBytes(sTxt);
    }
    //----------------------------------------------------
    public void setHexLoopData(char[] sHex){
        this._bLoopData = MyFunc.HexToByteArr(new String(sHex));
    }


    //----------------------------------------------------
    public int getiDelay()
    {
        return iDelay;
    }
    //----------------------------------------------------
    public void setiDelay(int iDelay)
    {
        this.iDelay = iDelay;
    }
    //----------------------------------------------------
    public void startSend()
    {
        if (mSendThread != null)
        {
            mSendThread.setResume();
        }
    }
    //----------------------------------------------------
    public void stopSend()
    {
        if (mSendThread != null)
        {
            mSendThread.setSuspendFlag();
        }
    }
    public void destroySend(){
        if(mSendThread != null){
            mSendThread.setkillThread();
            mSendThread.setResume();
            mSendThread = null;
        }
    }
    //----------------------------------------------------
    @Override
    public String toString() {
        return sPort;
    }
    //----------------------------------------------------
    protected abstract void onDataReceived(byte[] ComRecData);
}
