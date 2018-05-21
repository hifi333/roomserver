package com.tianfang.skill.eclass;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;

public class LBWSSession extends WebSocketClient {

    private LBServerMakeConnect2OtherLBServer lbServer;
    String targetip;



    private Thread sendTothisWsSessionThread;
    private ArrayBlockingQueue dataBulk = new ArrayBlockingQueue(10000*10);
    long  badtrytimes_add=0;
    long  firstBadtrytimelong_add =0;
    long  lastBadtrytimeLong_add=0;


    long  badtrytimes_takesend=0;
    long  firstBadtrytimelong_takesend =0;
    long  lastBadtrytimeLong_takesend=0;


    public boolean getHealth_addBulk(){


        if(badtrytimes_add ==0) return true;
        else  if(lastBadtrytimeLong_add - firstBadtrytimelong_add > 1000*60)  //已经尝试了60秒了.
            return false;
        else
            return true;

        //badtrytimes 已经丢了的信息条数.

    }


    public boolean getHealth_takesend(){


        if(badtrytimes_takesend ==0) return true;
        else  if(lastBadtrytimeLong_takesend - firstBadtrytimelong_takesend > 1000*60)  //已经尝试了60秒了.
            return false;
        else
            return true;

        //badtrytimes 已经丢了的信息条数.

    }


    public void addMoreMessage_produce(String newMsg){
        try {
            dataBulk.add(newMsg);
            badtrytimes_add=0;
        } catch (Exception e) {
            if(badtrytimes_add==0) firstBadtrytimelong_add = new Date().getTime();
            lastBadtrytimeLong_add = new Date().getTime();
            badtrytimes_add++;

            System.out.println(Thread.currentThread().getName() + " ===  "+e.getMessage() + " Queue full, new message droped for this LBSession.. "+targetip +
                    " and need remove this LBSession for it is bad, "+ " 已经丢了消息数:" + badtrytimes_add
                    + " 持续时间:(s)" + (lastBadtrytimeLong_add - firstBadtrytimelong_add)/1000);
        }

    }

//    synchronized  public ArrayList cutAllMessageto_consume(){
//
//        if(this.dataBulk.size()>0) {
//            ArrayList temp = this.dataBulk;
//
//            this.dataBulk = new ArrayList();
//
//            return temp;
//        }
//
//        return null;
//    }


    public LBWSSession(URI serverUri, LBServerMakeConnect2OtherLBServer lbServer) {
        super(serverUri);
        this.lbServer = lbServer;
        this.targetip = serverUri.getHost();



        sendTothisWsSessionThread = new Thread() {
            public void run() {
                while (true) {

                        try {
                            String oneNewMsg = (String) dataBulk.take();
                            send(oneNewMsg);
                            badtrytimes_takesend=0;
                        } catch (Exception e1) {
                            if(badtrytimes_takesend==0) firstBadtrytimelong_takesend = new Date().getTime();
                            lastBadtrytimeLong_takesend = new Date().getTime();
                            badtrytimes_takesend++;
                            System.out.println(Thread.currentThread().getName() + "LBWSSession转发到目标LB发生异常: 去除这个LB? 不能把, 该咋办还在想呢: 目标LB:"
                                    + targetip + e1.getMessage() + "已经丢了消息数:" + badtrytimes_takesend
                                    + " 持续时间:(s)" + (lastBadtrytimeLong_takesend - firstBadtrytimelong_takesend)/1000);
                        }


//                    //Cut the dataBulk current data to this thread to send out
//
//                    ArrayList toSendout = cutAllMessageto_consume();
//                    if (toSendout != null) {
//                        for (int i = 0; i < toSendout.size(); i++) {
//
//                            String message = (String) toSendout.get(i);
//
//                            try {
//                                send(message);
//
//                            } catch (Exception ee) {
//                                System.out.println("转发到目标LB发生异常: 去除这个LB? 不能把, 该咋办还在想呢: 目标LB:" + targetip);
//                            }
//                        }
//                    }
//
//                    try {
//                        Thread.sleep(100);  //10秒扫一遍
//                    } catch (Exception ee) {
//                        ee.printStackTrace();
//                    }
                }
            }
        };

        sendTothisWsSessionThread.start();


    }

    @Override
    public void onOpen(ServerHandshake arg0) {
        System.out.println("LBWSSession 打开链接, 保存这个wsclient  "  + this.targetip);
        this.lbServer.lbwsSessionList.add(this);
    }



    @Override
    public void onMessage(String arg0) {
    }

    @Override
    public void onError(Exception arg0) {
        System.out.println("LbWebSocketClient发生错误 " + this.targetip);
    }

    @Override
    public void onClose(int arg0, String arg1, boolean arg2) {
        System.out.println("LbWebSocketClient链接已关闭.去除wsclient,去除redis "  +  this.targetip+ arg0 +"  " +arg1);
        this.lbServer.redisService.removeSet(LBServer.rediskey_lbiplist, this.targetip);
        this.lbServer.lbwsSessionList.remove(this);

    }




}