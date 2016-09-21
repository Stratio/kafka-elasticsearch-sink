FROM confluentinc/cp-kafka-connect

ADD library/kafka-elasticsearch-sink-0.0.1.jar /usr/share/java/kafka/

# Jsons
ADD kafka-connect-jobs /docker/kafka-connect-jobs

# entrypoint script
ADD docker-entrypoint.sh /etc/docker-entrypoint.sh
RUN chown root:root /etc/docker-entrypoint.sh
RUN chmod 700 /etc/docker-entrypoint.sh

WORKDIR /opt/

ENTRYPOINT ["/etc/docker-entrypoint.sh"]