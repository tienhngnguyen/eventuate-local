package io.eventuate.local.polling;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@Import(EventTableChangesToAggregateTopicTranslatorConfiguration.class)
@Profile("EventuatePolling")
public class PollingEventTableChangesToAggregateTopicTranslatorConfiguration {

  @Bean
  public EventuateSchema eventuateSchema(@Value("${eventuate.database.schema:#{null}}") String eventuateDatabaseSchema) {
    return new EventuateSchema(eventuateDatabaseSchema);
  }

  @Bean
  public CdcKafkaPublisher<PublishedEvent> pollingCdcKafkaPublisher(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
    PublishingStrategy<PublishedEvent> publishingStrategy) {
    return new PollingCdcKafkaPublisher<>(eventuateKafkaConfigurationProperties.getBootstrapServers(), publishingStrategy);
  }

  @Bean
  public CdcProcessor<PublishedEvent> pollingCdcProcessor(EventuateConfigurationProperties eventuateConfigurationProperties,
    PollingDao<PublishedEventBean, PublishedEvent, String> pollingDao) {
    return new PollingCdcProcessor<>(pollingDao, eventuateConfigurationProperties.getPollingIntervalInMilliseconds());
  }

  @Bean
  public PollingDataProvider<PublishedEventBean, PublishedEvent, String> pollingDataProvider(EventuateConfigurationProperties eventuateConfigurationProperties,
          EventuateSchema eventuateSchema) {
    return new EventPollingDataProvider(eventuateSchema);
  }

  @Bean
  public PollingDao<PublishedEventBean, PublishedEvent, String> pollingDao(EventuateConfigurationProperties eventuateConfigurationProperties,
          PollingDataProvider<PublishedEventBean, PublishedEvent, String> pollingDataProvider,
          DataSource dataSource) {

    return new PollingDao<>(pollingDataProvider,
      dataSource,
      eventuateConfigurationProperties.getMaxEventsPerPolling(),
      eventuateConfigurationProperties.getMaxAttemptsForPolling(),
      eventuateConfigurationProperties.getPollingRetryIntervalInMilliseconds());
  }
}
