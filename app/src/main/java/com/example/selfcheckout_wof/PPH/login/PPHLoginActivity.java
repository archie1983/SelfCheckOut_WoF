package com.example.selfcheckout_wof.PPH.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.selfcheckout_wof.PPH.ui.ReaderConnectionActivity;
import com.example.selfcheckout_wof.PPH.ui.StepView;
import com.example.selfcheckout_wof.PPH.ui.ToolbarActivity;
import com.example.selfcheckout_wof.R;
import com.example.selfcheckout_wof.btprinter_zj.BTPrintManagement;
import com.example.selfcheckout_wof.btprinter_zj.BTPrinterConstants;
import com.paypal.paypalretailsdk.AppInfo;
import com.paypal.paypalretailsdk.Merchant;
import com.paypal.paypalretailsdk.RetailSDK;
import com.paypal.paypalretailsdk.RetailSDKException;
import com.paypal.paypalretailsdk.SdkCredential;

import java.util.Set;

import static com.example.selfcheckout_wof.PPH.ui.OfflinePayActivity.OFFLINE_INIT;
import static com.example.selfcheckout_wof.PPH.ui.OfflinePayActivity.OFFLINE_MODE;
import static com.example.selfcheckout_wof.PPH.ui.OfflinePayActivity.PREF_NAME;

/**
 * The Activity that allows the user to log onto Paypal and connect the card reader.
 */
  // If we use toolbar as the base class for this activity, then the whole application needs to
  // use toolbar and all activities need to extend ToolbarActivity instead of AppCompatActivity.
  // Maybe we'll want that in the future, but for now we need this activity to extend AppCompatActivity
  // and not the ToolbarActivity.
public class PPHLoginActivity extends ToolbarActivity implements View.OnClickListener
//public class PPHLoginActivity extends AppCompatActivity implements View.OnClickListener
{
  private static final String LOG_TAG = PPHLoginActivity.class.getSimpleName();
  public static final String PREFS_NAME = "SDKSampleAppPreferences";
  public static final String PREF_TOKEN_KEY_NAME = "lastToken";
  // private static final String MID_TIER_URL_FOR_LIVE = "http://pph-retail-sdk-sample.herokuapp.com/toPayPal/live";
  //private static final String MID_TIER_URL_FOR_LIVE = "https://ae-co-test.herokuapp.com/toPayPal/live?returnTokenOnQueryString=true";
  //private static final String MID_TIER_URL_FOR_LIVE = "https://pph.elksnis.co.uk/toPayPal/live?returnTokenOnQueryString=true";
  private static final String MID_TIER_URL_FOR_LIVE = "https://pph.elksnis.co.uk/toPayPal/live";
  // private static final String MID_TIER_URL_FOR_SANDBOX = "http://pph-retail-sdk-sample.herokuapp.com/toPayPal/sandbox";
  //private static final String MID_TIER_URL_FOR_SANDBOX = "https://pph-retail-sdk-sample.herokuapp.com/toPayPal/sandbox?returnTokenOnQueryString=true";
  //private static final String MID_TIER_URL_FOR_SANDBOX = "https://ae-co-test.herokuapp.com/toPayPal/sandbox?returnTokenOnQueryString=true";
  private static final String MID_TIER_URL_FOR_SANDBOX = "https://pph.elksnis.co.uk/toPayPal/sandbox?returnTokenOnQueryString=true";
  private static final String SW_REPOSITORY = "production"; // "production-stage"
  public static final String INTENT_URL_WEBVIEW = "URL_FOR_WEBVIEW";
  public static final String INTENT_ISLIVE_WEBVIEW = "ISLIVE_FOR_WEBVIEW";

  private ProgressDialog mProgressDialog = null;
  private RadioGroup radioGroup1;

  private StepView stpConnectToPaypal, stpConnectPrinter;
  private Boolean offlineClicked;

  private Button connectButton, testPrinterButton;


  // abstract method from ToolbarActivity
  @Override
  public int getLayoutResId()
  {
    return R.layout.pph_login_activity;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    Log.d(LOG_TAG, "onCreate");
    //setContentView(R.layout.pph_login_activity);

    radioGroup1 = (RadioGroup) findViewById(R.id.radioGroup1);
    connectButton = (Button) findViewById(R.id.connect_reader_button);
    testPrinterButton = (Button) findViewById(R.id.test_printer_button);

    stpConnectToPaypal = (StepView) findViewById(R.id.stpConnectToPaypal);
    stpConnectToPaypal.setOnButtonClickListener(this);

    /**
     * Printer connection stepview
     */
    stpConnectPrinter = (StepView) findViewById(R.id.stpConnectPrinter);
    stpConnectPrinter.setOnButtonClickListener(this);

    offlineClicked = false;
  }



  @Override
  public void onConfigurationChanged(Configuration newConfig)
  {
    super.onConfigurationChanged(newConfig);
    Log.d(LOG_TAG, "onConfigurationChanged");
  }



  public void onInitMerchantClicked()
  {
    RadioButton sandboxButton = (RadioButton) findViewById(R.id.radioSandbox);
    RadioButton liveButton = (RadioButton) findViewById(R.id.radioLive);

    stpConnectToPaypal.setCodeText(getString(R.string.pph_connecting));

    if (sandboxButton.isChecked())
    {
      /*If User selected Sandbox environment then we need to check if we already have sandbox token form mid tier server or not.
       * If we don't have the token then we need to start web view, else we can use the token and set it to sdk.
       */
      //String token = LocalPreferences.getSandboxMidtierToken(LoginActivity.this);
      String accessToken = PPHLocalPreferences.getSandboxMidtierAccessToken(PPHLoginActivity.this);
      String refreshUrl = PPHLocalPreferences.getSandboxMidtierRefreshUrl(PPHLoginActivity.this);
      String env = PPHLocalPreferences.getSandboxMidtierEnv(PPHLoginActivity.this);
      if (null == accessToken || null == refreshUrl || null == env)
      {
        startWebView(MID_TIER_URL_FOR_SANDBOX, true, false);
      }
      else
      {
        Log.d(LOG_TAG, "onLoginButtonClicked looks like we have sandbox access token: " + accessToken);
        //initializeMerchant(token, SW_REPOSITORY);
        SdkCredential credential = new SdkCredential(env, accessToken);
        credential.setTokenRefreshCredentials(refreshUrl);
        Log.d(LOG_TAG, "onLoginButtonClicked looks like we have live token. Starting payment options activity");
        initializeMerchant(credential);

      }
    }
    else
    {
      /* If User selected Live environment then we need to check if we already have live token form mid tier server or not.
       * If we don't have the token then we need to start web view, else we can use the token and set it to sdk.
       */
      //String token = LocalPreferences.getLiveMidtierToken(LoginActivity.this);
      String accessToken = PPHLocalPreferences.getLiveMidtierAccessToken(PPHLoginActivity.this);
      String refreshUrl = PPHLocalPreferences.getLiveMidtierRefreshUrl(PPHLoginActivity.this);
      String env = PPHLocalPreferences.getLiveMidtierEnv(PPHLoginActivity.this);

      if (null == accessToken || null == refreshUrl || null == env)
      {
        startWebView(MID_TIER_URL_FOR_LIVE, false, true);
        //hardCodingInitMerchant();
      }
      else
      {
        Log.d(LOG_TAG, "onLoginButtonClicked looks like we have live access token: " + accessToken);
        SdkCredential credential = new SdkCredential(env, accessToken);
        credential.setTokenRefreshCredentials(refreshUrl);
        Log.d(LOG_TAG, "onLoginButtonClicked looks like we have live token. Starting payment options activity");
        initializeMerchant(credential);
      }
    }
  }

  private void hardCodingInitMerchant()//use this method when using stage rather than live
  {
    Log.d(LOG_TAG, "hard-coding for initializeMerchant()");
    // hard coding the initializeMerchant
    String access_token = "A103.LYafNu2QoAUgEDGshkgLaN9M2xSeTnhTfrFns2HNYdceSa6GO3LhScOMQzdX3TI8.ZeE98NKhHF72Wu-4QNLQ4Rlct8i";
    String env = "stage2d0020";
    Log.d(LOG_TAG, "shouldOverrideURLLoading: access_token: " + access_token);
    Log.d(LOG_TAG, "shouldOverrideURLLoading: env: " + env);
    SdkCredential credential = new SdkCredential(env, access_token);
    // credential.setTokenRefreshCredentials(refresh_url);
    initializeMerchant(credential);
  }

  public void onLogoutClicked(View view)
  {
    RetailSDK.logout();

    // Need to remove tokens from local preferences
    PPHLocalPreferences.storeSandboxMidTierCredentials(PPHLoginActivity.this, null, null, null);
    PPHLocalPreferences.storeLiveMidTierCredentials(PPHLoginActivity.this, null, null, null);

    Toast.makeText(getApplicationContext(), "Logged out! Please initialize Merchant.", Toast.LENGTH_SHORT).show();

    Intent intent = getIntent();
    finish();
    startActivity(intent);
  }


  public void onConnectCardReaderClicked(View view)
  {
    Intent readerConnectionIntent = new Intent(PPHLoginActivity.this, ReaderConnectionActivity.class);
    readerConnectionIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    startActivity(readerConnectionIntent);
  }

  /**
   * Test printer after we've connected to it.
   * @param view
   */
  public void onTestPrinter(View view) {
    BTPrintManagement.testPrinter();
  }

  private void startWebView(String url, final boolean isSandBox, final boolean isLive)
  {
    Log.d(LOG_TAG, "startWebView url: " + url + " isSandbox: " + isSandBox + " isLive: " + isLive);

    final WebView webView = (WebView) findViewById(R.id.id_webView);
    webView.setVisibility(View.VISIBLE);

    webView.getSettings().setJavaScriptEnabled(true);
    webView.requestFocus(View.FOCUS_DOWN);
    webView.setWebViewClient(new WebViewClient()
    {
      public boolean shouldOverrideUrlLoading(WebView view, String url)
      {
        Log.d(LOG_TAG, "shouldOverrideURLLoading: url: " + url);
        //String returnStringCheckParam = "retailsdksampleapp://oauth?sdk_token=";
        String returnStringCheckParam = "retailsdksampleapp://oauth?access_token=";

        // List<NameValuePair> parameters = URLEncodedUtils.parse(new URI(url));
        Uri uri = Uri.parse(url);
        Set<String> paramNames = uri.getQueryParameterNames();
        for (String key: paramNames) {
          String value = uri.getQueryParameter(key);
          Log.d(LOG_TAG, "shouldOverrideURLLoading: name: " + key + " value: " + value);
        }

        if (null != url && url.startsWith(returnStringCheckParam))
        {
          if (paramNames.contains("access_token") && paramNames.contains("refresh_url") && paramNames.contains("env"))
          {
            String access_token = uri.getQueryParameter("access_token");
            String refresh_url = uri.getQueryParameter("refresh_url");
            String env = uri.getQueryParameter("env");
            Log.d(LOG_TAG, "shouldOverrideURLLoading: access_token: " + access_token);
            Log.d(LOG_TAG, "shouldOverrideURLLoading: refresh_url: " + refresh_url);
            Log.d(LOG_TAG, "shouldOverrideURLLoading: env: " + env);
            SdkCredential credential = new SdkCredential(env, access_token);
            credential.setTokenRefreshCredentials(refresh_url);
            //String compositeToken = url.substring(returnStringCheckParam.length());
            //Log.d(LOG_TAG, "shouldOverrideURLLoading compositeToken: " + compositeToken);
            if (isSandBox)
            {
              //LocalPreferences.storeSandboxMidTierToken(LoginActivity.this, compositeToken);
              //startPaymentOptionsActivity(compositeToken, PayPalHereSDK.Sandbox);
              //initializeMerchant(compositeToken, SW_REPOSITORY);
              PPHLocalPreferences.storeSandboxMidTierCredentials(PPHLoginActivity.this, access_token, refresh_url, env);
              initializeMerchant(credential);
            }
            else if (isLive)
            {
              PPHLocalPreferences.storeLiveMidTierCredentials(PPHLoginActivity.this, access_token, refresh_url, env);
              //LocalPreferences.storeLiveMidTierToken(LoginActivity.this, compositeToken);
              // startPaymentOptionsActivity(compositeToken, PayPalHereSDK.Live);
              initializeMerchant(credential);
            }
            webView.setVisibility(View.GONE);
            return true;
          }
        }
        return false;
      }
    });

    webView.loadUrl(url);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == 1) {
      if (resultCode == Activity.RESULT_OK) {
        String result = data.getStringExtra("result");
        Log.d(LOG_TAG, "onActivityResult result: " + result);
      }
      if (resultCode == Activity.RESULT_CANCELED) {
        Log.d(LOG_TAG, "onActivityResult RESULT_CANCELED! ");
        //Write your code if there's no result
      }
    } else if (requestCode == BTPrinterConstants.REQUEST_CONNECT_DEVICE ||
            requestCode == BTPrinterConstants.REQUEST_ENABLE_BT) {
      /*
       * If we have BT printer codes coming back to deal with, then handle that with the
       * static functions that we have.
       */
      BTPrintManagement.processBTActivityResult(requestCode, resultCode, data);
      stpConnectPrinter.hideProgressBarShowTick();
      testPrinterButton.setVisibility(View.VISIBLE);
    }
  }

  private void initializeMerchant(final String token, String repository)
  {
    Log.d(LOG_TAG, "initializeMerchant token: " + token);
    Log.d(LOG_TAG, "initializeMerchant serverName: " + repository);

    try
    {
      showProcessingProgressbar();
      RetailSDK.initializeMerchant(token, repository, new RetailSDK.MerchantInitializedCallback()
      {
        @Override
        public void merchantInitialized(RetailSDKException error, Merchant merchant)
        {
          saveToken(token);
          SharedPreferences pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
          SharedPreferences.Editor editor = pref.edit();
          editor.putBoolean(OFFLINE_MODE, false);
          editor.putBoolean(OFFLINE_INIT, false);
          editor.apply();
          editor.commit();
          PPHLoginActivity.this.merchantReady(error, merchant);
        }
      });
    }
    catch (Exception x)
    {
      try
      {
        Log.e(LOG_TAG, "exception: " + x.toString());
        //statusText.setText(x.toString());
      }
      catch (Exception ignore)
      {
        ignore.printStackTrace();
      }
      x.printStackTrace();
    }
  }

  private void initializeMerchant(final SdkCredential credential)
  {
    try
    {
      showProcessingProgressbar();
      RetailSDK.initializeMerchant(credential, new RetailSDK.MerchantInitializedCallback()
      {
        @Override
        public void merchantInitialized(RetailSDKException error, Merchant merchant)
        {
          SharedPreferences pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
          SharedPreferences.Editor editor = pref.edit();
          editor.putBoolean(OFFLINE_MODE, false);
          editor.putBoolean(OFFLINE_INIT, false);
          editor.apply();
          editor.commit();
          PPHLoginActivity.this.merchantReady(error, merchant);
        }
      });
    }
    catch (Exception x)
    {
      try
      {
        Log.e(LOG_TAG, "exception: " + x.toString());
        //statusText.setText(x.toString());
      }
      catch (Exception ignore)
      {
        ignore.printStackTrace();
      }
      x.printStackTrace();
    }
  }

  private void initializeMerchantOffline()
  {
    try {
      showProcessingProgressbar();
      RetailSDK.initializeMerchantOffline(new RetailSDK.MerchantInitializedCallback()
      {
        @Override
        public void merchantInitialized(RetailSDKException error, Merchant merchant)
        {
          offlineClicked = true;
          if (error == null) {
            SharedPreferences pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean(OFFLINE_MODE, true);
            editor.putBoolean(OFFLINE_INIT, true);
            editor.apply();
            editor.commit();
          }
          PPHLoginActivity.this.merchantReady(error, merchant);
        }
      });
    }
    catch (Exception x)
    {
      Log.e(LOG_TAG, "Exception: " + x.toString());
      x.printStackTrace();
    }
  }


  void merchantReady(final RetailSDKException error, final Merchant merchant)
  {
    if (error == null)
    {
      PPHLoginActivity.this.runOnUiThread(new Runnable()
      {
        @Override
        public void run()
        {
          // Add the BN code for Partner tracking. To obtain this value, contact
          // your PayPal account representative. Please do not change this value when
          // using this sample app for testing.
          merchant.setReferrerCode("PPHSDK_SampleApp_Android");

          Log.d(LOG_TAG, "merchantReady without any error");
          cancelProgressbar();

          stpConnectToPaypal.setStepCompleted();
          final TextView txtMerchantEmail = (TextView) findViewById(R.id.merchant_email);
          if (offlineClicked) {
            txtMerchantEmail.setText("Offline Merchant loaded");
            stpConnectToPaypal.setCodeText("");
          }
          else
          {
            txtMerchantEmail.setText(merchant.getEmailAddress());
            stpConnectToPaypal.setCodeText(getString(R.string.pph_connected_as) + " " + merchant.getEmailAddress());
          }
          final RelativeLayout logoutContainer = (RelativeLayout) findViewById(R.id.logout);
          logoutContainer.setVisibility(View.VISIBLE);
          connectButton.setVisibility(View.VISIBLE);
        }
      });
    }
    else
    {
      PPHLoginActivity.this.runOnUiThread(new Runnable()
      {
        @Override
        public void run()
        {
          Log.d(LOG_TAG, "RetailSDK initialize on Error:" + error.toString());
          cancelProgressbar();
          AlertDialog.Builder builder = new AlertDialog.Builder(PPHLoginActivity.this);
          builder.setTitle(R.string.error_title);
          if (offlineClicked) {
            builder.setMessage(error.getMessage());
          } else
          {
            builder.setMessage(R.string.error_initialize_msg);
          }
          builder.setCancelable(false);
          builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener()
          {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
              Log.d(LOG_TAG, "RetailSDK Initialize error AlertDialog onClick");
              dialog.dismiss();
              finish();
            }
          });
          builder.show();
        }
      });
    }
  }


  private void saveToken(String token)
  {
    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    SharedPreferences.Editor editor = settings.edit();
    editor.putString(PREF_TOKEN_KEY_NAME, token);
    editor.commit();
  }


  private void showProcessingProgressbar()
  {
    stpConnectToPaypal.showProgressBar();
  }


  private void cancelProgressbar()
  {
    stpConnectToPaypal.hideProgressBarShowTick();
  }


  @Override
  public void onClick(View v)
  {
    if (v == stpConnectToPaypal.getButton()){
      initSDK();
    } else if(v == stpConnectPrinter.getButton()) {
      /**
       * Going through all BT printer connection steps, which we normally do as part of form
       * loading in @link com.example.selfcheckout_wof.SelfCheckoutChargeActivity
       */
      stpConnectPrinter.showProgressBar();
      BTPrintManagement.setPrinterReadyBehaviour(new Runnable() {
        @Override
        public void run() {
          stpConnectPrinter.hideProgressBarShowTick();
          testPrinterButton.setVisibility(View.VISIBLE);
        }
      });
      BTPrintManagement.setContext(this);
      BTPrintManagement.createBTPrinterAdapter();

      /*
       * Attempt to create the BT printer service. If it needs to be created, then this may
       * invoke an activity that shows all BT devices and then the returning intent from that
       * activity will be processed in this activity and things like progress bar disabled, etc.
       *
       * But if printer service doesn't need to be created, then we need to disable progress
       * bar here.
       */
      if (!BTPrintManagement.createBTPrinterService()) {
        //BTPrintManagement.processBTActivityResult(requestCode, resultCode, data);
        BTPrintManagement.tryToConnectToCurrentMACAddress(false);
      }

      BTPrintManagement.startBTPrinterService();
    }
  }

  @Override
  protected void onStop() {
    /**
     * Making sure that we free BT resources that we used for printer connection.
     * We won't need it anymore because we only wanted to make sure that we can connect
     * to the printer and we wanted to have the MAC address at this point so that we can
     * quikly re-connect when we really need to print.
     */
    super.onStop();
    BTPrintManagement.stopBTPrinterService();
  }

  public void initSDK()
  {
    try
    {
      stpConnectToPaypal.setCodeText(getString(R.string.pph_initialising_connection));
      showProcessingProgressbar();
      //AppInfo info = new AppInfo(getString(R.string.pph_storage_name), "1.0", "01");
      AppInfo info = new AppInfo(getString(R.string.app_name), "1.0", "01");
      RetailSDK.initialize(getApplicationContext(), new RetailSDK.AppState()
      {
        @Override
        public Activity getCurrentActivity()
        {
          return PPHLoginActivity.this;
        }


        @Override
        public boolean getIsTabletMode()
        {
          return false;
        }
      }, info);
      /**
       * Add this observer to handle insecure network errors from the sdk
       */
      RetailSDK.addUntrustedNetworkObserver(new RetailSDK.UntrusterNetworkObserver() {
        @Override
        public void untrustedNetwork(RetailSDKException error) {
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
              builder.setMessage("Insecure network. Please join a secure network and open the app again")
                  .setCancelable(true)
                  .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                      finish();
                    }
                  });
              AlertDialog alert = builder.create();
              alert.show();
              stpConnectToPaypal.setCodeText("");
            }
          });
        }
      });
    }
    catch (RetailSDKException e)
    {
      e.printStackTrace();
    }

    onInitMerchantClicked();
  }
}
