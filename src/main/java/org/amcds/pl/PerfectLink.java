package org.amcds.pl;

import org.amcds.CommunicationProtocol.*;
import org.amcds.other.Abstraction;
import org.amcds.other.Util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class PerfectLink implements Abstraction {
    public BlockingQueue<Message> systemQueue;

    private ProcessId thisProcessId;
    public List<ProcessId> processList;

    public String systemId;

    public String abstraction;

    public ProcessId hostProcessId;

    public PerfectLink(ProcessId thisProcessId, BlockingQueue<Message> systemQueue, List<ProcessId> processList, String systemId, String abstraction) {
        this.systemQueue = systemQueue;
        this.thisProcessId = thisProcessId;
        this.processList = processList;
        this.systemId = systemId;
        this.abstraction = abstraction;
        this.hostProcessId = ProcessId.newBuilder().setHost("127.0.0.1").setPort(5000).setOwner("hub").setIndex(0).build();
    }

    public ProcessId getProcId(String host, int port){
        for(ProcessId processId : this.processList){
            if(processId.getHost().toString().equals(host) && processId.getPort() == port){
                return processId;
            }
        }
        return hostProcessId;
    }

    public void handleMessage(Message message) {
        System.out.println("\n\n In perfect link");
        if(message.getType() == Message.Type.NETWORK_MESSAGE){
            System.out.println("Handling network message \n\n");
            PlDeliver plDeliver = PlDeliver.newBuilder()
                    .setSender(this.getProcId(
                            message
                            .getNetworkMessage()
                            .getSenderHost(),
                            message
                                    .getNetworkMessage()
                                    .getSenderListeningPort()))
                    .setMessage(message
                            .getNetworkMessage()
                            .getMessage())
                    .build();
            Message wrapper = Util.wrapMessage(plDeliver,message.getFromAbstractionId(),abstraction,message.getSystemId());
//            Message wrapper = Message.newBuilder()
//                    .setSystemId(message.getSystemId())
//                    .setFromAbstractionId(message.getFromAbstractionId())
//                    .setToAbstractionId(abstraction)
//                    .setType(Message.Type.PL_DELIVER)
//                    .setPlDeliver(plDeliver).build();
            System.out.println("With message -- \n" + wrapper.toString());
            this.enqueueMessageToEventLoop(wrapper);
        }else {
            if(message.getType() == Message.Type.PL_SEND){
                try{
                    System.out.println("Sending message");
                    this.sendMessage(message);
                }catch (Exception e){
                    System.out.println(e.getMessage());
                }

            }
        }
        System.out.println("\n\n");
    }

    public void enqueueMessageToEventLoop(Message message){
        systemQueue.add(message);
    }

    public void sendMessage(Message message) throws IOException {
        Message wrapper = this.createNetworkMessage(message);

        String host;
        int port;

        if(message.getPlSend().hasDestination()){

            host = message.getPlSend().getDestination().getHost();
            port = message.getPlSend().getDestination().getPort();

            if(host.equals(this.thisProcessId.getHost()) && port == this.thisProcessId.getPort()){
                this.enqueueMessageToEventLoop(wrapper);
            }else{
                Socket connectionSocket = new Socket(host, port);
                DataOutputStream output = new DataOutputStream(connectionSocket.getOutputStream());
                System.out.println("-----------------Send message to "+host+":"+port+"--------------------\n\n\n"+wrapper.toString());
                Util.messageToOutput(wrapper, output);
            }
        }else{
            host = this.hostProcessId.getHost();
            port = this.hostProcessId.getPort();

            System.out.println(wrapper);

            Socket connectionSocket = new Socket(host, port);
            DataOutputStream output = new DataOutputStream(connectionSocket.getOutputStream());

            Util.messageToOutput(wrapper, output);
        }
    }

    public Message createNetworkMessage(Message content){
        NetworkMessage networkMessage = NetworkMessage
                .newBuilder()
                .setMessage(content.getPlSend().getMessage())
                .setSenderHost(thisProcessId.getHost())
                .setSenderListeningPort(thisProcessId.getPort())
                .build();

        Message wrapper = Message
                .newBuilder()
                .setType(Message.Type.NETWORK_MESSAGE)
                .setNetworkMessage(networkMessage)
                .setSystemId(this.systemId)
                .setToAbstractionId(content.getToAbstractionId())
                .build();
        return wrapper;
    }
}
