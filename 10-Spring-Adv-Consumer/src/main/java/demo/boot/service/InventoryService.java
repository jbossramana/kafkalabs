package demo.boot.service;

import org.springframework.stereotype.Service;

import demo.boot.model.OrderEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class InventoryService {

 public void updateInventory(OrderEvent event) {

     log.info(
             "Inventory updating for product={}",
             event.getProduct()
     );

     log.info(
             "Inventory updated successfully"
     );
 }
}
