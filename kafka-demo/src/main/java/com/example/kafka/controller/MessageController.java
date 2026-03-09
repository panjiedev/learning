package com.example.kafka.controller;

import com.example.kafka.producer.KafkaProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final KafkaProducer kafkaProducer;

    public MessageController(KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    @PostMapping
    public ResponseEntity<String> sendMessage(@RequestParam("message") String message) {
        kafkaProducer.sendMessage(message);
        return ResponseEntity.ok("Message sent to the Kafka Topic Successfully");
    }

    @PostMapping("/batch")
    public ResponseEntity<String> sendBatchMessages(@RequestParam(value = "count", defaultValue = "100000") int count,
            @RequestParam("message") String message) {
        for (int i = 0; i < count; i++) {
            kafkaProducer.sendMessage(message + " - index " + i);
        }
        return ResponseEntity.ok(count + " messages sent to the Kafka Topic Successfully");
    }
}
