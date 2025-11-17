package Food.Delivery.System.restaurant_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic menuUpdatesTopic() {
        return TopicBuilder.name("menu_updates")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
