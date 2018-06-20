package com.walmart.transaction;

/**
 * Created by rnasina on 6/15/18.
 */
public interface Context {
    <T> T get(Ref<T> tRef);
}
