package com.example.selfcheckout_wof.PPH.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.selfcheckout_wof.R;

public class StepView extends LinearLayout
{

  private ImageView tick;
  private ImageView cross;
  private Button button;
  private TextView titleTextView;
  private TextView codeTextView;
  private Context context;
  private LinearLayout container;
  private ProgressBar progress;
  public StepView(Context context)
  {
    super(context);
    this.context = context;
  }

  public StepView(Context context, AttributeSet attrs){
    super(context,attrs);

    this.context = context;
    TypedArray attrArray = context.obtainStyledAttributes(attrs, R.styleable.step_view_styleable);

    String title = attrArray.getString(R.styleable.step_view_styleable_title_text);
    String code = attrArray.getString(R.styleable.step_view_styleable_code_text);
    String buttonText = attrArray.getString(R.styleable.step_view_styleable_button_text);
    boolean enabled = attrArray.getBoolean(R.styleable.step_view_styleable_enabled,true);


    title = title == null ? "" : title;
    code = code == null ? "" : code;
    buttonText = buttonText == null ? "" : buttonText;

    LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    container = (LinearLayout) layoutInflater.inflate(R.layout.pph_step_view_layout,this,true);

    titleTextView = (TextView)container.findViewById(R.id.title);
    codeTextView = (TextView)container.findViewById(R.id.code_text);
    button = (Button)container.findViewById(R.id.button);
    tick = (ImageView)container.findViewById(R.id.done_tick);
    cross = (ImageView) container.findViewById(R.id.done_cross);
    progress = (ProgressBar)container.findViewById(R.id.progress);

    titleTextView.setText(title);
    codeTextView.setText(code);
    button.setText(buttonText);
    if (enabled){
      setStepEnabled();
    }else{
      setStepDisabled();
    }

    attrArray.recycle();


  }


  public void setStepDisabled(){
    button.setVisibility(GONE);
    tick.setVisibility(GONE);
    titleTextView.setTextColor(getResources().getColor(R.color.sdk_gray));
    codeTextView.setTextColor(getResources().getColor(R.color.sdk_gray));

  }

  public void setStepEnabled(){
    button.setVisibility(VISIBLE);
    tick.setVisibility(GONE);
    titleTextView.setTextColor(getResources().getColor(R.color.sdk_black));
    codeTextView.setTextColor(getResources().getColor(R.color.sdk_black));
  }

  public void setStepCompleted(){
    button.setVisibility(GONE);
    tick.setVisibility(VISIBLE);
    tick.setImageResource(R.drawable.pph_small_blue_tick);
    titleTextView.setTextColor(getResources().getColor(R.color.sdk_black));
    codeTextView.setTextColor(getResources().getColor(R.color.sdk_black));
  }

  public void setStepCrossed()
  {
    button.setVisibility(GONE);
    tick.setVisibility(GONE);
    cross.setVisibility(VISIBLE);
    cross.setImageResource(R.drawable.pph_cancel_x);
    titleTextView.setTextColor(getResources().getColor(R.color.sdk_black));
    codeTextView.setTextColor(getResources().getColor(R.color.sdk_black));
  }
  public Button getButton(){
    return button;
  }

  public void setOnButtonClickListener(OnClickListener clickListener){
    button.setOnClickListener(clickListener);
  }

  public void setOnStepClickListener(OnClickListener clickListener){
    container.setOnClickListener(clickListener);
  }


  public void showProgressBar()
  {
    button.setVisibility(GONE);
    tick.setVisibility(GONE);
    progress.setVisibility(VISIBLE);
  }

  public void hideProgressBarShowButton(){
    progress.setVisibility(GONE);
    tick.setVisibility(GONE);
    button.setVisibility(VISIBLE);
  }

  public void hideProgressBarShowTick(){
    progress.setVisibility(GONE);
    button.setVisibility(GONE);
    tick.setVisibility(VISIBLE);
  }
}
