package com.example.sharedingjdbcdemo.config;

import lombok.extern.log4j.Log4j2;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.underlying.route.context.RouteContext;
import org.apache.shardingsphere.underlying.route.hook.RoutingHook;

import java.util.Arrays;
import java.util.List;

@Log4j2
public class AutoCreateTableRoutingHook implements RoutingHook {


    protected static AutoCreateTableManager manager;


    public static AutoCreateTableManager getManager() {
        return manager;
    }

    public static List<String> getTables(String table) {
        return Arrays.asList("teacher", "student");
    }

    @Override
    public void start(String sql) {
    }

    @Override
    public void finishSuccess(RouteContext routeContext, SchemaMetaData schemaMetaData) {
        if (AutoCreateTableRoutingHook.getManager() != null) {
            AutoCreateTableRoutingHook.getManager().checkDataNodesForRouting(routeContext);
        }
    }

    @Override
    public void finishFailure(Exception cause) {

    }
}
