package netty;

import codeanddecoder.NettyKryoDecoder;
import codeanddecoder.NettyKryoEncoder;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.AttributeKey;
import serialization.KryoSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serialization.RpcRequest;
import serialization.RpcResponse;

import java.lang.reflect.Method;

/**
 * Created by HuHongBo on 2022/11/16.
 * 客户端中有一个向服务端发送RpcRequest类型的消息的sendMessage()方法，通过这个sendMessage()方法可以将RpcRequest类型的消息，也就是RpcRequest对象，发送到服务端，并且可以同步获取到服务端返回的结果，也就是RpcResponse对象
 */
public class NettyClient {
    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    //后期可以将这些信息提取到配置文件中，再引入配置文件即可
    private final String host;
    private final int port;
    //ServerBootstrap 类用于创建服务端实例，Bootstrap 用于创建客户端实例
    private static final Bootstrap b;

    //初始化Netty相关资源比如EventLoopGroup，Bootstrap
    static {
        //用于接收客户端连接的线程池:通常被称为bossGroup, bossGroup的构造方法与处理I/O读写的线程池相同( workerGroup),都是通过new NioEventLoopGroup创建。bossGroup的线程数建议设置为1,因为它仅负责接收客户端的连接，不做复杂的逻辑处理，为了尽可能少地占用资源，它的取值越小越好。
        //通常我们使用如下的方式配置主从 Reactor 线程模型：Netty 的线程池指的就是 NioEventLoopGroup 的实例；线程池中的单个线程，指的是NioEventLoop 的实例。NioEventLoopGroup 有多个构造方法用于参数设置，最简单地，我们采用无参构造函数，或仅仅设置线程数量就可以了，其他的参数采用默认值。线程池 NioEventLoopGroup 中的每一个线程 NioEventLoop 也可以当做一个线程池来用，只不过它只有一个线程
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();//Netty 提供了高效的主从 Reactor 多线程模型，主 Reactor 线程负责新的网络连接 Channel 创建，然后把 Channel 注册到从 Reactor，由从 Reactor 线程负责处理后续的 I/O 操作。主从 Reactor 多线程模型很好地解决了高并发场景下单个 NIO 线程无法承载海量客户端连接建立以及 I/O 操作的性能瓶颈。
        //1.首先初始化了一个Bootstrap，算一个启动器，用于创建客户端实例
        b = new Bootstrap();
        KryoSerializer kryoSerializer = new KryoSerializer();
        b.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                //连接的超时时间，超过这个时间还是建立不上的话则代表连接失败
                //如果15秒内没有发送数据给服务端的话就发送一次心跳请求
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel){
                        /**
                         * 自定义序列化编码器
                         */
                        //RpcResponse->ByteBuf
                        socketChannel.pipeline().addLast(new NettyKryoDecoder(kryoSerializer, RpcResponse.class));
                        //ByteBuf->RpcRequest
                        socketChannel.pipeline().addLast(new NettyKryoEncoder(kryoSerializer, RpcRequest.class));
                        socketChannel.pipeline().addLast(new NettyClientHandler());
                    }

                });
    }

    /**
     * 发送消息到服务端
     * @param rpcRequest 消息体
     * @return 服务端返回的数据
     */
    public RpcResponse sendMessage(RpcRequest rpcRequest){
        try {
            //2.通过Bootstrap对象连接服务器
            ChannelFuture channelFuture = b.connect(host, port).sync();
            logger.info("client connect {}", host + ":" + port);
            Channel futureChannel = channelFuture.channel();
            logger.info("send message");
            if (futureChannel != null){
                //3.通过Channel向服务端发送消息RpcRequest
                futureChannel.writeAndFlush(rpcRequest).addListener(future -> {
                    if (future.isSuccess()){
                        logger.info("client send message: [{}]", rpcRequest.toString());
                    }else {
                        logger.error("send failed:", future.cause());
                    }
                });
                //4.过Channel向服务端发送消息RpcRequest,发送成功后阻塞等待，直到Channel关闭
                futureChannel.closeFuture().sync();
                //将服务端返回的数据也就是RpcResponse对象取出，也就是说此时客户端拿到了服务端返回的结果RpcResponse
                AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse");
                //通过channel和key将数据读取出来
                return futureChannel.attr(key).get();
            }
        }catch (InterruptedException e){
            logger.error("occur exception when connect server:", e);
        }
        return null;
    }

    public static void main(String[] args) {
        RpcRequest rpcRequest = RpcRequest.builder()
                .invokeInterfaceName("interface")
                .invokeMethodName("Iam宇宙第一AIywmAI").build();
//                .methodName("Iam宇宙第一AIywmAI").build();
        NettyClient nettyClient = new NettyClient("127.0.0.1", 2221);
        //启动客户端并发送4次消息给服务端
        for (int i = 0; i < 3; i++) {
            nettyClient.sendMessage(rpcRequest);
            System.out.println("第" + i + "次消息为：" + rpcRequest);
        }
        RpcResponse rpcResponse = nettyClient.sendMessage(rpcRequest);
        System.out.println(rpcResponse.toString());
    }

}
