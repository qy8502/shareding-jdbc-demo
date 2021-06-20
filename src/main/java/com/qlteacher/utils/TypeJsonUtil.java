package com.qlteacher.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.cache.support.NullValue;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * @ClassName com.qlteacher.mq.easy.utils
 * @Description
 * @Author Qu Yankai
 * @Date 2018/3/9 17:50
 */
@SuppressWarnings("deprecation")
public class TypeJsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.registerModule(new SimpleModule().addSerializer(new NullValueSerializer((String)null)));
        MAPPER.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
    }

    // 父类的这个内部类是私有的,没办法又复制了一遍
    private static class NullValueSerializer extends StdSerializer<NullValue> {

        private static final long serialVersionUID = 1999052150548658808L;

        private final String classIdentifier;

        /**
         * @param classIdentifier
         *            can be {@literal null} and will be defaulted to {@code @class}.
         */
        NullValueSerializer(String classIdentifier) {

            super(NullValue.class);
            this.classIdentifier = StringUtils.hasText(classIdentifier) ? classIdentifier : "@class";
        }

        /*
         * (non-Javadoc)
         * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
         */
        @Override
        public void serialize(NullValue value, JsonGenerator jgen, SerializerProvider provider) throws IOException {

            jgen.writeStartObject();
            jgen.writeStringField(classIdentifier, NullValue.class.getName());
            jgen.writeEndObject();
        }
    }

    /**
     * Read mq data object.
     *
     * @param item
     *            the item
     * @return the object
     */
    public static Object readValue(byte[] item) {
        return readValue(item, Object.class);
    }

    /**
     * Read mq data object.
     *
     * @param item
     *            the item
     * @return the object
     */
    public static <T> T readValue(byte[] item, Class<T> valueType) {
        try {
            return MAPPER.readValue(item, valueType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Write mq data object.
     *
     * @param args
     *            the args
     * @return the object
     */
    public static byte[] writeValueAsBytes(Object args) {
        try {
            return MAPPER.writeValueAsBytes(args);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Read mq data object.
     *
     * @param item
     *            the item
     * @return the object
     */
    public static Object readValue(String item) {
        return readValue(item, Object.class);
    }

    /**
     * Read mq data object.
     *
     * @param item
     *            the item
     * @return the object
     */
    public static <T> T readValue(String item, Class<T> valueType) {
        try {
            return MAPPER.readValue(item, valueType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Write mq data object.
     *
     * @param args
     *            the args
     * @return the object
     */
    public static String writeValueAsString(Object args) {
        try {
            return MAPPER.writeValueAsString(args);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
