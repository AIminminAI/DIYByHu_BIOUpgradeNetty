package codeanddecoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;
import serialization.Serializer;

/**
 * Created by HuHongBo on 2022/11/17.
 * 自定义的编码器。负责出战消息，将消息格式转换为字节数组然后写到字节数据的容器ByteBuf对象中
 * 网络传输需要通过字节流来实现，ByteBuf可以看作是Netty提供的字节数据的容器，使用ByteBuf会让我们更加方便的处理字节数据
 */
@AllArgsConstructor
public class NettyKryoEncoder extends MessageToByteEncoder<Object> {
    private final Serializer serializer;
    private final Class<?> genericClass;


    @Override
    public void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) {
        if (genericClass.isInstance(o)){
            //1.将对象转换为byte
            byte[] body = serializer.serialize(o);
            //2.读取消息的长度
            int dataLength = body.length;
            //3.写入消息对应的字节数组长度，writeIndex+4
            byteBuf.writeInt(dataLength);
            //4.将字节数组写入ByteBuf对象中
            byteBuf.writeBytes(body);
        }
    }
}
