package com.example.api.java8.program;

public class EvenOddThread implements Runnable{
    Object object;
    static int i =1;

    EvenOddThread(Object object){
        this.object = object;
    }

    @Override
    public void run() {

            while(i<=10){

                if(i%2==0 && Thread.currentThread().getName().equals("even")){
                    synchronized (object){
                        System.out.print(Thread.currentThread().getName()+" : "+ i);
                        i++;
                        try {
                            object.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                if(i%2!=0 && Thread.currentThread().getName().equals("odd")){
                    synchronized (object){
                        System.out.print(Thread.currentThread().getName()+" : "+ i);
                        i++;
                        object.notify();
                    }
                }
            }
    }

    public static void main(String[] args) {
        Object lock=new Object();
        Runnable r1=new EvenOddThread(lock);
        Runnable r2=new EvenOddThread(lock);
        new Thread(r1, "even").start();
        new Thread(r2, "odd").start();
    }
}
