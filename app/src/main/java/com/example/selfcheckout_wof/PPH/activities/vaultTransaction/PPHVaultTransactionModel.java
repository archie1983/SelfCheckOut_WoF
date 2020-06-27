package com.example.selfcheckout_wof.PPH.activities.vaultTransaction;

import com.paypal.paypalretailsdk.RetailSDK;
import com.paypal.paypalretailsdk.TransactionManager;

class PPHVaultTransactionModel
{

  String getBraintreeLoginURL()
  {
    return RetailSDK.getBraintreeManager().getBtLoginUrl();
  }


  boolean isBraintreeReturnUrlValid(String returnUrl)
  {
    return RetailSDK.getBraintreeManager().isBtReturnUrlValid(returnUrl);
  }


  void createTransaction(TransactionManager.TransactionCallback transactionCallback)
  {
    RetailSDK.getTransactionManager().createVaultTransaction(transactionCallback);
  }
}
