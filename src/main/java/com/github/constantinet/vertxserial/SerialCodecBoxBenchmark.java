package com.github.constantinet.vertxserial;

import com.github.constantinet.vertxserial.codec.SerialMessageCodec;
import com.github.constantinet.vertxserial.data.Box;
import com.github.constantinet.vertxserial.serializer.BoxSerializer;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 4, time = 1)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class SerialCodecBoxBenchmark {

    @State(Scope.Benchmark)
    public static class BenchmarkState {

        private final MessageCodec<Box, Box> codec;

        public BenchmarkState() {
            this.codec = new SerialMessageCodec<>(new BoxSerializer());
        }
    }

    @State(Scope.Thread)
    public static class ThreadState {

        private final Box object;
        private final Buffer buffer;

        public ThreadState() {
            this.object = new Box(-1, "test");
            this.buffer = Buffer.buffer();
        }
    }

    @Benchmark
    public void runBenchmark(final BenchmarkState benchmarkState, final ThreadState threadState) {
        benchmarkState.codec.encodeToWire(threadState.buffer, threadState.object);
        benchmarkState.codec.decodeFromWire(0, threadState.buffer);
    }
}