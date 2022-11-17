package netty;

import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serialization.RpcRequest;
import serialization.RpcResponse;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by HuHongBo on 2022/11/17.
 */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);
    private static final AtomicInteger atomicInteger = new AtomicInteger(1);

    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object message){
        try {
            RpcRequest rpcRequest = (RpcRequest) message;
            logger.info("server receive message:[{}], times:[{}]", rpcRequest, atomicInteger.getAndIncrement());
            RpcResponse messageFromServer = RpcResponse.builder().message("message from server").build();
            ChannelFuture channelFuture = channelHandlerContext.writeAndFlush(messageFromServer);
            channelFuture.addListener(ChannelFutureListener.CLOSE);
        } finally {
            ReferenceCountUtil.release(message);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable cause) throws Exception{
        logger.error("server catch exception", cause);
        channelHandlerContext.close();
    }
}
