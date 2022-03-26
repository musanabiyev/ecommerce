package com.base.ecommerce.configure.kafka;

import com.base.ecommerce.dto.request.IncrementView;
import com.base.ecommerce.exception.KafkaErrorHandler;
import com.base.ecommerce.model.Product;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;
@Configuration
public class IncrementViewKafkaConfig {
    @Value(value = "${kafka.bootstrap.servers}")
    private String bootstrapServers;

    @Value(value = "${kafka.group.id}")
    private String groupId;

    @Bean
    public ProducerFactory<String, IncrementView> viewedProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);

    }

    @Bean
    public KafkaTemplate<String, IncrementView> viewKafkaTemplate() {
        return new KafkaTemplate<>(viewedProducerFactory());
    }

    @Bean
    public ConsumerFactory<String, IncrementView> viewConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JsonDeserializer<>(IncrementView.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, IncrementView>
    viewConcurrentKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, IncrementView> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(viewConsumerFactory());
        factory.setErrorHandler(new KafkaErrorHandler());
        return factory;
    }

}
