package org.apache.kafka.connect.es;

import java.util.Map;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.config.ConfigException;

import java.util.HashMap;


public class KafkaElasticSearchSinkConnectorConfig extends AbstractConfig {

    public static final String CLUSTER_NAME = "elasticsearch.cluster.name";
    public static final String CLUSTER_NAME_DOC = "Elastic search cluster name";

    public static final String HOSTS = "elasticsearch.hosts";
    public static final String HOSTS_DOC = "A comma separated Elastic search hosts including port with a :. For " +
            "example localhost:9300";

    public static final String BULK_SIZE = "elasticsearch.bulk.size";
    public static final String BULK_SIZE_DOC = "The number of messages to be bulk indexed into elasticsearch";

    public static final String DOCUMENT_NAME = "elasticsearch.document.name";
    public static final String DOCUMENT_NAME_DOC = "The topicIndexes type of Elasticsearch index";

    public static final String TOPICS = "topics";
    public static final String TOPICS_DOC = "topics in kafka";

    public static final String INDEX = "elasticsearch.index";
    public static final String INDEX_DOC = "The name of elasticsearch index";

    public static final String DELETE_ID_FIELD = "elasticsearch.delete.idField";
    public static final String DEFAULT_DELETE_ID_FIELD = "id";
    public static final String DELETE_ID_FIELD_DOC = "Id field when the action type is delete, update or upsert";

    public static final String ACTION_TYPE = "action.type";
    public static final String ACTION_TYPE_DOC = "The action type against how the messages should be processed." +
            " Default is: index. The following options are available:\n"
            + "index : Creates documents in ES with the values in the received message\n"
            + "update : Update documents in ES with the values in the received message based on id field\n" +
            "upsert : Update and create if not exists documents in ES with the values in the received " +
            "message based on id field set\n"
            + "delete : Deletes documents from ES based on id field in the received message";


    static ConfigDef config = new ConfigDef().define(HOSTS, Type.STRING, Importance.HIGH, HOSTS_DOC)
            .define(CLUSTER_NAME, Type.STRING, Importance.HIGH, CLUSTER_NAME_DOC)
            .define(INDEX, Type.STRING, Importance.HIGH, INDEX_DOC)
            .define(BULK_SIZE, Type.INT, Importance.HIGH, BULK_SIZE_DOC)
            .define(TOPICS, Type.STRING, Importance.HIGH, TOPICS_DOC)
            .define(DOCUMENT_NAME, Type.STRING, Importance.HIGH, DOCUMENT_NAME_DOC)
            .define(DELETE_ID_FIELD, Type.STRING, Importance.MEDIUM, DELETE_ID_FIELD_DOC)
            .define(ACTION_TYPE, Type.STRING, Importance.MEDIUM, ACTION_TYPE_DOC);

    public KafkaElasticSearchSinkConnectorConfig(Map<String, String> props) {
        super(config, props);
    }

    public enum ActionType {

        INDEX("index"), DELETE("delete"), UPDATE("update"), UPSERT("upsert");

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

    public ActionType getActionType() {
        try {
            return ActionType.toType(getString(ACTION_TYPE));
        } catch (ConfigException e) {
            return ActionType.INDEX;
        }
    }

}