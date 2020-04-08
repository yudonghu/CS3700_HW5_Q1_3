import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class person implements Callable{
    final int ITEM_NEED_TO_PRODUCE = 100;
    private String name;
    private int itemRemaining;
    //private static Lock bufferLock = new ReentrantLock();
    private BlockingQueue itemBuffer;
    static int workingProducerAmount = 0;

    static AtomicInteger produceCount = new AtomicInteger(0);
    static AtomicInteger consumeCount = new AtomicInteger(0);


    public person(String name,BlockingQueue<String> q){
        this.name = name;
        if(name.contains("Producer")){
            workingProducerAmount++;
        }
        this.itemRemaining = ITEM_NEED_TO_PRODUCE;
        this.itemBuffer = q;
    }

    @Override
    public Object call() throws Exception {
        long start = System.currentTimeMillis();
        if(this.name.contains("Producer")){
            while(itemRemaining > 0){
                //if(itemBuffer.remainingCapacity()> 0){
                //System.out.println(produceCount.get() +" "+ consumeCount.get() +" ");
                //Thread.sleep(3000);
                if(produceCount.get() - consumeCount.get() < 10){
                    //System.out.println(produceCount.get() +" "+ consumeCount.get() +" "+ itemBuffer);
                    try{
                        itemBuffer.add("Item #" +(ITEM_NEED_TO_PRODUCE-itemRemaining) +" from "+ this.name);
                        produceCount.incrementAndGet();
                        itemRemaining--;
                        System.out.format(this.name+": Item #%d added to buffer - remaining capacity: %d\n",ITEM_NEED_TO_PRODUCE-itemRemaining,itemBuffer.remainingCapacity());
                    }catch(Exception e){
                        //e.printStackTrace();
                    }
                }
            }
            workingProducerAmount--;
        }else{//"Consumer"
            int counter = 0;
            Thread.sleep(5);
            while( (workingProducerAmount!=0) || (!itemBuffer.isEmpty()) ){
                //if((!itemBuffer.isEmpty()) ){
                if(produceCount.get() - consumeCount.get() > 0 ){
                    try{
                        String item = (String)itemBuffer.take();
                        consumeCount.incrementAndGet();
                        //itemConsumed++;
                        System.out.format(this.name + ":consumed  %s\n", item);
                        counter++;
                        Thread.sleep(1000);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
            System.out.println(this.name +": total items consumed: "+counter);
        }
        return System.currentTimeMillis()-start;
    }

}

public class HW5_Q1_3 {
    public static void main(String[] args){

        final int PRODUCER_AMOUNT = 2;
        final int CONSUMER_AMOUNT = 5;
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(10);

        ExecutorService pool = Executors.newFixedThreadPool((PRODUCER_AMOUNT+CONSUMER_AMOUNT));
        for(int i = 0;i < PRODUCER_AMOUNT;i++){
            pool.submit(new person(("Producer "+i),queue));
        }
        Future[] result = new Future[CONSUMER_AMOUNT];
        for(int i = 0;i < CONSUMER_AMOUNT;i++){
            result[i] = pool.submit(new person(("Consumer "+i),queue));
        }
        pool.shutdown();



        long escapedTime = 0;
        for(int i = 0;i <CONSUMER_AMOUNT;i++ ){
            try {
                long tempLong = (long) result[i].get();
                if(tempLong > escapedTime){
                    escapedTime=tempLong;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Evaluating Implementation, Case : Atomic Variable");
        System.out.println("Producer amount: "+PRODUCER_AMOUNT +" & Consumer amount: "+  CONSUMER_AMOUNT+"\nTime used: "+escapedTime+" milli-sec.");

    }
}
