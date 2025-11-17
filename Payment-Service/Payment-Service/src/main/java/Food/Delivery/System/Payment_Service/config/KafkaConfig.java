package Food.Delivery.System.Payment_Service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@EnableKafka
public class KafkaConfig {
    @Bean
    public NewTopic paymentSuccessTopic() {
        return new NewTopic("payment_success", 3, (short) 1);
    }
    @Bean
    public NewTopic paymentFailureTopic() {
        return new NewTopic("payment_failure", 3, (short) 1);
    }
    @Bean
    public NewTopic orderCreatedTopic() {
        return new NewTopic("order_created", 3, (short) 1);
    }
}
