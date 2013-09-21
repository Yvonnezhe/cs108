package P1Bank;/* User: Peter  Date: 16.09.13  Time: 16:42 */

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Bank {
    private static final int DEFAULT_BALANCE = 1000;

    private static final String SMALL_FILE = "C:\\Users\\Peter\\IdeaProjects\\cs108\\hw4\\P0InputFiles\\small.txt";
    private static final String MEDIUM_FILE = "C:\\Users\\Peter\\IdeaProjects\\cs108\\hw4\\P0InputFiles\\5k.txt";
    private static final String BIG_FILE = "C:\\Users\\Peter\\IdeaProjects\\cs108\\hw4\\P0InputFiles\\100k.txt";

    private static final int NUM_OF_ACCOUNTS = 100;
    private static final int NUM_OF_THREADS = 4;
    private static final Transaction FINAL_TRANSACTION = new Transaction(-1,0,0);

    private static BlockingQueue<Transaction> transactionsQueue = new LinkedBlockingQueue<Transaction>();
    private static ArrayList<Account> accountsList = new ArrayList<Account>();
    private static ArrayList<Worker> workersList = new ArrayList<Worker>();

    public Bank(int accountsNumber, int workersNumber) {
        for (int i = 0; i < accountsNumber; i++)
            accountsList.add(new Account(i, DEFAULT_BALANCE));

        for (int i = 0; i < workersNumber; i++)
            workersList.add(new Worker());

        for (Worker w : workersList)
            w.start();

    }
    private void startProcessingFrom(String filename){
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
            while(true){
                String line = br.readLine();
                if(line == null){
                    for(int i = 0; i < NUM_OF_THREADS; i++)
                        transactionsQueue.put(FINAL_TRANSACTION);
                    break;
                }
                int[] t = parseData(line);

                transactionsQueue.put(new Transaction(t[0], t[1],  t[2]));
                transactionsQueue.put(new Transaction(t[1], t[0], -t[2]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private int[] parseData(String line) throws InterruptedException {
        String [] data = line.split(" ");
        int[] fromTOAmount = new int[data.length];

        for(int i = 0; i < data.length; i++)
            fromTOAmount[i] = Integer.parseInt(data[i]);

        return fromTOAmount;


    }

    private void close() {
        try {
            for (Worker w : workersList)
                w.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.print("Bank is closed");
    }

    public class Worker extends Thread{
        @Override
        public void run(){
            while (true) {
                try {

                    Transaction nextTransaction = transactionsQueue.take();

                    if(nextTransaction.equals(FINAL_TRANSACTION))
                        break;

                    Account acc = accountsList.get(nextTransaction.getTo());
                    acc.makeTransaction(nextTransaction);
                    System.out.println(acc);

                } catch (InterruptedException e) {
                    e.printStackTrace();

                }
            }
        }

    }

    public static void main(String []args){
        Bank bank = new Bank(NUM_OF_ACCOUNTS, NUM_OF_THREADS);
        //bank.startProcessingFrom(SMALL_FILE);
        //bank.startProcessingFrom(MEDIUM_FILE);
        bank.startProcessingFrom(BIG_FILE);
        bank.close();
    }




}
