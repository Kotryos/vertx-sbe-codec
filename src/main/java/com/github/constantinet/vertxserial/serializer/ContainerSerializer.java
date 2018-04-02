package com.github.constantinet.vertxserial.serializer;

import com.github.constantinet.vertxserial.data.Box;
import com.github.constantinet.vertxserial.data.Container;
import com.twitter.serial.serializer.ObjectSerializer;
import com.twitter.serial.serializer.SerializationContext;
import com.twitter.serial.serializer.Serializer;
import com.twitter.serial.stream.SerializerInput;
import com.twitter.serial.stream.SerializerOutput;

import java.io.IOException;
import java.util.List;

public class ContainerSerializer extends ObjectSerializer<Container> {

    private final Serializer<List<Box>> contentsSerializer;

    public ContainerSerializer(final Serializer<List<Box>> contentsSerializer) {
        this.contentsSerializer = contentsSerializer;
    }

    @Override
    protected void serializeObject(final SerializationContext context,
                                   final SerializerOutput output,
                                   final Container object) throws IOException {
        output
                .writeInt(object.getId())
                .writeObject(context, object.getContents(), contentsSerializer);
    }

    @Override
    protected Container deserializeObject(final SerializationContext context,
                                          final SerializerInput input,
                                          final int versionNumber) throws IOException, ClassNotFoundException {
        return new Container(input.readInt(), input.readObject(context, contentsSerializer));
    }
}