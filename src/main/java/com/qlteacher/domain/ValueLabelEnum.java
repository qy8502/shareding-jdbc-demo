package com.qlteacher.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * 值和说明的枚举标准
 * @param <T>
 */
public interface ValueLabelEnum<T> {
    /**
     * 值
     */
    T getValue();

    /**
     * 说明
     */
    String getLabel();

    /**
     * 必须重写Object.toString()使其返回getValue();
     * @return getValue();
     */
    String toString();

    static Map<Class<?>, Map<String, ValueLabelEnum>> enumValueMapCache = new HashMap<Class<?>, Map<String, ValueLabelEnum>>();

    @SuppressWarnings({"unchecked"})
    static <E extends Enum<E>> E valueOf(Object value, Class<E> clazz) {
        if (ValueLabelEnum.class.isAssignableFrom(clazz)) {
            if (!enumValueMapCache.containsKey(clazz)) {
                ValueLabelEnum[] enumValues = (ValueLabelEnum[]) clazz.getEnumConstants();
                HashMap<String, ValueLabelEnum> map = new HashMap<String, ValueLabelEnum>();
                // from last to first, so that in case of duplicate values, first wins
                for (ValueLabelEnum e : enumValues) {
                    map.put(String.valueOf(e.getValue()), e);
                }
                enumValueMapCache.put(clazz, map);

            }
            return (E) enumValueMapCache.get(clazz).get(String.valueOf(value));
        } else {
            try {
                return Enum.valueOf(clazz, String.valueOf(value));
            } catch (Exception e) {
                return clazz.getEnumConstants()[Integer.parseInt(String.valueOf(value))];
            }
        }
    }

}
