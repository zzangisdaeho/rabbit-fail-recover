package com.example.rabbiterrorhandling.rabbit;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomMessageRecover implements MessageRecoverer {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void recover(Message message, Throwable cause) {
        // count the number of retries
        Integer retryCount = message.getMessageProperties().getHeader("retryCount");
        if(retryCount == null) retryCount = 0;
        if (retryCount < 3) {
            // increment the retry count
            message.getMessageProperties().setHeader("retryCount", retryCount + 1);
            // requeue the message
            rabbitTemplate.send(message.getMessageProperties().getReceivedExchange(),
                    message.getMessageProperties().getReceivedRoutingKey(),
                    message);
        } else {
            // send the message to the DLX
            rabbitTemplate.send("dlxExchange", "dlxRoutingKey", message);
        }
    }

}