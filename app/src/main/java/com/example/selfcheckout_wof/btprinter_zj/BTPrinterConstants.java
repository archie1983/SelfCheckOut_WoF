package com.example.selfcheckout_wof.btprinter_zj;

public class BTPrinterConstants {
    /******************************************************************************************************/
    // Debugging
    public static final String TAG = "BlueTooth_Printer";
    public static final boolean DEBUG = true;
    /******************************************************************************************************/
    // Message types sent from the BluetoothService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_CONNECTION_LOST = 6;
    public static final int MESSAGE_UNABLE_CONNECT = 7;
    /*******************************************************************************************************/
    // Key names received from the BluetoothService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    public static final int REQUEST_CONNECT_DEVICE = 1;
    public static final int REQUEST_ENABLE_BT = 2;
    public static final int REQUEST_CHOSE_BMP = 3;
    public static final int REQUEST_CAMER = 4;

    //QRcode
    public static final int QR_WIDTH = 350;
    public static final int QR_HEIGHT = 350;
    /*******************************************************************************************************/
    public static final String CHINESE = "GBK";
    public static final String THAI = "CP874";
    public static final String KOREAN = "EUC-KR";
    public static final String BIG5 = "BIG5";
}
