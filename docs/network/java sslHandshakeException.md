# java sslHandshakeException 问题

## 遇到问题：
通过httpClient调用https请求时遇到问题
```shell
Caused by: javax.net.ssl.SSLHandshakeException: No appropriate protocol (protocol is disabled or cipher suites are inappropriate)
	at sun.security.ssl.HandshakeContext.<init>(HandshakeContext.java:171)
	at sun.security.ssl.ClientHandshakeContext.<init>(ClientHandshakeContext.java:98)
	at sun.security.ssl.TransportContext.kickstart(TransportContext.java:220)
	at sun.security.ssl.SSLSocketImpl.startHandshake(SSLSocketImpl.java:428)
	at org.apache.http.conn.ssl.SSLConnectionSocketFactory.createLayeredSocket(SSLConnectionSocketFactory.java:436)
	at org.apache.http.conn.ssl.SSLConnectionSocketFactory.connectSocket(SSLConnectionSocketFactory.java:384)
	at org.apache.http.impl.conn.DefaultHttpClientConnectionOperator.connect(DefaultHttpClientConnectionOperator.java:142)
	at org.apache.http.impl.conn.PoolingHttpClientConnectionManager.connect(PoolingHttpClientConnectionManager.java:374)
	at org.apache.http.impl.execchain.MainClientExec.establishRoute(MainClientExec.java:393)
	at org.apache.http.impl.execchain.MainClientExec.execute(MainClientExec.java:236)
	at org.apache.http.impl.execchain.ProtocolExec.execute(ProtocolExec.java:186)
	at org.apache.http.impl.execchain.RetryExec.execute(RetryExec.java:89)
	at org.apache.http.impl.execchain.RedirectExec.execute(RedirectExec.java:110)
	at org.apache.http.impl.client.InternalHttpClient.doExecute(InternalHttpClient.java:185)
	at org.apache.http.impl.client.CloseableHttpClient.execute(CloseableHttpClient.java:83)
	at org.apache.http.impl.client.CloseableHttpClient.execute(CloseableHttpClient.java:108)
	at com.ruimin.intfin.sdk.tools.HttpClientUtil.post(HttpClientUtil.java:278)
	... 57 common frames omitted
```

## 处理问题：
修改 `/usr/local/java/jdk1.8.0_221/jre/lib/security/java.security`

找出其中的
```shell
jdk.tls.disabledAlgorithms=SSLv3, RC4, DES, MD5withRSA, DH keySize < 1024, \
    EC keySize < 224, 3DES_EDE_CBC, anon, NULL
```

注释掉其中的SSLv3.


## 为什么sslv3会被禁用

SSLv3漏洞（CVE-2014-3566），该漏洞贯穿于所有的SSLv3版本中，利用该漏洞，黑客可以通过中间人攻击等类似的方式(只要劫持到的数据加密两端均使用SSL3.0)，便可以成功获取到传输数据(例如cookies)。针对此漏洞，需要服务器端和客户端均停用SSLv3协议。