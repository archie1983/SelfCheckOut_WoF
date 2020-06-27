package com.example.selfcheckout_wof.PPH.activities.vaultAndPayTransaction;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.selfcheckout_wof.PPH.ui.StepView;
import com.example.selfcheckout_wof.R;
import com.paypal.paypalretailsdk.RetailSDKException;
import com.paypal.paypalretailsdk.TransactionRecord;
import com.paypal.paypalretailsdk.VaultRecord;

import java.util.Objects;


public class PPHVaultAndPayTransaction extends AppCompatActivity
{
  private String LOG_TAG = PPHVaultAndPayTransaction.class.getSimpleName();
  private PPHVaultAndPayTransactionViewModel _vaultAndPayTransactionViewModel;

  StepView _createInvoiceStep, _addInvoiceItemStep, _vaultAndPayStep;
  EditText _amount;


  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.pph_activity_vault_and_pay_transaction);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);

    _vaultAndPayTransactionViewModel = ViewModelProviders.of(PPHVaultAndPayTransaction.this).get(PPHVaultAndPayTransactionViewModel.class);
    _createInvoiceStep = findViewById(R.id.create_invoice_step);
    _addInvoiceItemStep = findViewById(R.id.add_invoice_item_step);
    _vaultAndPayStep = findViewById(R.id.begin_vault_and_pay_step);
    _amount = findViewById(R.id.amount);
    _amount.setFocusable(false);

    _createInvoiceStep.setStepEnabled();
  }


  @Override
  protected void onStart()
  {
    super.onStart();

    _createInvoiceStep.setOnButtonClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View view)
      {
        if (_vaultAndPayTransactionViewModel.createInvoice())
        {
          _addInvoiceItemStep.setStepEnabled();
          _createInvoiceStep.setStepCompleted();
          _amount.setFocusableInTouchMode(true);
        }
      }
    });

    _addInvoiceItemStep.setOnButtonClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View view)
      {
        if (_vaultAndPayTransactionViewModel.addInvoiceItem("Item", "1", _amount.getText().toString(), "101", ""))
        {
          _vaultAndPayStep.setStepEnabled();
        }
      }
    });

    _vaultAndPayStep.setOnButtonClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View view)
      {

        _vaultAndPayTransactionViewModel.getTransactionRecordLiveData().observe(PPHVaultAndPayTransaction.this, new Observer<TransactionRecord>()
        {
          @Override
          public void onChanged(@Nullable TransactionRecord transactionRecord)
          {
            if (transactionRecord != null)
            {
              _vaultAndPayStep.setStepCompleted();
            }
          }
        });
        _vaultAndPayTransactionViewModel.getVaultRecordLiveData().observe(PPHVaultAndPayTransaction.this, new Observer<VaultRecord>()
        {
          @Override
          public void onChanged(@Nullable VaultRecord vaultRecord)
          {
            if (vaultRecord != null)
            {
              _vaultAndPayStep.setStepCompleted();
            }
          }
        });
        _vaultAndPayTransactionViewModel.getRetailSDKExceptionMutableLiveData().observe(PPHVaultAndPayTransaction.this, new Observer<RetailSDKException>()
        {
          @Override
          public void onChanged(@Nullable RetailSDKException error)
          {
            if (error != null)
            {
              _vaultAndPayStep.setStepCrossed();
            }
          }
        });
        _vaultAndPayTransactionViewModel.beginPayment();
      }
    });
  }
}
