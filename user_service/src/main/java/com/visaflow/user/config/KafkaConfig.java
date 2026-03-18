package com.visaflow.user.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic companyEventsTopic() {
        return TopicBuilder.name("company-events").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic profileEventsTopic() {
        return TopicBuilder.name("profile-events").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic subscriptionEventsTopic() {
        return TopicBuilder.name("subscription-events").partitions(3).replicas(1).build();
    }
}
