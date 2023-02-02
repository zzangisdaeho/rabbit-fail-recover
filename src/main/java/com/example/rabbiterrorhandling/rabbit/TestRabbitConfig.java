package com.example.rabbiterrorhandling.rabbit;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;

@Configuration
public class TestRabbitConfig {

    //----------------------------------------------DLX-------------------------------------------
    @Value("${rabbitmq.dlx-exchange}")
    private String dlxExchangeName;

    @Value("${rabbitmq.dlx-queue}")
    private String dlxQueueName;

    @Bean
    public TopicExchange deadLetterExchange() {
        return ExchangeBuilder.topicExchange(dlxExchangeName).build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(dlxQueueName).build();
    }

    @Bean
    public Binding deadLetterBinding(TopicExchange deadLetterExchange, Queue deadLetterQueue) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with("#");
    }

    //----------------------------------------------DLX-------------------------------------------

    @Bean
    public DirectExchange testDlxExchange() {
        return new DirectExchange("test.dlx.exchange");
    }

    @Bean
    public Queue testDlxQueue() {
        return QueueBuilder.durable("test.dlx.queue")
                .deadLetterExchange(dlxExchangeName)
                .deadLetterRoutingKey("")
//                .withArgument("x-dead-letter-exchange", dlxExchangeName)
//                .withArgument("x-dead-letter-routing-key", "")
                .build();
    }

    @Bean
    public Binding mainBinding(DirectExchange testExchange, Queue testDlxQueue) {
        return BindingBuilder.bind(testDlxQueue).to(testExchange).with("test.dlx");
    }

}
