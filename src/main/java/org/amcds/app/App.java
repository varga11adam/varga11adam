package org.amcds.app;

import org.amcds.CommunicationProtocol.*;
import org.amcds.other.Abstraction;

import java.util.concurrent.BlockingQueue;

public class App implements Abstraction {

    BlockingQueue<Message> systemQueue;

    public App(BlockingQueue<Message> systemQueue){
        this.systemQueue = systemQueue;
    }
    @Override
    public void handleMessage(Message message) {
        //System.out.println("---Message---");
        //System.out.println(message.getType());
        if(message.getType() == Message.Type.PL_DELIVER){
            Message content = message.getPlDeliver().getMessage();
            //System.out.println("---PL DELIVER---");
            switch (content.getType()){
                case APP_BROADCAST:
                    //System.out.println("---App broadcast---");
                    AppValue appValue = AppValue.newBuilder().setValue(content.getAppBroadcast().getValue()).build();
                    Message bebWrapper = Message.newBuilder().setType(Message.Type.APP_VALUE).setFromAbstractionId("app").setToAbstractionId("app").setAppValue(appValue).build();
                    BebBroadcast bebBroadcast = BebBroadcast.newBuilder().setMessage(bebWrapper).build();
                    Message wrapper = Message.newBuilder().setType(Message.Type.BEB_BROADCAST).setFromAbstractionId("app").setToAbstractionId("app.beb").setBebBroadcast(bebBroadcast).build();
                    this.enqueueMessageToEventLoop(wrapper);
                    break;
                case APP_VALUE:
                    //System.out.println("---App VALUE---");
                    AppValue appValue1 = message.getPlDeliver().getMessage().getAppValue();
                    Message appValWrapper = Message.newBuilder().setType(Message.Type.APP_VALUE).setAppValue(appValue1).build();
                    PlSend plSend = PlSend.newBuilder().setMessage(appValWrapper).build();
                    wrapper = Message.newBuilder().setType(Message.Type.PL_SEND).setFromAbstractionId("app").setToAbstractionId("app.pl").setPlSend(plSend).build();
                    this.enqueueMessageToEventLoop(wrapper);
                    break;
                default:
                    System.out.println("Not implemented yet");
                    break;
            }
        }
        if(message.getType() == Message.Type.BEB_DELIVER){
            //System.out.println("---BEB DELIVER---");
            AppValue appValue1 = message.getBebDeliver().getMessage().getAppValue();
            Message appValWrapper = Message.newBuilder().setType(Message.Type.APP_VALUE).setAppValue(appValue1).build();
            PlSend plSend = PlSend.newBuilder().setMessage(appValWrapper).build();
            Message wrapper = Message.newBuilder().setType(Message.Type.PL_SEND).setFromAbstractionId("app").setToAbstractionId("app.pl").setPlSend(plSend).build();
            this.enqueueMessageToEventLoop(wrapper);
        }
    }

    @Override
    public void enqueueMessageToEventLoop(Message message) {
        this.systemQueue.add(message);
    }
}
