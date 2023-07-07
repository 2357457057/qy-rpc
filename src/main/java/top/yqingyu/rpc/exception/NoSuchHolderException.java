package top.yqingyu.rpc.exception;

import top.yqingyu.common.exception.QyRuntimeException;
import top.yqingyu.common.utils.StringUtil;

public class NoSuchHolderException extends QyRuntimeException {

    public NoSuchHolderException() {
    }

    public NoSuchHolderException(String message, Object... o) {
        super(message, o);
    }

    public NoSuchHolderException(Throwable cause, String message, Object... o) {
        super(cause, message, o);
    }

    public NoSuchHolderException(Throwable cause) {
        super(cause);
    }

    public NoSuchHolderException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, Object... o) {
        super(message, cause, enableSuppression, writableStackTrace, o);
    }
}
