package demo.boot.service;

import org.springframework.stereotype.Service;

import demo.boot.model.OrderEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NotificationService {

 public void sendNotification(OrderEvent event) {

     log.info(
             "Sending email notification to customer={}",
             event.getCustomerName()
     );

     log.info(
             "Notification sent successfully"
     );
 }
}