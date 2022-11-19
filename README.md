# DIYByHu_BIOUpgradeNetty
把前面的while家族的BIO升级一下，用用Netty


根据日志捋一下，发了4次消息。客户端与服务端交互状态
![Nettyclient-server日志](https://user-images.githubusercontent.com/72067353/202409803-943879b2-e8ca-4464-a89b-2871a9d572f9.png)


选用的序列化方式：Kryo
Kryo is a Java serialization framework with a focus on speed, efficiency, and a user-friendly API.
Steps for usage:
1.The first thing we need to do is to add the kryo dependency to our pom.xml.The latest version of this artifact can be found on Maven Central[https://search.maven.org/search?q=g:com.esotericsoftware%20AND%20a:kryo].
![image](https://user-images.githubusercontent.com/72067353/202830378-bb3dc538-e2c5-4e17-b826-928d74e1da45.png)

2.
