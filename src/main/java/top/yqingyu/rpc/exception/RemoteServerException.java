package top.yqingyu.rpc.exception;

import top.yqingyu.common.exception.QyRuntimeException;

public class RemoteServerException extends QyRuntimeException {
    public RemoteServerException() {
    }

    public RemoteServerException(String message, Object... o) {
        super(message, o);
    }

    public RemoteServerException(Throwable cause, String message, Object... o) {
        super(cause, message, o);
    }

    public RemoteServerException(Throwable cause) {
        super(cause);
    }

    public RemoteServerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, Object... o) {
        super(message, cause, enableSuppression, writableStackTrace, o);
    }
}
