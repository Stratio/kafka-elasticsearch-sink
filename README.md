[![Coverage Status](https://coveralls.io/repos/github/Stratio/kafka-elasticsearch-sink/badge.svg?branch=master)](https://coveralls.io/github/Stratio/kafka-elasticsearch-sink?branch=master)

# kafka-elasticsearch-sink

kafka-elasticsearch-sink is a library for generate tasks in Kafka Connect for connecting to ElasticSearch.

 
## Requirements

This library requires Kafka 0.10.0 and ElasticSearch 2.0.2


## Using the library

The ElasticSearchSinkTask can be configured with the following configuration.

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
 to make updates, deletes or upserts in the ElasticSearch indexes.
 
 
### Build

    `mvn clean package`


# License #

Licensed to STRATIO (C) under one or more contributor license agreements.
See the NOTICE file distributed with this work for additional information
regarding copyright ownership.  The STRATIO (C) licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
