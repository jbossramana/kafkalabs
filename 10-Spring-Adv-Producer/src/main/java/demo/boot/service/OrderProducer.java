package demo.boot.service;

import demo.boot.model.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public void publishOrder(OrderEvent event) {

        log.info(
                "Starting Kafka transaction for orderId={}",
                event.getOrderId()
        );

        Message<OrderEvent> message =
                MessageBuilder
                        .withPayload(event)

                        .setHeader(
                                KafkaHeaders.TOPIC,
                                "orders"
                        )

                        .setHeader(
                                KafkaHeaders.KEY,
                                event.getOrderId()
                        )

                        .build();

        kafkaTemplate
                .send(message)

                .whenComplete((result, ex) -> {

                    if (ex != null) {

                        log.error(
                                """
                                ====================================
                                TRANSACTION FAILED
                                ====================================
                                OrderId : {}
                                Reason  : {}
                                ====================================
                                """,

                                event.getOrderId(),
                                ex.getMessage()
                        );

                    } else {

                        log.info(
                                """
                                ====================================
                                TRANSACTION COMMITTED
                                ====================================
                                Topic      : {}
                                Partition  : {}
                                Offset     : {}
                                OrderId    : {}
                                ====================================
                                """,

                                result.getRecordMetadata().topic(),

                                result.getRecordMetadata().partition(),

                                result.getRecordMetadata().offset(),

                                event.getOrderId()
                        );
                    }
                });
    }
}