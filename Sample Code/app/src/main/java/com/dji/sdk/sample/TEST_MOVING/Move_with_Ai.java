package com.dji.sdk.sample.TEST_MOVING;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;

import com.dji.sdk.sample.R;
import com.dji.sdk.sample.common.DJISampleApplication;
import com.dji.sdk.sample.common.Utils;
import com.dji.sdk.sample.common.test_View;
import com.dji.sdk.sample.utils.DJIModuleVerificationUtil;

import java.util.Timer;
import java.util.TimerTask;

import dji.common.error.DJIError;
import dji.common.flightcontroller.DJIFlightControllerCurrentState;
import dji.common.flightcontroller.DJIVirtualStickFlightControlData;
import dji.common.flightcontroller.DJIVirtualStickVerticalControlMode;
import dji.common.util.DJICommonCallbacks.DJICompletionCallback;
import dji.sdk.flightcontroller.DJIFlightController;
import dji.sdk.flightcontroller.DJIFlightControllerDelegate;

/* Created by admin on 2016-09-29.
*/
enum MoveType{
    move_x(0),
    move_y(1),
    move_z(2),
    move_yaw(3),
    move_stop(4);
    private int MoveType_Num;
    MoveType(int i){ MoveType_Num=i; }

    public int getType() {  return MoveType_Num;}

}

public class Move_with_Ai extends test_View {

    private static final int CHANGE_DESCRIPTION_TEXTVIEW = 0;

    private DJIFlightController mFlightController;

    private String orientationMode;
    private float mPitch = 0;
    private float mRoll = 0;
    private float mYaw = 0;
    private float mThrottle =0 ;
    private int port = 5000;
//    private SendVirtualStickDataThread mSend_virtual_data_Thread  = null;
    private Timer mSendVirtualStickDataTimer;
    private boolean mVerticalControlFlag = true;
    private SendVirtualStickDataTask mSendVirtualStickDataTask;

    private MoveType mMovetype;
    private Handler mHandler = new Handler(new Handler.Callback(){

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case CHANGE_DESCRIPTION_TEXTVIEW :
                    //  Thread_text.setText("스레드 테스트 : 0 \n");
                    break;

                default:
                    break;
            }
            return false;
        }
    });

    public Move_with_Ai(Context context, AttributeSet attrs) {
            super(context, attrs);


            middleBtn.setText("-착륙-");
            leftBtn.setText("이륙");
            rightBtn.setText("Vitual실행");
            middle_leftBtn.setText("드론상하");
            middle_rightBtn.setText("정지");
            below_leftBtn.setText("드론앞뒤");
            below_middleBtn.setText("회전");
            below_rightBtn.setText("드론좌우");

        mMovetype = MoveType.move_stop;


    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (DJIModuleVerificationUtil.isFlightControllerAvailable()) {
            mFlightController = DJISampleApplication.getAircraftInstance().getFlightController();

            mFlightController.setUpdateSystemStateCallback(
                    new DJIFlightControllerDelegate.FlightControllerUpdateSystemStateCallback() {
                        @Override
                        public void onResult(DJIFlightControllerCurrentState
                                                     djiFlightControllerCurrentState) {
                            mHandler.sendEmptyMessage(CHANGE_DESCRIPTION_TEXTVIEW);
                        }
                    });
        }
    }


     @Override
     protected int getInfoResourceId() {
         return R.string.orientation_mode_description;
     }


    @Override       //이륙
    protected void getLeftBtnMethod() {
        if (DJIModuleVerificationUtil.isFlightControllerAvailable()) {

            DJISampleApplication.getAircraftInstance().getFlightController().takeOff(
                    new DJICompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            Utils.showDialogBasedOnError(getContext(), djiError);
                        }
                    }
            );
        }
    }

    @Override                    //착륙
    protected void getMiddleBtnMethod() {

        if (DJIModuleVerificationUtil.isFlightControllerAvailable()) {
            mFlightController = DJISampleApplication.getAircraftInstance().getFlightController();
            mFlightController.getLandingGear().turnOnAutoLandingGear(new DJICompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {

                }
            });
            mFlightController.autoLanding(
                    new DJICompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            Utils.setResultToToast(
                                    getContext(),
                                    "Result: " + (djiError == null ?
                                            "Success" : djiError.getDescription()));
                        }
                    }
            );
        }

    }

    @Override             // Virtual Mode 실행
    protected void getRightBtnMethod() {

                // 모드 설정
            if (DJIModuleVerificationUtil.isFlightControllerAvailable()) {
                mFlightController = DJISampleApplication.getAircraftInstance().getFlightController();
                mFlightController.setVerticalControlMode(     DJIVirtualStickVerticalControlMode.Position);
                // mFlightController.setRollPitchControlMode(     DJIVirtualStickRollPitchControlMode.Angle );

        }
        // 드론 조종 데이터 드론 전송 쓰레드 생성
       // if(mSend_virtual_data_Thread == null) {
          //  mSend_virtual_data_Thread = new SendVirtualStickDataThread(true);
        //    mSend_virtual_data_Thread.start();
     //   }


        //버츄얼 스틱 모드 켜기
            DJISampleApplication.getAircraftInstance().
                    getFlightController().enableVirtualStickControlMode(
                    new DJICompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            Utils.showDialogBasedOnError(getContext(), djiError);
                        }
                    }
            );

     //   mSend_virtual_data_Thread.startThread(10);

    }


    @Override               // 정지
    protected void getMiddle_rightBtnMethod()
    {

    //    if(     mSend_virtual_data_Thread != null)
    //        mSend_virtual_data_Thread.stopThread();


        //버추얼 스틱 모드 종료(정지)
        DJISampleApplication.getAircraftInstance().
                getFlightController().disableVirtualStickControlMode(
                new DJICompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        Utils.showDialogBasedOnError(getContext(), djiError);
                    }
                }
        );

    }

    @Override         // 드론이동 - 상하
    protected void getMiddle_leftBtnMethod(){


        String ret;
        mMovetype = MoveType.move_y;
     //   ret = mSend_virtual_data_Thread.startThread(mMovetype.getType());

            try {
                Utils.setResultToToast(getContext(), DJISampleApplication.
                        getAircraftInstance().getFlightController().
                        getVerticalControlMode().name());
            } catch(Exception ex) {};
        try {
//            Utils.setResultToToast(getContext(), ret);
        } catch(Exception ex) {};


    }
     // 드론이동 - 앞뒤
     protected  void getBelow_leftBtnMethod()
     {


         String ret;
         mMovetype = MoveType.move_z;
      //   ret = mSend_virtual_data_Thread.startThread(mMovetype.getType());

             try {
                 Utils.setResultToToast(getContext(), DJISampleApplication.
                         getAircraftInstance().getFlightController().
                         getVerticalControlMode().name());
             } catch(Exception ex) {};

         try {
     //        Utils.setResultToToast(getContext(), ret);
         } catch(Exception ex) {};

     }

     // 드론 - 회전(yaw)
     protected  void getBelow_middleBtnMethod()
     {


         String ret;
         mMovetype = MoveType.move_yaw;

       // ret = mSend_virtual_data_Thread.startThread(mMovetype.getType());
             try {
            //     Utils.setResultToToast(getContext(), ret);
             } catch(Exception ex) {};

     }

     // 드론이동 - 좌우
     protected  void getBelow_rightBtnMethod()
     {

        String ret;
         int a;
         mMovetype = MoveType.move_x;

       //  ret =  mSend_virtual_data_Thread.startThread(mMovetype.getType());
         try {
    //         Utils.setResultToToast(getContext(), ret);
         } catch(Exception ex) {};


     }

    public void onTouch() {

    }
    class SendVirtualStickDataTask extends TimerTask {

        @Override
        public void run() {
            if (DJIModuleVerificationUtil.isFlightControllerAvailable()) {
                DJISampleApplication.getAircraftInstance().
                        getFlightController().sendVirtualStickFlightControlData(
                        new DJIVirtualStickFlightControlData(
                                mPitch, mRoll, mYaw, mThrottle
                        ), new DJICompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                            }
                        }
                );
            }
        }
    }
/*
    class SendVirtualStickDataThread extends Thread{
        private boolean isXPlay = false;
        private boolean isYPlay = false;
        private boolean isZPlay = false;
        private boolean isYawPlay = false;

        private int i = 0;
        private int m_velocity = 1;
        float verticalMaxSpeed = 0;
        float yawMaxSpeed = 0;
        float pitchJoyControlMaxSpeed = 0;
        float rollJoyControlMaxSpeed = 0;
        public SendVirtualStickDataThread(boolean isXPlay){

            this.isXPlay = false;
            this.isYPlay = false;
            this.isZPlay = false;
            this.isYawPlay = false;
            try {
                Utils.setResultToToast(getContext(), "스레드생성");
            } catch(Exception ex) {};
            verticalMaxSpeed = DJIFlightControllerDataType.DJIVirtualStickVerticalControlMaxVelocity;
            yawMaxSpeed = DJIFlightControllerDataType.DJIVirtualStickYawControlMaxAngularVelocity;
            pitchJoyControlMaxSpeed = DJIFlightControllerDataType.DJIVirtualStickRollPitchControlMaxVelocity;
            rollJoyControlMaxSpeed = DJIFlightControllerDataType.DJIVirtualStickRollPitchControlMaxVelocity;
        }

        @Override
        public void finalize(){
            try {
                Utils.setResultToToast(getContext(), "스레드파괴");
            } catch(Exception ex) {};

        }

        public void stopThread(){
            this.isXPlay = false;
            this.isYPlay = false;
            this.isZPlay = false;
            this.isYawPlay = false;
            mYaw = 0.0f;
            mPitch = 0.0f;

        }
        */
    /*
        public String startThread(int num)
        {

            String str;

            stopThread(); // 모든 방향 이동 정지 후 다시 이동시키는거 활성화

            switch(num)
            {
                case 0 :  this.isXPlay = true; str = "X호출 후 종료"; break;    //x

                case 1: this.isYPlay = true; str=  "Y호출 후 종료";   break;  //y

                case 2:  this.isZPlay = true; str=  "Z호출 후 종료"; break;    //z

                case 3:  this.isYawPlay = true;str=  "Yaw호출 후 종료"; break;   //yaw



                default : str = "디폴트호출 후 종료" ;break;

            }
            if(m_velocity == 1)
                m_velocity = -1;
            else
                m_velocity = 1;

            return str;
        }





        @Override
        public void run() {
            super.run();

            while (true) {
                try { Thread.sleep(100);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {

                            //     mYaw = (float)(verticalMaxSpeed * 0.07);
                                if (isYPlay) {
                                    mThrottle = (float) (yawMaxSpeed * 0.05 * m_velocity);
                                    //throttle 상,하 (단위는 위치)
                                    Thread_text.setText((i++) + "- mthrottle : " + mThrottle);
                                }
                                if (isXPlay) {
                                    mYaw = (float) (yawMaxSpeed * 0.03 * m_velocity);
                                    //yaw가 좌우 (단위는 속도)
                                    Thread_text.setText((i++) + "- mYaw : " + mYaw);
                                } else
                                {
                                    mYaw =(float) 0.0;
                               //     Thread_text.setText((i++) + "- mYaw : " + mYaw);
                                }

                                if (isZPlay) {
                                    mPitch = (float) (pitchJoyControlMaxSpeed * 0.05 * m_velocity);
                                    //pitch가 앞뒤다 ( 단위는 속도)
                                    Thread_text.setText((i++) + "- mPitch : " + mPitch);
                                } else
                                {

                                    mPitch = (float)0.0;
                                 //   Thread_text.setText((i++) + "- mPitch : " + mPitch);

                                }

                                if (isYawPlay) {

                                    mRoll = (float) (rollJoyControlMaxSpeed * 0.5 * m_velocity);
                                    // roll이 회전(단위는 각도인듯)
                                    Thread_text.setText((i++) + "- mRoll : " + mRoll);
                                }

                                if (null == mSendVirtualStickDataTimer) {
                                    mSendVirtualStickDataTask = new SendVirtualStickDataTask();
                                    mSendVirtualStickDataTimer = new Timer();
                                    mSendVirtualStickDataTimer.schedule(mSendVirtualStickDataTask, 0, 200);
                                }

                                //Thread_text.setText("스레드테스트 : "+i++);


                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }




        }
    }
    */
}
