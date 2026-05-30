package demo.boot.service;

public class PaymentFailedException
extends RuntimeException {

public PaymentFailedException(String message) {

super(message);
}
}
