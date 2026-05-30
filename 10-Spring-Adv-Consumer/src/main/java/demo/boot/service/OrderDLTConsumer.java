package demo.boot.service;



import demo.boot.model.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderDLTConsumer {

    @KafkaListener(
            topics = "orders.DLT",
            groupId = "orders-dlt-group"
    )
    public void consumeDLT(OrderEvent event) {

        log.error(
                """
                ====================================
                DLT MESSAGE RECEIVED
                ====================================
                OrderId       : {}
                Customer      : {}
                Product       : {}
                Amount        : {}
                Action        : MANUAL_INVESTIGATION
                ====================================
                """,

                event.getOrderId(),
                event.getCustomerName(),
                event.getProduct(),
                event.getAmount()
        );

        // save to DB
        // send email alert
        // manual reprocessing
    }
}
