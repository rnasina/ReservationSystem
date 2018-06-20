package com.walmart.transaction;

/**
 * Created by rnasina on 6/15/18.
 */
public class TransactionWithResultBlock<T> {

    private final TransactionalWithResult<T> target;

    public TransactionWithResultBlock(TransactionalWithResult<T> t) {
        this.target = t;
    }

    public T run(Transaction tx) {
        if (target != null) {
            return target.run(tx);
        }
        return null;
    }
}
