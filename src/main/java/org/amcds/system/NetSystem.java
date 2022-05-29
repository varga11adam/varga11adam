package org.amcds.system;

import org.amcds.CommunicationProtocol.*;
import org.amcds.app.App;
import org.amcds.beb.BestEffortBroadcast;
import org.amcds.other.Abstraction;
import org.amcds.pl.PerfectLink;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

public class NetSystem implements Runnable {
    public List<ProcessId> processList;
    public BlockingQueue<Message> eventQueue;

    public boolean systemAlive;
    public String systemId;

    public ProcessId ownerProcId;
    public String hubHost;
    public int hubPort;

    public PerfectLink perfectLink;
    public BestEffortBroadcast bestEffortBroadcast;
    private boolean bebRegistered;
    public App app;

    public NetSystem(Message initMessage, String ownerHost, int ownerPort, String hubHost, int hubPort) {
        ProcInitializeSystem procinit = initMessage.getNetworkMessage().getMessage().getProcInitializeSystem();
        this.processList = procinit.getProcessesList();
        this.eventQueue = new LinkedBlockingQueue<Message>();
        this.systemAlive = true;
        this.systemId = initMessage.getSystemId();
        try {
            this.ownerProcId = getThisProcessId(ownerHost, ownerPort);
        }catch (Exception e){
            e.printStackTrace();
            this.systemAlive = false;
        }
        this.registerApp();
        this.registerPl();
        this.bebRegistered = false;
        this.hubHost = hubHost;
        this.hubPort = hubPort;
    }

    private void registerApp(){
        this.app = new App(this.eventQueue);
    }

    private void registerPl(){
        this.perfectLink = new PerfectLink(this.ownerProcId, this.eventQueue, this.processList, this.systemId, "app");
    }

    private void registerBeb(){
        this.bestEffortBroadcast = new BestEffortBroadcast(this.eventQueue, this.processList);
    }

    private ProcessId getThisProcessId(String host, int port) throws Exception {
        for (ProcessId processId : processList){
            if(host.equals(processId.getHost()) && port==processId.getPort()) {
                return processId;
            }
        }
        throw new Exception("Process not found!");
    }

    public void destroySystem(){
        this.systemAlive = false;
    }

    public Abstraction getAbstractionHandler(String abstractionId){
        switch (abstractionId){
            case "app":
                return app;
            case "app.pl":
                perfectLink.abstraction = "app";
                return perfectLink;
            case "app.beb":
                if(!bebRegistered){
                    registerBeb();
                    bebRegistered = true;
                }
                return bestEffortBroadcast;
            case "app.beb.pl":
                perfectLink.abstraction = "app.beb";
                return perfectLink;
            default:
                System.out.println("No handler for provided abstraction");
                return null;
        }
    }

    public void run(){
        Abstraction handler;
        Message message;
        while(systemAlive) {
            if(!eventQueue.isEmpty()){
                try {
                    message = eventQueue.take();

                    System.out.println(message.getType().toString() + ", to abstratction " + message.getToAbstractionId());

                    handler = getAbstractionHandler(message.getToAbstractionId());

                    if(handler != null)
                        handler.handleMessage(message);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
