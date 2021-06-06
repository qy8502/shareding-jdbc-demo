/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.sharedingjdbcdemo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.*;

/**
 * Properties configuration properties.
 */
@ConfigurationProperties(prefix = AutoCreateTableProperties.CONFIGURATION_PREFIX)
@Getter
@Setter
public class AutoCreateTableProperties {

    public static final String CONFIGURATION_PREFIX = "spring.shardingsphere.sharding.auto-create-table";

    /**
     * 是否开启自动创建表
     */
    protected boolean enabled = false;

    /**
     * 刷新数据库表将新增加的表纳入ShardingJdbc数据节点的间隔时间（单位：毫秒，-1不刷新）
     */
    protected long refreshTimeSpan = 1000L;

    /**
     * 开启自动创建表的逻辑表（ 逻辑表名称: 子表正则表达式 ）
     */
    protected Map<String,String> tables = new HashMap<>();

    private Properties props = new Properties();
}
