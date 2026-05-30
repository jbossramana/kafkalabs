Download & Install openssl
https://slproweb.com/products/Win32OpenSSL.html


PART 1 — Create Certificates (Windows CMD)
SET VALIDITY=1825
SET HOST=localhost
SET SSLPASS=kafka@123
SET USERNAME=acladmin
SET CLIENTPASS=client@123

1️⃣ Create Certificate Authority
openssl req -new -x509 -keyout ca-key -out ca-cert -days %VALIDITY% -passout pass:%SSLPASS% -subj "/C=XX/ST=kafka/L=kafka/O=kafka/OU=kafka/CN=MyKafkaCA"

2️⃣ Create Server Keystore
keytool -genkey -alias %HOST% -keystore kafka.server.keystore.jks -storepass %SSLPASS% -keypass %SSLPASS% -validity %VALIDITY% -keyalg RSA -dname "CN=%HOST%, OU=kafka, O=kafka, L=kafka, ST=kafka, C=XX"

3️⃣ Import CA into Server Truststore
keytool -import -alias CARoot -file ca-cert -keystore kafka.server.truststore.jks -storepass %SSLPASS% -noprompt

4️⃣ Create CSR from Server Keystore
keytool -certreq -alias %HOST% -file server.csr -keystore kafka.server.keystore.jks -storepass %SSLPASS%

5️⃣ Sign Server Certificate with CA
openssl x509 -req -CA ca-cert -CAkey ca-key -in server.csr -out server-signed.crt -days %VALIDITY% -CAcreateserial -passin pass:%SSLPASS%

6️⃣ Import CA + Signed Cert into Server Keystore
keytool -import -alias CARoot -file ca-cert -keystore kafka.server.keystore.jks -storepass %SSLPASS% -noprompt
keytool -import -alias %HOST% -file server-signed.crt -keystore kafka.server.keystore.jks -storepass %SSLPASS% -noprompt

🧑‍💻 PART 2 — Create Client Certificates (Mutual TLS)

7️⃣ Create Client Keystore
keytool -genkey -alias %USERNAME% -keystore kafka.client.keystore.jks -storepass %CLIENTPASS% -keypass %CLIENTPASS% -validity %VALIDITY% -keyalg RSA -dname "CN=%USERNAME%, OU=kafka, O=kafka, L=kafka, ST=kafka, C=XX"

8️⃣ Import CA into Client Truststore
keytool -import -alias CARoot -file ca-cert -keystore kafka.client.truststore.jks -storepass %CLIENTPASS% -noprompt

9️⃣ Create CSR from Client
openssl x509 -req -CA ca-cert -CAkey ca-key -in client.csr -out client-signed.crt -days %VALIDITY% -CAcreateserial -passin pass:%SSLPASS%

🔟 Sign Client CSR
openssl x509 -req -CA ca-cert -CAkey ca-key -in client.csr -out client-signed.crt -days %VALIDITY% -CAcreateserial -passin pass:%SSLPASS%


server.properties
=================

listeners=SSL://:9092,CONTROLLER://:9093
advertised.listeners=SSL://localhost:9092,CONTROLLER://localhost:9093
inter.broker.listener.name=SSL

ssl.keystore.location=D:/newkafka/ssl/kafka.server.keystore.jks
ssl.keystore.password=kafka@123
ssl.key.password=kafka@123

ssl.truststore.location=D:/newkafka/ssl/kafka.server.truststore.jks
ssl.truststore.password=kafka@123

ssl.client.auth=required
ssl.protocol=TLSv1.2
ssl.enabled.protocols=TLSv1.2,TLSv1.3

# Required for self-signed CN mismatch
ssl.endpoint.identification.algorithm=

