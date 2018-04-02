package com.github.constantinet.vertxserial;

import com.github.constantinet.vertxserial.data.Box;
import com.github.constantinet.vertxserial.data.Container;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.eventbus.impl.codecs.JsonObjectMessageCodec;
import io.vertx.core.json.JsonObject;
import org.openjdk.jmh.annotations.*;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 4, time = 1)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class JsonObjectCodecContainerBenchmark {

    @State(Scope.Benchmark)
    public static class BenchmarkState {

        private final MessageCodec<JsonObject, JsonObject> codec;

        public BenchmarkState() {
            this.codec = new JsonObjectMessageCodec();
        }
    }

    @State(Scope.Thread)
    public static class ThreadState {

        private final JsonObject object;
        private final Buffer buffer;

        public ThreadState() {
            this.object = JsonObject.mapFrom(
                    new Container(1, Collections.singletonList(new Box(100, "test"))));
            this.buffer = Buffer.buffer();
        }
    }

    @Benchmark
    public void runBenchmark(final BenchmarkState benchmarkState, final ThreadState threadState) {
        benchmarkState.codec.encodeToWire(threadState.buffer, threadState.object);
        benchmarkState.codec.decodeFromWire(0, threadState.buffer);
    }
}