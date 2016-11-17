package com.actimedia.udptest;

/**
 * Created by admin on 2016-10-23.
 */

public class Drone_Recv_Protocol {
    public double ConnectCheck;
    public   String name;
    public double pX,pY,pZ, pYaw;
    public Boolean ScreenShot;
    public void PacketToZero()
    {
        pX = 0; pY =0; pZ = 0; pYaw = 0;


    }
}
