package serialization;

import lombok.*;

/**
 * Created by HuHongBo on 2022/11/16.
 * 客户端的Client Stub（client stub）【本地代理模块 Proxy】 接收到调用请求后 Client Stub负责将方法、参数等组装成能够进行网络传输的消息体（序列化），也就是RpcRequest；或者说客户端会通过本地代理模块 Proxy 调用服务端，Proxy 模块收到负责将方法、参数等数据转化成网络字节流
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@ToString
public class RpcRequest {
    private String invokeInterfaceName;
    private String invokeMethodName;
}
