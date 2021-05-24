package ccait.ccweb.logging;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.status.ErrorStatus;
import entity.query.core.ApplicationConfig;
import entity.tool.util.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.Properties;

public class KafkaAppender<E> extends AppenderBase<E> {

    protected Layout<E> layout;
    private static final Logger LOGGER = LoggerFactory.getLogger("local");
    private boolean logToLocal = false;
    private String producerProperties;
    private String topic;
    private KafkaProducer producer;

    public void start() {
        super.start();
        int errors = 0;
        if (this.layout == null) {
            this.addStatus(new ErrorStatus("No layout set for the appender named \"" + this.name + "\".", this));
            ++errors;
        }

        if (errors == 0) {
            super.start();
        }

        LOGGER.info("Starting KafkaAppender...");
        final Properties properties = new Properties();
        try {
            properties.load(new StringReader(producerProperties));
            if(StringUtils.isNotEmpty(ApplicationConfig.getInstance().get("${logging.kafka.broker}", ""))) {
                properties.put("bootstrap.servers", ApplicationConfig.getInstance().get("${logging.kafka.broker}", properties.getProperty("bootstrap.servers")));
            }
            else  if(StringUtils.isNotEmpty(ApplicationConfig.getInstance().get("${spring.kafka.bootstrap-servers}", ""))) {
                properties.put("bootstrap.servers", ApplicationConfig.getInstance().get("${spring.kafka.bootstrap-servers}", properties.getProperty("bootstrap.servers")));
            }
            producer = new KafkaProducer<>(properties);
        } catch (Exception exception) {
            System.out.println("KafkaAppender: Exception initializing Producer. " + exception + " : " + exception.getMessage());
        }
        System.out.println("KafkaAppender: Producer initialized: " + producer);
        if (topic == null) {
            System.out.println("KafkaAppender requires a topic. Add this to the appender configuration.");
        } else {
            topic = topic.replace("kafka_env_IS_UNDEFINED", ApplicationConfig.getInstance().get("${logging.kafka.env}", ""));
            topic = topic.replace("spring_application_name_IS_UNDEFINED", ApplicationConfig.getInstance().get("${spring.application.name}", ""));
            System.out.println("KafkaAppender will publish messages to the '" + topic + "' topic.");
        }
        LOGGER.info("producerProperties = {}", producerProperties);
        LOGGER.info("Kafka Producer Properties = {}", properties);
        if (logToLocal) {
            LOGGER.info("KafkaAppender: producerProperties = '" + producerProperties + "'.");
            LOGGER.info("KafkaAppender: properties = '" + properties + "'.");
        }
    }

    @Override
    public void stop() {
        super.stop();
        LOGGER.info("Stopping KafkaAppender...");
        producer.close();
    }

    @Override
    protected void append(E event) {
        /**
         * 源码这里是用Formatter类转为JSON
         */
        String msg = layout.doLayout(event);
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, msg);
        producer.send(producerRecord);
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public boolean getLogToLocal() {
        return logToLocal;
    }

    public void setLogToLocal(String logToLocal) {
        if (Boolean.valueOf(logToLocal)) {
            this.logToLocal = true;
        }
    }

    public void setLayout(Layout<E> layout) {
        this.layout = layout;
    }

    public String getKafkaProducerProperties() {
        return producerProperties;
    }

    public void setKafkaProducerProperties(String kafkaProducerProperties) {
        this.producerProperties = kafkaProducerProperties;
    }
}