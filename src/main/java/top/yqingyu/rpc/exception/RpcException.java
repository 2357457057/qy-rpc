package top.yqingyu.rpc.exception;

import top.yqingyu.common.exception.QyRuntimeException;
import top.yqingyu.common.utils.StringUtil;

public class RpcException extends QyRuntimeException {
    public RpcException() {
    }

    public RpcException(String message, Object... o) {
        super(message, o);
    }

    public RpcException(Throwable cause, String message, Object... o) {
        super(cause, message, o);
    }

    public RpcException(Throwable cause) {
        super(cause);
    }

    public RpcException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, Object... o) {
        super(message, cause, enableSuppression, writableStackTrace, o);
    }
}
