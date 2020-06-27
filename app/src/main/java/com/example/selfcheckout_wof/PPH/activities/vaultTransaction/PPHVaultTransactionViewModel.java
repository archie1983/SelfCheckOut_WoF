package com.example.selfcheckout_wof.PPH.activities.vaultTransaction;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.paypal.paypalretailsdk.RetailSDKException;
import com.paypal.paypalretailsdk.TransactionBeginOptions;
import com.paypal.paypalretailsdk.TransactionBeginOptionsPaymentTypes;
import com.paypal.paypalretailsdk.TransactionBeginOptionsVaultProvider;
import com.paypal.paypalretailsdk.TransactionBeginOptionsVaultType;
import com.paypal.paypalretailsdk.TransactionContext;
import com.paypal.paypalretailsdk.TransactionManager;
import com.paypal.paypalretailsdk.VaultRecord;

public class PPHVaultTransactionViewModel extends ViewModel
{
  private PPHVaultTransactionModel _vaultTransactionModel;
  private final String LOG_TAG = "VaultTransactionVM";
  private final String BRAINTREE_CUSTOMER_ID = "4085815786";
  private MutableLiveData<VaultRecord> _vaultRecordLiveData;
  private MutableLiveData<RetailSDKException> _retailSDKExceptionMutableLiveData;


  public MutableLiveData<RetailSDKException> getRetailSDKExceptionMutableLiveData()
  {
    if (_retailSDKExceptionMutableLiveData == null)
    {
      _retailSDKExceptionMutableLiveData = new MutableLiveData<>();
    }
    return _retailSDKExceptionMutableLiveData;
  }


  public MutableLiveData<VaultRecord> getVaultRecordLiveData()
  {
    if (_vaultRecordLiveData == null)
    {
      _vaultRecordLiveData = new MutableLiveData<>();
    }
    return _vaultRecordLiveData;
  }


  public PPHVaultTransactionViewModel()
  {
    _vaultTransactionModel = new PPHVaultTransactionModel();
  }


  String getBraintreeLoginUrl()
  {
    return _vaultTransactionModel.getBraintreeLoginURL();
  }


  boolean isBraintreeReturnUrlValid(String returnUrl)
  {
    return _vaultTransactionModel.isBraintreeReturnUrlValid(returnUrl);
  }


  void beginPayment()
  {
    _vaultTransactionModel.createTransaction(
        new TransactionManager.TransactionCallback()
        {
          @Override
          public void transaction(RetailSDKException error, TransactionContext transactionContext)
          {
            if (error != null)
            {
              _retailSDKExceptionMutableLiveData.setValue(error);
            }
            else
            {
              transactionContext.setVaultCompletedHandler(new TransactionContext.VaultCompletedCallback()
              {
                @Override
                public void vaultCompleted(final RetailSDKException error, final VaultRecord vaultRecord)
                {
                  if (error != null)
                  {
                    new Handler(Looper.getMainLooper()).post(new Runnable()
                    {
                      @Override
                      public void run()
                      {
                        _retailSDKExceptionMutableLiveData.setValue(error);
                      }
                    });
                  }
                  else
                  {
                    new Handler(Looper.getMainLooper()).post(new Runnable()
                    {
                      @Override
                      public void run()
                      {
                        _vaultRecordLiveData.setValue(vaultRecord);
                      }
                    });
                  }
                }
              });

              TransactionBeginOptions transactionBeginOptions = new TransactionBeginOptions();
              transactionBeginOptions.setPaymentType(TransactionBeginOptionsPaymentTypes.card);
              transactionBeginOptions.setVaultProvider(TransactionBeginOptionsVaultProvider.Braintree);
              transactionBeginOptions.setVaultType(TransactionBeginOptionsVaultType.VaultOnly);
              transactionBeginOptions.setVaultCustomerId(BRAINTREE_CUSTOMER_ID);
              transactionContext.beginPayment(transactionBeginOptions);
            }
          }
        }
    );
  }


  @Override
  protected void onCleared()
  {
    super.onCleared();
  }
}
