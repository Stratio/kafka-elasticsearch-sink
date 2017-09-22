package org.apache.kafka.connect.es;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.utils.AppInfoParser;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.sink.SinkConnector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * HdfsSinkConnector is a Kafka Connect Connector implementation that ingest data from Kafka to HDFS.
 */
public class KafkaElasticSearchSinkConnector extends SinkConnector {

    Map<String, String> configProperties;
    KafkaElasticSearchSinkConnectorConfig config;

    @Override
    public String version() {
        return AppInfoParser.getVersion();
    }

    @Override
    public void start(Map<String, String> props) throws ConnectException {
        try {
            configProperties = props;
            config = new KafkaElasticSearchSinkConnectorConfig(props);
            //} catch (ConfigException e) {
        } catch (Exception e) {
            throw new ConnectException("Couldn't start KafkaElasticSearchSinkConnector due to configuration error", e);
        }
    }

    @Override
    public Class<? extends Task> taskClass() {
        return KafkaElasticSearchSinkTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        List<Map<String, String>> taskConfigs = new ArrayList<>();
        Map<String, String> taskProps = new HashMap<>();
        taskProps.putAll(configProperties);
        for (int i = 0; i < maxTasks; i++) {
            taskConfigs.add(taskProps);
        }
        return taskConfigs;
    }

    @Override
    public void stop() throws ConnectException {

    }

    @Override
    public ConfigDef config() {
        return KafkaElasticSearchSinkConnectorConfig.config;
    }
}