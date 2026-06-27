package com.github.constantinet.vertxserial.codec;

import com.github.constantinet.vertxserial.data.Box;
import com.github.constantinet.vertxserial.data.Container;
import com.github.constantinet.vertxserial.serializer.BoxSerializer;
import com.github.constantinet.vertxserial.serializer.ContainerSerializer;
import com.twitter.serial.serializer.CollectionSerializers;
import com.twitter.serial.serializer.Serializer;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

@ExtendWith(VertxExtension.class)
class SerialMessageCodecTest {

    private static SerialMessageCodec<Box> boxCodec() {
        return new SerialMessageCodec<>(new BoxSerializer());
    }

    private static SerialMessageCodec<Container> containerCodec() {
        final Serializer<List<Box>> contentsSerializer =
                CollectionSerializers.getListSerializer(new BoxSerializer());
        return new SerialMessageCodec<>(new ContainerSerializer(contentsSerializer));
    }

    @Test
    void boxRoundTripsOverTheWire() {
        final SerialMessageCodec<Box> codec = boxCodec();
        final Box original = new Box(42, "widget");

        final Buffer buffer = Buffer.buffer();
        codec.encodeToWire(buffer, original);
        final Box decoded = codec.decodeFromWire(0, buffer);

        assertEquals(original.getId(), decoded.getId());
        assertEquals(original.getDescription(), decoded.getDescription());
    }

    @Test
    void containerWithNestedBoxesRoundTripsOverTheWire() {
        final SerialMessageCodec<Container> codec = containerCodec();
        final Container original =
                new Container(1, List.of(new Box(100, "first"), new Box(200, "second")));

        final Buffer buffer = Buffer.buffer();
        codec.encodeToWire(buffer, original);
        final Container decoded = codec.decodeFromWire(0, buffer);

        assertEquals(original.getId(), decoded.getId());
        assertEquals(2, decoded.getContents().size());
        assertEquals(100, decoded.getContents().get(0).getId());
        assertEquals("first", decoded.getContents().get(0).getDescription());
        assertEquals("second", decoded.getContents().get(1).getDescription());
    }

    @Test
    void transformReturnsADistinctButEqualCopy() {
        final SerialMessageCodec<Box> codec = boxCodec();
        final Box original = new Box(7, "copy-me");

        final Box transformed = codec.transform(original);

        assertNotSame(original, transformed);
        assertEquals(original.getId(), transformed.getId());
        assertEquals(original.getDescription(), transformed.getDescription());
    }

    @Test
    void boxIsDeliveredOverTheLocalEventBus(final Vertx vertx, final VertxTestContext testContext) {
        vertx.eventBus().registerDefaultCodec(Box.class, boxCodec());
        vertx.eventBus().<Box>consumer("box.echo", message -> message.reply(message.body()));

        vertx.eventBus().<Box>request("box.echo", new Box(9, "ping"))
                .onComplete(testContext.succeeding(reply -> testContext.verify(() -> {
                    assertEquals(9, reply.body().getId());
                    assertEquals("ping", reply.body().getDescription());
                    testContext.completeNow();
                })));
    }
}
