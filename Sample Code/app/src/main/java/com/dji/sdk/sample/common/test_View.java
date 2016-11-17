package com.dji.sdk.sample.common;

import android.app.Service;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dji.sdk.sample.R;

/**
 * Created by dji on 15/12/20.
 */
public abstract class test_View extends RelativeLayout implements View.OnClickListener {


    protected Button middleBtn;
    protected Button leftBtn;
    protected Button rightBtn;
    protected Button middle_rightBtn;
    protected Button middle_leftBtn;
    protected Button below_leftBtn;
    protected Button below_middleBtn;
    protected Button below_rightBtn;
    protected TextView Controller_X_text;
    protected TextView Controller_Y_text;
    protected TextView Controller_Z_text;
    public test_View(Context context, AttributeSet attrs) {
        super(context, attrs);
        initUI(context, attrs);
    }

    private void initUI(Context context, AttributeSet attrs) {
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Service.LAYOUT_INFLATER_SERVICE);

        View content = layoutInflater.inflate(R.layout.test_view2, null, false);
        addView(content, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));



        middleBtn = (Button) findViewById(R.id.button2);
        middle_rightBtn = (Button) findViewById(R.id.button5);
        middle_leftBtn = (Button) findViewById(R.id.button4);
        leftBtn = (Button) findViewById(R.id.button1);
        rightBtn = (Button) findViewById(R.id.button3);
        below_middleBtn = (Button) findViewById(R.id.button8);
        below_rightBtn = (Button) findViewById(R.id.button7);
        below_leftBtn = (Button) findViewById(R.id.button6);

        Controller_X_text = (TextView) findViewById(R.id.textView1);
        Controller_Y_text = (TextView) findViewById(R.id.textView6);
        Controller_Z_text = (TextView) findViewById(R.id.textView8);
        Controller_X_text.setText(getString(getInfoResourceId()));

        middleBtn.setOnClickListener(this);
        leftBtn.setOnClickListener(this);
        rightBtn.setOnClickListener(this);
        middle_leftBtn.setOnClickListener(this);
        middle_rightBtn.setOnClickListener(this);

        below_middleBtn.setOnClickListener(this);
        below_rightBtn.setOnClickListener(this);
        below_leftBtn.setOnClickListener(this);
    }

    private String getString(int id) {
        return getResources().getString(id);
    }

    @Override
    public  void onClick(View v) {
        switch(v.getId()) {
            case R.id.button2 :
                getMiddleBtnMethod();
                break;

            case R.id.button1 :
                getLeftBtnMethod();
                break;

            case R.id.button4 :
                getMiddle_leftBtnMethod();
                break;

            case R.id.button5 :
                getMiddle_rightBtnMethod();
                break;


            case R.id.button3:
                getRightBtnMethod();
                break;
            case R.id.button6:
                getBelow_leftBtnMethod();
                break;
            case R.id.button7:
                getBelow_middleBtnMethod();
                break;
            case R.id.button8:
                getBelow_rightBtnMethod();
                break;
        }
    }





    protected abstract int getInfoResourceId();

    protected abstract void getMiddle_leftBtnMethod();
    protected abstract void getMiddle_rightBtnMethod();
    protected abstract void getMiddleBtnMethod();
    protected abstract void getLeftBtnMethod();
    protected abstract void getRightBtnMethod();
    protected abstract void getBelow_leftBtnMethod();
    protected abstract void getBelow_middleBtnMethod();
    protected abstract void getBelow_rightBtnMethod();
}
