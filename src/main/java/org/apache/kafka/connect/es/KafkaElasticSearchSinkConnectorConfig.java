package org.apache.kafka.connect.es;

import java.util.Map;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;


public class KafkaElasticSearchSinkConnectorConfig extends AbstractConfig {

    public static final String CLUSTER_NAME = "elasticsearch.cluster.name";
    public static final String CLUSTER_NAME_DOC = "A comma separated Elastic search cluster including port with a :. For example localhost:9300";

    public static final String HOSTS = "elasticsearch.hosts";
    public static final String HOSTS_DOC = "elasticsearch.hosts";

    public static final String BULK_SIZE = "elasticsearch.bulk.size";
    public static final String BULK_SIZE_DOC = "The number of messages to be bulk indexed into elasticsearch.";

    public static final String DOCUMENT_NAME = "elasticsearch.document.name";
    public static final String DOCUMENT_NAME_DOC = "elasticsearch.document.name";

    public static final String TOPICS = "topics";
    public static final String TOPICS_DOC = "topics of kafka";

    public static final String INDEX = "elasticsearch.index";
    public static final String INDEX_DOC = "The name of elasticsearch index";


    static ConfigDef config = new ConfigDef().define(HOSTS, Type.STRING, Importance.HIGH,HOSTS_DOC)
            .define(CLUSTER_NAME, Type.STRING, Importance.HIGH, CLUSTER_NAME_DOC)
            .define(INDEX, Type.STRING,Importance.HIGH, INDEX_DOC)
            .define(BULK_SIZE, Type.INT,Importance.HIGH, BULK_SIZE_DOC)
            .define(TOPICS,Type.STRING,Importance.HIGH, TOPICS_DOC)
            .define(DOCUMENT_NAME, Type.STRING,Importance.HIGH, DOCUMENT_NAME_DOC);

    public KafkaElasticSearchSinkConnectorConfig(Map<String, String> props) {
        super(config, props);
    }

}