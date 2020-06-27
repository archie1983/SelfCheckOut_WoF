package com.example.selfcheckout_wof.PPH.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.example.selfcheckout_wof.R;

/* Custom preference to show editText in preferenceFragment.
  Android's default EditTextPreference{@link android.support.v7.preference.EditTextPreference},
  shows the EditText in a Dialog box  */
public class EditTextPreference extends Preference
{
  private EditText tagEditText;
  private String text;
  public EditTextPreference(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    setLayoutResource(R.layout.pph_edit_text_preference);
  }
  public EditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    setLayoutResource(R.layout.pph_edit_text_preference);
  }



  @Override
  public void onBindViewHolder(PreferenceViewHolder holder)
  {
    super.onBindViewHolder(holder);
    tagEditText = (EditText) holder.itemView;
    tagEditText.setText(text);


  }

  public String getText(){
    return tagEditText.getText().toString();
  }

  public void setText(String text){
    this.text = text;
    if(tagEditText!=null){
      tagEditText.setText(text);
    }
  }



}
