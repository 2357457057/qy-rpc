package top.yqingyu.rpc.producer;

import io.netty.channel.ChannelHandlerContext;
import top.yqingyu.common.qydata.DataMap;
import top.yqingyu.qymsg.DataType;
import top.yqingyu.qymsg.MsgHelper;
import top.yqingyu.qymsg.MsgType;
import top.yqingyu.qymsg.QyMsg;
import top.yqingyu.qymsg.netty.QyMsgServerHandler;
import top.yqingyu.rpc.Dict;

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
        QyMsg deal = deal(ctx, msg);
        deal.setFrom(producer.serverName);
        deal.setDataType(msg.getDataType());
        deal.setMsgType(msg.getMsgType());
        return deal;
    }

    QyMsg deal(ChannelHandlerContext ctx, QyMsg msg) {
        QyMsg qyMsg = new QyMsg(MsgType.NORM_MSG, DataType.OBJECT);
        if (MsgType.AC.equals(msg.getMsgType())) {
            qyMsg.putMsg(Dict.invokeSuccess);
            qyMsg.putMsgData(Dict.serviceIdentifierTag, new String(producer.serviceIdentifierTag, StandardCharsets.UTF_8));
            return qyMsg;
        }

        String s = MsgHelper.gainMsg(msg);
        Bean bean = ROUTING_TABLE.get(s);
        if (bean == null) {
            qyMsg.putMsg(Dict.invokeNoSuch);
            return qyMsg;
        }
        try {
            DataMap dataMap = msg.getDataMap();
            Object[] o = (Object[]) dataMap.get(Dict.parameterList);
            Object invoke = bean.invoke(o);
            qyMsg.putMsg(Dict.invokeSuccess);
            qyMsg.putMsgData(Dict.invokeResult, invoke);
        } catch (Throwable e) {
            qyMsg.putMsg(Dict.invokeThrowError);
            qyMsg.putMsgData(Dict.invokeResult, e);
            serverExceptionHandler.exceptionCallBack(ctx.channel().remoteAddress(), msg, e);
        }
        return qyMsg;
    }
}
