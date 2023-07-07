package top.yqingyu.rpc.exception;

import top.yqingyu.common.utils.StringUtil;

public class RpcException extends RuntimeException {
    public RpcException() {
    }

    public RpcException(String message, Object... o) {
        super(StringUtil.fillBrace(message, o));
    }

    public RpcException(Throwable cause, String message, Object... o) {
        super(StringUtil.fillBrace(message, o), cause);
    }

    public RpcException(Throwable cause) {
        super(cause);
    }

    public RpcException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, Object... o) {
        super(StringUtil.fillBrace(message, o), cause, enableSuppression, writableStackTrace);
    }
}
