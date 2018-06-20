package com.walmart.transaction;

@FunctionalInterface
public interface Transactional {

    void run(Transaction transaction);

}
