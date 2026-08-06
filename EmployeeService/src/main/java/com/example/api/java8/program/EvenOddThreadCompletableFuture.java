package com.example.api.java8.program;

import java.util.concurrent.CompletableFuture;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

public class EvenOddThreadCompletableFuture {

    private static Object object = new Object();
    private static IntPredicate evenCond=e->e%2==0;
    private static IntPredicate oddCond=e->e%2!=0;

    public static void main(String[] args) throws InterruptedException {
        CompletableFuture.runAsync(()->EvenOddThreadCompletableFuture.printNumber(evenCond));
        CompletableFuture.runAsync(()->EvenOddThreadCompletableFuture.printNumber(oddCond));
        Thread.sleep(1000);
    }

    public static void printNumber(IntPredicate cond){
        IntStream.rangeClosed(1,10).filter(cond).forEach(EvenOddThreadCompletableFuture::execute);
    }

    static public void execute(int no){

        synchronized (object){

            try {
                System.out.print(Thread.currentThread().getName()+" : "+no+"\n");
                object.notify();
                object.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
