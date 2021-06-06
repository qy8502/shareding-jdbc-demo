package com.example.sharedingjdbcdemo.config;

import lombok.extern.log4j.Log4j2;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import java.util.Collection;

@Log4j2
public class DefaultDatabasePreciseShardingAlgorithm implements PreciseShardingAlgorithm<String> {


    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<String> shardingValue) {
        String realTableName = new StringBuilder().append(shardingValue.getLogicTableName()).append("-")
                .append("student".equals(shardingValue.getValue()) ? "student" : "teacher").toString();
        return realTableName;
    }
}
