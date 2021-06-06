package com.example.sharedingjdbcdemo.config;

import lombok.extern.log4j.Log4j2;
import org.apache.shardingsphere.core.rule.TableRule;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;
import org.apache.shardingsphere.underlying.common.rule.DataNode;
import org.apache.shardingsphere.underlying.route.context.RouteContext;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Log4j2
public final class AutoCreateTableManager {

    protected Map<String, Set<String>> logicTables;
    protected final Map<String, DataNode> actualTables = new ConcurrentHashMap();
    protected ShardingDataSource shardingDataSource;
    protected AutoCreateTableProperties properties;
    protected ScheduledExecutorService scheduledThreadPool;

    public Map<String, Set<String>> getLogicTablesMap() {
        return logicTables;
    }

    public Map<String, DataNode> getActualTablesMap() {
        return actualTables;
    }

    public ShardingDataSource getShardingDataSource() {
        return shardingDataSource;
    }

    public AutoCreateTableProperties getProperties() {
        return properties;
    }

    public boolean isEnabled() {
        return getProperties().isEnabled();
    }

    public AutoCreateTableManager(ShardingDataSource dataSource, AutoCreateTableProperties properties) {
        if (dataSource == null) {
            throw new NoSuchBeanDefinitionException("实例化AutoCreateTableManager必须提供ShardingDataSource实例");
        }
        this.shardingDataSource = dataSource;
        this.properties = properties != null ? properties : new AutoCreateTableProperties();
        this.logicTables = properties.tables.keySet().stream()
                .filter((key) -> {
                    if (dataSource.getRuntimeContext().getRule().getTableRule(key) == null) {
                        log.error("实例化AutoCreateTableManager未能找到{}表规则", key);
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toMap(Function.identity(), (key) ->
                        new HashSet<>(dataSource.getRuntimeContext().getRule().getTableRule(key).getActualDatasourceNames())));
        if (!this.logicTables.isEmpty() && this.properties.getRefreshTimeSpan() >= 0) {
            scheduledThreadPool = new ScheduledThreadPoolExecutor(1);
            scheduledThreadPool.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    refreshDataNodes(true);
                    log.debug("AutoCreateTableManager 刷新所有数据节点!");
                }
            }, 1000, this.properties.getRefreshTimeSpan(), TimeUnit.MILLISECONDS);

        }
        log.warn("AutoCreateTableManager 实例化成功，自动创建分表对应关系：{}", logicTables);
    }


    /**
     * 更新表规则中的数据节点
     */
    protected synchronized void setTableRuleDataNodes(TableRule tableRule, List<DataNode> newDataNodes, boolean merge) {

        try {
            Set<String> actualTablesRule = new HashSet<>();
            Map<DataNode, Integer> dataNodeIndexMap = new HashMap<>();
            AtomicInteger index = new AtomicInteger(0);
            Map<String, Set<String>> datasourceToTablesMap = new HashMap<>();
            Map<String, DataNode> actualTablesNew = new HashMap<>();
            // 整理方法
            Consumer<DataNode> setter = dataNode -> {
                String dataNodeString = dataNode.toString();
                if (merge && actualTablesNew.containsKey(dataNodeString)) {
                    return;
                }
                actualTablesRule.add(dataNode.getTableName());
                datasourceToTablesMap.computeIfAbsent(dataNode.getDataSourceName(), (key) -> new HashSet<>()).add(dataNode.getTableName());
                dataNodeIndexMap.put(dataNode, index.intValue());
                actualTablesNew.put(dataNodeString, dataNode);
                index.incrementAndGet();
            };
            // 如果是合并模式，先整理以前的
            if (merge) {
                tableRule.getActualDataNodes().forEach(setter);
            }
            int indexNew = index.intValue();
            // 整理新加的
            newDataNodes.forEach(setter);
            if (indexNew < index.intValue() || !merge) {
                // 设置actualDataNodesField
                Field actualDataNodesField = TableRule.class.getDeclaredField("actualDataNodes");
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(actualDataNodesField, actualDataNodesField.getModifiers() & ~Modifier.FINAL);
                actualDataNodesField.setAccessible(true);
                actualDataNodesField.set(tableRule, newDataNodes);
                // 设置actualTablesField
                Field actualTablesField = TableRule.class.getDeclaredField("actualTables");
                actualTablesField.setAccessible(true);
                actualTablesField.set(tableRule, actualTablesRule);
                // 设置dataNodeIndexMapField
                Field dataNodeIndexMapField = TableRule.class.getDeclaredField("dataNodeIndexMap");
                dataNodeIndexMapField.setAccessible(true);
                dataNodeIndexMapField.set(tableRule, dataNodeIndexMap);
                // 设置datasourceToTablesMapField
                Field datasourceToTablesMapField = TableRule.class.getDeclaredField("datasourceToTablesMap");
                datasourceToTablesMapField.setAccessible(true);
                datasourceToTablesMapField.set(tableRule, datasourceToTablesMap);
                log.warn("AutoCreateTableManager 成功更新表" + tableRule.getLogicTable() + "规则数据节点 {}!", newDataNodes);
                getActualTablesMap().putAll(actualTablesNew);
            }
        } catch (Exception ex) {
            log.error("AutoCreateTableManager 更新表" + tableRule.getLogicTable() + "规则数据节点发生异常!", ex);
        }
    }

    public void initDataNodes() {
        refreshDataNodes(false);
        log.warn("AutoCreateTableManager 成功初始化所有数据节点!");
    }

    protected void refreshDataNodes(boolean merge) {
        this.getLogicTablesMap().forEach((tableName, databases) -> {
            TableRule rule = getShardingDataSource().getRuntimeContext().getRule().getTableRule(tableName);
            List<DataNode> dataNodesNew = collectDataNodesFromDatabase(rule, databases);
            // 如果一张子表都没用，防止出错，使用模板表
            if (dataNodesNew.isEmpty()) {
                dataNodesNew = databases.stream().map((database) -> new DataNode(database, tableName)).collect(Collectors.toList());
            }
            setTableRuleDataNodes(rule, dataNodesNew, merge);
        });
    }

    protected List<DataNode> collectDataNodesFromDatabase(TableRule rule, Set<String> databases) {
        String tableName = rule.getLogicTable();
        List<DataNode> newDataNodes = new ArrayList<>();
        databases.forEach(database -> {
            DataSource dataSource = getMasterDataSource(database);
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SHOW TABLES");
                while (rs.next()) {
                    String table = rs.getString(1);
                    if (table.matches(getProperties().getTables().get(rule.getLogicTable()))) {
                        DataNode newDataNode = new DataNode(database, table);
                        newDataNodes.add(newDataNode);
                    }
                }
            } catch (SQLException ex) {
                log.error("AutoCreateTableManager 收集表" + tableName + "时发生异常", ex);
            }
        });
        return newDataNodes;
    }


    /**
     * 检查路由是否有需要创建的新表
     *
     * @param routeContext
     */
    public void checkDataNodesForRouting(RouteContext routeContext) {
        if (isEnabled()) {
            Map<String, Map<String, List<DataNode>>> tableDataNodes = new HashMap<>();
            routeContext.getRouteResult().getRouteUnits().forEach(item ->
                    item.getTableMappers().forEach(table -> {
                        if (getLogicTablesMap().containsKey(table.getLogicName())) {
                            DataNode dataNode = new DataNode(item.getDataSourceMapper().getLogicName(), table.getActualName());
                            if (!getActualTablesMap().containsKey(dataNode.toString())) {
                                tableDataNodes
                                        .computeIfAbsent(table.getLogicName(), (key) -> new HashMap<>())
                                        .computeIfAbsent(item.getDataSourceMapper().getLogicName(), (key) -> new ArrayList<>())
                                        .add(dataNode);
                            }
                        }
                    }));

            createTables(tableDataNodes);
        }
    }

    /**
     * 创建新表
     *
     * @param tableDataNodes
     */
    protected void createTables(Map<String, Map<String, List<DataNode>>> tableDataNodes) {
        tableDataNodes.forEach((tableName, databaseMap) -> {
            TableRule rule = getShardingDataSource().getRuntimeContext().getRule().getTableRule(tableName);
            List<DataNode> newDataNodes = new ArrayList<>();
            databaseMap.forEach((datasourceName, nodes) -> {
                DataSource dataSource = getMasterDataSource(datasourceName);
                if (dataSource == null) {
                    return;
                }
                try (Connection conn = dataSource.getConnection();
                     Statement stmt = conn.createStatement()) {
                    for (DataNode dataNode : nodes) {
                        String creatsql = new StringBuilder()
                                .append("CREATE TABLE IF NOT EXISTS `").append(dataNode.getTableName()).append("` LIKE `")
                                .append(tableName).append("`;").toString();
                        int result = stmt.executeUpdate(creatsql);
                        if (0 == result) {
                            log.error("AutoCreateTableManager 成功创建表{}！", dataNode.getTableName());
                        } else {
                            log.error("AutoCreateTableManager 未能创建表{}，可能表已存在！", dataNode.getTableName());
                        }
                        newDataNodes.add(dataNode);
                    }
                } catch (SQLException ex) {
                    log.error("AutoCreateTableManager 创建" + tableName + "相关子表时发生异常" + newDataNodes.toString(), ex);
                }
            });
            if (!newDataNodes.isEmpty()) {
                setTableRuleDataNodes(rule, newDataNodes, true);
            }

        });
    }


    private DataSource getMasterDataSource(String datasourceName) {
        String masterDataSourceName = getMasterDataSourceName(datasourceName);
        DataSource dataSource = getShardingDataSource().getDataSourceMap().get(masterDataSourceName);
        if (dataSource == null) {
            log.error("AutoCreateTableManager 未能找到对应的数据源" + datasourceName + "或其主库");
            return null;
        }
        return dataSource;
    }

    private String getMasterDataSourceName(String datasourceName) {
        String masterDataSourceName = getShardingDataSource().getRuntimeContext().getRule().getMasterSlaveRules().stream()
                .filter(each -> each.getName().equalsIgnoreCase(datasourceName))
                .map(e -> e.getMasterDataSourceName()).findFirst().orElse(datasourceName);
        return masterDataSourceName;
    }
}
