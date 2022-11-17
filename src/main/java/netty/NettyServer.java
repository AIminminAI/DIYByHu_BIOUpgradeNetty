package netty;

import codeanddecoder.NettyKryoDecoder;
import codeanddecoder.NettyKryoEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serialization.KryoSerializer;
import serialization.RpcRequest;
import serialization.RpcResponse;

/**
 * Created by HuHongBo on 2022/11/17.
 * NettyServer就是用来开启一个服务器，用于接受客户端的请求，然后处理请求
 */
public class NettyServer {
    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);
    private final int port;

    private NettyServer(int port){
        this.port = port;
    }

    private void run(){
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        KryoSerializer kryoSerializer = new KryoSerializer();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                            .channel(NioServerSocketChannel.class)
                            //TCP默认开启了Nagle算法，该算法的作用是尽可能的发送大数据块减少网络传输的次数。TCP_NODELAY参数的作用就是控制是否启用Nagle算法
                            .childOption(ChannelOption.TCP_NODELAY, true)
                            //是否开启TCP底层心跳机制
                            .childOption(ChannelOption.SO_KEEPALIVE, true)
                            //表示系统用于临时存放已完成三次握手的请求的队列的最大长度，如果连接建立频繁，服务器处理创建连接较慢，可以适当调大这个参数
                            .option(ChannelOption.SO_BACKLOG, 128)
                            .handler(new LoggingHandler(LogLevel.INFO))
                            .childHandler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel ch){
                                    ch.pipeline().addLast(new NettyKryoDecoder(kryoSerializer, RpcRequest.class));
                                    ch.pipeline().addLast(new NettyKryoEncoder(kryoSerializer, RpcResponse.class));
                                    ch.pipeline().addLast(new NettyServerHandler());
                                }
                            });
            //绑定端口，同步等待绑定成功
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            //等待服务端监听端口关闭
            channelFuture.channel().closeFuture().sync();
        }catch (InterruptedException e){
            logger.error("occur exception when start server:", e);
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new NettyServer(2221).run();
    }

}
