package serialization;

import lombok.*;

/**
 * Created by HuHongBo on 2022/11/16.
 * 服务端server stub（桩）得到方法执行结果并 将执行结果组装成能够进行网络传输的消息体RpcResponse（序列化）并发送至消费方
 * 客户端 Stub （client stub）接收到RpcResponse类型的消息后并将RpcResponse类型的消息反序列化为Java对象，这样也就得到了最终远程调用的结果
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@ToString
public class RpcResponse {
    private String message;
}
