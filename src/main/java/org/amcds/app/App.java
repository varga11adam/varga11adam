package org.amcds.app;

import org.amcds.CommunicationProtocol.*;
import org.amcds.other.Abstraction;
import org.amcds.other.Util;

import java.util.concurrent.BlockingQueue;

public class App implements Abstraction {

    BlockingQueue<Message> systemQueue;

    public App(BlockingQueue<Message> systemQueue){
        this.systemQueue = systemQueue;
    }
    @Override
    public void handleMessage(Message message) {
        if(message.getType() == Message.Type.PL_DELIVER){
            Message content = message.getPlDeliver().getMessage();
            System.out.println("\n\nHandling pl_deliver");
            switch (content.getType()){
                case APP_BROADCAST:
                    System.out.println("---App broadcast---");

                    AppValue appValue = AppValue.newBuilder()
                            .setValue(content.getAppBroadcast()
                                    .getValue())
                            .build();

                    Message bebWrapper = Util.wrapMessage(appValue,"app","app",message.getSystemId());

                    BebBroadcast bebBroadcast = BebBroadcast.newBuilder()
                            .setMessage(bebWrapper)
                            .build();

                    Message wrapper = Util.wrapMessage(bebBroadcast,"app","app.beb",message.getSystemId());

                    this.enqueueMessageToEventLoop(wrapper);
                    break;
                case APP_VALUE:
                    System.out.println("---App VALUE---");

                    AppValue appValue1 = message.getPlDeliver().getMessage().getAppValue();

                    Message appValWrapper = Util.wrapMessage(appValue1,"","",message.getSystemId());

                    PlSend plSend = PlSend.newBuilder()
                            .setMessage(appValWrapper)
                            .build();

                    wrapper = Util.wrapMessage(plSend,"app","app.pl",message.getSystemId());

                    this.enqueueMessageToEventLoop(wrapper);
                    break;
                default:
                    System.out.println("Not implemented yet");
                    break;
            }
        }
        if(message.getType() == Message.Type.BEB_DELIVER){
            System.out.println("Handling Beb deliver");

            AppValue appValue1 = message.getBebDeliver().getMessage().getAppValue();

            Message appValWrapper = Util.wrapMessage(appValue1,"","",message.getSystemId());

            PlSend plSend = PlSend.newBuilder()
                    .setMessage(appValWrapper)
                    .build();

            Message wrapper = Util.wrapMessage(plSend,"app","app.pl",message.getSystemId());

            this.enqueueMessageToEventLoop(wrapper);
        }
    }

    @Override
    public void enqueueMessageToEventLoop(Message message) {
        this.systemQueue.add(message);
    }
}
