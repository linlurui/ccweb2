/**
 *
 *  License: http://www.apache.org/licenses/LICENSE-2.0
 *  Home page: https://github.com/linlurui/ccweb
 *  Note: to build on java, include the jdk1.8+ compiler symbol (and yes,
 *  I know the difference between language and runtime versions; this is a compromise).
 * @author linlurui
 * @Date Date: 2019-02-10
 */

package ccait.ccweb.config;

import com.google.common.collect.Lists;
import entity.query.core.ApplicationConfig;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;
import reactor.netty.Connection;
import reactor.netty.DisposableServer;
import reactor.netty.NettyInbound;
import reactor.netty.tcp.TcpServer;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

@Component
public class MqttServer {

    private static final int MAX_BYTES = 6*1024*1024;

    private Disposable disposable;

    @Value("${mqtt.server.username:}")
    private String username = "";

    @Value("${mqtt.server.password:}")
    private String password = "";

    @Value("${mqtt.server.host:127.0.0.1}")
    private String host = "127.0.0.1";

    @Value("${mqtt.server.port:1999}")
    private int port = 1999;

    @Value("${mqtt.server.log:true}")
    private Boolean enableLog = true;

    @Value("${mqtt.server.ssl:false}")
    private Boolean enableSsl = false;

    @Value("${mqtt.server.heat:60}")
    private int heart = 60;

    private TcpServer server;

    private static final Logger log = LoggerFactory.getLogger(MQTTServerConfig.class);

    UnicastProcessor<Connection> unicastProcessor =UnicastProcessor.create();

    public MqttServer() {
        username = ApplicationConfig.getInstance().get("${mqtt.server.username}", username);
        password = ApplicationConfig.getInstance().get("${mqtt.server.password}", password);
        host = ApplicationConfig.getInstance().get("${mqtt.server.host}", host);
        port = ApplicationConfig.getInstance().get("${mqtt.server.port}", port);
        enableLog = ApplicationConfig.getInstance().get("${mqtt.server.log}", enableLog);
        enableSsl = ApplicationConfig.getInstance().get("${mqtt.server.ssl}", enableSsl);
        heart = ApplicationConfig.getInstance().get("${mqtt.server.heart}", heart);
    }

    public void run() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);


        server = TcpServer.create()
                .port(port)
                .wiretap(enableLog)
                .host(host);

        if(enableSsl) {
            server.secure(ssl -> ssl.sslContext(Objects.requireNonNull(buildContext())));
        }

        server.doOnConnection(connection -> {
            getHandlers().forEach(connection::addHandlerLast);
            unicastProcessor.onNext(connection);
        }).bind().doOnError(e -> {
            log.error("MQTT Error: " + e.getMessage(), e);
        }).map(this::handle)
                .doOnError(e -> {
                    log.error("MQTT Handle Message Error: " + e.getMessage(), e);
                })
                .block();

        System.out.println(String.format("Listening %s:%s from ccait.ccweb of MqttServer!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!", host, port));

        latch.await();
    }

    private Disposable handle(DisposableServer disposableServer) {

        disposable = unicastProcessor.subscribe(this::subscribe);

        return disposable;
    }

    private void subscribe(Connection conn) {
        NettyInbound inbound = conn.inbound();
        Disposable disposable = Mono.fromRunnable(conn::dispose)// 定时关闭
                .delaySubscription(Duration.ofSeconds(10))
                .subscribe();
        conn.channel().attr(AttributeKey.valueOf("transport_connection")).set(conn); // 设置connection
        conn.channel().attr(AttributeKey.valueOf("close_connection")).set(disposable);   // 设置close
        conn.onReadIdle(heart, () -> conn.dispose()); // 心跳超时关闭
        conn.onDispose(() -> { // 关闭时处理
            log.info("connection dispose...");
        });
        inbound.receiveObject().cast(MqttMessage.class)
                .subscribe(message -> {
                    if(message.decoderResult().isSuccess()){
                        log.info("channel {} info{}", conn, message);

                    }
                    else {
                        log.error("accept message error====>>>");
                        log.error(message.decoderResult().toString());
                    }
                });
    }

    private List<ChannelHandler> getHandlers() {
        return Lists.newArrayList( new MqttDecoder(MAX_BYTES), MqttEncoder.INSTANCE);
    }

    private SslContext buildContext() {
        try {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            return SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } catch (Exception e) {
            log.error("ssl error: " + e.getMessage());
        }
        return null;
    }
}
