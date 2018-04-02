package com.github.constantinet.vertxserial.codec;

import com.github.constantinet.vertxserial.data.Box;
import com.github.constantinet.vertxserial.data.Container;
import com.github.constantinet.vertxserial.serializer.BoxSerializer;
import com.github.constantinet.vertxserial.serializer.ContainerSerializer;
import com.twitter.serial.serializer.CollectionSerializers;
import io.vertx.core.VertxOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.eventbus.Message;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import rx.Single;

import java.util.Collections;

@RunWith(VertxUnitRunner.class)
public class SerialMessageCodecIntegrationTest {

    private static final String TEST_ADDRESS = "test.address";

    private Vertx senderVertx;
    private Vertx receiverVertx;
    private String verticleDeploymentId;

    @Before
    public void setUpTest(final TestContext testContext) {
        final Async async = testContext.async();

        // creating a vertx for sending and another vertx for receiving data to test transferring over the wire
        Single.zip(getVertx(), getVertx(),
                (senderVertx, receiverVertx) -> {
                    registerCodecs(senderVertx);
                    registerCodecs(receiverVertx);

                    this.senderVertx = senderVertx;
                    this.receiverVertx = receiverVertx;

                    return receiverVertx;
                })
                .flatMapCompletable(receiverVertx -> receiverVertx
                        .rxDeployVerticle(TestVerticle.class.getName())
                        .doOnSuccess(deploymentId -> this.verticleDeploymentId = deploymentId)
                        .toCompletable())
                .subscribe(async::complete, testContext::fail);
    }

    @After
    public void tearDown(final TestContext testContext) {
        final Async async = testContext.async();

        receiverVertx.rxUndeploy(verticleDeploymentId)
                .toCompletable()
                .subscribe(async::complete, testContext::fail);
    }

    @Test
    public void shouldSendAndReceiveObjectCorrectly_whenComplexDataSent(final TestContext testContext) {
        // given
        final Async async = testContext.async();
        final Container givenContainer = new Container(1, Collections.singletonList(new Box(100, "test")));

        // when
        senderVertx.eventBus().<Container>rxSend(TEST_ADDRESS, givenContainer)
                .map(Message::body)
                // then
                .doOnSuccess(container -> testContext.assertEquals(container.getId(), 1))
                .doOnSuccess(container -> testContext.assertEquals(container.getContents().size(), 1))
                .doOnSuccess(container -> testContext.assertEquals(container.getContents().get(0).getId(), 100))
                .doOnSuccess(container -> testContext.assertEquals(container.getContents().get(0).getDescription(), "test"))
                .toCompletable()
                .subscribe(async::complete, testContext::fail);
    }

    @Test
    public void shouldSendAndReceiveObjectCorrectly_whenSimpleDataSent(final TestContext testContext) {
        // given
        final Async async = testContext.async();
        final Box givenBox = new Box(1, "test");

        // when
        senderVertx.eventBus().<Box>rxSend(TEST_ADDRESS, givenBox)
                .map(Message::body)
                // then
                .doOnSuccess(box -> testContext.assertEquals(box.getId(), 1))
                .doOnSuccess(box -> testContext.assertEquals(box.getDescription(), "test"))
                .toCompletable()
                .subscribe(async::complete, testContext::fail);
    }

    private Single<Vertx> getVertx() {
        return Vertx.rxClusteredVertx(new VertxOptions());
    }

    private void registerCodecs(final Vertx vertx) {
        final BoxSerializer boxSerializer = new BoxSerializer();
        final ContainerSerializer containerSerializer = new ContainerSerializer(
                CollectionSerializers.getListSerializer(boxSerializer));

        vertx.eventBus().getDelegate()
                .registerDefaultCodec(Box.class, new SerialMessageCodec<>(boxSerializer));
        vertx.eventBus().getDelegate()
                .registerDefaultCodec(Container.class, new SerialMessageCodec<>(containerSerializer));
    }

    public static class TestVerticle extends AbstractVerticle {
        @Override
        public void start() throws Exception {
            vertx.eventBus().consumer(TEST_ADDRESS, msg -> msg.reply(msg.body()));
        }
    }
}