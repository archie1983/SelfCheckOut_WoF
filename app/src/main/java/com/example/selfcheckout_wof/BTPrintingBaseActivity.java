package com.example.selfcheckout_wof;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;

import com.example.selfcheckout_wof.btprinter_zj.BTPrintManagement;
import com.example.selfcheckout_wof.btprinter_zj.BTPrinterConstants;
import com.example.selfcheckout_wof.custom_components.SalesProcessNavigationFragment;

/**
 * Base class for the activity where we will print receipt and invoke payment.
 * This class will take care infrastructure required for printing over
 * BT.
 */
public class BTPrintingBaseActivity extends AppCompatActivity {

    public void onPrintReceipt(View view) {
        BTPrintManagement.handleReceiptPrinting();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BTPrintManagement.setContext(this);
        BTPrintManagement.createBTPrinterAdapter();
    }

    @Override
    public void onStart() {
        super.onStart();
        BTPrintManagement.createBTPrinterService();
    }

    @Override
    protected synchronized void onResume() {
        super.onResume();
        BTPrintManagement.startBTPrinterService();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        BTPrintManagement.stopBTPrinterService();
        if (BTPrinterConstants.DEBUG)
            Log.e(BTPrinterConstants.TAG, "--- ON DESTROY ---");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /*
         * If we have BT printer codes coming back to deal with, then handle that with the
         * static functions that we have.
         */
        if (requestCode == BTPrinterConstants.REQUEST_CONNECT_DEVICE ||
                requestCode == BTPrinterConstants.REQUEST_ENABLE_BT) {
            BTPrintManagement.processBTActivityResult(requestCode, resultCode, data);
        }
    }
}