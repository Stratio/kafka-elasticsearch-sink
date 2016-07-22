package org.apache.kafka.connect.es;

import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.errors.RetriableException;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.*;

/**
 * ElasticsearchSinkTask is a Task that takes records loaded from Kafka and sends them to
 * another system.
 *
 */
public class KafkaElasticSearchSinkTask extends SinkTask {

    static final Logger log = LoggerFactory.getLogger(KafkaElasticSearchSinkTask.class);

    Integer bulkSize;
    String documentName;
    Client client;
    Map<String, String> mapping;

    @Override
    public String version() {
        return new KafkaElasticSearchSinkConnector().version();
    }

    /**
     * Start the Task. Handles configuration parsing and one-time setup of the task.
     *
     * @param props initial configuration
     */
    @Override
    public void start(Map<String, String> props) {

        mapping = new HashMap<>(0);
        String clusterName = props.get(KafkaElasticSearchSinkConnectorConfig.CLUSTER_NAME);
        String hosts = props.get(KafkaElasticSearchSinkConnectorConfig.HOSTS);
        documentName = props.get(KafkaElasticSearchSinkConnectorConfig.DOCUMENT_NAME);
        String topics = props.get(KafkaElasticSearchSinkConnectorConfig.TOPICS);
        String indexes = props.get(KafkaElasticSearchSinkConnectorConfig.INDEX);

        try {
            bulkSize = Integer.parseInt(props.get(KafkaElasticSearchSinkConnectorConfig.BULK_SIZE));
        } catch (Exception e) {
            throw new ConnectException("Setting elasticsearch.bulk.size should be an integer");
        }

        List<String> hostsList = new ArrayList<>(Arrays.asList(hosts.replaceAll(" ", "").split(",")));
        List<String> topicsList = Arrays.asList(topics.replaceAll(" ", "").split(","));
        List<String> indexesList = Arrays.asList(indexes.replaceAll(" ", "").split(","));

        if (topicsList.size() != indexesList.size()) {
            throw new ConnectException("The number of indexes should be the same as the number of topics");
        }

        for (int i = 0; i < topicsList.size(); i++) {
            mapping.put(topicsList.get(i), indexesList.get(i));
        }

        try {
            Settings settings = Settings.settingsBuilder()
                    .put("cluster.name", clusterName).build();

            client = TransportClient.builder().settings(settings).build();
            log.info("topic"+ topics);
            for (String host : hostsList) {
                String address;
                Integer port;
                String[] hostArray = host.split(":");
                address = hostArray[0];

                try {
                    port = Integer.parseInt(hostArray[1]);
                } catch (Exception e) {
                    port = 9300;
                }
                log.info("address "+address+"port "+port);
                ((TransportClient) client).addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(address), port));
            }
        } catch (Exception e) {
            throw new ConnectException("Impossible to connect to hosts");
        }

    }

    /**
     * Put the records in the sink.
     *
     * @param sinkRecords the set of records to send.
     */
    @Override
    public void put(Collection<SinkRecord> sinkRecords) {
        try {
            List<SinkRecord> records = new ArrayList<SinkRecord>(sinkRecords);
            for (int i = 0; i < records.size(); i++) {
                BulkRequestBuilder bulkRequest = client.prepareBulk().setRefresh(Boolean.TRUE);
                for (int j = 0; j < bulkSize && i < records.size(); j++, i++) {
                    SinkRecord record = records.get(i);

                    Map<String, Object> jsonMap = ( Map<String, Object>)record.value();

                    String index = mapping.get(record.topic());

                    bulkRequest.add(
                            client.prepareIndex(index, documentName).setSource(jsonMap)
                    );
                }
                i--;
                BulkResponse bulkResponse = bulkRequest.execute().actionGet();
                if (bulkResponse.hasFailures()) {
                    for (BulkItemResponse item : bulkResponse) {
                        log.error(item.getFailureMessage());
                    }
                }
            }
        } catch (Exception e) {
            throw new RetriableException("Elasticsearch not connected",e);
        }
    }

    @Override
    public void flush(Map<TopicPartition, OffsetAndMetadata> offsets) {}

    @Override
    public void stop() {
        //close connection
        client.close();
    }
}