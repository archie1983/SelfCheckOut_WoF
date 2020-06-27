package com.example.selfcheckout_wof.PPH.ui;

/**
 * Created by muozdemir on 12/5/17.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.selfcheckout_wof.R;
import com.example.selfcheckout_wof.PPH.login.PPHLoginActivity;

public class MainActivity extends Activity
{
  private static final String LOG_TAG = MainActivity.class.getSimpleName();


  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.pph_start_activity);
  }


  public void onStartClicked(View view)
  {
    Log.d(LOG_TAG, "goToLoginActivity");
    Intent intent = new Intent(MainActivity.this, PPHLoginActivity.class);
    startActivity(intent);
  }

}