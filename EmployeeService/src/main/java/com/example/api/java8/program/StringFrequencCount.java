package com.example.api.java8.program;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StringFrequencCount {

    public static void main(String[] args) {

        String s = "janakraj";

        Map<String, Long> map=  Arrays.stream(s.split(""))
                .collect(
                        Collectors.groupingBy(
                                Function.identity(),
                                Collectors.counting()
                        )
                );

        System.out.print(map);


    }
}
