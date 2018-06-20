package com.walmart.transaction;

/**
 * Created by rnasina on 6/15/18.
 */
public final class TransactionalMemory {

    public static Object commitLock = new Object();

    private TransactionalMemory() {
    }

    public static void transaction(TransactionBlock block) {
        boolean committed = false;
        while (!committed) {
            Transaction tx = new Transaction();
            block.run(tx);
            committed = tx.commit();
            if (!committed) {
                System.out.println("Failed commit. Retrying...");
            }
        }
    }

    public static <T> T transactionWithResult(TransactionWithResultBlock<T> block) {
        boolean committed = false;
        T result = null;
        while (!committed) {
            Transaction tx = new Transaction();
            result = block.run(tx);
            committed = tx.commit();
            if (!committed) {
                System.out.println("Failed commit when result is " + result + " Retrying....");
            }
        }
        return result;

    }

}
