package com.example.kafka.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class KafkaConsumer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);
    private final AtomicInteger count = new AtomicInteger(0);

    @KafkaListener(topics = "demo-topic-batch", groupId = "my-batch-group", concurrency = "3")
    public void consume(String message) {
        int currentCount = count.incrementAndGet();
        if (currentCount % 10000 == 0 || currentCount == 1 || currentCount == 100000) {
            logger.info("#### -> Consumed {} messages so far. Last message: [{}] processed by {}",
                    currentCount, message, Thread.currentThread().getName());
        }
    }
}
