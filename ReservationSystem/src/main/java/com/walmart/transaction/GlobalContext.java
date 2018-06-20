package com.walmart.transaction;

/**
 * Created by rnasina on 6/15/18.
 */
public class GlobalContext implements Context {

    private static final GlobalContext INSTANCE = new GlobalContext();

    private GlobalContext() {
        if (INSTANCE != null) {
            throw new IllegalStateException("Already instantiated.");
        }
    }

    public static GlobalContext getInstance() {
        return INSTANCE;
    }

    public <T> T get(Ref<T> ref) {
        return ref.content.value;
    }
}
