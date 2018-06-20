package com.walmart.transaction;

import com.walmart.transaction.Transaction;

/**
 * Created by rnasina on 6/15/18.
 */
public interface TransactionalWithResult<T> {

    T run(Transaction transaction);
}
