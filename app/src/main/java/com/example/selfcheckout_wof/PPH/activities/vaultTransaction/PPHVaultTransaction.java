package com.example.selfcheckout_wof.PPH.activities.vaultTransaction;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.selfcheckout_wof.PPH.ui.StepView;
import com.example.selfcheckout_wof.R;
import com.paypal.paypalretailsdk.RetailSDKException;
import com.paypal.paypalretailsdk.VaultRecord;

import java.util.Objects;


public class PPHVaultTransaction extends AppCompatActivity
{
  private String LOG_TAG = PPHVaultTransaction.class.getSimpleName();
  private PPHVaultTransactionViewModel _vaultTransactionViewModel;

  StepView _vaultStep;


  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.pph_activity_vault_transaction);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);

    _vaultTransactionViewModel = ViewModelProviders.of(PPHVaultTransaction.this).get(PPHVaultTransactionViewModel.class);
    _vaultStep = findViewById(R.id.begin_vault_step);
    _vaultStep.setStepEnabled();
  }


  @Override
  protected void onStart()
  {
    super.onStart();

    _vaultStep.setOnButtonClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View view)
      {

        _vaultTransactionViewModel.getVaultRecordLiveData().observe(PPHVaultTransaction.this, new Observer<VaultRecord>()
        {
          @Override
          public void onChanged(@Nullable VaultRecord vaultRecord)
          {
            if (vaultRecord != null)
            {
              _vaultStep.setStepCompleted();
            }
          }
        });
        _vaultTransactionViewModel.getRetailSDKExceptionMutableLiveData().observe(PPHVaultTransaction.this, new Observer<RetailSDKException>()
        {
          @Override
          public void onChanged(@Nullable RetailSDKException error)
          {
            if (error != null)
            {
              _vaultStep.setStepCrossed();
            }
          }
        });
        _vaultTransactionViewModel.beginPayment();
      }
    });
  }
}
