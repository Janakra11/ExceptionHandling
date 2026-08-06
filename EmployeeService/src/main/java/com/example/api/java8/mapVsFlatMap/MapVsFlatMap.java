package com.example.api.java8.mapVsFlatMap;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MapVsFlatMap {

    public static void main(String[] args) {


        List<Customer> customers = Stream.of(
                new Customer(101, "john", "john@gmail.com", Arrays.asList("397937955", "21654725")),
                new Customer(102, "smith", "smith@gmail.com", Arrays.asList("89563865", "2487238947")),
                new Customer(103, "peter", "peter@gmail.com", Arrays.asList("38946328654", "3286487236")),
                new Customer(104, "kely", "kely@gmail.com", Arrays.asList("389246829364", "948609467"))
        ).collect(Collectors.toList());


        //get list of Emails of Customer using map

        List<String> customerMails = customers.stream().map(Customer::getEmail).collect(Collectors.toList());

        System.out.print(customerMails+"\n");

        //Get list of phone numbers for all customers using flatMap
        List<String> customerMobileNos = customers.stream()
                .flatMap(c->c.getPhoneNumbers().stream()).collect(Collectors.toList());

        System.out.print(customerMobileNos);
    }
}
