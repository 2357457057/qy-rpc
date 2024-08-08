package top.yqingyu.rpc.producer;

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yqingyu.common.qydata.DataMap;
import top.yqingyu.common.utils.StringUtil;
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
        try {
            ProducerCtx.newInstance(linkId);
            QyMsg deal = deal(ctx, msg);
            if (deal != null) {
                deal.setFrom(producer.serverName);
                deal.setDataType(msg.getDataType());
                deal.setMsgType(msg.getMsgType());
            }
            return deal;
        } finally {
            ProducerCtx.remove();
        }
    }

    QyMsg deal(ChannelHandlerContext ctx, QyMsg msg) {
        QyMsg rtnMsg = new QyMsg(MsgType.NORM_MSG, msg.getDataType());
        if (MsgType.AC.equals(msg.getMsgType())) {
            rtnMsg.putMsg(Constants.invokeSuccess);
            rtnMsg.putMsgData(Constants.serviceIdentifierTag, Producer.serviceIdentifierTag);
            if (logger.isInfoEnabled())
                logger.info("client connected from :{}", msg.getFrom());
            return rtnMsg;
        }
        if (MsgType.HEART_BEAT.equals(msg.getMsgType())) {
            return null;
        }
        ProducerCtx producerCtx = ProducerCtx.getCtx();
        producerCtx.invokeStr = MsgHelper.gainMsg(msg);
        String from = msg.getFrom();
        try {
            producerCtx.from = from;
            Bean bean = ROUTING_TABLE.get(producerCtx.invokeStr);
            producerCtx.bean = bean;
            if (bean == null) {
                producer.INVOKE_NO_SUCH.get().invoke();
                rtnMsg.putMsg(Constants.invokeNoSuch);
                return rtnMsg;
            }
            DataMap dataMap = msg.getDataMap();
            producerCtx.args = (Object[]) dataMap.get(Constants.parameterList);
            bean.invoke();
            rtnMsg.putMsg(Constants.invokeSuccess);
            rtnMsg.putMsgData(Constants.invokeResult, producerCtx.rtn);
        } catch (Throwable e) {
            Throwable cause = e.getCause();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            cause.printStackTrace(new PrintStream(outputStream));
            String name = cause.getClass().getName();
            String message = cause.getMessage();
            rtnMsg.putMsg(Constants.invokeThrowError);
            rtnMsg.putMsgData(Constants.invokeResult, outputStream.toString(StandardCharsets.UTF_8));
            rtnMsg.putMsgData(Constants.invokeErrorClass, name);
            rtnMsg.putMsgData(Constants.invokeErrorMessage, StringUtil.isEmpty(message) ? "" : message);
            serverExceptionHandler.exceptionCallBack(ctx.channel().remoteAddress(), msg, e);
        }
        return rtnMsg;
    }

}
