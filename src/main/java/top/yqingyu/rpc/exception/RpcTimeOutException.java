package top.yqingyu.rpc.exception;

import top.yqingyu.common.exception.QyRuntimeException;

public class RpcTimeOutException extends QyRuntimeException {
    public RpcTimeOutException() {
    }

    public RpcTimeOutException(String message, Object... o) {
        super(message, o);
    }

    public RpcTimeOutException(Throwable cause, String message, Object... o) {
        super(cause, message, o);
    }

    public RpcTimeOutException(Throwable cause) {
        super(cause);
    }

    public RpcTimeOutException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, Object... o) {
        super(message, cause, enableSuppression, writableStackTrace, o);
    }
}
