[![Coverage Status](https://coveralls.io/repos/github/Stratio/kafka-elasticsearch-sink/badge.svg?branch=master)](https://coveralls.io/github/Stratio/kafka-elasticsearch-sink?branch=master)

# kafka-elasticsearch-sink

kafka-elasticsearch-sink is a library for generate tasks in Kafka Connect for connecting to Elasticsearch.

 
## Requirements

This library requires Kafka 0.10.0 and Elasticsearch 2.0.2


## Using the library

The ElasticsearchSinkTask can be configured with the following configuration.

  ```
    connector.class=org.apache.kafka.connect.es.KafkaElasticSearchSinkConnector,
    topics=metadata
    action.type=insert,
    elasticsearch.cluster.name=dg-cluster
    elasticsearch.hosts=127.0.0.1:9300
    elasticsearch.index=dg-metadata
    elasticsearch.mapping.type=type-v0
    elasticsearch.bulk.size=100000
  ```

By default the library insert data from Kafka topics, in addiction is possible to create Kafka Connect tasks in order
 to make updates, deletes or upserts in the Elasticsearch indexes.
 
Is necessary add the jar plugin inside the connectors directory and this directory should be added in the classpath:

```
  mkdir ${KAFKA_PATH}/libs_connect
  cp -r target/kafka-elasticsearch-sink-0.0.1-SNAPSHOT-jar-with-dependencies.jar ${KAFKA_PATH}/libs_connect/
  export CLASSPATH=${KAFKA_PATH}/libs_connect/*
```
  
If the user want make persistent this change can add this export in the file ".bashrc"
 

### Build

    `mvn clean package`


