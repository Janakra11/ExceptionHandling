package com.example.api.java8.functionalInterface;

public class PayTm implements UPIPayment{

    @Override
    public String doPayment(String source, String target) {
        String txPat=UPIPayment.datePattern("yyyy-MM-dd");
        return "";
    }

    @Override
    public double getScratchCard() {
        return UPIPayment.super.getScratchCard();
    }
}
