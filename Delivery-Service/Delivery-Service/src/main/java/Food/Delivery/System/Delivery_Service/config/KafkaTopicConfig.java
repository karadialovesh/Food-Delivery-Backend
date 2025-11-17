package Food.Delivery.System.Delivery_Service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    @Bean
    public NewTopic deliveryAssignedTopic() { return TopicBuilder.name("delivery_assigned").partitions(3).replicas((short)1).build(); }

}
