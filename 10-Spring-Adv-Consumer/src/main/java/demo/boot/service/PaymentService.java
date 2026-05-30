package demo.boot.service;


import org.springframework.stereotype.Service;

import demo.boot.model.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import demo.boot.model.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PaymentService {

    public void processPayment(OrderEvent event) {

        log.info(
                "Payment processing started for orderId={}",
                event.getOrderId()
        );

        // simulate payment validation

        if (event.getAmount() > 50000) {

            throw new PaymentFailedException(
                    "HIGH_AMOUNT_TRANSACTION"
            );
        }

        log.info(
                "Payment successful for customer={}",
                event.getCustomerName()
        );
    }
}