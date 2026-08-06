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

       Q.What all funcational interfaces in JAVA8?
       Ans: 
            1. Funtion   :   T apply(R r)
            2. Predicate :   boolean test(T t)
            3. Consumer  :   void accept(T t)
            4. Supplier  :   T get()

2. Lambda Expression :
   Lambda expression basically express instances of functional interface  in other word it represent
   method of function using an expression.
    
    Q.What are advantages and disadvantages of Lambda expression?
    Ans:
        Advantages:
            1.Avoid writing anonymous implmentation
            2.It saves lots of code.
            3.Code is directly readable without interpretation
        Disadvantages:
            1.Hard to use without IDE
            2.complex to debug

3. Stream API:
    Q. What is stream API ?
    Ans: Stream API introduced in java8 and it is used to processed collection of objects with functional
         coding style using lambda expression.
    

    Q. what is stream in Java8?
    Ans: Stream is sequence of objects that supports various methods pipelined to produce desired output.
        Features of Java Streams:
        A stream is not a data structure instead it takes input from Arrays, collection or I/O channels.
        A Stream doesnot change original data structure, they only provide the result based on pipeline method.

   
    Q. What is method reference in JAVA8?
    Ans: Method reference is shorthand notation of a lambda expression calling method.
         for ex: (t)-> System.out::println;
         we can call static method reference and instance method refernce

12. Spell Few stream API method you used in projects?
Ans:
    filter(), forEach(), sorted(), map(), flatMap(), reduced(), groupingBy(), count(), collect()

13. When to use map() and flatMap()?
Ans. map():
    It processes stream of values.
    It does only mapping.
    It’s mapper function produces single value for each input value.
    It is a One-To-One mapping.
    Data Transformation : From Stream to Stream
    Use this method when the mapper function is producing a single value for each input value

    flatMap()
    It processes stream of stream values.
    It performs mapping as well as flattening.
    It’s mapper function produces multiple values for each input value.
    It is a One-To-Many mapping.
    Data Transformation : From Stream<Stream to Stream
    Use this method when the mapper function is producing multiple values for each input value.

14.WAP to find frequence of each character in a given string?
15.Assuming you list of employees in various department WAP to get highest paid employee from each department?

16. Stream vs Parallel stream?
Ans : both are used to process object of collection for stream is sequenctional operation on sigle core where 
     as parallel stream all core process to excute parallel processing. 

17. What is CompletableFuture?
Ans: CompletableFuture is used asynchronus programming in java. Asynchronus programming means of writing
    non-blocking code by running task on a separate thread than the main application thread and notify the 
    main thread about its progress, completion or failure.  
18. Why CompletableFuture why not future ?
Ans: future cannot be manully completed
     Multiple future cannot be chained together
     You cannot combine multiple future together
     No Exception handling
19. How to decide thread pool size?
Ans: CPU intensive Task
    IO intensive Task
20. WAP to print even and odd using 2 thread?
