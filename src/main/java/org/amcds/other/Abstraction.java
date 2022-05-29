package org.amcds.other;

import org.amcds.CommunicationProtocol.Message;
public interface Abstraction {
    /*
    *   Abstraction message handler method
    */
    public void handleMessage(Message message);

    public void enqueueMessageToEventLoop(Message message);
}
