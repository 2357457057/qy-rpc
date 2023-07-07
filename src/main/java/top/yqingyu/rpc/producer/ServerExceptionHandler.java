package top.yqingyu.rpc.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yqingyu.qymsg.QyMsg;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public interface ServerExceptionHandler {
    Logger logger = LoggerFactory.getLogger(ServerExceptionHandler.class);

    default void exceptionCallBack(SocketAddress socketAddress, QyMsg msg, Throwable t) {
        InetSocketAddress address = (InetSocketAddress) socketAddress;
        logger.error("QyRpc invoke error from ip:{} consumer:{}", address.getAddress().getHostAddress(), msg.getFrom(), t);
    }
}
