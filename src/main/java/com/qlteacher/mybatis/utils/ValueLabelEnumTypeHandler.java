package com.qlteacher.mybatis.utils;

import com.qlteacher.domain.ValueLabelEnum;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * mybatis处理ValueLabelEnum枚举数据
 *
 * @param <E> 实现ValueLabelEnum的枚举类型
 */
public class ValueLabelEnumTypeHandler<E extends Enum<E>> extends BaseTypeHandler<E> {

    private Class<E> type;

    public ValueLabelEnumTypeHandler() {
        System.out.println("没有指定类型的初始化");
    }

    public ValueLabelEnumTypeHandler(Class<E> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        this.type = type;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {
        if (parameter instanceof ValueLabelEnum) {
            ps.setString(i, String.valueOf(((ValueLabelEnum) parameter).getValue()));
        } else if (jdbcType == null) {
            ps.setString(i, parameter.name());
        } else {
            ps.setObject(i, parameter.name(), jdbcType.TYPE_CODE); // see r3589
        }
    }

    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String s = rs.getString(columnName);
        return ValueLabelEnum.valueOf(s, type);
    }

    @Override
    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String s = rs.getString(columnIndex);
        return ValueLabelEnum.valueOf(s, type);
    }

    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String s = cs.getString(columnIndex);
        return ValueLabelEnum.valueOf(s, type);
    }

}
