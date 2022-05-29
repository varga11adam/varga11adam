package org.amcds.beb;

import org.amcds.CommunicationProtocol.*;
import org.amcds.other.Abstraction;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class BestEffortBroadcast implements Abstraction {
    public BlockingQueue<Message> systemQueue;
    public List<ProcessId> processList;
    public BestEffortBroadcast(BlockingQueue<Message> systemQueue, List<ProcessId> processList){
        this.processList = processList;
        this.systemQueue = systemQueue;
    }

    @Override
    public void handleMessage(Message message) {
        System.out.println("\n\nIn Best Effort Broadcast");
        if(message.getType() == Message.Type.BEB_BROADCAST){
            System.out.println("Handling Beb Broadcast\n\n");
            for(ProcessId processId: processList){
                PlSend plSend = PlSend.newBuilder().setDestination(processId).setMessage(message.getBebBroadcast().getMessage()).build();
                Message wrapper = Message.newBuilder().setType(Message.Type.PL_SEND).setFromAbstractionId("app.beb").setToAbstractionId("app.beb.pl").setPlSend(plSend).build();
                System.out.println("\nWith beb repeated broadcast message\n" + wrapper.toString());
                this.enqueueMessageToEventLoop(wrapper);
            }
        }
        if(message.getType() == Message.Type.PL_DELIVER){
            System.out.println("Handling Pl deliver\n\n");
            BebDeliver bebDeliver = BebDeliver.newBuilder().setSender(message.getPlDeliver().getSender()).setMessage(message.getPlDeliver().getMessage()).build();
            Message wrapper = Message.newBuilder().setType(Message.Type.BEB_DELIVER).setBebDeliver(bebDeliver).setFromAbstractionId("app.beb").setToAbstractionId(message.getPlDeliver().getMessage().getToAbstractionId()).build();
            System.out.println("\nWith beb deliver message\n" + wrapper.toString());
            this.enqueueMessageToEventLoop(wrapper);
        }
    }

    @Override
    public void enqueueMessageToEventLoop(Message message) {
        this.systemQueue.add(message);
    }
}
