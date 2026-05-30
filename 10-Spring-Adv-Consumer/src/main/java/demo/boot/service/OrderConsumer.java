package demo.boot.service;


import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import demo.boot.model.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderConsumer {

    private final PaymentService paymentService;

    private final InventoryService inventoryService;

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "orders",
            groupId = "order-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeOrders(
            List<ConsumerRecord<String, OrderEvent>> records,
            Acknowledgment acknowledgment) {

        log.info(
                "Batch received with size={}",
                records.size()
        );

        OrderEvent event = null;

        try {

            for (ConsumerRecord<String, OrderEvent> record : records) {

                event = record.value();

                log.info(
                        """
                        ====================================
                        ORDER RECEIVED
                        ====================================
                        Topic      : {}
                        Partition  : {}
                        Offset     : {}
                        Key        : {}
                        Event      : {}
                        ====================================
                        """,

                        record.topic(),
                        record.partition(),
                        record.offset(),
                        record.key(),
                        event
                );

                validate(event);

                paymentService.processPayment(event);

                inventoryService.updateInventory(event);

                notificationService.sendNotification(event);

                log.info(
                        """
                        ====================================
                        ORDER PROCESSED SUCCESSFULLY
                        ====================================
                        OrderId : {}
                        ====================================
                        """,
                        event.getOrderId()
                );
            }

            // manual commit
            acknowledgment.acknowledge();

            log.info(
                    "Offsets committed successfully"
            );

        } catch (Exception ex) {

            log.error(
                    """
                    ====================================
                    ORDER PROCESSING FAILED
                    ====================================
                    OrderId : {}
                    Reason  : {}
                    ====================================
                    """,
                    event != null
                            ? event.getOrderId()
                            : "UNKNOWN",

                    ex.getMessage()
            );

            // IMPORTANT
            // retry + DLT
            throw ex;
        }
    }

    private void validate(OrderEvent event) {

        if (event.getOrderId() == null) {

            throw new RuntimeException(
                    "ORDER_ID_MISSING"
            );
        }

        if (event.getCustomerName() == null) {

            throw new RuntimeException(
                    "CUSTOMER_NAME_MISSING"
            );
        }

        if (event.getAmount() == null
                || event.getAmount() <= 0) {

            throw new RuntimeException(
                    "INVALID_AMOUNT"
            );
        }
    }
}