package org.amcds;

import org.amcds.CommunicationProtocol.*;
import org.amcds.other.Util;
import org.amcds.system.NetSystem;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Process {
    public static void main(String argv[]) throws Exception
    {
        int PORT = 5004;
        int HUB_PORT = 5000;
        String HUB_ADDRESS = "127.0.0.1";
        String IP = "127.0.0.1";
        String OWNER = "adam";
        int INDEX = 1;

        ProcessId processId = ProcessId.newBuilder().setOwner(OWNER).setPort(PORT).setHost(IP).setIndex(INDEX).build();

        HashMap<String, NetSystem> registeredSystems = new HashMap<String, NetSystem>();

        ServerSocket socket = new ServerSocket(PORT);

        Socket connectionSocket = new Socket(HUB_ADDRESS,HUB_PORT);

        registerProcess(OWNER,INDEX,IP,PORT,connectionSocket);

        while(true)
        {
            try{
                connectionSocket = socket.accept();
            }catch (IOException e) {
                System.out.println("Could not listen on port:" + PORT);
                System.exit(-1);
            }

            CommunicationProtocol.Message message = Util.messageFromInput(connectionSocket.getInputStream());

            Message.Type messageType = message.getNetworkMessage().getMessage().getType();

            switch (messageType){
                case PROC_INITIALIZE_SYSTEM:
                    NetSystem ns = new NetSystem(message, IP, PORT, HUB_ADDRESS, HUB_PORT);

                    registeredSystems.put(ns.systemId, ns);

                    Thread system = new Thread(ns);
                    system.start();

                    System.out.println("System "+ns.systemId+" started");
                    break;
                case PROC_DESTROY_SYSTEM:
                    ns = registeredSystems.get(message.getSystemId());

                    ns.destroySystem();

                    System.out.println("System "+ns.systemId+" destroyed");
                    break;
                default:
                    ns = registeredSystems.get(message.getSystemId());

                    ns.eventQueue.add(message);
                    break;
            }
        }
    }

    public static void registerProcess(String owner, int index, String host, int listeningPort, Socket clientSocket) throws IOException {
        ProcRegistration registration = ProcRegistration
                .newBuilder()
                .setIndex(index)
                .setOwner(owner)
                .build();

        CommunicationProtocol.Message message = Util.wrapMessage(registration,"app","app","");

        NetworkMessage nm = NetworkMessage
                .newBuilder()
                .setMessage(message)
                .setSenderHost(host)
                .setSenderListeningPort(listeningPort)
                .build();

        CommunicationProtocol.Message wm = Util.wrapMessage(nm,"","","");

        System.out.println(wm);
        DataOutputStream dOut = new DataOutputStream(clientSocket.getOutputStream());

        Util.messageToOutput(wm,dOut);

        System.out.println("Sent registration for process " + owner + "-" + index + " on " + host + ":" +listeningPort);
    }


}
