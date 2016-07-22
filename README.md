# kafka-elasticsearch-sink

KafkaConnect (CopyCat) for writing data to ElasticSearch. The ElasticSearchSinkTask can be configured with the following configuration.

<pre>
<code>
topics=health<br/>
elasticsearch.cluster.name=dg-cluster<br/>
elasticsearch.hosts=127.0.0.1:9300<br/>
elasticsearch.index=test<br/>
elasticsearch.document.name=type1<br/>
elasticsearch.bulk.size=1000000<br/>
</code>
</pre>
