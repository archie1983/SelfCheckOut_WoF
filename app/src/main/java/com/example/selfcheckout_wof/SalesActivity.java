package com.example.selfcheckout_wof;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.example.selfcheckout_wof.btprinter_zj.BTPrinterConstants;
import com.example.selfcheckout_wof.btprinter_zj.BluetoothService;
import com.example.selfcheckout_wof.btprinter_zj.DeviceListActivity;
import com.example.selfcheckout_wof.btprinter_zj.PrinterCommand;
import com.example.selfcheckout_wof.custom_components.SalesProcessNavigationFragment;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SalesActivity extends AppCompatActivity
        implements SalesProcessNavigationFragment.OnFragmentInteractionListener {

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler sysControlsHideHandler = new Handler();

    /**
     * Parent ID for items that have no parent (the top level items e.g. Food, Drink, etc.).
     */
    public static final int TOP_LEVEL_ITEMS = 0;

    private View contentView;
    private final Runnable mShowContentRunnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            contentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    /**
     * Bluetooth receipt printer related variables
     */
    // Name of the connected receipt printer device - for debug purposes
    private String receiptPrinterDeviceName = null;
    // Local Bluetooth adapter for the receipt printer
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the services for the receipt printer
    private BluetoothService receiptPrinterService = null;

    /**
     * This handler is basically a reaction of GUI to different BT events.
     */
    private final Handler receiptPrinterActions = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BTPrinterConstants.MESSAGE_STATE_CHANGE:
                    if (BTPrinterConstants.DEBUG)
                        Log.i(BTPrinterConstants.TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            //txtStatus.setText(R.string.connected);
                            //txtStatus.append(mConnectedDeviceName);
                            Print_Test();//
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            //txtStatus.setText(R.string.connecting);
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            //txtStatus.setText(R.string.disconnected);
                            break;
                    }
                    break;
                case BTPrinterConstants.MESSAGE_WRITE:

                    break;
                case BTPrinterConstants.MESSAGE_READ:

                    break;
                case BTPrinterConstants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    receiptPrinterDeviceName = msg.getData().getString(BTPrinterConstants.DEVICE_NAME);
                    Toast.makeText(getApplicationContext(),
                            "Connected to " + receiptPrinterDeviceName,
                            Toast.LENGTH_SHORT).show();
                    break;
                case BTPrinterConstants.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(),
                            msg.getData().getString(BTPrinterConstants.TOAST), Toast.LENGTH_SHORT)
                            .show();
                    break;
                case BTPrinterConstants.MESSAGE_CONNECTION_LOST:    //蓝牙已断开连接
                    Toast.makeText(getApplicationContext(), "Device connection was lost",
                            Toast.LENGTH_SHORT).show();
                    break;
                case BTPrinterConstants.MESSAGE_UNABLE_CONNECT:     //无法连接设备
                    Toast.makeText(getApplicationContext(), "Unable to connect device",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    /**
     * Connect to the receipt printer.
     */
    private void connectToBlueToothPrinter() {
        Intent serverIntent = new Intent(SalesActivity.this, DeviceListActivity.class);
        startActivityForResult(serverIntent, BTPrinterConstants.REQUEST_CONNECT_DEVICE);
    }

    private void Print_Test(){
        String msg = "Congratulations!\n\n";
        String data = "Oscar Is A Good Boy and Eva is. :-)\n\n";
        SendDataByte(PrinterCommand.POS_Print_Text(msg, BTPrinterConstants.CHINESE, 0, 1, 1, 0));
        SendDataByte(PrinterCommand.POS_Print_Text(data, BTPrinterConstants.CHINESE, 0, 0, 0, 0));
        SendDataByte(PrinterCommand.POS_Set_Cut(1));
        SendDataByte(PrinterCommand.POS_Set_PrtInit());
    }

    /*
     *SendDataByte
     */
    private void SendDataByte(byte[] data) {

        if (receiptPrinterService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        receiptPrinterService.write(data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * Get the BT adapter so that it can be used later in the onStart() method.
         */
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available",
                    Toast.LENGTH_LONG).show();
            finish();
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_sales);

        /*
         * When we create this activity, we start with the base page
         */
        displayAvailableSalesItemsOrCurrentOrder(0, TOP_LEVEL_ITEMS, SalesProcessNavigationFragment.SalesProcesses.LOAD_PAGE);

        contentView = findViewById(R.id.frmSalesItemsListBrowse);
    }

    @Override
    public void onStart() {
        super.onStart();

        // If Bluetooth is not on, request that it be enabled.
        // createService() will then be called during onActivityResult()
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, BTPrinterConstants.REQUEST_ENABLE_BT);
            // Otherwise, setup the session
        } else {
            if (receiptPrinterService == null)
                createService();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();

        if (receiptPrinterService != null) {

            if (receiptPrinterService.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth services
                receiptPrinterService.start();
            }
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        // Stop the Bluetooth services
        if (receiptPrinterService != null)
            receiptPrinterService.stop();
        if (BTPrinterConstants.DEBUG)
            Log.e(BTPrinterConstants.TAG, "--- ON DESTROY ---");
    }

    /**
     * Creating a BT service for the receipt printer.
     */
    private void createService() {
        receiptPrinterService = new BluetoothService(this, receiptPrinterActions);
        connectToBlueToothPrinter();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (BTPrinterConstants.DEBUG)
            Log.d(BTPrinterConstants.TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case BTPrinterConstants.REQUEST_CONNECT_DEVICE: {
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras().getString(
                            DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    if (BluetoothAdapter.checkBluetoothAddress(address)) {
                        BluetoothDevice device = mBluetoothAdapter
                                .getRemoteDevice(address);
                        // Attempt to connect to the device
                        receiptPrinterService.connect(device);
                    }
                }
                break;
            }
            case BTPrinterConstants.REQUEST_ENABLE_BT: {
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a session
                    createService();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Log.d(BTPrinterConstants.TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            }
//            case REQUEST_CHOSE_BMP:{
//                if (resultCode == Activity.RESULT_OK){
//                    Uri selectedImage = data.getData();
//                    String[] filePathColumn = { MediaColumns.DATA };
//
//                    Cursor cursor = getContentResolver().query(selectedImage,
//                            filePathColumn, null, null, null);
//                    cursor.moveToFirst();
//
//                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//                    String picturePath = cursor.getString(columnIndex);
//                    cursor.close();
//
//                    BitmapFactory.Options opts = new BitmapFactory.Options();
//                    opts.inJustDecodeBounds = true;
//                    BitmapFactory.decodeFile(picturePath, opts);
//                    opts.inJustDecodeBounds = false;
//                    if (opts.outWidth > 1200) {
//                        opts.inSampleSize = opts.outWidth / 1200;
//                    }
//                    Bitmap bitmap = BitmapFactory.decodeFile(picturePath, opts);
//                    if (null != bitmap) {
//                        imageViewPicture.setImageBitmap(bitmap);
//                    }
//                }else{
//                    Toast.makeText(this, getString(R.string.msg_statev1), Toast.LENGTH_SHORT).show();
//                }
//                break;
//            }
//            case REQUEST_CAMER:{
//                if (resultCode == Activity.RESULT_OK){
//                    handleSmallCameraPhoto(data);
//                }else{
//                    Toast.makeText(this, getText(R.string.camer), Toast.LENGTH_SHORT).show();
//                }
//                break;
//            }
        }
    }

    /**
     * Creates (or reloads) the two fragments, that I have for displaying sales items
     * navigation buttons and sales items selection page.
     * @param pageNumber page number to load
     * @param parentID parent, whose page to load
     * @param process indicator of whether user needs to be shown the actual meal instead of a
     *                page with items to select or the final order.
     */
    private void displayAvailableSalesItemsOrCurrentOrder(int pageNumber,
                                                   int parentID,
                                                   SalesProcessNavigationFragment.SalesProcesses process){
        final FragmentManager fm = getSupportFragmentManager();

        if (process == SalesProcessNavigationFragment.SalesProcesses.SEE_ORDER) {
            /**
             * The fragment that shows the current selection and allows to check out at any time.
             */
            final SalesProcessNavigationFragment seeMealFragment =
                    SalesProcessNavigationFragment.newInstance(pageNumber, parentID, true, process);

            fm.beginTransaction()
                    .replace(R.id.frmSalesItemsListSeeMeal, seeMealFragment, "si_see_meal")
                    .commit();
        } else if (process == SalesProcessNavigationFragment.SalesProcesses.LOAD_PAGE) {
            /**
             * The fragment that allows to make choices for the meal.
             */
            final SalesProcessNavigationFragment dataFragment =
                    SalesProcessNavigationFragment.newInstance(pageNumber, parentID, false, process);

            fm.beginTransaction()
                    .replace(R.id.frmSalesItemsListBrowse, dataFragment, "si_list")
                    .commit();
        } else if (process == SalesProcessNavigationFragment.SalesProcesses.GO_TO_CHECKOUT) {
            /**
             * The fragment that allows to make choices for the meal.
             */
            final SalesProcessNavigationFragment dataFragment =
                    SalesProcessNavigationFragment.newInstance(pageNumber, parentID, false, process);

            /*
             * Now we will load the part where we normally put items that are available on menu,
             * now we will load there the final order that the user has chosen and offer a checkout
             * option there.
             */
            fm.beginTransaction()
                    .replace(R.id.frmSalesItemsListBrowse, dataFragment, "si_list")
                    .commit();
        }
    }

    public void onGoToAdminClick(View view) {
        Intent showAdminActivity = new Intent(this, AdminActivity.class);
        startActivity(showAdminActivity);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Hide the system controls because we want user to use only our interface.
        hideSystemControls();
    }

    /**
     * Hides the normal system navigation controls (back, home, show processes)
     * in a way that's most optimal to the Android versions covered by the app.
     */
    private void hideSystemControls() {
//        sysControlsHideHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                ActionBar actionBar = getSupportActionBar();
//                if (actionBar != null) {
//                    actionBar.hide();
//                }
//
//                sysControlsHideHandler.postDelayed(mShowContentRunnable, UI_ANIMATION_DELAY);
//            }
//        }, 100);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        sysControlsHideHandler.postDelayed(mShowContentRunnable, UI_ANIMATION_DELAY);
    }

    @Override
    public void onFragmentInteraction(SalesProcessNavigationFragment.SalesProcesses process, int pageNumber, int parentId) {
        displayAvailableSalesItemsOrCurrentOrder(pageNumber, parentId, process);
    }
}
