package com.sentaroh.android.Utilities3;

public class SafException extends Exception {
    private static final long serialVersionUID = 1L;
    private Throwable mException = null;
    private String mMessage = null;

    public SafException(Throwable e, Throwable cause) {
        mException = new Exception(e.getMessage(), cause);
        mException.setStackTrace(e.getStackTrace());
        mMessage = e.getMessage();
    }

    public SafException(String msg) {
        mException = new Exception(msg);
        mMessage = msg;
    }

    public Throwable getCause() {
        return mException.getCause();
    }

    public String getMessage() {
        return mMessage;
    }

}
