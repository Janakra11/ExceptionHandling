package com.example.api.java8.program;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

public class EmpDepartMaxSalary {

    public static void main(String[] args) {

        List<Emp>  emps = Arrays.asList(
                new Emp(1, "John Doe", "Development", 8000),
                new Emp(2, "Jane Smith", "Development", 80000),
                new Emp(3, "Robert Brown", "Sales",60000),
                new Emp(4, "Lisa White", "HR", 55000),
                new Emp(5, "Michael Green", "Finance", 90000),
                new Emp(6, "Sophia Brown", "Development",85000),
                new Emp(7, "James Wilson", "Marketing",  72000),
                new Emp(8, "Olivia Harris", "Development", 88000),
                new Emp(9, "William Lee", "Sales", 78000),
                new Emp(10, "Emily Clark", "Development", 95000)
        );


        //Approach 1
        Comparator<Emp> empComparator= Comparator.comparing(Emp::getSalary);

        Map<String, Optional<Emp>> empMap= emps.stream().collect(
                Collectors.groupingBy(
                        Emp::getDepart,
                        Collectors.reducing(
                                BinaryOperator.maxBy(empComparator))
                        )
        );

        System.out.print(empMap);


        System.out.print("\n");
        //Approach 2


        Map<String, Emp> empOptionalMap= emps.stream()
                .collect(
                        Collectors.groupingBy(
                                Emp::getDepart,
                                Collectors.collectingAndThen(
                                        Collectors.maxBy(Comparator.comparingInt(Emp::getSalary)),
                                        Optional::get
                                )
                        )
                );

        System.out.print(empOptionalMap);


    }
}
