package com.example.api.java8.functionalInterface;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Java8functionalInterfaces {

    public static void main(String[] args) {

        List<Integer> list = Arrays.asList(12,22,2,3,4,5,6,7,8,9,10);
        Predicate<Integer> evenNoPredicate = n-> n%2==0;
        Function<Integer, Integer> squareNoFunction = n->n*n;
        Consumer<Integer> consumerPrint = n->{System.out.print(n);};
        //Supplier<Integer> integerSupplier = ()-> {System.out::println};

        System.out.print(squareNoFunction.apply(10)+"\n");
        System.out.print(evenNoPredicate.test(2)+"\n");
        consumerPrint.accept(10);

        list.stream().filter(x->x>4)
                .sorted().forEach(System.out::println);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                System.out.print("in inside the run()");
            }
        };

        //Lambda expression
        Runnable runnable1 = ()->System.out.print("inside run()2");
        //new Thread(runnable1).getName();

        //MyFunction myFunction = (i)-> i*10;
        //System.out.print(myFunction.test(15));
    }
}
