package com.example.selfcheckout_wof;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.selfcheckout_wof.PPH.ui.AuthCaptureActivity;
import com.example.selfcheckout_wof.PPH.ui.OfflinePayActivity;
import com.example.selfcheckout_wof.PPH.ui.OfflinePaySuccessActivity;
import com.example.selfcheckout_wof.PPH.ui.PaymentOptionsActivity;
import com.example.selfcheckout_wof.PPH.ui.RefundActivity;
import com.example.selfcheckout_wof.PPH.ui.StepView;
import com.example.selfcheckout_wof.PPH.ui.VaultActivity;
import com.example.selfcheckout_wof.btprinter_zj.BTPrintManagement;
import com.example.selfcheckout_wof.custom_components.UsersSelectedChoice;
import com.example.selfcheckout_wof.custom_components.componentActions.ConfiguredMeal;
import com.example.selfcheckout_wof.data.PurchasableGoods;
import com.paypal.paypalretailsdk.DeviceUpdate;
import com.paypal.paypalretailsdk.FormFactor;
import com.paypal.paypalretailsdk.Invoice;
import com.paypal.paypalretailsdk.OfflinePaymentInfo;
import com.paypal.paypalretailsdk.OfflineTransactionRecord;
import com.paypal.paypalretailsdk.PaymentDevice;
import com.paypal.paypalretailsdk.RetailSDK;
import com.paypal.paypalretailsdk.RetailSDKException;
import com.paypal.paypalretailsdk.TransactionBeginOptions;
import com.paypal.paypalretailsdk.TransactionBeginOptionsVaultProvider;
import com.paypal.paypalretailsdk.TransactionBeginOptionsVaultType;
import com.paypal.paypalretailsdk.TransactionContext;
import com.paypal.paypalretailsdk.TransactionManager;
import com.paypal.paypalretailsdk.TransactionRecord;
import com.paypal.paypalretailsdk.VaultRecord;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SelfCheckoutChargeActivity extends BTPrintingBaseActivity
{
    private static final String LOG_TAG = SelfCheckoutChargeActivity.class.getSimpleName();
    public static final String INTENT_TRANX_TOTAL_AMOUNT = "TOTAL_AMOUNT";
    public static final String INTENT_AUTH_ID = "AUTH_ID";
    public static final String INTENT_INVOICE_ID = "INVOICE_ID";
    public static final String INTENT_VAULT_ID = "VAULT_ID";
    //private static final int REQUEST_OPTIONS_ACTIVITY = 8001;

    TransactionContext currentTransaction;
    Invoice currentInvoice;
    Invoice invoiceForRefund;

    private SharedPreferences sharedPrefs;

    // payment option booleans
    private boolean isAuthCaptureEnabled = true;
    private TransactionBeginOptionsVaultType vaultType = TransactionBeginOptionsVaultType.PayOnly;
    private boolean isCardReaderPromptEnabled = true;
    private boolean isAppPromptEnabled = true;
    private boolean isTippingOnReaderEnabled = false;
    private boolean isAmountBasedTippingEnabled = false;
    private boolean isQuickChipEnabled = false;
    private boolean isMagneticSwipeEnabled = true;
    private boolean isChipEnabled = true;
    private boolean isContactlessEnabled = true;
    private boolean isManualCardEnabled = true;
    private boolean isSecureManualEnabled = true;
    private String customerId = "";
    private String tagString = "";
    private String mVaultId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.pph_self_checkout_transaction_activity);

        Log.d(LOG_TAG, "onCreate");
        TextView paymentAmountText = (TextView) findViewById(R.id.payment_amount_text);
        paymentAmountText.setText(getString(R.string.payment_amount) + " (" + NumberFormat.getCurrencyInstance().getCurrency().getSymbol() + ")");

        sharedPrefs = getSharedPreferences(OfflinePayActivity.PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * This is where we'll keep the total amount of money that we need to charge for.
     * We'll get this from the intent extra, that started this activity.
     */
    private double orderAmount = 0.0;

    @Override
    protected void onResume()
    {
        super.onResume();

        /**
         * Extracting the total amount tha we need to charge for.
         */
        Intent i= getIntent();
        orderAmount = i.getDoubleExtra(INTENT_TRANX_TOTAL_AMOUNT, 0.0);

        if(sharedPrefs.getBoolean(OfflinePayActivity.OFFLINE_MODE,false))
        {
            if (RetailSDK.getTransactionManager().getOfflinePaymentEligibility()){
                RetailSDK.getTransactionManager().startOfflinePayment(new TransactionManager.OfflinePaymentStatusCallback() {
                    @Override
                    public void offlinePaymentStatus(RetailSDKException error, OfflinePaymentInfo offlinePaymentInfo) {
                        if (error != null) {
                            Toast.makeText(getApplicationContext(), error.getDeveloperMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), "Merchant not whitelisted for offline payments", Toast.LENGTH_LONG).show();
            }
        }else{
            RetailSDK.getTransactionManager().stopOfflinePayment(new TransactionManager.OfflinePaymentStatusCallback()
            {
                @Override
                public void offlinePaymentStatus(RetailSDKException error, OfflinePaymentInfo offlinePaymentInfo)
                {
                    if (error != null) {
                        Toast.makeText(getApplicationContext(), error.getDeveloperMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    public void onPayByCard(View view) {
        createInvoice();
        createTransactionAndAcceptIt();
    }

    /**
     * Creates invoice
     */
    public void createInvoice()
    {
        Log.d(LOG_TAG, "createInvoice");
        if (vaultType == TransactionBeginOptionsVaultType.VaultOnly)
        {
            return;
        }

        Log.d(LOG_TAG, "createInvoice amount:" + orderAmount);

        /**
         * Preparing the amount for payment.
         * Another way to do it, is to create an invoice with each item that is being purchased.
         */
        currentInvoice = new Invoice(RetailSDK.getMerchant().getCurrency());
        Iterator<ConfiguredMeal> itCurrentOrder = UsersSelectedChoice.getCurrentOrder();

        /*
         * Going through the meals in the order.
         */
        while (itCurrentOrder.hasNext()) {
            ConfiguredMeal cm = itCurrentOrder.next();

            ArrayList<PurchasableGoods> itemsInCurrentMeal = cm.getCurrentMealItems();

            /*
             * Going through each item in each meal.
             */
            if (itemsInCurrentMeal != null && itemsInCurrentMeal.size() > 0) {
                for (PurchasableGoods pg : cm.getCurrentMealItems()) {
                    BigDecimal amount = BigDecimal.ZERO;
                    amount = new BigDecimal((double)pg.getPrice() / 100.0);
                    /*
                     * If we have a non-zero amount, then add that to the invoice.
                     */
                    if (amount.compareTo(BigDecimal.ZERO) != 0)
                    {
                        BigDecimal quantity = new BigDecimal(1);
                        currentInvoice.addItem(pg.getLabel(), quantity, amount, pg.getID(), null);
                    }
                }
            }
        }

        Log.d(LOG_TAG, "AE2 : Invoice created");
    }

    public void createTransactionAndAcceptIt()
    {
        Log.d(LOG_TAG, "createTransaction");
        if (vaultType == TransactionBeginOptionsVaultType.VaultOnly) {
            RetailSDK.getTransactionManager().createVaultTransaction(new TransactionManager.TransactionCallback()
            {
                @Override
                public void transaction(RetailSDKException e, TransactionContext context)
                {
                    if (e != null) {
                        final String errorTxt = e.toString();
                        SelfCheckoutChargeActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "create transaction error: " + errorTxt, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }else{
                        currentTransaction = context;
                        acceptTransaction();
                    }
                }
            });
        }
        else {
            RetailSDK.getTransactionManager().createTransaction(currentInvoice, new TransactionManager.TransactionCallback()
            {
                @Override
                public void transaction(RetailSDKException e, final TransactionContext context)
                {
                    if (e != null) {
                        final String errorTxt = e.toString();
                        SelfCheckoutChargeActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "create transaction error: " + errorTxt, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }else{
                        currentTransaction = context;
                        Log.d(LOG_TAG, "AE2 : Current transaction acquired");
                        acceptTransaction();
                    }
                }
            });
        }
    }

    public void acceptTransaction()
    {
        Log.d(LOG_TAG, "acceptTransaction");

        PaymentDevice activeDevice = RetailSDK.getDeviceManager().getActiveReader();
        DeviceUpdate deviceUpdate = activeDevice.getPendingUpdate();
        if (deviceUpdate != null && deviceUpdate.getIsRequired() && !deviceUpdate.getWasInstalled())
        {
            deviceUpdate.offer(new DeviceUpdate.CompletedCallback()
            {
                @Override
                public void completed(RetailSDKException e, Boolean aBoolean)
                {
                    Log.d(LOG_TAG, "device update completed");
                    Log.d(LOG_TAG, "AE2 : Starting payment");
                    SelfCheckoutChargeActivity.this.beginPayment();
                }
            });
        }
        else {
            Log.d(LOG_TAG, "AE2 : Starting payment");
            beginPayment();
        }
    }

    public void onCreateInvoiceClicked()
    {
        Log.d(LOG_TAG, "onCreateInvoiceClicked");
        if (vaultType == TransactionBeginOptionsVaultType.VaultOnly)
        {
            return;
        }

        Log.d(LOG_TAG, "onCreateInvoiceClicked amount:" + orderAmount);

        /**
         * Preparing the amount for payment.
         * Another way to do it, is to create an invoice with each item that is being purchased.
         */
        currentInvoice = new Invoice(RetailSDK.getMerchant().getCurrency());
        Iterator<ConfiguredMeal> itCurrentOrder = UsersSelectedChoice.getCurrentOrder();

        /*
         * Going through the meals in the order.
         */
        while (itCurrentOrder.hasNext()) {
            ConfiguredMeal cm = itCurrentOrder.next();

            ArrayList<PurchasableGoods> itemsInCurrentMeal = cm.getCurrentMealItems();

            /*
             * Going through each item in each meal.
             */
            if (itemsInCurrentMeal != null && itemsInCurrentMeal.size() > 0) {
                for (PurchasableGoods pg : cm.getCurrentMealItems()) {
                    BigDecimal amount = BigDecimal.ZERO;
                    amount = new BigDecimal((double)pg.getPrice() / 100.0);
                    /*
                     * If we have a non-zero amount, then add that to the invoice.
                     */
                    if (amount.compareTo(BigDecimal.ZERO) != 0)
                    {
                        BigDecimal quantity = new BigDecimal(1);
                        currentInvoice.addItem(pg.getLabel(), quantity, amount, pg.getID(), null);
                    }
                }
            }
        }

        Log.d(LOG_TAG, "AE : Invoice created");
    }

    public void onCreateTransactionClicked()
    {
        Log.d(LOG_TAG, "onCreateTransactionClicked");
        if (vaultType == TransactionBeginOptionsVaultType.VaultOnly) {
            RetailSDK.getTransactionManager().createVaultTransaction(new TransactionManager.TransactionCallback()
            {
                @Override
                public void transaction(RetailSDKException e, TransactionContext context)
                {
                    if (e != null) {
                        final String errorTxt = e.toString();
                        SelfCheckoutChargeActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "create transaction error: " + errorTxt, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }else{
                        currentTransaction = context;
                    }
                }
            });
        }
        else {
            RetailSDK.getTransactionManager().createTransaction(currentInvoice, new TransactionManager.TransactionCallback()
            {
                @Override
                public void transaction(RetailSDKException e, final TransactionContext context)
                {
                    if (e != null) {
                        final String errorTxt = e.toString();
                        SelfCheckoutChargeActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "create transaction error: " + errorTxt, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }else{
                        currentTransaction = context;
                        Log.d(LOG_TAG, "AE : Current transaction acquired");
                    }
                }
            });
        }
    }

    public void onAcceptTransactionClicked()
    {
        Log.d(LOG_TAG, "onAcceptTransactionClicked");

        PaymentDevice activeDevice = RetailSDK.getDeviceManager().getActiveReader();
        DeviceUpdate deviceUpdate = activeDevice.getPendingUpdate();
        if (deviceUpdate != null && deviceUpdate.getIsRequired() && !deviceUpdate.getWasInstalled()) {
            deviceUpdate.offer(new DeviceUpdate.CompletedCallback()
            {
                @Override
                public void completed(RetailSDKException e, Boolean aBoolean)
                {
                    Log.d(LOG_TAG, "device update completed");
                    Log.d(LOG_TAG, "AE : Starting payment");
                    SelfCheckoutChargeActivity.this.beginPayment();
                }
            });

        } else {
            Log.d(LOG_TAG, "AE : Starting payment");
            beginPayment();
        }
    }

    private void beginPayment()
    {
        currentTransaction.setCompletedHandler(new TransactionContext.TransactionCompletedCallback() {
            @Override
            public void transactionCompleted(RetailSDKException error, TransactionRecord record) {
                SelfCheckoutChargeActivity.this.transactionCompleted(error, record);
            }
        });
        currentTransaction.setVaultCompletedHandler(new TransactionContext.VaultCompletedCallback()
        {
            @Override
            public void vaultCompleted(RetailSDKException error, VaultRecord record)
            {
                SelfCheckoutChargeActivity.this.vaultCompleted(error, record);
            }
        });
        currentTransaction.setOfflineTransactionAdditionHandler(new TransactionContext.OfflineTransactionAddedCallback() {
            @Override
            public void offlineTransactionAdded(RetailSDKException error, OfflineTransactionRecord offlineTransactionRecord)
            {
                SelfCheckoutChargeActivity.this.offlineTransactionAdded(error, offlineTransactionRecord);
            }
        });

        TransactionBeginOptions options = new TransactionBeginOptions();
        options.setShowPromptInCardReader(isCardReaderPromptEnabled);
        options.setShowPromptInApp(isAppPromptEnabled);
        options.setIsAuthCapture(isAuthCaptureEnabled);
        options.setAmountBasedTipping(isAmountBasedTippingEnabled);
        options.setQuickChipEnabled(isQuickChipEnabled);
        options.setTippingOnReaderEnabled(isTippingOnReaderEnabled);
        options.setTag(tagString);
        options.setPreferredFormFactors(getPreferredFormFactors());
        if (vaultType == TransactionBeginOptionsVaultType.VaultOnly)
        {
            options.setVaultCustomerId(customerId);
            options.setVaultType(TransactionBeginOptionsVaultType.VaultOnly);
            options.setVaultProvider(TransactionBeginOptionsVaultProvider.Braintree);
        } else if (vaultType == TransactionBeginOptionsVaultType.PayAndVault) {
            options.setVaultCustomerId(customerId);
            options.setVaultType(TransactionBeginOptionsVaultType.PayAndVault);
            options.setVaultProvider(TransactionBeginOptionsVaultProvider.Braintree);
        }
        else
        {
            options.setVaultType(TransactionBeginOptionsVaultType.PayOnly);
            // we want to send customerId even for pay only txs.
            // BT merchants can make payments and add customer id with it.
            if (!customerId.isEmpty()) {
                options.setVaultCustomerId(customerId);
            }
        }
        currentTransaction.beginPayment(options);
    }

    void transactionCompleted(RetailSDKException error, final TransactionRecord record) {
        if (error != null) {
            final String errorTxt = error.toString();

            this.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    Toast.makeText(getApplicationContext(), "transaction error: " + errorTxt, Toast.LENGTH_SHORT).show();
                    finish();
                    startActivity(getIntent());
                }
            });
        } else {
            invoiceForRefund = currentTransaction.getInvoice();
            final String recordTxt =  record.getTransactionNumber();

            BTPrintManagement.setreceiptPrintedBehaviour(new Runnable() {
                @Override
                public void run() {
                    UsersSelectedChoice.clearCurrentMeal();
                    UsersSelectedChoice.clearOrder();
                    goBackToSales();
                }
            });

            BTPrintManagement.handleReceiptPrinting();

            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //BTPrintManagement.handleReceiptPrinting();
                    Toast.makeText(getApplicationContext(), String.format("Completed Transaction %s", recordTxt), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    void vaultCompleted(RetailSDKException error, final VaultRecord record) {
        if (error != null)
        {
            final String errorTxt = error.toString();
            this.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    Toast.makeText(getApplicationContext(), "vault error: " + errorTxt, Toast.LENGTH_SHORT).show();
                    //refundButton.setEnabled(false);
                }
            });
        }
        else
        {
            invoiceForRefund = currentTransaction.getInvoice();
            final String recordTxt = record.getVaultId();
            this.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    if (vaultType == TransactionBeginOptionsVaultType.VaultOnly)
                    {
                        goToVaultActivity(record);
                    }
                    else // payAndVault
                    {
                        mVaultId = record.getVaultId();
                    }
                    Toast.makeText(getApplicationContext(), String.format("Completed Vault %s", recordTxt), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    
    void offlineTransactionAdded(RetailSDKException error, final OfflineTransactionRecord record) {
        if (error != null)
        {
            final String errorTxt = error.toString();
            this.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    Toast.makeText(getApplicationContext(), "offline error: " + errorTxt, Toast.LENGTH_SHORT).show();
                    //refundButton.setEnabled(false);
                }
            });
        }
        else
        {
            this.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    Toast.makeText(getApplicationContext(), "offline success: " + record.getId(), Toast.LENGTH_SHORT).show();
                    //refundButton.setEnabled(false);
                }
            });
            goToOfflinePayCompleteActivity();
        }
    }

    /**
     * Open the sales activity to start the next selection of goods
     */
    private void goBackToSales() {
        Intent showSalesActivity = new Intent(this, SalesActivity.class);
        startActivity(showSalesActivity);
    }

    private void goToOfflinePayCompleteActivity()
    {
        Intent intent = new Intent(SelfCheckoutChargeActivity.this, OfflinePaySuccessActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void goToAuthCaptureActivity(TransactionRecord record){
        Log.d(LOG_TAG, "goToAuthCaptureActivity");
        AuthCaptureActivity.invoiceForRefund = invoiceForRefund;
        Intent intent = new Intent(SelfCheckoutChargeActivity.this, AuthCaptureActivity.class);
        String authId = record.getTransactionNumber();
        String invoiceId = record.getInvoiceId();
        BigDecimal amount = currentInvoice.getTotal();
        Log.d(LOG_TAG, "goToAuthCaptureActivity total: " + amount);
        intent.putExtra(INTENT_TRANX_TOTAL_AMOUNT, amount);
        intent.putExtra(INTENT_AUTH_ID, authId);
        intent.putExtra(INTENT_INVOICE_ID, invoiceId);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void goToVaultActivity(VaultRecord record){
        Log.d(LOG_TAG, "goToVaultActivity");
        Intent intent = new Intent(SelfCheckoutChargeActivity.this, VaultActivity.class);
        String vaultId = record.getVaultId();
        Log.d(LOG_TAG, "goToAuthCaptureActivity vaultId: " + vaultId);
        intent.putExtra(INTENT_VAULT_ID, vaultId);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void goToRefundActivity(){
        Log.d(LOG_TAG, "goToRefundActivity");
        RefundActivity.invoiceForRefund = invoiceForRefund;
        Intent refundIntent = new Intent(SelfCheckoutChargeActivity.this, RefundActivity.class);
        BigDecimal amount = currentInvoice.getTotal();
        Log.d(LOG_TAG, "goToRefundActivity total: " + amount);
        refundIntent.putExtra(INTENT_TRANX_TOTAL_AMOUNT, amount);
        if (vaultType == TransactionBeginOptionsVaultType.PayAndVault) {
            refundIntent.putExtra(INTENT_VAULT_ID, mVaultId);
        }
        refundIntent.putExtra(INTENT_VAULT_ID, mVaultId);
        refundIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(refundIntent);
    }

    private void showInvalidAmountAlertDialog(){
        Log.d(LOG_TAG, "showInvalidAmountAlertDialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(SelfCheckoutChargeActivity.this);
        builder.setTitle(R.string.error_title);
        builder.setMessage(R.string.error_invalid_amount);
        builder.setCancelable(false);
        builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(LOG_TAG, "takePayment invalid amount alert dialog onClick");
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void showTransactionAlreadyCreatedDialog(){
        Log.d(LOG_TAG, "showTransactionAlreadyCreatedDialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(SelfCheckoutChargeActivity.this);
        builder.setTitle(R.string.dialog_transaction_created_title);
        builder.setMessage(R.string.dialog_transaction_created_message);
        builder.setCancelable(false);
        builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            Log.d(LOG_TAG, "show Transaction Already Created Dialog onClick");
            dialog.dismiss();
          }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)
        {
//            if (requestCode == REQUEST_OPTIONS_ACTIVITY)
//            {
//                Bundle optionsBundle = data.getExtras();
//                isAuthCaptureEnabled = optionsBundle.getBoolean(OPTION_AUTH_CAPTURE);
//                vaultType = TransactionBeginOptionsVaultType.fromInt(optionsBundle.getInt(OPTION_VAULT_TYPE));
//                isAppPromptEnabled = optionsBundle.getBoolean(OPTION_APP_PROMPT);
//                isTippingOnReaderEnabled = optionsBundle.getBoolean(OPTION_TIP_ON_READER);
//                isAmountBasedTippingEnabled = optionsBundle.getBoolean(OPTION_AMOUNT_TIP);
//                isQuickChipEnabled = optionsBundle.getBoolean(OPTION_QUICK_CHIP_ENABLED);
//                isMagneticSwipeEnabled = optionsBundle.getBoolean(OPTION_MAGNETIC_SWIPE);
//                isChipEnabled = optionsBundle.getBoolean(OPTION_CHIP);
//                isContactlessEnabled = optionsBundle.getBoolean(OPTION_CONTACTLESS);
//                isManualCardEnabled = optionsBundle.getBoolean(OPTION_MANUAL_CARD);
//                isSecureManualEnabled = optionsBundle.getBoolean(OPTION_SECURE_MANUAL);
//                customerId = optionsBundle.getString(OPTION_CUSTOMER_ID);
//                tagString = optionsBundle.getString(OPTION_TAG);
//            }
        }
    }

    public List<FormFactor> getPreferredFormFactors()
    {
        List<FormFactor> formFactors = new ArrayList<>();
        if (isMagneticSwipeEnabled)
        {
            formFactors.add(FormFactor.MagneticCardSwipe);
        }
        if (isChipEnabled)
        {
            formFactors.add(FormFactor.Chip);
        }
        if (isContactlessEnabled)
        {
            formFactors.add(FormFactor.EmvCertifiedContactless);
        }
        if (isSecureManualEnabled)
        {
            formFactors.add(FormFactor.SecureManualEntry);
        }
        if (isManualCardEnabled)
        {
            formFactors.add(FormFactor.ManualCardEntry);
        }

        if (formFactors.size() == 0)
        {
            formFactors.add(FormFactor.None);
        }
        return formFactors;

    }
}
