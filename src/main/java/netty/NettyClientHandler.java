package netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serialization.RpcResponse;

/**
 * Created by HuHongBo on 2022/11/17.
 * NettyClientHandler用来读取服务端发来的RpcResponse消息对象，并将RpcResponse消息对象保存到AttributeMap上，AttributeMap可以看出是一个Channel的共享数据源
 */
public class NettyClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object message){
        try {
            RpcResponse rpcResponse = (RpcResponse) message;
            logger.info("client receive message:[{}]", rpcResponse.toString());
            //声明一个AttributeKey对象。AttributeMap是一个接口，类似与Map结构
            //Channel实现了AttributeMap接口，所以说Channel具有AttributeMap的属性，每个Channel上的AttributeMap属于共享数据。
            AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse");
            //将服务端的返回结果保存到AttributeMap上，AttributeMap可以看作是一个共享数据源
            //AttributeMap的key是AttributeKey,value是Attribute
            channelHandlerContext.channel().attr(key).set(rpcResponse);
            channelHandlerContext.channel().close();
        }finally {
            ReferenceCountUtil.release(message);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable cause){
        logger.error("client acugh exception", cause);
        channelHandlerContext.close();
    }
}
