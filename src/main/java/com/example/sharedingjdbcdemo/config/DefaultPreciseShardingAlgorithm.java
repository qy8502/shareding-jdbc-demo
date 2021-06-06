package com.example.sharedingjdbcdemo.config;

import lombok.extern.log4j.Log4j2;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import java.util.Collection;

@Log4j2
public class DefaultPreciseShardingAlgorithm implements PreciseShardingAlgorithm<String> {


    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<String> shardingValue) {
//        log.error("dataSource:{}", dataSourceMap.size());
        return new StringBuilder().append(shardingValue.getLogicTableName()).append("_").append(shardingValue.getValue()).toString();
//        if (AutoCreateTableService.isAutoCreateTable()) {
//            AutoCreateTableService.instance.checkTable(realTableName, shardingValue);
//        }
//        return realTableName;
    }
}
