package top.yqingyu.rpc.exception;

import top.yqingyu.common.exception.QyRuntimeException;
import top.yqingyu.common.utils.StringUtil;

public class IllegalRpcArgumentException extends QyRuntimeException {
    public IllegalRpcArgumentException() {
    }

    public IllegalRpcArgumentException(String message, Object... o) {
        super(message, o);
    }

    public IllegalRpcArgumentException(Throwable cause, String message, Object... o) {
        super(cause, message, o);
    }

    public IllegalRpcArgumentException(Throwable cause) {
        super(cause);
    }

    public IllegalRpcArgumentException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, Object... o) {
        super(message, cause, enableSuppression, writableStackTrace, o);
    }
}
