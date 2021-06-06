package com.example.sharedingjdbcdemo.config;

import lombok.extern.log4j.Log4j2;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;
import org.apache.shardingsphere.shardingjdbc.spring.boot.SpringBootConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter(SpringBootConfiguration.class)
@EnableConfigurationProperties({
        AutoCreateTableProperties.class})
@ConditionalOnProperty(value = AutoCreateTableProperties.CONFIGURATION_PREFIX + ".enabled", havingValue = "true")
@Log4j2
public class AutoCreateTableConfiguration {

    @Bean
    public AutoCreateTableManager autoCreateTableManager(
            AutoCreateTableProperties properties,
            @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
            @Autowired(required = false) ShardingDataSource dataSource) {

        AutoCreateTableManager autoCreateTableManager = new AutoCreateTableManager(dataSource, properties);
        autoCreateTableManager.initDataNodes();
        AutoCreateTableRoutingHook.manager = autoCreateTableManager;
        return autoCreateTableManager;
    }
}
