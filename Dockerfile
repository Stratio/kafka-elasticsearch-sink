FROM dbtucker/cp-kafka-connect:3.0.0

ARG VERSION
ADD "target/kafka-elasticsearch-sink-${VERSION}-jar-with-dependencies.jar" /etc/kafka-connect/jars/

# Delete library dependecies for hdfs and jdbc connect
RUN rm -rf /usr/share/java/kafka-connect-hdfs
RUN rm -rf /usr/share/java/kafka-connect-jdbc

# Delete library conflict with netty in elasticsearch library
RUN rm /usr/share/java/confluent-common/netty-3.2.2.Final.jar

# Task in json format, this task will be sent to the Kafka Connect API
ADD docker/elasticsearch-task.json /etc/kafka-connect-tasks/

# Entrypoint
ADD docker/bootstrap.sh /etc/bootstrap.sh
RUN chown root:root /etc/bootstrap.sh
RUN chmod 700 /etc/bootstrap.sh

ENTRYPOINT ["/etc/bootstrap.sh"]