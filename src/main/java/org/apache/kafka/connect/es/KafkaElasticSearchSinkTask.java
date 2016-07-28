package org.apache.kafka.connect.es;

import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.errors.RetriableException;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.kafka.connect.es.KafkaElasticSearchSinkConnectorConfig.*;

import java.net.InetAddress;
import java.util.*;

/**
 * ElasticsearchSinkTask is a Task that takes records loaded from Kafka and sends them to
 * another system.
 */
public class KafkaElasticSearchSinkTask extends SinkTask {

    private static final Logger log = LoggerFactory.getLogger(KafkaElasticSearchSinkTask.class);

    private KafkaElasticSearchSinkConnectorConfig config;
    private Client client;
    private Map<String, String> topicIndexes;
    private Map<String, String> topicMappings;
    private ActionType actionType;
    private Integer bulkSize;
    private String idField;

    public KafkaElasticSearchSinkTask() {
        topicIndexes = new HashMap<>(0);
        topicMappings = new HashMap<>(0);
    }

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

        try {
            config = new KafkaElasticSearchSinkConnectorConfig(props);
        } catch (ConfigException e) {
            throw new ConnectException("Couldn't start " + KafkaElasticSearchSinkConnector.class.getName() + " due to configuration error.", e);
        }

        String clusterName = config.getString(KafkaElasticSearchSinkConnectorConfig.CLUSTER_NAME);
        String hosts = config.getString(KafkaElasticSearchSinkConnectorConfig.HOSTS);
        String topics = config.getString(KafkaElasticSearchSinkConnectorConfig.TOPICS);
        String indexes = config.getString(KafkaElasticSearchSinkConnectorConfig.INDEX);
        String mappingTypes = config.getString(KafkaElasticSearchSinkConnectorConfig.MAPPING_TYPE);
        actionType = config.getActionType(config.getString(KafkaElasticSearchSinkConnectorConfig.ACTION_TYPE));
        idField = config.getString(KafkaElasticSearchSinkConnectorConfig.ID_FIELD);
        bulkSize = config.getInt(KafkaElasticSearchSinkConnectorConfig.BULK_SIZE);

        List<String> hostsList = new ArrayList<>(Arrays.asList(hosts.replaceAll(" ", "").split(",")));
        List<String> topicsList = Arrays.asList(topics.replaceAll(" ", "").split(","));
        List<String> indexesList = Arrays.asList(indexes.replaceAll(" ", "").split(","));
        List<String> mappingTypesList = Arrays.asList(mappingTypes.replaceAll(" ", "").split(","));

        if (topicsList.size() != indexesList.size()) {
            throw new ConnectException("The number of indexes should be the same as the number of topics");
        }

        for (int i = 0; i < topicsList.size(); i++) {
            topicIndexes.put(topicsList.get(i), indexesList.get(i));
            topicMappings.put(topicsList.get(i), mappingTypesList.get(i));
        }

        try {
            Settings settings = Settings.settingsBuilder().put("cluster.name", clusterName).build();
            client = TransportClient.builder().settings(settings).build();
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
                log.info("address " + address + "port " + port);
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
            List<SinkRecord> records = new ArrayList<>(sinkRecords);

            for (int i = 0; i < records.size(); i++) {
                BulkRequestBuilder bulkRequest = client.prepareBulk().setRefresh(Boolean.TRUE);

                for (int j = 0; j < bulkSize && i < records.size(); j++, i++) {
                    SinkRecord record = records.get(i);
                    Map<String, Object> jsonMap = (Map<String, Object>) record.value();

                    if (!jsonMap.isEmpty()) {
                        String index = topicIndexes.get(record.topic());
                        String mappingType = topicMappings.get(record.topic());
                        String id;

                        switch (actionType) {
                            case INSERT:
                                bulkRequest.add(client.prepareIndex(index, mappingType).setSource(jsonMap));
                                break;
                            case DELETE:
                                id = (String) jsonMap.get(idField);
                                bulkRequest.add(Requests.deleteRequest(index).type(mappingType).id(id));
                                break;
                            case UPDATE:
                                id = (String) jsonMap.get(idField);
                                bulkRequest.add(new UpdateRequest(index, mappingType, id).doc(jsonMap));
                                break;
                            case UPSERT:
                                id = (String) jsonMap.get(idField);
                                IndexRequest indexRequest = new IndexRequest(index, mappingType, id).source(jsonMap);
                                bulkRequest.add(new UpdateRequest(index, mappingType, id).doc(jsonMap)
                                        .upsert(indexRequest));
                                break;
                        }
                    }
                }
                i--;
                if (bulkRequest.numberOfActions() > 0) {
                    BulkResponse bulkResponse = bulkRequest.execute().actionGet();
                    if (bulkResponse.hasFailures()) {
                        for (BulkItemResponse item : bulkResponse) {
                            log.error(item.getFailureMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RetriableException("Elasticsearch not connected", e);
        }
    }

    @Override
    public void flush(Map<TopicPartition, OffsetAndMetadata> offsets) {
    }

    @Override
    public void stop() {
        client.close();
    }

    public KafkaElasticSearchSinkConnectorConfig getConfig() {
        return config;
    }

    public void setConfig(KafkaElasticSearchSinkConnectorConfig config) {
        this.config = config;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Map<String, String> getTopicIndexes() {
        return topicIndexes;
    }

    public void setTopicIndexes(Map<String, String> topicIndexes) {
        this.topicIndexes = topicIndexes;
    }

    public Map<String, String> getTopicMappings() {
        return topicMappings;
    }

    public void setTopicMappings(Map<String, String> topicMappings) {
        this.topicMappings = topicMappings;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public Integer getBulkSize() {
        return bulkSize;
    }

    public void setBulkSize(Integer bulkSize) {
        this.bulkSize = bulkSize;
    }

    public String getIdField() {
        return idField;
    }

    public void setIdField(String idField) {
        this.idField = idField;
    }
}