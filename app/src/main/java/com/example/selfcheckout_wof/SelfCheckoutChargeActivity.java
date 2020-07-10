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

public class SelfCheckoutChargeActivity extends BTPrintingBaseActivity implements View.OnClickListener
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

    private StepView createInvoiceStep;
    private StepView createTxnStep;
    private StepView acceptTxnStep;
    private TextView step3Text;
    private SharedPreferences sharedPrefs;

    // payment option constants
    public static final String OPTION_AUTH_CAPTURE = "authCapture";
    public static final String OPTION_VAULT_TYPE = "vaultType";
    public static final String OPTION_CARD_READER_PROMPT = "cardReader";
    public static final String OPTION_APP_PROMPT= "appPrompt";
    public static final String OPTION_TIP_ON_READER = "tipReader";
    public static final String OPTION_AMOUNT_TIP = "amountTip";
    public static final String OPTION_QUICK_CHIP_ENABLED = "quickChipEnabled";
    public static final String OPTION_MAGNETIC_SWIPE = "magneticSwipe";
    public static final String OPTION_CHIP = "chip";
    public static final String OPTION_CONTACTLESS = "contactless";
    public static final String OPTION_MANUAL_CARD= "manualCard";
    public static final String OPTION_SECURE_MANUAL= "secureManual";
    public static final String OPTION_CUSTOMER_ID= "customoerId";
    public static final String OPTION_TAG= "tag";

    // payment option booleans
    private boolean isAuthCaptureEnabled = false;
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
        createInvoiceStep = (StepView)findViewById(R.id.create_invoice_step);
        createInvoiceStep.setOnButtonClickListener(this);
        createTxnStep = (StepView)findViewById(R.id.create_txn_step);
        createTxnStep.setOnButtonClickListener(this);
        acceptTxnStep = (StepView)findViewById(R.id.accept_txn_step);
        acceptTxnStep.setOnButtonClickListener(this);

        sharedPrefs = getSharedPreferences(OfflinePayActivity.PREF_NAME, Context.MODE_PRIVATE);

        createInvoiceStep.setStepEnabled();
        createTxnStep.setStepDisabled();
        acceptTxnStep.setStepDisabled();
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

    public void onCreateInvoiceClicked()
    {
        Log.d(LOG_TAG, "onCreateInvoiceClicked");
        if (vaultType == TransactionBeginOptionsVaultType.VaultOnly)
        {
            return;
        }

        Log.d(LOG_TAG, "onCreateInvoiceClicked amount:" + orderAmount);

//        /**
//         * Preparing the amount for payment.
//         * One way to do it, is to create an invoice with one item - the grand total.
//         */
//        BigDecimal amount = BigDecimal.ZERO;
//        amount = new BigDecimal(orderAmount);
//        if (amount.compareTo(BigDecimal.ZERO) == 0)
//        {
//            showInvalidAmountAlertDialog();
//            return;
//        }
//
//
//        currentInvoice = new Invoice(RetailSDK.getMerchant().getCurrency());
//        BigDecimal quantity = new BigDecimal(1);
//        currentInvoice.addItem("Item", quantity, amount, 1, null);
//        // BigDecimal gratuityAmt = new BigDecimal(gratuityField.getText().toString());
//        // if(gratuityAmt.intValue() > 0){
//        //    invoice.setGratuityAmount(gratuityAmt);
//        // }

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

        // BigDecimal gratuityAmt = new BigDecimal(gratuityField.getText().toString());
        // if(gratuityAmt.intValue() > 0){
        //    invoice.setGratuityAmount(gratuityAmt);
        // }
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
        if (deviceUpdate != null && deviceUpdate.getIsRequired() && !deviceUpdate.getWasInstalled())
        {
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

        }
        else {
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
                    // refundButton.setEnabled(false);
                    finish();
                    startActivity(getIntent());
                }
            });
        } else {
            invoiceForRefund = currentTransaction.getInvoice();
            final String recordTxt =  record.getTransactionNumber();
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isAuthCaptureEnabled) {
                        goToAuthCaptureActivity(record);
                    }
                    else
                    {
                        goToRefundActivity();
                    }
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


    @Override
    public void onClick(View v)
    {
        if (v == createInvoiceStep.getButton())
        {
            onCreateInvoiceClicked();
            createInvoiceStep.setStepCompleted();
            createTxnStep.setStepEnabled();
        }
        else if(v == createTxnStep.getButton())
        {
            onCreateTransactionClicked();
            createTxnStep.setStepCompleted();
            // Don't enable payment options step if offline mode is selected
            if(!sharedPrefs.getBoolean(OfflinePayActivity.OFFLINE_MODE,false))
            {
                // enablePaymentOptionsStep();
                acceptTxnStep.setStepEnabled();
            } else {
                acceptTxnStep.setStepEnabled();
            }
        }
        else if(v == acceptTxnStep.getButton())
        {
            onAcceptTransactionClicked();
        }
    }




//    @Override
//    public void onClick(View v)
//    {
//        if (v == createInvoiceStep.getButton())
//        {
//            onCreateInvoiceClicked();
//            createInvoiceStep.setStepCompleted();
//            createTxnStep.setStepEnabled();
//        }
//        else if(v == createTxnStep.getButton())
//        {
//            onCreateTransactionClicked();
//            createTxnStep.setStepCompleted();
//            // Don't enable payment options step if offline mode is selected
//            if(!sharedPrefs.getBoolean(OfflinePayActivity.OFFLINE_MODE,false))
//            {
//                // enablePaymentOptionsStep();
//                acceptTxnStep.setStepEnabled();
//            } else {
//                acceptTxnStep.setStepEnabled();
//            }
//        }
//        else if(v == acceptTxnStep.getButton())
//        {
//            onAcceptTransactionClicked();
//        }else if(v == paymentOptionsStep){
//            Intent optionsActivity = new Intent(this, PaymentOptionsActivity.class);
//            optionsActivity.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//            optionsActivity.putExtras(getOptionsBundle());
//            startActivityForResult(optionsActivity,REQUEST_OPTIONS_ACTIVITY);
//        }else if(v == offlineModeContainer){
//            if (currentTransaction != null) {
//                showTransactionAlreadyCreatedDialog();
//            } else {
//                Intent offlineActivity = new Intent(this, OfflinePayActivity.class);
//                offlineActivity.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//                startActivity(offlineActivity);
//            }
//        }
//    }







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



    private Bundle getOptionsBundle()
    {
        Bundle bundle = new Bundle();
        bundle.putBoolean(OPTION_AUTH_CAPTURE,isAuthCaptureEnabled);
        bundle.putInt(OPTION_VAULT_TYPE,vaultType.getValue());
        bundle.putBoolean(OPTION_CARD_READER_PROMPT,isCardReaderPromptEnabled);
        bundle.putBoolean(OPTION_APP_PROMPT,isAppPromptEnabled);
        bundle.putBoolean(OPTION_TIP_ON_READER,isTippingOnReaderEnabled);
        bundle.putBoolean(OPTION_AMOUNT_TIP,isAmountBasedTippingEnabled);
        bundle.putBoolean(OPTION_QUICK_CHIP_ENABLED, isQuickChipEnabled);
        bundle.putBoolean(OPTION_MAGNETIC_SWIPE,isMagneticSwipeEnabled);
        bundle.putBoolean(OPTION_CHIP,isChipEnabled);
        bundle.putBoolean(OPTION_CONTACTLESS,isContactlessEnabled);
        bundle.putBoolean(OPTION_MANUAL_CARD,isManualCardEnabled);
        bundle.putBoolean(OPTION_SECURE_MANUAL,isSecureManualEnabled);
        bundle.putString(OPTION_CUSTOMER_ID,customerId);
        bundle.putString(OPTION_TAG,tagString);
        return bundle;
    }
}
