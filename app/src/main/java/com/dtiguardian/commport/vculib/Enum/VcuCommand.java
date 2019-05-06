package com.dtiguardian.commport.vculib.Enum;

public enum VcuCommand {
    CloseValve,
    OpenValve,
    GetLogHeader,
    GetLogEntry,
    ClearLog,
    ClearValveCycleCount,
    SendFirmwareHeader,
    SendFirmwareLine,
    VerifyFirmware,
    JumpToBootloader,
    SetDefaults,
    SetTrim,
    Cycler
}
