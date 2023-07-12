package top.yqingyu.rpc.producer;

import io.netty.channel.ChannelHandlerContext;
import top.yqingyu.common.qydata.DataMap;
import top.yqingyu.qymsg.DataType;
import top.yqingyu.qymsg.MsgHelper;
import top.yqingyu.qymsg.MsgType;
import top.yqingyu.qymsg.QyMsg;
import top.yqingyu.qymsg.netty.QyMsgServerHandler;
import top.yqingyu.rpc.Constants;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

public class RpcHandler extends QyMsgServerHandler {
    final ConcurrentHashMap<String, Bean> ROUTING_TABLE;
    final ServerExceptionHandler serverExceptionHandler;
    final Producer producer;

    public RpcHandler(Producer producer) {
        ROUTING_TABLE = producer.ROUTING_TABLE;
        this.serverExceptionHandler = producer.exceptionHandler;
        this.producer = producer;
    }

    @Override
    protected QyMsg handle(ChannelHandlerContext ctx, QyMsg msg) throws Exception {
        String linkId = MsgHelper.gainMsgValue(msg, Constants.linkId);
        linkId = linkId == null ? msg.getFrom() : linkId;
        Producer.RPC_LINK_ID.put(Thread.currentThread(), linkId);
        QyMsg deal = deal(ctx, msg);
        if (deal != null) {
            deal.setFrom(producer.serverName);
            deal.setDataType(msg.getDataType());
            deal.setMsgType(msg.getMsgType());
        }
        return deal;
    }

    QyMsg deal(ChannelHandlerContext ctx, QyMsg msg) {
        QyMsg qyMsg = new QyMsg(MsgType.NORM_MSG, DataType.OBJECT);
        if (MsgType.AC.equals(msg.getMsgType())) {
            qyMsg.putMsg(Constants.invokeSuccess);
            qyMsg.putMsgData(Constants.serviceIdentifierTag, new String(producer.serviceIdentifierTag, StandardCharsets.UTF_8));
            return qyMsg;
        }
        if (MsgType.HEART_BEAT.equals(msg.getMsgType())) {
            return null;
        }
        String s = MsgHelper.gainMsg(msg);
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
            qyMsg.putMsg(Constants.invokeThrowError);
            qyMsg.putMsgData(Constants.invokeResult, e);
            serverExceptionHandler.exceptionCallBack(ctx.channel().remoteAddress(), msg, e);
        }
        return qyMsg;
    }
}
