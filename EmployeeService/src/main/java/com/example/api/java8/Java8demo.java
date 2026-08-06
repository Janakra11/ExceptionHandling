package com.example.api.java8;

public class Java8demo {


    //Functional Interface is interface containing only one abstract ,any no of default and static method allowed

    /*
    What all Java8 features as follows:
    1. Functional Interface :
        Functional Interface is interface containing only one abstract ,any no of default and static method allowed
        with allowed @FunctionalInterface

       Q. Can you tell how many functional interfaces before JAVA8?
       Ans: Callble(call), Runnable(run()), Comparator(compare())
       Q. Can you wirte fuctional interface?
       Ans: for Ex.
       @FunctionalInterface
       interface UPIPay{

           String doPayment(String source, String target);

           default double sratchCard(){
               return new Random().nextDouble();
           }

           static String datePattern(String pattern){
                DateFormatter dt = new SimpleDateFormatter(pattern);
                return dt.format(new Date());
           }
       }

       Q.Can we extend functional Interface from another functional interface?

       ans> Yes we can  extend another function interface but then that interface will be normal inetrface as
            it has two abstract method one from another extened interface and one its own.

       Q.


    2. Lambda Expression :

    3. Stream:

    4. CompletableFuture:

    5. Java Date and Time API:

     */
}
