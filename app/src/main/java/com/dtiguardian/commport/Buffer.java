package com.dtiguardian.commport;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class Buffer {
    int size = 2048;
    volatile byte[] buffer;
    Stopwatch timeout;
    int readTimeout = 250;
    int prevReadTimeout = 250;
    volatile int length = 0;
    private final char NewLine = '\r';
    private Object bufferLock = new Object();

    public Buffer(){
        timeout = new Stopwatch();
        buffer = new byte[2048];
    }

    public void setTimeout(int timeout){
        prevReadTimeout = readTimeout;
        readTimeout = timeout;
    }

    public void restoreTimeout(){
        readTimeout = prevReadTimeout;
    }

    public void add(byte[] data, int len){
        String[] commands;
        if(len + length < size){
            System.arraycopy(data,0,buffer,length,len);
            length = length + len;
            if((newLineReceivedListener != null) && lookForCR()){
                byte[] tmp = new byte[length];
                System.arraycopy(buffer, 0, tmp, 0, length);
                commands = new String(tmp).split("\r");
                clear();
                newLineReceivedListener.StatusUpdateReceived(commands);
            }
        }
    }

    void addUnsignedByte(int b) {
        buffer[length] = (byte) b;
        length += 1;
    }

    boolean lookForCR(){
        if (length > 0)
        {
            for (int i = 0; i < length; i++)
            {
                if (buffer[i] == 13)
                    return true;
            }
        }
        return false;
    }

    void clear()
    {
        length = 0;
        buffer = new byte[size];
    }

    String readString(){
        String tmp = "";
        if(length > 0)
            tmp = new String(byteToInt(), 0 , length);
        clear();
        return tmp;
    }

    byte[] readBytes(){
        byte[] tmp = new byte[length];
        for(int i = 0; i < length; i++)
            tmp[i] = buffer[i];
        clear();
        return tmp;
    }

    byte[] readBytes(int bytes){
        synchronized(bufferLock){
            List<Byte> list = arrayToList(buffer);
            byte[] returning = listToArray(list.subList(0, bytes));
            list.subList(0, bytes + 1).clear();
            buffer = listToArray(list);
            return returning;
        }
    }

    int readByte() throws TimeoutException, InterruptedException{
        int timeout = 0;
        while(length < 1 && timeout < readTimeout){
            Thread.sleep(1);;
            timeout++;
        }
        if(length > 0){
            if(length > 1){
                byte[] tmpBuff = new byte[2048];
                int tmpInt = toUnsigned(buffer[0]);
                System.arraycopy(buffer, 1, tmpBuff, 0, length - 1);
                buffer = tmpBuff;
                return tmpInt;
            } else {
                int tmpInt = toUnsigned(buffer[0]);
                clear();
                return tmpInt;
            }
        } else {
            throw new TimeoutException("Readbyte timed out");
        }
    }

    String readLine() throws TimeoutException{
        String tmpStr;
        boolean crPresent;
        timeout.Start();

        while(!(crPresent = lookForCR()) && !timeout.Compare(readTimeout));

        if(crPresent){
            tmpStr = readString();
            tmpStr = tmpStr.replace("\r","");
            timeout.Reset();
            return tmpStr;
        } else {
            timeout.Reset();
            throw new TimeoutException("Readline timed out after " + String.valueOf(readTimeout));
        }
    }

    byte[] readLineBytes(){
        byte[] returnB = null;
        int tout = 0, idx = -1;
        timeout.Reset();
        timeout.Start();
        try{
            while(idx == -1){
                if((readTimeout > 0) && (tout++ > readTimeout)){
                    throw new TimeoutException("Readline time out after " + String.valueOf(readTimeout));
                }

                Thread.sleep(1);

                List<Byte> list = arrayToList(buffer);
                idx = list.indexOf((byte)NewLine);
            }

            if(idx > 0){
                returnB = readBytes(idx);
            }
        } catch (Exception e){
            e.printStackTrace();
            timeout.Reset();
        }
        return returnB;
    }

    private int[] byteToInt(){
        int[] tmp = new int[length];
        for(int i = 0; i < length; i++)
            tmp[i] = toUnsigned(buffer[i]);
        return tmp;
    }

    public static int toUnsigned(byte b){
        return (b & 0xff);
    }

    public int bytesToRead(){
        return length;
    }

    //-------------------------------------------------------------------- Util
    private List<Byte> arrayToList(byte[] array){
        List<Byte> returnList = new ArrayList<>();
        for(byte b : array){
            returnList.add(b);
        }
        return returnList;
    }

    private byte[] listToArray(List<Byte> list){
        byte[] returnArray = new byte[list.size()];
        for(int i = 0;i<list.size();i++){
            returnArray[i] = list.get(i);
        }
        return returnArray;
    }

    //--------------------------------------------------------- Events
    private NewLineReceivedListener newLineReceivedListener = null;
    public void setOnNewLineReceivedListener(NewLineReceivedListener value)
    {
        newLineReceivedListener = value;
    }

    //--------------------------------------------------------- Events interface
    public interface NewLineReceivedListener{
        public void StatusUpdateReceived(String[] data);
    }
}
