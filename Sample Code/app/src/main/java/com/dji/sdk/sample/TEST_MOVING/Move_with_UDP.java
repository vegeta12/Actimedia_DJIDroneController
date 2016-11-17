package com.dji.sdk.sample.TEST_MOVING;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;

import com.actimedia.udp_module.Recv_Drone_Thread;
import com.actimedia.udp_module.SendPaKcet_to_Mobile;
import com.actimedia.udptest.Drone_Recv_Protocol;
import com.actimedia.udptest.Drone_Send_Protocol;
import com.actimedia.udptest.PC_Send_Protocol;
import com.dji.sdk.sample.R;
import com.dji.sdk.sample.common.DJISampleApplication;
import com.dji.sdk.sample.common.Utils;
import com.dji.sdk.sample.common.test_View;
import com.dji.sdk.sample.utils.DJIModuleVerificationUtil;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import dji.common.error.DJIError;
import dji.common.flightcontroller.DJIFlightControllerCurrentState;
import dji.common.flightcontroller.DJIFlightControllerDataType;
import dji.common.flightcontroller.DJIVirtualStickFlightControlData;
import dji.common.flightcontroller.DJIVirtualStickFlightCoordinateSystem;
import dji.common.flightcontroller.DJIVirtualStickRollPitchControlMode;
import dji.common.flightcontroller.DJIVirtualStickVerticalControlMode;
import dji.common.util.DJICommonCallbacks.DJICompletionCallback;
import dji.sdk.flightcontroller.DJIFlightController;
import dji.sdk.flightcontroller.DJIFlightControllerDelegate;

/* Created by admin on 2016-09-29.
*/
public class Move_with_UDP extends test_View {

    private static final int CHANGE_DESCRIPTION_TEXTVIEW = 0;

    private DJIFlightController mFlightController;

    private String orientationMode;
    private float mPitch = 0;
    private float mRoll = 0;
    private float mYaw = 0;
    private float mThrottle = 0;
    private Boolean mPitchflag = false;
    private Boolean mRollflag = false;
    private Boolean mYawflag =false;
    private Boolean  mThrottleflag= false;
    private int port = 5000;
    private Timer mSendVirtualStickDataTimer;
    private boolean mVerticalControlFlag = true;
    private SendVirtualStickDataTask mSendVirtualStickDataTask;
    private String mobile_ip = "192.168.3.18";
    private String pc_ip = "220.149.217.78";
    private int port_send = 5500;

    private int port_recv = 6000;
    Drone_Send_Protocol drone_send_packet = new Drone_Send_Protocol();
    Drone_Recv_Protocol drone_recv_packet = new Drone_Recv_Protocol();

    SendPaKcet_to_Mobile mSendDroneThread;
    Recv_Drone_Thread mRecvDroneThread;
    UDPPacketlisten_Thread mUDPPacketlistenThread;
    private MoveType mMovetype;
    private Handler mHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case CHANGE_DESCRIPTION_TEXTVIEW:
                    //  Thread_text.setText("스레드 테스트 : 0 \n");
                    break;

                default:
                    break;
            }
            return false;
        }
    });

    public Move_with_UDP(Context context, AttributeSet attrs) {
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
            mFlightController.setVirtualStickAdvancedModeEnabled(true);
            mFlightController.setVerticalControlMode(DJIVirtualStickVerticalControlMode.Position);
            mFlightController.setRollPitchControlMode(DJIVirtualStickRollPitchControlMode.Velocity);
            DJISampleApplication.getAircraftInstance().getFlightController().
                    setHorizontalCoordinateSystem(
                            DJIVirtualStickFlightCoordinateSystem.Body  );

            // mFlightController.setRollPitchControlMode(     DJIVirtualStickRollPitchControlMode.Angle );

        }

        //컨트롤러에서 데이터 받아오는 쓰레드 생성

           mSendDroneThread = new SendPaKcet_to_Mobile();
           mSendDroneThread.GetInfo(pc_ip, port_send, drone_send_packet);
           new Thread(mSendDroneThread).start();



        mUDPPacketlistenThread = new UDPPacketlisten_Thread();
        mUDPPacketlistenThread.start();

        try {
            mRecvDroneThread = new Recv_Drone_Thread(port_recv, drone_recv_packet);
            mRecvDroneThread.start();
        } catch (IOException e) {
            //  text.setText("Server Thread를 시작하지 못했습니다." + e.toString());
        }

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

    }

    @Override               // 정지
    protected void getMiddle_rightBtnMethod() {

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
    protected void getMiddle_leftBtnMethod() {
     //   Controller_X_text.setText("UITest");
        float verticalJoyControlMaxSpeed = DJIFlightControllerDataType.DJIVirtualStickVerticalControlMaxVelocity;
        if(mThrottleflag== false)
        {
            mThrottle = (float) (100); //앞뒤 // 실제는 앞뒤
            mThrottleflag= true;
        }else{
            mThrottle = (float) (-100); //앞뒤 // 실제는 앞뒤
            mThrottleflag = false;
        }


        if (null == mSendVirtualStickDataTimer) {
            mSendVirtualStickDataTask = new SendVirtualStickDataTask();
            mSendVirtualStickDataTimer = new Timer();
            mSendVirtualStickDataTimer.schedule(mSendVirtualStickDataTask, 0, 200);
        }


    }

    // 드론이동 - 앞뒤
    protected void getBelow_leftBtnMethod() {
       // Controller_X_text.setText(""+mYaw);
      //  Controller_Y_text.setText(""+mThrottle);
       // Controller_Z_text.setText(""+mPitch);
        float verticalJoyControlMaxSpeed = DJIFlightControllerDataType.DJIVirtualStickVerticalControlMaxVelocity;
        if(mPitchflag== false)
        {
            mPitch = (float) (7); //앞뒤 // 실제는 앞뒤
            mPitchflag = true;
        }else{
            mPitch = (float) (0);
            mPitchflag = false;
        }

        if (null == mSendVirtualStickDataTimer) {
            mSendVirtualStickDataTask = new SendVirtualStickDataTask();
            mSendVirtualStickDataTimer = new Timer();
            mSendVirtualStickDataTimer.schedule(mSendVirtualStickDataTask, 0, 200);
        }
    }

    // 드론 - 회전(yaw)
    protected void getBelow_middleBtnMethod() {

        float verticalJoyControlMaxSpeed = DJIFlightControllerDataType.DJIVirtualStickVerticalControlMaxVelocity;

        if(mRollflag== false)
        {
            mRoll = (float) (7); //앞뒤 // 실제는 앞뒤
            mRollflag = true;
        }else{
            mRoll = (float) (0);
            mRollflag = false;
        }
        if (null == mSendVirtualStickDataTimer) {
            mSendVirtualStickDataTask = new SendVirtualStickDataTask();
            mSendVirtualStickDataTimer = new Timer();
            mSendVirtualStickDataTimer.schedule(mSendVirtualStickDataTask, 0, 200);
        }
    }

    // 드론이동 - 좌우
    protected void getBelow_rightBtnMethod() {

        float verticalJoyControlMaxSpeed = DJIFlightControllerDataType.DJIVirtualStickVerticalControlMaxVelocity;
        if(mYawflag== false)
        {
            mYaw = (float) (180); //앞뒤 // 실제는 앞뒤
            mYawflag = true;
        }else{
            mYaw = (float) (-180);
            mYawflag = false;
        }





        if (null == mSendVirtualStickDataTimer) {
            mSendVirtualStickDataTask = new SendVirtualStickDataTask();
            mSendVirtualStickDataTimer = new Timer();
            mSendVirtualStickDataTimer.schedule(mSendVirtualStickDataTask, 0, 200);
        }


    }

        class UDPPacketlisten_Thread extends Thread {
        float pitchJoyControlMaxSpeed = DJIFlightControllerDataType.DJIVirtualStickRollPitchControlMaxVelocity;
        float rollJoyControlMaxSpeed = DJIFlightControllerDataType.DJIVirtualStickRollPitchControlMaxVelocity;
        float verticalJoyControlMaxSpeed = DJIFlightControllerDataType.DJIVirtualStickVerticalControlMaxVelocity;
        float yawJoyControlMaxSpeed = DJIFlightControllerDataType.DJIVirtualStickYawControlMaxAngularVelocity;

        public void UDPPacketlistener() {

            //try {Utils.setResultToToast(getContext(), ""+drone_recv_packet.pX);} catch(Exception ex) {};

            mRoll = (float) (drone_recv_packet.pZ); //앞뒤
            mYaw = (float) (drone_recv_packet.pYaw); // 회전
            mPitch = (float) (drone_recv_packet.pX); // 좌우
            mThrottle = (float) (drone_recv_packet.pY); //위 아래
            if (Math.abs(drone_recv_packet.pZ) < 2) {
                drone_recv_packet.pZ = 0; // 임계영역처리해야함
            }
            if (Math.abs(drone_recv_packet.pY) < 2) {
                drone_recv_packet.pY = 0;
            }
            if (Math.abs(drone_recv_packet.pX) < 2) {
                drone_recv_packet.pX = 0;
            }
            if (Math.abs(drone_recv_packet.pYaw) < 2) {
                drone_recv_packet.pYaw = 0;
            }
            if (null == mSendVirtualStickDataTimer) {
                mSendVirtualStickDataTask = new SendVirtualStickDataTask();
                mSendVirtualStickDataTimer = new Timer();
                mSendVirtualStickDataTimer.schedule(mSendVirtualStickDataTask, 0, 50);
            }

        }

        public void UDPPacketSend()
        {
            if(DJIModuleVerificationUtil.isFlightControllerAvailable())
            {
                mFlightController = DJISampleApplication.getAircraftInstance().getFlightController();
                mFlightController.setUpdateSystemStateCallback(
                        new DJIFlightControllerDelegate.FlightControllerUpdateSystemStateCallback() {
                            @Override
                            public void onResult(DJIFlightControllerCurrentState djiFlightControllerCurrentState) {
                                drone_send_packet.altitude = djiFlightControllerCurrentState.getUltrasonicHeight();
                                drone_send_packet.longitude = (float) djiFlightControllerCurrentState.getAircraftLocation().getLongitude();
                                drone_send_packet.Latitude = (float) djiFlightControllerCurrentState.getAircraftLocation().getLatitude();
                                drone_send_packet.pitch = (float) djiFlightControllerCurrentState.getAttitude().pitch;
                                drone_send_packet.roll = (float) djiFlightControllerCurrentState.getAttitude().roll;
                                drone_send_packet.yaw = (float) djiFlightControllerCurrentState.getAttitude().yaw;
                                drone_send_packet.vX = djiFlightControllerCurrentState.getVelocityX();
                                drone_send_packet.vY = djiFlightControllerCurrentState.getVelocityY();
                                drone_send_packet.vZ = djiFlightControllerCurrentState.getVelocityZ();
                                mSendDroneThread = new SendPaKcet_to_Mobile();
                                mSendDroneThread.GetInfo(pc_ip, port_send, drone_send_packet);
                                new Thread(mSendDroneThread).start();
                            }});
            }
        }


            @Override
            public void run() {
                super.run();

                while (true) {
                    try { Thread.sleep(100);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (mRecvDroneThread.getReceiveChecker()) {
                                    UDPPacketlistener(); //UDP
                                    mRecvDroneThread.setReceiveChecker(false);
                                }
                                else
                                {
                                    drone_recv_packet.PacketToZero();

                                }
                                Controller_X_text.setText(""+mYaw);
                                Controller_Y_text.setText(""+mThrottle);
                                Controller_Z_text.setText(""+mPitch);

                                UDPPacketSend();
                                //Thread_text.setText("스레드테스트 : "+i++);


                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
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


}

