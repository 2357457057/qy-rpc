package top.yqingyu.rpc.producer;

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yqingyu.common.qydata.DataMap;
import top.yqingyu.common.utils.StringUtil;
import top.yqingyu.qymsg.DataType;
import top.yqingyu.qymsg.MsgHelper;
import top.yqingyu.qymsg.MsgType;
import top.yqingyu.qymsg.QyMsg;
import top.yqingyu.qymsg.netty.QyMsgServerHandler;
import top.yqingyu.rpc.Constants;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

public class RpcHandler extends QyMsgServerHandler {
    final ConcurrentHashMap<String, Bean> ROUTING_TABLE;
    final ServerExceptionHandler serverExceptionHandler;
    final Producer producer;
    public static final Logger logger = LoggerFactory.getLogger(RpcHandler.class);

    public RpcHandler(Producer producer) {
        ROUTING_TABLE = producer.ROUTING_TABLE;
        this.serverExceptionHandler = producer.exceptionHandler;
        this.producer = producer;
    }

    @Override
    protected QyMsg handle(ChannelHandlerContext ctx, QyMsg msg) throws Exception {
        String linkId = MsgHelper.gainMsgValue(msg, Constants.linkId);
        linkId = linkId == null ? msg.getFrom() : linkId;
        Producer.RPC_LINK_ID.put(Thread.currentThread().getName(), linkId);
        QyMsg deal = deal(ctx, msg);
        if (deal != null) {
            deal.setFrom(producer.serverName);
            deal.setDataType(msg.getDataType());
            deal.setMsgType(msg.getMsgType());
        }
        Producer.RPC_LINK_ID.remove(Thread.currentThread().getName(), linkId);
        return deal;
    }

    QyMsg deal(ChannelHandlerContext ctx, QyMsg msg) {
        QyMsg qyMsg = new QyMsg(MsgType.NORM_MSG, DataType.OBJECT);
        if (MsgType.AC.equals(msg.getMsgType())) {
            qyMsg.putMsg(Constants.invokeSuccess);
            qyMsg.putMsgData(Constants.serviceIdentifierTag, Producer.serviceIdentifierTag);
            return qyMsg;
        }
        if (MsgType.HEART_BEAT.equals(msg.getMsgType())) {
            return null;
        }
        String s = MsgHelper.gainMsg(msg);
        logger.debug("invoke from:{} data:{}", msg.getFrom(), s);
        Bean bean = ROUTING_TABLE.get(s);
        if (bean == null) {
            qyMsg.putMsg(Constants.invokeNoSuch);
            return qyMsg;
        }
        try {
            DataMap dataMap = msg.getDataMap();
            Object[] o = (Object[]) dataMap.get(Constants.parameterList);
            Object invoke = bean.invoke(o);
            qyMsg.putMsg(Constants.invokeSuccess);
            qyMsg.putMsgData(Constants.invokeResult, invoke);
        } catch (Throwable e) {
            Throwable cause = e.getCause();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            cause.printStackTrace(new PrintStream(outputStream));
            String name = cause.getClass().getName();
            String message = cause.getMessage();

            qyMsg.putMsg(Constants.invokeThrowError);
            qyMsg.putMsgData(Constants.invokeResult, outputStream.toString(StandardCharsets.UTF_8));
            qyMsg.putMsgData(Constants.invokeErrorClass, name);
            qyMsg.putMsgData(Constants.invokeErrorMessage, StringUtil.isEmpty(message) ? "" : message);
            serverExceptionHandler.exceptionCallBack(ctx.channel().remoteAddress(), msg, e);
        }
        logger.debug("invoked from:{} {}", msg.getFrom(), s);
        return qyMsg;
    }

}
