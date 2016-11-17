package com.actimedia.udptest;

/**
 * Created by admin on 2016-10-23.
 */

public class Drone_Send_Protocol {
    public float ConnectCheck = 0.0f;
    public   String name = "데이터 없음";
    public float altitude = 0, longitude = 0, Latitude = 0; //고도, 위도 , 경도
    public float pitch =0, roll=0, yaw=0; // 기울기
    public float vX=0,vY=0,vZ=0; // 속도

    public void PacketToZero()
    {

        float altitude = 0, longitude = 0, Latitude = 0; //고도, 위도 , 경도
        float pitch =0, roll=0, yaw=0; // 기울기
        float vX=0,vY=0,vZ=0; // 속도
    }
}
