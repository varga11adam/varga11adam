package org.amcds.other;
import org.amcds.CommunicationProtocol;
import org.amcds.CommunicationProtocol.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import com.google.protobuf.GeneratedMessageV3;

public class Util {
    public static Message messageFromInput(InputStream input) throws IOException {
        byte[] lengthArr = new byte[4];
        input.read(lengthArr);
        int length = ByteBuffer.wrap(lengthArr).getInt();
        byte[] value = new byte[length];
        input.readNBytes(value,0,length);
        return CommunicationProtocol.Message.parseFrom(value);
    }

    public static void messageToOutput(Message message, DataOutputStream output) throws IOException {
        byte[] byteArray = message.toByteArray();
        output.writeInt(byteArray.length); // write length of the message
        output.write(byteArray);
    }

    public static String getAbstractionIdParent(String abstractionId){
        int lstindex = abstractionId.lastIndexOf('.');
        System.out.println(abstractionId);
        if(lstindex > 0)
            return abstractionId.substring(0, abstractionId.lastIndexOf('.'));
        return abstractionId;
    }

    public static Message wrapMessage(GeneratedMessageV3 message, String fromAbstraction, String toAbstraction, String systemId){
        Message.Builder builder = Message.newBuilder()
                .setFromAbstractionId(fromAbstraction)
                .setToAbstractionId(toAbstraction)
                .setSystemId(systemId);
        Boolean typeSet = false;
        if(message.getClass() == PlDeliver.class){
            builder.setPlDeliver((PlDeliver) message).setType(Message.Type.PL_DELIVER);
            typeSet = true;
        }
        if(message.getClass() == PlSend.class){
            builder.setPlSend((PlSend) message).setType(Message.Type.PL_SEND);
            typeSet = true;
        }
        if(message.getClass() == BebDeliver.class){
            builder.setBebDeliver((BebDeliver)message).setType(Message.Type.BEB_DELIVER);
            typeSet = true;
        }
        if(message.getClass() == AppValue.class){
            builder.setAppValue((AppValue) message).setType(Message.Type.APP_VALUE);
            typeSet = true;
        }
        if(message.getClass() == BebBroadcast.class){
            builder.setBebBroadcast((BebBroadcast) message).setType(Message.Type.BEB_BROADCAST);
            typeSet = true;
        }
        if(message.getClass() == NetworkMessage.class){
            builder.setNetworkMessage((NetworkMessage) message).setType(Message.Type.NETWORK_MESSAGE);
            typeSet = true;
        }
        if(!typeSet){
            return null;
        }
        return builder.build();
    }

}
