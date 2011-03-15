package org.protege.owl.server.util;

public abstract class RunnableWithException<X extends Throwable> implements Runnable {
    private X exception;

    public X getException() {
        return exception;
    }

    public void setException(X exception) {
        this.exception = exception;
    }

}
