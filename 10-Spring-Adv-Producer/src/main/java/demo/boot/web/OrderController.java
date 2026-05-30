package demo.boot.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import demo.boot.model.OrderEvent;
import demo.boot.service.OrderProducer;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

 private final OrderProducer orderProducer;

 @PostMapping
 public ResponseEntity<String> createOrder(
         @RequestBody OrderEvent event) {

     orderProducer.publishOrder(event);

     return ResponseEntity.ok(
             "Order published successfully"
     );
 }
}