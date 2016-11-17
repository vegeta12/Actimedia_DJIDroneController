package com.actimedia.udp_module;

/**
 * Created by admin on 2016-10-24.
 */

import com.actimedia.udptest.Drone_Send_Protocol;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class SendPaKcet_to_Mobile implements Runnable {
    private int port_send;
    private String ip;
    private JSONObject j_obj;
    public Drone_Send_Protocol mMobile_send_packet;

    public void GetInfo(String ip, int port_send, Drone_Send_Protocol drone_send_packet) {
        mMobile_send_packet = drone_send_packet;
        this.ip = ip;
        this.port_send = port_send;
        j_obj = new JSONObject();
    }

    public void Drone_send_toJo(JSONObject jo, Drone_Send_Protocol packet) {
        jo.put("ConnectCheck", packet.ConnectCheck);
        jo.put("name", packet.name);
        jo.put("vX", packet.vX);
        jo.put("vY", packet.vY);
        jo.put("vZ", packet.vZ);
        jo.put("yaw", packet.yaw);
        jo.put("roll", packet.roll);
        jo.put("pitch", packet.pitch);
        jo.put("longitude", packet.longitude);
        jo.put("altitude", packet.altitude);
        jo.put("Latitude", packet.Latitude);
    }

    @Override
    public void run() {
            try {
                SocketAddress socketAddress = new InetSocketAddress(ip, port_send);
                DatagramSocket ds = new DatagramSocket();


                Drone_send_toJo(j_obj, mMobile_send_packet);
                String jsonText = JSONValue.toJSONString(j_obj);
                byte[] sendbuffer = jsonText.getBytes("UTF-8");

                // byte[] sendbuffer = SerializationUtils.serialize(DRONE_Packet);
                DatagramPacket dp = new DatagramPacket(sendbuffer, sendbuffer.length, socketAddress);
                ds.send(dp);
                j_obj.clear();


                Runnable showUpdate = new Runnable() {
                    @Override
                    public void run() {
                        //      text.setText(s2);
                        //						Toast.makeText(getApplicationContext(), s2, Toast.LENGTH_SHORT);
                    }
                };

                //         text.post(showUpdate);
                ds.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
    }
}