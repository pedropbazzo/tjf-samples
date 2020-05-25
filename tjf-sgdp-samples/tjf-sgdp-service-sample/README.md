# SGDP - Sistema de Gestão de Dados Pessoais

## Contexto

Para exemplificar a biblioteca **SGDP** vamos utilizar o mesmo projeto criado para o _sample_ da biblioteca **API JPA** disponível em nosso [GitHub][tjf-api-jpa-sample].

## Começando com SGDP

Com o projeto _sample_ **API JPA** em mãos, vamos alterar alguns campos no banco de dados e incluir as anotações disponibilizadas pelo SGDP na classe de entidade.

> Como _engine_ de banco de dados utilizaremos o [H2][h2].

## Dependências

Incluir a biblioteca como dependência no `pom.xml` da aplicação:

```xml
<dependency>
    <groupId>com.totvs.tjf</groupId>
    <artifactId>tjf-sgdp-core</artifactId>
</dependency>
```

Além das propriedades existentes no `application.yml` da aplicação, será necessário incluir novas propriedades. Conforme o exemplo:

```yml
cloud:
  stream:

    kafka:
      binder:
        brokers: localhost:9092
        configuration:
          auto:
            offset:
              reset: earliest

    binders:
      kafka1:
        type: kafka
      rabbit1:
        type: rabbit
        environment:
          spring:
            habbit:
              host: localhost:5672

    bindings:
      sgdp-kafka-reader:
        destination: sgdp-audit
        binder: kafka1
      sgdp-audit:
        destination: sgdp-audit
        contentType: application/json
        binder: kafka1

      sgdp-input:
        destination: sgdp-responses
        group: sw-sgdp-service
        binder: rabbit1
      sgdp-output:
        destination: sgdp-commands
        binder: rabbit1
```

## Auditoria

Para realizar a auditoria das informações, precisaremos subir uma imagem do Kafka no docker e para isso temos um `docker-compose.yml` na raiz desse projeto.

docker-compose.yml:

```yml
version: '2'
services:

    zoo1:
        image: zookeeper:3.4.9
        hostname: zoo1
        ports:
          - "2181:2181"
        environment:
            ZOO_MY_ID: 1
            ZOO_PORT: 2181
            ZOO_SERVERS: server.1=zoo1:2888:3888

    kafka1:
        image: confluentinc/cp-kafka:5.3.1
        hostname: kafka1
        ports:
          - "9092:9092"
        environment:
          KAFKA_ADVERTISED_LISTENERS: LISTENER_DOCKER_INTERNAL://kafka1:19092,LISTENER_DOCKER_EXTERNAL://127.0.0.1:9092
          KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: LISTENER_DOCKER_INTERNAL:PLAINTEXT,LISTENER_DOCKER_EXTERNAL:PLAINTEXT
          KAFKA_INTER_BROKER_LISTENER_NAME: LISTENER_DOCKER_INTERNAL
          KAFKA_ZOOKEEPER_CONNECT: "zoo1:2181"
          KAFKA_BROKER_ID: 1
          KAFKA_LOG4J_LOGGERS: "kafka.controller=INFO,kafka.producer.async.DefaultEventHandler=INFO,state.change.logger=INFO"
          KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
        depends_on:
          - zoo1
```

Apenas para validação, implementaremos duas classes para verificar o envio das mensagens para o Kafka.

**SGDPKafkaReader.java** - Utilizada como exchange para definir a fila de leitura.

```java
package br.com.star.wars;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface SGDPKafkaReader {
	
	String INPUT = "sgdp-kafka-reader";

	@Input(SGDPKafkaReader.INPUT)
	SubscribableChannel input();
    
}
```

**Subscriber.java** - Servirá como _listener_ para interceptarmos as mensagens que são enviadas para o Kafka.

```java
package br.com.star.wars;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;
import org.springframework.scheduling.annotation.Async;

@Async
@EnableBinding(SGDPKafkaReader.class)
public class Subscriber {
	
	@StreamListener(target = SGDPKafkaReader.INPUT)
	public void listener(Message<?> message) {
		System.out.println("#################### MENSAGEM ENVIADA ######################");
		System.out.println(message);
	}
}
```

## Hora de testar

> Os endpoints podem ser testados também via **Swagger-UI** pela url `localhost:9190/swagger-ui.html`.

**`Registro de tratamento de dados pessoais`**

Ao efetuar um GET no endpoint `localhost:8180/jedi/v1/jedis` da aplicação **sw-sgdp-app**, teremos as seguintes mensagens no log do serviço **sw-sgdp-service**.

```
#################### MENSAGEM ENVIADA ######################
GenericMessage [payload={"uuid":"9ab443bf-e0b4-4d1a-b49d-f28a1677a0b7","tenantId":null,"timestamp":"2020-05-25T14:34:02.971331","user":"anonymousUser","code":"","model":"com.tjf.sample.github.model.Jedi","key":{},"action":"LOAD","data":{"id":1,"name":"Qui-Gon Jinn","identification":7777,"email":"jinn@space.com","gender":"male"}}, headers={kafka_offset=0, scst_nativeHeadersPresent=true, kafka_consumer=org.apache.kafka.clients.consumer.KafkaConsumer@177cae96, deliveryAttempt=1, kafka_timestampType=CREATE_TIME, kafka_receivedMessageKey=null, kafka_receivedPartitionId=0, contentType=application/json, kafka_receivedTopic=sgdp-audit, kafka_receivedTimestamp=1590428043064, kafka_groupId=anonymous.effc3a52-1c60-4993-b75d-625670914f40}]
#################### MENSAGEM ENVIADA ######################
GenericMessage [payload={"uuid":"9609c97f-98a1-4092-b8db-5b75ff8e4325","tenantId":null,"timestamp":"2020-05-25T14:34:03.074585","user":"anonymousUser","code":"","model":"com.tjf.sample.github.model.Jedi","key":{},"action":"LOAD","data":{"id":2,"name":"Obi-Wan Kenobi","identification":123,"email":"obi@jedi.com","gender":"male"}}, headers={kafka_offset=1, scst_nativeHeadersPresent=true, kafka_consumer=org.apache.kafka.clients.consumer.KafkaConsumer@177cae96, deliveryAttempt=1, kafka_timestampType=CREATE_TIME, kafka_receivedMessageKey=null, kafka_receivedPartitionId=0, contentType=application/json, kafka_receivedTopic=sgdp-audit, kafka_receivedTimestamp=1590428043075, kafka_groupId=anonymous.effc3a52-1c60-4993-b75d-625670914f40}]
#################### MENSAGEM ENVIADA ######################
GenericMessage [payload={"uuid":"d90c067c-04bd-4385-8e18-c75dc95f9346","tenantId":null,"timestamp":"2020-05-25T14:34:03.075594","user":"anonymousUser","code":"","model":"com.tjf.sample.github.model.Jedi","key":{},"action":"LOAD","data":{"id":3,"name":"Anakin Skywalker","identification":6666,"email":"darkin@space.com","gender":"male"}}, headers={kafka_offset=2, scst_nativeHeadersPresent=true, kafka_consumer=org.apache.kafka.clients.consumer.KafkaConsumer@177cae96, deliveryAttempt=1, kafka_timestampType=CREATE_TIME, kafka_receivedMessageKey=null, kafka_receivedPartitionId=0, contentType=application/json, kafka_receivedTopic=sgdp-audit, kafka_receivedTimestamp=1590428043076, kafka_groupId=anonymous.effc3a52-1c60-4993-b75d-625670914f40}]
#################### MENSAGEM ENVIADA ######################
GenericMessage [payload={"uuid":"babbc9f3-7228-48e1-87c0-ac25b3ac831d","tenantId":null,"timestamp":"2020-05-25T14:34:03.076826","user":"anonymousUser","code":"","model":"com.tjf.sample.github.model.Jedi","key":{},"action":"LOAD","data":{"id":4,"name":"Yoda","identification":1,"email":"yoda@space.com","gender":"male"}}, headers={kafka_offset=3, scst_nativeHeadersPresent=true, kafka_consumer=org.apache.kafka.clients.consumer.KafkaConsumer@177cae96, deliveryAttempt=1, kafka_timestampType=CREATE_TIME, kafka_receivedMessageKey=null, kafka_receivedPartitionId=0, contentType=application/json, kafka_receivedTopic=sgdp-audit, kafka_receivedTimestamp=1590428043077, kafka_groupId=anonymous.effc3a52-1c60-4993-b75d-625670914f40}]
#################### MENSAGEM ENVIADA ######################
GenericMessage [payload={"uuid":"fd301fd4-ef4f-4834-89d0-dad44bc2fcd3","tenantId":null,"timestamp":"2020-05-25T14:34:03.077745","user":"anonymousUser","code":"","model":"com.tjf.sample.github.model.Jedi","key":{},"action":"LOAD","data":{"id":5,"name":"Mace Windu","identification":2341,"email":"mace_windu@jedi.com","gender":"male"}}, headers={kafka_offset=4, scst_nativeHeadersPresent=true, kafka_consumer=org.apache.kafka.clients.consumer.KafkaConsumer@177cae96, deliveryAttempt=1, kafka_timestampType=CREATE_TIME, kafka_receivedMessageKey=null, kafka_receivedPartitionId=0, contentType=application/json, kafka_receivedTopic=sgdp-audit, kafka_receivedTimestamp=1590428043078, kafka_groupId=anonymous.effc3a52-1c60-4993-b75d-625670914f40}]
#################### MENSAGEM ENVIADA ######################
GenericMessage [payload={"uuid":"4735b9f5-7c6c-4809-ad79-1143c2546d39","tenantId":null,"timestamp":"2020-05-25T14:34:03.078533","user":"anonymousUser","code":"","model":"com.tjf.sample.github.model.Jedi","key":{},"action":"LOAD","data":{"id":6,"name":"Count Dooku","identification":2431,"email":"dooku@dark.com","gender":"male"}}, headers={kafka_offset=5, scst_nativeHeadersPresent=true, kafka_consumer=org.apache.kafka.clients.consumer.KafkaConsumer@177cae96, deliveryAttempt=1, kafka_timestampType=CREATE_TIME, kafka_receivedMessageKey=null, kafka_receivedPartitionId=0, contentType=application/json, kafka_receivedTopic=sgdp-audit, kafka_receivedTimestamp=1590428043078, kafka_groupId=anonymous.effc3a52-1c60-4993-b75d-625670914f40}]
#################### MENSAGEM ENVIADA ######################
GenericMessage [payload={"uuid":"d69d4eec-a4d6-4784-83dd-77478dc1cb5c","tenantId":null,"timestamp":"2020-05-25T14:34:03.085006","user":"anonymousUser","code":"","model":"com.tjf.sample.github.model.Jedi","key":{},"action":"LOAD","data":{"id":7,"name":"Luke Skywalker","identification":8543,"email":"luke@jedi.com","gender":"male"}}, headers={kafka_offset=6, scst_nativeHeadersPresent=true, kafka_consumer=org.apache.kafka.clients.consumer.KafkaConsumer@177cae96, deliveryAttempt=1, kafka_timestampType=CREATE_TIME, kafka_receivedMessageKey=null, kafka_receivedPartitionId=0, contentType=application/json, kafka_receivedTopic=sgdp-audit, kafka_receivedTimestamp=1590428043085, kafka_groupId=anonymous.effc3a52-1c60-4993-b75d-625670914f40}]
#################### MENSAGEM ENVIADA ######################
GenericMessage [payload={"uuid":"9d8683c2-3378-4b83-b598-52878fb55fb0","tenantId":null,"timestamp":"2020-05-25T14:34:03.085664","user":"anonymousUser","code":"","model":"com.tjf.sample.github.model.Jedi","key":{},"action":"LOAD","data":{"id":8,"name":"Rey","identification":3421,"email":"rey@space.com","gender":"female"}}, headers={kafka_offset=7, scst_nativeHeadersPresent=true, kafka_consumer=org.apache.kafka.clients.consumer.KafkaConsumer@177cae96, deliveryAttempt=1, kafka_timestampType=CREATE_TIME, kafka_receivedMessageKey=null, kafka_receivedPartitionId=0, contentType=application/json, kafka_receivedTopic=sgdp-audit, kafka_receivedTimestamp=1590428043086, kafka_groupId=anonymous.effc3a52-1c60-4993-b75d-625670914f40}]
```

**`Consulta de dados pessoais`**

Para efetuar a consulta dos dados pessoais de um titular deve ser requisitada a url `localhost:9190/sgdp/v1/data?identification=1` informando a identificação do Jedi.

Os dados podem ser verificados no log do serviço.

**`Anonimização de dados pessoais`**

Para efetuar a anonimização dos dados pessoais de um titular deve ser requisitada a url `localhost:9190/sgdp/v1/mask?identification=1` informando a identificação do Jedi.

Os dados podem ser verificados no log do serviço.

## Isso é tudo pessoal!

Com isso terminamos nosso _sample_, fique a vontade para enriquecê-lo utilizando outros recursos propostos pelo componente [SGDP][tjf-sgdp] e enviar sugestões e melhorias para o projeto **TOTVS Java Framework**.

[tjf-api-jpa-sample]: https://github.com/totvs/tjf-samples/tree/master/tjf-api-samples/tjf-api-jpa-sample
[h2]: https://www.h2database.com
[tjf-sgdp]: https://tjf.totvs.com.br/wiki/tjf-sgdp-core
