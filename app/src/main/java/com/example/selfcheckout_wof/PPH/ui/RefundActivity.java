package com.example.selfcheckout_wof.PPH.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.selfcheckout_wof.R;
import com.paypal.paypalretailsdk.Invoice;
import com.paypal.paypalretailsdk.RetailInvoice;
import com.paypal.paypalretailsdk.RetailSDK;
import com.paypal.paypalretailsdk.RetailSDKException;
import com.paypal.paypalretailsdk.TransactionContext;
import com.paypal.paypalretailsdk.TransactionManager;
import com.paypal.paypalretailsdk.TransactionRecord;

import java.math.BigDecimal;
import java.text.NumberFormat;

/**
 * Created by muozdemir on 12/19/17.
 */

public class RefundActivity extends ToolbarActivity
{
  private static final String LOG_TAG = RefundActivity.class.getSimpleName();
  public static final String INTENT_TRANX_TOTAL_AMOUNT = "TOTAL_AMOUNT";
  public static final String INTENT_CAPTURE_TOTAL_AMOUNT = "CAPTURE_AMOUNT";
  public static final String INTENT_VAULT_ID = "VAULT_ID";


  public static Invoice invoiceForRefund = null;
  public static RetailInvoice invoiceForRefundCaptured = null;

  TransactionContext currentTransaction;
  BigDecimal currentAmount;
  boolean isCaptured = false;

  private StepView issueRefundStep;


  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    if(item.getItemId()==android.R.id.home){
      goBackToChargeActivity();

    }
    return true;
  }


  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    Log.d(LOG_TAG, "onCreate");
    setContentView(R.layout.pph_refund_activity);

    Intent intent = getIntent();
    currentAmount = new BigDecimal(0.0);
    if (intent.hasExtra(INTENT_TRANX_TOTAL_AMOUNT))
    {
      isCaptured = false;
      currentAmount = (BigDecimal) intent.getSerializableExtra(INTENT_TRANX_TOTAL_AMOUNT);
      Log.d(LOG_TAG, "onCreate amount:" + currentAmount);
      final TextView txtAmount = (TextView) findViewById(R.id.amount);
      if (intent.hasExtra(INTENT_VAULT_ID)) {
        String vaultId = (String) intent.getSerializableExtra(INTENT_VAULT_ID);
        txtAmount.setText("Your payment of " + currencyFormat(currentAmount) +" was successful with Vault " + vaultId);
      } else {
        txtAmount.setText("Your payment of " + currencyFormat(currentAmount) +" was successful");
      }
    }
    else if (intent.hasExtra(INTENT_CAPTURE_TOTAL_AMOUNT))
    {
      isCaptured = true;
      currentAmount = (BigDecimal) intent.getSerializableExtra(INTENT_CAPTURE_TOTAL_AMOUNT);
      Log.d(LOG_TAG, "onCreate captur amount:" + currentAmount);
      final TextView txtAmount = (TextView) findViewById(R.id.amount);
      txtAmount.setText("Your payment of " + currencyFormat(currentAmount) +" was successfully captured.");

    }

    issueRefundStep = (StepView) findViewById(R.id.refund_step);
    issueRefundStep.setOnButtonClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        onProvideRefundClicked();
      }
    });

  }


  @Override
  public int getLayoutResId()
  {
    return R.layout.pph_refund_activity;
  }




  public static String currencyFormat(BigDecimal n)
  {
    return NumberFormat.getCurrencyInstance().format(n);
  }


  public void onSkipRefundClicked(View view)
  {
    goBackToChargeActivity();
  }


  public void onProvideRefundClicked()
  {
    RetailSDK.setCurrentApplicationActivity(this);

    if (isCaptured)
    {
     RetailSDK.getTransactionManager().createTransaction(invoiceForRefundCaptured, new TransactionManager.TransactionCallback()
     {
       @Override
       public void transaction(RetailSDKException error, TransactionContext transactionContext)
       {
         transactionContext.setCompletedHandler(new TransactionContext.TransactionCompletedCallback()
         {
           @Override
           public void transactionCompleted(RetailSDKException error, TransactionRecord record)
           {
             Log.d(LOG_TAG, "createTx for captured -> transactionCompleted");
             RefundActivity.this.refundCompleted(error, record);
           }
         });
         transactionContext.beginRefund(true, currentAmount);
       }
     });

     return;
    }

    currentTransaction = RetailSDK.createTransaction(invoiceForRefund);
    currentTransaction.setCompletedHandler(new TransactionContext.TransactionCompletedCallback()
    {
      @Override
      public void transactionCompleted(RetailSDKException error, TransactionRecord record)
      {
        RefundActivity.this.refundCompleted(error, record);
      }
    });
    currentTransaction.beginRefund(true, currentAmount);
  }


  private void refundCompleted(RetailSDKException error, TransactionRecord record)
  {
    if (error != null)
    {
      final String errorTxt = error.toString();
      this.runOnUiThread(new Runnable()
      {
        @Override
        public void run()
        {
          Toast.makeText(getApplicationContext(), "refund error: " + errorTxt, Toast.LENGTH_SHORT).show();
        }
      });

    }
    else
    {
      final String txnNumber = record.getTransactionNumber();
      this.runOnUiThread(new Runnable()
      {
        @Override
        public void run()
        {
          Toast.makeText(getApplicationContext(), String.format("Completed refund for Transaction %s", txnNumber), Toast.LENGTH_SHORT).show();
          RefundActivity.this.goBackToChargeActivity();
        }
      });
    }
  }


  public void goBackToChargeActivity()
  {
    Log.d(LOG_TAG, "goToChargeActivity");
    Intent intent = new Intent(RefundActivity.this, ChargeActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    startActivity(intent);
  }

}
