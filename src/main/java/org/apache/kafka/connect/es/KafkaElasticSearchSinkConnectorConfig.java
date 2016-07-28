package org.apache.kafka.connect.es;

import java.util.Map;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;

import java.util.HashMap;


public class KafkaElasticSearchSinkConnectorConfig extends AbstractConfig {

    static final String CLUSTER_NAME = "elasticsearch.cluster.name";
    static final String HOSTS = "elasticsearch.hosts";
    static final String BULK_SIZE = "elasticsearch.bulk.size";
    static final String MAPPING_TYPE = "elasticsearch.mapping.type";
    static final String TOPICS = "topics";
    static final String INDEX = "elasticsearch.index";
    static final String ID_FIELD = "elasticsearch.idField";
    static final String ACTION_TYPE = "action.type";

    private static final String DEFAULT_ACTION_TYPE = "insert";
    private static final String DEFAULT_ID_FIELD = "id";
    private static final Integer DEFAULT_BULK_SIZE = 250;

    private static final String CLUSTER_NAME_DOC = "Elastic search cluster name";
    private static final String HOSTS_DOC = "A comma separated Elastic search hosts including port with a :. For " +
            "example localhost:9300";
    private static final String BULK_SIZE_DOC = "The number of messages to be bulk indexed into elasticsearch";
    private static final String MAPPING_TYPE_DOC = "The mapping associated to the topic-indexes";
    private static final String TOPICS_DOC = "topics in kafka";
    private static final String INDEX_DOC = "The name of elasticsearch index";
    private static final String ID_FIELD_DOC = "Id field when the action type is delete, update or upsert";
    private static final String ACTION_TYPE_DOC = "The action type against how the messages should be processed." +
            " Default is: index. The following options are available:\n"
            + "insert : Creates documents in ES with the values in the received message\n"
            + "update : Update documents in ES with the values in the received message based on id field\n" +
            "upsert : Update and create if not exists documents in ES with the values in the received " +
            "message based on id field set\n"
            + "delete : Deletes documents from ES based on id field in the received message";

    static ConfigDef config = new ConfigDef().define(HOSTS, Type.STRING, Importance.HIGH, HOSTS_DOC)
            .define(CLUSTER_NAME, Type.STRING, Importance.HIGH, CLUSTER_NAME_DOC)
            .define(INDEX, Type.STRING, Importance.HIGH, INDEX_DOC)
            .define(BULK_SIZE, Type.INT, DEFAULT_BULK_SIZE, Importance.HIGH, BULK_SIZE_DOC)
            .define(TOPICS, Type.STRING, Importance.HIGH, TOPICS_DOC)
            .define(MAPPING_TYPE, Type.STRING, Importance.HIGH, MAPPING_TYPE_DOC)
            .define(ID_FIELD, Type.STRING, DEFAULT_ID_FIELD, Importance.MEDIUM, ID_FIELD_DOC)
            .define(ACTION_TYPE, Type.STRING, DEFAULT_ACTION_TYPE, Importance.HIGH, ACTION_TYPE_DOC);

    KafkaElasticSearchSinkConnectorConfig(Map<String, String> props) {
        super(config, props);
    }

    enum ActionType {

        DELETE("delete"), UPDATE("update"), UPSERT("upsert"), INSERT("insert");

        private String actionType;
        private static Map<String, ActionType> ACTIONS = init();

        ActionType(String actionType) {
            this.actionType = actionType;
        }

        public String toValue() {
            return actionType;
        }

        public static ActionType toType(String value) {
            return ACTIONS.get(value);
        }

        public static Map<String, ActionType> init() {
            Map<String, ActionType> actions = new HashMap<>();
            ActionType[] types = values();
            for (ActionType type : types) {
                actions.put(type.name().toLowerCase(), type);
            }
            return actions;
        }
    }

    ActionType getActionType(String actionType) {
        return ActionType.toType(actionType);
    }

}