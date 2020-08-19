package com.example.selfcheckout_wof;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.selfcheckout_wof.PPH.login.PPHLoginActivity;
import com.example.selfcheckout_wof.custom_components.exceptions.DataImportExportException;
import com.example.selfcheckout_wof.custom_components.utils.CheckOutDBCache;
import com.example.selfcheckout_wof.custom_components.utils.PopupQuestionsAndMessages;
import com.example.selfcheckout_wof.custom_components.utils.SqliteExportAndImport;
import com.example.selfcheckout_wof.data.DBThread;
import com.example.selfcheckout_wof.data.SystemChoices;

public class AdminActivity extends AppCompatActivity {

    /*
     * Flags of whether our hardware is connected. If it is not, then we can't start sales.
     */
    private boolean printerConnected = false;
    private boolean cardReaderConnected = false;

    /**
     * Intent receiver for handling intents issued by GUI (e.g. when we want to launch
     * the sales activity or data management, etc.)
     */
    private BroadcastReceiver selfCheckoutIntentReveiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            /**
             * Handling the intent generated by one of the main GUI Admin choices
             * (typically generated in SystemChoiceItemView).
             */
            switch(SystemChoices.lookUpByIntent(intent)) {
                case MANAGE_DATA:
                    onManageData(null);
                    break;
                case START_SALES:
                    onStartSales(null);
                    break;
                case EXPORT_DB:
                    onExportDB(null);
                case IMPORT_DB:
                    onImportDB(null);
                    break;
                case LOGIN_PAYPAL:
                    //initPaypalHere();
                    configurePaypalHere();
                    break;
                case UNKNOWN:
                    break;
            }
        }
    };

    protected boolean shouldAskPermissions() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @TargetApi(23)
    protected void askPermissions() {
        String[] permissions = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"
        };
        int requestCode = 200;
        requestPermissions(permissions, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        //# Ensuring that we have permissions for SD card read/write
        if (shouldAskPermissions()) {
            askPermissions();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        /**
         * Making sure that this activity can handle all our SystemChoices intents.
         */
        IntentFilter filter = new IntentFilter();
        for (SystemChoices sc : SystemChoices.values()) {
            filter.addAction(sc.getIntent().getAction());
        }

        filter.addCategory(Intent.CATEGORY_DEFAULT);
        this.registerReceiver(selfCheckoutIntentReveiver,filter);

        /**
         * Making sure that we have the previously used BT devices available
         * for when we want to re-connect automatically if possible.
         */
        DBThread.addTask(new Runnable() {
            @Override
            public void run() {
                CheckOutDBCache.getInstance().getLastUsedZJ_BTPrinter();
            }
        });
    }

    /** Called when another activity is taking focus. */
    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(selfCheckoutIntentReveiver);
    }

    /**
     * "Manage Data" button press
     * @param view
     */
    public void onManageData(View view) {
        Intent showDataAdminActivity = new Intent(this, DataAdminActivity.class);
        startActivity(showDataAdminActivity);
    }

    /**
     * "Start Sales" button press
     * @param view
     */
    public void onStartSales(View view) {
        if (cardReaderConnected && printerConnected) {
            Intent showSalesActivity = new Intent(this, SalesActivity.class);
            startActivity(showSalesActivity);
        } else {
            /*
             * If hardware has not been connected, then we can't start sales.
             */
            PopupQuestionsAndMessages.pleaseConnectToPrinterAndCardReader(this);
        }
    }

    private static final int TO_EXPORT_DB_REQUEST_CODE = 1;
    private static final int TO_IMPORT_DB_REQUEST_CODE = 2;
    private static final int TO_CONNECT_HW_REQUEST_CODE = 3;
    /**
     * "Export DB" button press
     * @param view
     */
    public void onExportDB(View view) {
        /*
         * If the export button was pressed, then we'll ask user for where to store the exported
         * data with the help of this activity and intent. It will later call back the
         * onActivityResult(...) function, where the real thing starts.
         */
        startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), TO_EXPORT_DB_REQUEST_CODE);
    }

    /**
     * "Import DB" button press
     * @param view
     */
    public void onImportDB(View view) {
        /*
         * If the import button was pressed, then we'll ask user for where to read the
         * data with the help of this activity and intent. It will later call back the
         * onActivityResult(...) function, where the real thing starts.
         */
        startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), TO_IMPORT_DB_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (resultCode != RESULT_OK)
            return;

        Uri treeUri = null;
        DocumentFile pickedDir = null;
        /*
         * If we're coming back from "Export DB" or "Import DB", then there should be a directory
         * tree selected.
         */
        if ((requestCode == TO_EXPORT_DB_REQUEST_CODE || requestCode == TO_IMPORT_DB_REQUEST_CODE) && resultCode == RESULT_OK) {
            treeUri = resultData.getData();
            pickedDir = DocumentFile.fromTreeUri(this, treeUri);
        }

        if (requestCode == TO_EXPORT_DB_REQUEST_CODE && resultCode == RESULT_OK) {
            grantUriPermission(getPackageName(), treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            try {
                SqliteExportAndImport.export(getApplicationContext(), getContentResolver(), pickedDir, CheckOutDBCache.getDBInstance().getOpenHelper().getReadableDatabase());
            } catch (DataImportExportException exc) {
                Log.d(LOG_TAG, exc.getMessage());
            }
        } else if (requestCode == TO_IMPORT_DB_REQUEST_CODE && resultCode == RESULT_OK) {
            grantUriPermission(getPackageName(), treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
                SqliteExportAndImport.importData(getApplicationContext(), getContentResolver(), pickedDir,
                        CheckOutDBCache.getDBInstance().getOpenHelper().getWritableDatabase());
            } catch (DataImportExportException exc) {
                Log.d(LOG_TAG, exc.getMessage());
            }
        } else if (requestCode == TO_CONNECT_HW_REQUEST_CODE && resultCode == RESULT_OK) {
            cardReaderConnected = resultData.getBooleanExtra(PPHLoginActivity.CARD_READER_CONNECTED, false);
            printerConnected = resultData.getBooleanExtra(PPHLoginActivity.PRINTER_CONNECTED, false);
        }
    }

    private static final String LOG_TAG = AdminActivity.class.getSimpleName();

    private void configurePaypalHere() {
        Intent pphLoginActivity = new Intent(this, PPHLoginActivity.class);
        startActivityForResult(pphLoginActivity, TO_CONNECT_HW_REQUEST_CODE);
    }
}
