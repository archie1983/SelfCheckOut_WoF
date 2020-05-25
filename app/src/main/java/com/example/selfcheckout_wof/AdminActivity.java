package com.example.selfcheckout_wof;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.selfcheckout_wof.custom_components.exceptions.DataImportExportException;
import com.example.selfcheckout_wof.custom_components.utils.PaypalHereVariables;
import com.example.selfcheckout_wof.custom_components.utils.SalesItemsCache;
import com.example.selfcheckout_wof.custom_components.utils.SqliteExportAndImport;
import com.example.selfcheckout_wof.data.SystemChoices;
import com.paypal.paypalretailsdk.AppInfo;
import com.paypal.paypalretailsdk.Merchant;
import com.paypal.paypalretailsdk.RetailSDK;
import com.paypal.paypalretailsdk.RetailSDKException;
import com.paypal.paypalretailsdk.SdkCredential;
import java.util.Set;

public class AdminActivity extends AppCompatActivity {

    /**
     * Intent receiver for handling intents issued by GUI (e.g. when we want to launch
     * the sales activity or data management, etc.)
     */
    private BroadcastReceiver selfCheckoutIntentReveiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            /**
             * Handling the intent generated by one of the main GUI Admin choices
             * (typically generated in SystemChoiceItemView).
             */
            switch(SystemChoices.lookUpByIntent(intent)) {
                case MANAGE_DATA:
                    onManageData(null);
                    break;
                case START_SALES:
                    onStartSales(null);
                    break;
                case EXPORT_DB:
                    onExportDB(null);
                case IMPORT_DB:
                    onImportDB(null);
                    break;
                case LOGIN_PAYPAL:
                    initPaypalHere();
                    break;
                case UNKNOWN:
                    break;
            }
        }
    };

    protected boolean shouldAskPermissions() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @TargetApi(23)
    protected void askPermissions() {
        String[] permissions = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"
        };
        int requestCode = 200;
        requestPermissions(permissions, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        progress = (ProgressBar)findViewById(R.id.progress);

        //# Ensuring that we have persmissions for SD card read/write
        if (shouldAskPermissions()) {
            askPermissions();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        for (SystemChoices sc : SystemChoices.values()) {
            filter.addAction(sc.getIntent().getAction());
        }

        filter.addCategory(Intent.CATEGORY_DEFAULT);
        this.registerReceiver(selfCheckoutIntentReveiver,filter);
    }

    /** Called when another activity is taking focus. */
    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(selfCheckoutIntentReveiver);
    }

    /**
     * "Manage Data" button press
     * @param view
     */
    public void onManageData(View view) {
        Intent showDataAdminActivity = new Intent(this, DataAdminActivity.class);
        startActivity(showDataAdminActivity);
    }

    /**
     * "Start Sales" button press
     * @param view
     */
    public void onStartSales(View view) {
        Intent showSalesActivity = new Intent(this, SalesActivity.class);
        startActivity(showSalesActivity);
    }

    private static final int TO_EXPORT_DB_REQUEST_CODE = 1;
    private static final int TO_IMPORT_DB_REQUEST_CODE = 2;
    /**
     * "Export DB" button press
     * @param view
     */
    public void onExportDB(View view) {
        /*
         * If the export button was pressed, then we'll ask user for where to store the exported
         * data with the help of this activity and intent. It will later call back the
         * onActivityResult(...) function, where the real thing starts.
         */
        startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), TO_EXPORT_DB_REQUEST_CODE);
    }

    /**
     * "Import DB" button press
     * @param view
     */
    public void onImportDB(View view) {
        /*
         * If the import button was pressed, then we'll ask user for where to read the
         * data with the help of this activity and intent. It will later call back the
         * onActivityResult(...) function, where the real thing starts.
         */
        startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), TO_IMPORT_DB_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (resultCode != RESULT_OK)
            return;
        Uri treeUri = resultData.getData();
        DocumentFile pickedDir = DocumentFile.fromTreeUri(this, treeUri);

        if (requestCode == TO_EXPORT_DB_REQUEST_CODE) {
            grantUriPermission(getPackageName(), treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            try {
                SqliteExportAndImport.export(getApplicationContext(), getContentResolver(), pickedDir, SalesItemsCache.getDBInstance().getOpenHelper().getReadableDatabase());
            } catch (DataImportExportException exc) {
                Log.d(LOG_TAG, exc.getMessage());
            }
        } else if (requestCode == TO_IMPORT_DB_REQUEST_CODE) {
            grantUriPermission(getPackageName(), treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
                SqliteExportAndImport.importData(getApplicationContext(), getContentResolver(), pickedDir,
                        SalesItemsCache.getDBInstance().getOpenHelper().getWritableDatabase());
            } catch (DataImportExportException exc) {
                Log.d(LOG_TAG, exc.getMessage());
            }
        }
    }

    /**
     * "Log in to PayPal" button press
     * @param view
     */
    public void onLogInPayPal(View view) {
        initPaypalHere();
    }

    private static final String LOG_TAG = AdminActivity.class.getSimpleName();
    private static final String MID_TIER_URL_FOR_LIVE = "https://pph-retail-sdk-sample.herokuapp.com/toPayPal/live?returnTokenOnQueryString=true";
    public static final String PREF_NAME = "SelfCheckOutPrefs";
    public static final String OFFLINE_MODE ="offlineMode";
    public static final String OFFLINE_INIT ="offlineInit";

    private ProgressBar progress;
    /**
     * Initialise the PaypalHere SDK. The context here should be application context
     * (getApplicationContext()) rather than just the activity, because this method
     * will likely be called many times during normal operation fo the app.
     *
     */
    public void initPaypalHere()
    {
        try
        {
            AppInfo info = new AppInfo("SelfCheckOut_WoF", "1.0", "01");
            RetailSDK.initialize(getApplicationContext(), new RetailSDK.AppState()
            {
                @Override
                public Activity getCurrentActivity()
                {
                    return AdminActivity.this;
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
                    AdminActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(AdminActivity.this);
                            builder.setMessage("Insecure network. Please join a secure network and open the app again")
                                    .setCancelable(true)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            finish();
                                        }
                                    });
                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                    });
                }
            });
        }
        catch (RetailSDKException e)
        {
            e.printStackTrace();
        }
        initPaypalHereStep2();
    }

    private void initPaypalHereStep2() {
        /* If User selected Live environment then we need to check if we already have live token form mid tier server or not.
         * If we don't have the token then we need to start web view, else we can use the token and set it to sdk.
         */
        //String token = LocalPreferences.getLiveMidtierToken(LoginActivity.this);
        String accessToken = PaypalHereVariables.getLiveMidtierAccessToken(this);
        String refreshUrl = PaypalHereVariables.getLiveMidtierRefreshUrl(this);
        String env = PaypalHereVariables.getLiveMidtierEnv(this);

        if (null == accessToken || null == refreshUrl || null == env)
        {
            startWebView(this, MID_TIER_URL_FOR_LIVE, false, true);
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

    private void startWebView(final Activity activity, String url, final boolean isSandBox, final boolean isLive)
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
                            PaypalHereVariables.storeSandboxMidTierCredentials(activity, access_token, refresh_url, env);
                            initializeMerchant(credential);
                        }
                        else if (isLive)
                        {
                            PaypalHereVariables.storeLiveMidTierCredentials(activity, access_token, refresh_url, env);
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

    private void initializeMerchant(final SdkCredential credential)
    {
        try
        {
            showProgressBar();
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
                    AdminActivity.this.merchantReady(error, merchant);
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

    void merchantReady(final RetailSDKException error, final Merchant merchant)
    {
        if (error == null)
        {
            AdminActivity.this.runOnUiThread(new Runnable()
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

                    final TextView txtMerchantEmail = (TextView) findViewById(R.id.merchant_email);
                    txtMerchantEmail.setText(getResources().getString(R.string.txtMerchantEmail) + merchant.getEmailAddress());
                }
            });
        }
        else
        {
            AdminActivity.this.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    Log.d(LOG_TAG, "RetailSDK initialize on Error:" + error.toString());
                    cancelProgressbar();
                    AlertDialog.Builder builder = new AlertDialog.Builder(AdminActivity.this);
                    builder.setTitle(R.string.paypal_error_title);
                    builder.setMessage(R.string.paypal_error_initialize_msg);
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

    public void showProgressBar()
    {
        progress.setVisibility(View.VISIBLE);
    }

    public void cancelProgressbar(){
        progress.setVisibility(View.GONE);
    }
}
