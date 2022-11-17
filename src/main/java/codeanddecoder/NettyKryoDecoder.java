package codeanddecoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serialization.Serializer;

import java.util.List;

/**
 * Created by HuHongBo on 2022/11/17.
 * NettyKryoDecoder是我们自定义的解码器，负责处理入战消息，NettyKryoDecoder会从ByteBuf中读取到业务对象对应的字节序列，然后再将字节序列转换为我们的业务对象
 */
@AllArgsConstructor
@Slf4j
public class NettyKryoDecoder extends ByteToMessageDecoder {
    private final Serializer serializer;
    private final Class<?> genericClass;

    /**
     * Netty传输的消息长度也就是对象序列化后对应的字节数组的大小，存储在ByteBuf头部
     */
    private static final int BODY_LENGTH = 4;

    /**
     * 用来解码ByteBuf对象
     * @param channelHandlerContext 解码器关联的ChannelHandlerContext对象
     * @param in 入战数据，也就是ByteBuf对象
     * @param out 解码之后的数据对象需要添加到out对象里面
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
        //1.byteBuf中写入的消息长度所占的字节数已经是4了，所以byteBuf的可读字节必须大于4
        if (in.readableBytes() >= BODY_LENGTH){
            //2.标记当前readIndex的位置，一边日后重置readIndex的时候用
            in.markReaderIndex();
            //3.读取消息的长度。消息长度是encode的时候我们自己写入的，NettyKryoEncoder中encode中写到了
            int dataLength = in.readInt();
            //4.遇到不合理的情况直接return
            if (dataLength < 0 || in.readableBytes() < 0){
                log.error("data length or byteBuf readableBytes is not valid");
                return;
            }
            //5.如果可读字节数小于消息长度的话，说明是不完整的消息，就需要重置readIndex
            if (in.readableBytes() < dataLength){
                in.resetReaderIndex();
                return;
            }
            //6.走到这一步就说明没什么字节数组相关问题了，可以序列化了
            byte[] body = new byte[dataLength];
            in.readBytes(body);
            Object obj = serializer.deserialize(body, genericClass);
            out.add(obj);
            log.info("successful decode ByteBuf to Object");
        }
    }
}
