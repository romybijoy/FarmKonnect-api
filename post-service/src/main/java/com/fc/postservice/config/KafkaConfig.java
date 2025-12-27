package com.fc.postservice.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka producer configuration for Post Service.
 * Defines:
 *  - ProducerFactory for creating Kafka producers
 *  - KafkaTemplate for sending Kafka messages
 * Configured for high reliability with recommended defaults such as:
 *  - acks=all
 *  - retries
 *  - batching and linger settings
 */
@Configuration
public class KafkaConfig {

    /** Kafka broker address (externalized via application.yml) */
    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    /**
     * Creates a ProducerFactory to construct Kafka producers with
     * string-based key/value serialization.
     *
     * @return configured ProducerFactory instance
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // Use Kafka's StringSerializer, not Jackson's
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // recommended additional settings (tweak to your needs)
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");         // strongest durability
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 5);        // batching small delay
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);  // 16KB

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Creates a KafkaTemplate for sending messages to Kafka topics.
     *
     * @return KafkaTemplate bean for String key/value messages
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
