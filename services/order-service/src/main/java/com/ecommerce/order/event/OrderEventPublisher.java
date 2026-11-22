package com.ecommerce.order.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Sends order events to Kafka only after the DB transaction commits, so a rolled-back
 * order never produces an event. Keyed by orderId so all events for one order stay
 * ordered within a partition.
 *
 * Caveat: the send is not atomic with the DB commit (a crash between commit and send
 * loses the event). Move to a transactional outbox if that matters.
 */
@Slf4j
@Component
public class OrderEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String createdTopic;
    private final String statusChangedTopic;

    public OrderEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                               @Value("${kafka.topics.order-created}") String createdTopic,
                               @Value("${kafka.topics.order-status-changed}") String statusChangedTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.createdTopic = createdTopic;
        this.statusChangedTopic = statusChangedTopic;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(OrderCreatedEvent event) {
        send(createdTopic, event.orderId().toString(), event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(OrderStatusChangedEvent event) {
        send(statusChangedTopic, event.orderId().toString(), event);
    }

    private void send(String topic, String key, Object payload) {
        kafkaTemplate.send(topic, key, payload).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish to {} key={}", topic, key, ex);
            } else {
                log.debug("Published to {} key={} offset={}", topic, key, result.getRecordMetadata().offset());
            }
        });
    }
}
