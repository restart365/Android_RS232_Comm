package com.dtiguardian.commport;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dtiguardian.commport.vculib.Enum.VcuCommand;
import com.dtiguardian.commport.vculib.Enum.VcuConnectionState;
import com.dtiguardian.commport.vculib.Interface.VcuInterface;
import com.dtiguardian.commport.vculib.struct.Status;
import com.dtiguardian.commport.vculib.struct.VcuData;
import com.dtiguardian.commport.vculib.util.BufferHandler;
import com.dtiguardian.commport.vculib.util.XTEA;
import com.dtiguardian.commport.vculib.vcu.Vcu;
import com.dtiguardian.commport.vculib.vcu.Vcu50;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class MainActivity extends AppCompatActivity {

    private TextView tvOutput, tvStatus;
    private Button btnControl, btnConnect;

    private Rs232 port;

    private String output = "";

    private AsyncHelper helper;

    Vcu vcu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvOutput = findViewById(R.id.output);
        tvStatus = findViewById(R.id.textView);
        btnControl = findViewById(R.id.btnControl);
        btnConnect = findViewById(R.id.btnConnect);
    }

    @Override
    protected void onResume() {
        super.onResume();
        port = new Rs232("/dev/ttyHSL0");
        port.connect();

        btnControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controlValve();
            }
        });

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helper = new AsyncHelper();
                helper.execute();
            }
        });
    }

    @Override
    protected void onDestroy() {
        vcu.Disconnect();
        port.close();
        super.onDestroy();
    }

    //-------------------------------------------------------------------------------------- Control
    private void controlValve(){
        if(vcu == null){
            return;
        }
        switch (vcu.getMainData().Status.getValveState()){
            case 1:
                vcu.simpleCommand(VcuCommand.CloseValve);
                break;
            case 4:
                vcu.simpleCommand(VcuCommand.OpenValve);
                break;
            case 16:
                vcu.simpleCommand(VcuCommand.OpenValve);
                break;
        }
    }

    //-------------------------------------------------------------------------------------- Connect
    private VcuConnectionState connect(){
        boolean ans = false;
        println("Looking for VCU on port");
        ans = handshake();
        if(!ans){
            println("No VCU found");
            port.close();
            return VcuConnectionState.NoResponse;
        }
        vcu = querySignature();
        if(vcu != null){
            println("Reading parameters...");
            if(vcu.readParams()){
                println("Success.");
                println("Reading labels...");
                if(vcu.readLabels()){
                    println("Success.");
                    return VcuConnectionState.Connected;
                } else {
                    println("Failed.");
                    return VcuConnectionState.BadLabels;
                }
            } else {
                println("Failed.");
                return VcuConnectionState.BadParameters;
            }
        } else {
            port.close();
            return VcuConnectionState.Unknown;
        }
    }

    private Vcu querySignature(){
        try{
            println("Querying signature...");
            port.sendTxt("@AHS\r");
            String ans = port.readLine();

            VcuData data = VcuData.NewVcuData(Base64.decode(ans, Base64.NO_WRAP));

            switch (data.VcuVersion){
                case 50:
                    println(data.signature.GetHardwareString() + " found.");
                    return new Vcu50(data, port);
                case -1:
                    println("Signature matches old firmware, please update firmware first");
                    port.close();
                    return null;
                default:
                    println("Unknown error, signature returned an unexpected value");
                    return null;
            }

        } catch (Exception e){
            println("Failed");
            return null;
        }
    }

    private boolean handshake(){
        port.setTimeout(100);
        println("Looking for VCU on " + port.toString());
        try{
            port.discardBuffer();
            port.sendTxt("@AHW\r");
            String ans = port.readLine();
            int[] result = bytesToUnsigned(Base64.decode(ans, Base64.DEFAULT));
            int result1 = result[0] * 256 + result[1];
            int result2 = result[0] + result[1] * 256;
            if (result1 == 57547 || result2 == 57547)
            {
                return true;
            }
        } catch (Exception e){}

        return false;
    }

    private int[] bytesToUnsigned(byte[] b){
        int[] unsigned = new int[b.length];
        for(int i=0;i<b.length;i++){
            unsigned[i] = b[i] & 0xff;
        }
        return unsigned;
    }

    private void println(String str){
        output = output + str + "\n";
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvOutput.setText(output);
            }
        });

    }

    class AsyncHelper extends AsyncTask<Integer, Void, VcuConnectionState>{
        @Override
        protected VcuConnectionState doInBackground(Integer... integers) {
            return connect();
        }

        @Override
        protected void onPostExecute(VcuConnectionState vcuConnectionState) {
            if(vcuConnectionState == VcuConnectionState.Connected){
                vcu.Connect(vcuInterface);
                vcuInterface.OnConnectionChanged(vcuConnectionState);
            }
        }
    }

    VcuInterface vcuInterface = new VcuInterface() {
        @Override
        public void OnUpdateAvailable() {
            Intent intent = new Intent();
            intent.setAction("vcu.update");
            sendBroadcast(intent);
        }

        @Override
        public void OnConnectionChanged(VcuConnectionState result) {
            final VcuConnectionState fResult = result;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch(fResult){
                        case Connected:
                            println("Connected.");
                            vcu.backupMainData();
                            tvStatus.setText(vcu.getMainData().SysParams.getSerialString());
                            break;
                        case Disconnected:
                            println("Disconnected");
                            port.close();
                        default:
                            println("Error");

                    }
                }
            });
        }

        @Override
        public void customMessage(String msg) {}
    };
}
