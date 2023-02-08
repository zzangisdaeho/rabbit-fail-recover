package com.example.rabbiterrorhandling.rabbit.consumer;

import com.rabbitmq.client.Channel;
import lombok.AllArgsConstructor;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@AllArgsConstructor
public class TestConsumer {

    private final RabbitTemplate rabbitTemplate;

    private final boolean flag = false;

    @RabbitListener(queues = "test.dlx.queue")
    public void processMessage(String messageBody, Channel channel, Message message) throws IOException {
        try {
            System.out.println("messageBody = " + messageBody);
            if(!flag){
                throw new RuntimeException("error");
            }
        } catch (Exception e) {
            // count the number of retries
            Integer retryCount = message.getMessageProperties().getHeader("retryCount");
            if(retryCount == null) retryCount = 0;
            if (retryCount < 2) {
                // increment the retry count
                message.getMessageProperties().setHeader("retryCount", retryCount + 1);
                // requeue the message
                rabbitTemplate.send(message.getMessageProperties().getReceivedExchange(), message.getMessageProperties().getReceivedRoutingKey(), message);
            } else {
                throw new AmqpRejectAndDontRequeueException(e);
            }
        }
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    exchange = @Exchange(value = "exchange.not.exist", type = ExchangeTypes.FANOUT),
                    value = @Queue(value = "queue.not.exist", durable = "false", exclusive = "true", autoDelete = "true"
                            , arguments = {@Argument(name = "x-dead-letter-exchange", value = "test.dlx.exchange")})
            )
    )
    public void testChannel(String messageBody, Channel channel, Message message){
        System.out.println("messageBody = " + messageBody);
    }

    @RabbitListener(queuesToDeclare = {@Queue(name = "queue.not.exist2", exclusive = "true",
            arguments = {@Argument(name = "x-dead-letter-exchange", value = "dlx.exchange-fanout.dlx.v0")})}
    )
    public void testChannel2(String messageBody, Channel channel, Message message) throws IOException {
        System.out.println("messageBody = " + messageBody);
    }

}
