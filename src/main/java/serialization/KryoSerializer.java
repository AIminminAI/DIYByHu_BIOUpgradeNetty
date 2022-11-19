package serialization;

import serialization.exception.SerializeException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Created by HHB on 2022/11/17.
 * 自定义的kryo序列化实现类
 */
public class KryoSerializer implements Serializer{
    /**
     * 由于Kryo不是线程安全的，所以每个线程都应该有自己的Kryo，Input和Output实例
     */
    //Java8中ThreadLocal对象提供了一个Lambda构造方式，实现了非常简洁的构造方法：withInitial。这个方法采用Lambda方式传入实现了 Supplier 函数接口的参数。用ThreadLocal作为容器，当每个线程访问这个 kryoThreadLocal 变量时，ThreadLocal会为每个线程提供一份kryoThreadLocal变量，就可以保证各个线程互不影响
    //其实ThreadLocal.withInitial(()...)感觉就跟ThreadLocal t = new ThreadLocal要表达的意思一样
    private static final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(()->{
        Kryo kryo = new Kryo();
        //If we need more control over the serialization process, we have two options; we can write our own Serializer class and register it with Kryo or let the class handle the serialization by itself.
        kryo.register(RpcResponse.class);
        kryo.register(RpcRequest.class);
        kryo.setReferences(true);//默认值为true，表示是否关闭注册行为，关闭之后可能存在序列化问题，一般推荐设置为true
        kryo.setRegistrationRequired(false);//默认值为false，是否关闭循环引用，可以提高性能，但是一般不推荐设置为true
        return kryo;
    });

    @Override
    public byte[] serialize(Object obj){
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Output output = new Output(byteArrayOutputStream);
            Kryo kryo = kryoThreadLocal.get();
            //Object->byte:将对象序列化为byte数组
            kryo.writeObject(output, obj);
            kryoThreadLocal.remove();
            return output.toBytes();
        }catch (Exception e){
            throw new SerializeException("序列化失败");
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz){
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            Input input = new Input(byteArrayInputStream);
            Kryo kryo = kryoThreadLocal.get();
            //byte->Object:从byte数组中反序列化出对象
            Object o = kryo.readObject(input, clazz);
            kryoThreadLocal.remove();
            return clazz.cast(o);
        }catch (Exception e){
            throw new SerializeException("反序列化失败");
        }
    }

}
