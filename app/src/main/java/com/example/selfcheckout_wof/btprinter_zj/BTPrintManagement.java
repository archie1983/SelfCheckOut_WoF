package com.example.selfcheckout_wof.btprinter_zj;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.selfcheckout_wof.R;
import com.example.selfcheckout_wof.SalesActivity;
import com.example.selfcheckout_wof.custom_components.UsersSelectedChoice;
import com.example.selfcheckout_wof.custom_components.componentActions.ConfiguredMeal;
import com.example.selfcheckout_wof.custom_components.utils.CheckOutDBCache;
import com.example.selfcheckout_wof.custom_components.utils.Formatting;
import com.example.selfcheckout_wof.data.DBThread;
import com.example.selfcheckout_wof.data.PurchasableGoods;
import com.example.selfcheckout_wof.data.StoredBTDevices;

import java.util.Iterator;

public class BTPrintManagement {
    /**
     * Bluetooth receipt printer related variables
     */
    // Name of the connected receipt printer device - for debug purposes
    private static String receiptPrinterDeviceName = null;
    // MAC address of the receipt printer. We'll need to get it once. After that we can re-use
    // it to send data to the printer.
    private static String receiptPrinterDeviceAddr = null;
    // Local Bluetooth adapter for the receipt printer
    private static BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the services for the receipt printer
    private static BluetoothService receiptPrinterService = null;

    private static AppCompatActivity context;

    public static void setContext(AppCompatActivity context_in) {
        context = context_in;
    }

    /**
     * What we want to do when printer becomes ready.
     */
    private static Runnable printerReady = null;

    public static final void setPrinterReadyBehaviour(Runnable printerReady_in) {
        printerReady = printerReady_in;
    }

    /**
     * This handler is basically a reaction of GUI to different BT events.
     */
    private static final Handler receiptPrinterActions = new Handler() {
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
                            //Print_Test();//
                            printReceipt();
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            //txtStatus.setText(R.string.connecting);
                            break;
                        case BluetoothService.STATE_LISTEN:
                            break;
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
                    if (context != null) {
                        Toast.makeText(context,
                                "Connected to " + receiptPrinterDeviceName,
                                Toast.LENGTH_SHORT).show();
                        if (printerReady != null) {
                            printerReady.run();
                        }
                    }
                    break;
                case BTPrinterConstants.MESSAGE_TOAST:
                    if (context != null) {
                        Toast.makeText(context,
                                msg.getData().getString(BTPrinterConstants.TOAST), Toast.LENGTH_SHORT)
                                .show();
                    }
                    break;
                case BTPrinterConstants.MESSAGE_CONNECTION_LOST:    //蓝牙已断开连接
                    if (context != null) {
                        Toast.makeText(context, R.string.device_connection_lost,
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
                case BTPrinterConstants.MESSAGE_UNABLE_CONNECT:     //无法连接设备
                    if (context != null) {
                        Toast.makeText(context, R.string.unable_to_connect_device,
                                Toast.LENGTH_SHORT).show();
                        connectToBlueToothPrinter();
                    }
                    break;
            }
        }
    };

    /**
     * Creating a BT service for the receipt printer. After creating the service
     * we will either go through the full connection process and return true
     * or we will restore the printer's address from what we already have and
     * return false.
     */
    private static boolean createService() {
        receiptPrinterService = new BluetoothService(receiptPrinterActions);

        /**
         * First try and read the DB for an existing last used printer device.
         * If that exists, then use that address. If not, then prompt user to scan
         * and select a device.
         */
        StoredBTDevices currentDevice = CheckOutDBCache.getInstance().getLastUsedZJ_BTPrinter();

        /**
         * If we don't have a current device or the one we have is the dummy one with empty address
         * (see com.example.selfcheckout_wof.custom_components.utils.CheckOutDBCache#getLastUsedZJ_BTPrinter()),
         * then don't get the address but instead go through the full process of connecting.
         */
        if (currentDevice != null && currentDevice.getDeviceAddr() != "") {
            receiptPrinterDeviceAddr = currentDevice.getDeviceAddr();
            return false;
        } else {
            connectToBlueToothPrinter();
            return true;
        }
    }

    public static final void createBTPrinterAdapter() {
        /*
         * Get the BT adapter so that it can be used later in the onStart() method.
         */
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            if (context != null) {
                Toast.makeText(context, R.string.bt_not_available,
                        Toast.LENGTH_LONG).show();
            }
            //finish();
        }
    }

    /**
     * Creates the BT printer service (starts the processes that create it) if needed and returns
     * true. If it already exists, then it won't create it, but just return false.
     *
     * @return
     */
    public static final boolean createBTPrinterService() {
        // If Bluetooth is not on, request that it be enabled.
        // createService() will then be called during onActivityResult()
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

                if (context != null) {
                    context.startActivityForResult(enableIntent, BTPrinterConstants.REQUEST_ENABLE_BT);
                }
                // Otherwise, setup the session
            } else {
                /**
                 * If the printer service already exists, then stop it and re-create.
                 */
                if (receiptPrinterService == null) {
                    //stopBTPrinterService();
                    return createService();
                } else {
                    /*
                     * If we already have a receipt printer service, then we're as good as connected.
                     * Stop the progress bar and display the test printer button.
                     */
                    return false;
                }
            }
        }
        return true;
    }

    public static final void stopBTPrinterService() {
        // Stop the Bluetooth services
        if (receiptPrinterService != null) {
            receiptPrinterService.stop();
            //receiptPrinterService = null;
        }
    }

    public static final void startBTPrinterService() {
        if (receiptPrinterService != null) {

            if (receiptPrinterService.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth services
                receiptPrinterService.start();
            }
        }
    }

    public static final void handleReceiptPrinting() {
        if (mBluetoothAdapter != null) {
            if (receiptPrinterService.getState() == BluetoothService.STATE_CONNECTED) {
                /*
                 * Looks like we're connected, try printing.
                 */
                printReceipt();
            } else {
                /**
                 * If our current address looks like a MAC address, then try to print
                 * to that (no need to save to DB because this should already be there),
                 * if not, ask user to select the printer.
                 */
                if (tryToConnectToCurrentMACAddress(false)) {
                    /*
                     * We tried to connect to and address that looked valid.
                     * Let's see if it worked. This getState will trigger the callback.
                     */
                    receiptPrinterService.getState();
                } else {
                    /*
                     * Ask user to select the printer to connect to.
                     */
                    connectToBlueToothPrinter();
                }
            }
        }
    }

    /**
     * If our current address looks like a MAC address, then try to connect
     * to that.
     */
    public static boolean tryToConnectToCurrentMACAddress(boolean saveToDBIfMACGood) {
        // Get the BLuetoothDevice object
        if (BluetoothAdapter.checkBluetoothAddress(receiptPrinterDeviceAddr)) {
            BluetoothDevice device = mBluetoothAdapter
                    .getRemoteDevice(receiptPrinterDeviceAddr);
            // Attempt to connect to the device
            receiptPrinterService.connect(device);

            if (saveToDBIfMACGood) {
                /*
                 * Store or update the device in the DB. Through its own thread of course
                 * to avoid RoomDB complaints.
                 */
                DBThread.addTask(new Runnable() {
                    @Override
                    public void run() {
                        CheckOutDBCache.getInstance().storeLastUsedZJ_BTPrinter(device);
                    }
                });
            }

            return true;
        } else {
            return false;
        }
    }

    /**
     * Prints the actual receipt from the current order.
     */
    private static void printReceipt() {
        Iterator<ConfiguredMeal> mealsInCurrentOrder = UsersSelectedChoice.getCurrentOrder();

        /**
         * If there's not meals in the order, then we don't need to print anything.
         */
        if (!mealsInCurrentOrder.hasNext()) {
            return;
        }

        /*
         * Going through the current order and preparing items one line at a time for each
         * meal.
         */
        int orderTotal = 0;
        int mealTotal = 0;
        String textToPrint  = "";

        //String s = String.format("%-15s %5s %10s\n", "Item", "Qty", "Price");
        //String s1 = String.format("%-15s %5s %10s\n", "----", "---", "-----");

        /*
         * Looking at the each meal in the order.
         */
        while (mealsInCurrentOrder.hasNext()) {
            ConfiguredMeal currentMealInOrder = mealsInCurrentOrder.next();
            /*
             * Assembling the text to print on the receipt.
             * First - the meal name.
             */
            textToPrint += currentMealInOrder.getMealName() + "\n";

            if (currentMealInOrder.getCurrentMealItems() != null) {
                /*
                 * Now let's go through the items in each meal.
                 */
                for (PurchasableGoods pg : currentMealInOrder.getCurrentMealItems()) {
                    //String line = String.format("%.40s %5d %10.2f\n", itemName, quantity, price);
                    textToPrint += String.format("%-30.30s %10.10s", pg.getLabel(), Formatting.formatCash(pg.getPrice())) + "\n";

                    //textToPrint += pg.getLabel() + "        " + Formatting.formatCash(pg.getPrice()) + "\n";
                    mealTotal += pg.getPrice();
                }

                orderTotal += mealTotal;
                mealTotal = 0;
                textToPrint += "\n";
            }
        }

        textToPrint += "\n" + String.format("%-30.30s %10.10s", "Total:", Formatting.formatCash(orderTotal)) + "\n\n";
        //textToPrint += "\nTotal: " + Formatting.formatCash(orderTotal) + "\n\n";

        SendDataByte(PrinterCommand.POS_Print_Text(textToPrint, BTPrinterConstants.UTF16, BTPrinterConstants.WEST_EUR, 0, 0, 0));
        SendDataByte(PrinterCommand.POS_Set_Cut(1));
        SendDataByte(PrinterCommand.POS_Set_PrtInit());
    }

    /**
     * Prints a test message to test printer connectivity.
     */
    public static void testPrinter(){
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
    private static void SendDataByte(byte[] data) {

        if (receiptPrinterService.getState() != BluetoothService.STATE_CONNECTED) {
            if (context != null) {
                Toast.makeText(context, R.string.not_connected, Toast.LENGTH_SHORT)
                        .show();
            }
            return;
        }
        receiptPrinterService.write(data);
    }

    /**
     * Connect to the receipt printer.
     */
    private static void connectToBlueToothPrinter() {
        if (context != null) {
            Intent serverIntent = new Intent(context, DeviceListActivity.class);
            context.startActivityForResult(serverIntent, BTPrinterConstants.REQUEST_CONNECT_DEVICE);
        }
    }

    public final static void processBTActivityResult(int requestCode, int resultCode, Intent data) {
        if (mBluetoothAdapter != null) {
            if (BTPrinterConstants.DEBUG)
                Log.d(BTPrinterConstants.TAG, "onActivityResult " + resultCode);
            switch (requestCode) {
                case BTPrinterConstants.REQUEST_CONNECT_DEVICE: {
                    // When DeviceListActivity returns with a device to connect
                    if (resultCode == Activity.RESULT_OK) {
                        // Get the device MAC address
                        receiptPrinterDeviceAddr = data.getExtras().getString(
                                DeviceListActivity.EXTRA_DEVICE_ADDRESS);

                        // and try to connect to it and save it to DB.
                        tryToConnectToCurrentMACAddress(true);
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
                        if (context != null) {
                            Toast.makeText(context, R.string.bt_not_enabled_leaving,
                                    Toast.LENGTH_SHORT).show();
                            //context.finish();
                        }
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
    }

}
