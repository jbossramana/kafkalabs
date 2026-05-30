package com.shipment.microservice;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Shipment {


    @Id
    @GeneratedValue
    private Long id;

    @Column
    private String address;


    @Column
    private String status;


    @Column 
    private long orderId;


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public String getAddress() {
        return address;
    }


    public void setAddress(String address) {
        this.address = address;
    }


    public String getStatus() {
        return status;
    }


    public void setStatus(String status) {
        this.status = status;
    }


    public long getOrderId() {
        return orderId;
    }


    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }
    
}
