package com.example.sharedingjdbcdemo.config;

import com.google.common.base.CaseFormat;
import lombok.SneakyThrows;
import org.apache.shardingsphere.spring.boot.datasource.DataSourcePropertiesSetter;
import org.apache.shardingsphere.spring.boot.util.PropertyUtil;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;

public final class DruidDataSourcePropertiesSetter implements DataSourcePropertiesSetter {

    @Override
    @SneakyThrows(ReflectiveOperationException.class)
    public void propertiesSet(final Environment environment, final String prefix, final String dataSourceName, final DataSource dataSource) {
        Properties properties = new Properties();
        String springPropertiesKey = "spring.datasource";
        String datasourcePropertiesKey = prefix + dataSourceName.trim();
        if (PropertyUtil.containPropertyPrefix(environment, springPropertiesKey)) {
            Map datasourceProperties = PropertyUtil.handle(environment, springPropertiesKey, Map.class);
            putProperties(properties, datasourceProperties, null);
        }
        if (PropertyUtil.containPropertyPrefix(environment, datasourcePropertiesKey)) {
            Map datasourceProperties = PropertyUtil.handle(environment, datasourcePropertiesKey, Map.class);
            putProperties(properties, datasourceProperties, null);
        }
        if (!properties.isEmpty()) {
            Method method = dataSource.getClass().getMethod("setConnectProperties", Properties.class);
            method.invoke(dataSource, properties);
        }
    }

    private void putProperties(Properties properties, Map<String, Object> map, String prefix) {
        String prefixKey = StringUtils.hasLength(prefix) ? prefix : "druid";
        map.forEach((key, value) -> {
            String propertyName = key;
            if (key.contains("-")) {
                propertyName = CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, key);
            }
            propertyName = propertyName.equals(prefixKey) ? prefixKey : prefixKey + "." + propertyName;
            if (value instanceof Map) {
                putProperties(properties, (Map) value, propertyName);
            } else {
                properties.put(propertyName, value);
            }
        });
    }

    @Override
    public String getType() {
        return "com.alibaba.druid.pool.DruidDataSource";
    }
}
