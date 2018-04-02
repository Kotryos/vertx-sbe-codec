package com.github.constantinet.vertxserial.codec;

import com.github.constantinet.vertxserial.data.Copyable;
import com.twitter.serial.serializer.Serializer;
import com.twitter.serial.stream.Serial;
import com.twitter.serial.stream.bytebuffer.ByteBufferSerial;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

import java.io.IOException;
import java.util.Objects;

public class SerialMessageCodec<T extends Copyable<T>> implements MessageCodec<T, T> {

    private static final int CODEC_ID = -1;

    private final Serializer<T> serializer;
    private final Serial serial;

    public SerialMessageCodec(final Serializer<T> serializer) {
        Objects.requireNonNull(serializer);

        this.serializer = serializer;
        this.serial = new ByteBufferSerial();
    }

    @Override
    public void encodeToWire(final Buffer buffer, final T t) {
        try {
            final byte[] bytes = serial.toByteArray(t, serializer);
            buffer.appendInt(bytes.length);
            buffer.appendBytes(bytes);
        } catch (final IOException ex) {
            throw new MessageCodecException(ex);
        }
    }

    @Override
    public T decodeFromWire(final int pos, final Buffer buffer) {
        try {
            int _pos = pos;
            final byte[] bytes = buffer.getBytes(_pos += 4, _pos + buffer.getInt(pos));
            return serial.fromByteArray(bytes, serializer);
        } catch (final IOException | ClassNotFoundException ex) {
            throw new MessageCodecException(ex);
        }
    }

    @Override
    public T transform(T obj) {
        return obj.copy();
    }

    @Override
    public String name() {
        return this.getClass().getSimpleName() + "OnTopOf" + serializer.getClass().getSimpleName();
    }

    @Override
    public byte systemCodecID() {
        return CODEC_ID;
    }
}