package com.github.constantinet.vertxserial;

import com.github.constantinet.vertxserial.codec.SerialMessageCodec;
import com.github.constantinet.vertxserial.data.Box;
import com.github.constantinet.vertxserial.data.Container;
import com.github.constantinet.vertxserial.serializer.BoxSerializer;
import com.github.constantinet.vertxserial.serializer.ContainerSerializer;
import com.twitter.serial.serializer.CollectionSerializers;
import com.twitter.serial.serializer.Serializer;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import org.openjdk.jmh.annotations.*;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 4, time = 1)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class SerialCodecContainerBenchmark {

    @State(Scope.Benchmark)
    public static class BenchmarkState {

        private final MessageCodec<Container, Container> codec;

        public BenchmarkState() {
            final Serializer<List<Box>> contentsSerializer = CollectionSerializers.getListSerializer(new BoxSerializer());
            final Serializer<Container> containerSerializer = new ContainerSerializer(contentsSerializer);

            this.codec = new SerialMessageCodec<>(containerSerializer);
        }
    }

    @State(Scope.Thread)
    public static class ThreadState {

        private final Container object;
        private final Buffer buffer;

        public ThreadState() {
            this.object = new Container(1, Collections.singletonList(new Box(100, "test")));
            this.buffer = Buffer.buffer();
        }
    }

    @Benchmark
    public void runBenchmark(final BenchmarkState benchmarkState, final ThreadState threadState) {
        benchmarkState.codec.encodeToWire(threadState.buffer, threadState.object);
        benchmarkState.codec.decodeFromWire(0, threadState.buffer);
    }
}