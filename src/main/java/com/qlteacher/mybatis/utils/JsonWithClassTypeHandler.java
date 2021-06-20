package com.qlteacher.mybatis.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qlteacher.utils.TypeJsonUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Log4j2
public class JsonWithClassTypeHandler<T> extends BaseTypeHandler<T> {

    /**
     * 不带class信息的，兼容JsonTypeHandler获取数据
     */
    private static ObjectMapper objectMapper = new ObjectMapper();
    static {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    protected Class<T> clazz;
    public JsonWithClassTypeHandler() {
        System.out.println("没有指定类型的初始化");
    }

    public JsonWithClassTypeHandler(Class<T> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        this.clazz = clazz;
    }

    public String stringify(Object object) {
        if (object == null) {
            return "";
        }
        try {
            return TypeJsonUtil.writeValueAsString(object);
        } catch (Exception e) {
            log.error("JsonWithClassTypeHandler序列化出错！写入null数据，必须尽快解决！", e);
        }

        return null;
    }

    public T parse(String json) {

        if (json == null || json.length() == 0) {
            return null;
        }

        try {
            return TypeJsonUtil.readValue(json, clazz);
        } catch (Exception e) {
            log.error("JsonWithClassTypeHandler反序列化出错！尝试使用JsonTypeHandler方式反序列化！", e);
            try {
                return objectMapper.readValue(json, clazz);
            } catch (Exception ex) {
                log.error("JsonWithClassTypeHandler反序列化出错！尝试使用JsonTypeHandler方式反序列化出错！读取返回null数据，必须尽快解决！", ex);
            }
        }

        return null;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
        // TODO Auto-generated method stub
        ps.setString(i, stringify(parameter));
    }


    @Override
    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parse(rs.getString(columnName));
    }

    @Override
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parse(rs.getString(columnIndex));
    }

    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parse(cs.getString(columnIndex));
    }

}
