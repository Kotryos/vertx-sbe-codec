package com.github.constantinet.vertxserial.serializer;

import com.github.constantinet.vertxserial.data.Box;
import com.twitter.serial.serializer.ObjectSerializer;
import com.twitter.serial.serializer.SerializationContext;
import com.twitter.serial.stream.SerializerInput;
import com.twitter.serial.stream.SerializerOutput;

import java.io.IOException;

public class BoxSerializer extends ObjectSerializer<Box> {

    @Override
    protected void serializeObject(final SerializationContext context,
                                   final SerializerOutput output,
                                   final Box box) throws IOException {
        output
                .writeInt(box.getId())
                .writeString(box.getDescription());
    }

    @Override
    protected Box deserializeObject(final SerializationContext context,
                                    final SerializerInput input,
                                    final int versionNumber) throws IOException {
        return new Box(input.readInt(), input.readString());
    }
}