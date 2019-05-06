package com.dtiguardian.commport.vculib.Interface;

import com.dtiguardian.commport.vculib.Enum.VcuConnectionState;

public interface VcuInterface {
    void OnUpdateAvailable();

    void OnConnectionChanged(VcuConnectionState result);

    void customMessage(String msg);
}
