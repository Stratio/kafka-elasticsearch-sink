FROM dbtucker/cp-kafka-connect:3.0.0

ADD library/kafka-elasticsearch-sink-0.0.1.jar /etc/kafka-connect/jars/
RUN rm -rf /usr/share/java/kafka-connect-hdfs
RUN rm -rf /usr/share/java/kafka-connect-jdbc
RUN rm /usr/share/java/confluent-common/netty-3.2.2.Final.jar

# Jsons
ADD docker/elasticsearch-task.json /etc/kafka-connect-tasks/

# entrypoint
ADD docker/docker-entrypoint.sh /etc/docker-entrypoint.sh
RUN chown root:root /etc/docker-entrypoint.sh
RUN chmod 700 /etc/docker-entrypoint.sh

ENTRYPOINT ["/etc/docker-entrypoint.sh"]