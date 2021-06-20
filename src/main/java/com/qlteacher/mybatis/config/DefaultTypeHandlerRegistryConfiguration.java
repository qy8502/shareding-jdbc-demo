package com.qlteacher.mybatis.config;

import com.qlteacher.mybatis.utils.JsonTypeHandlerFactory;
import com.qlteacher.mybatis.utils.JsonWithClassTypeHandler;
import com.qlteacher.mybatis.utils.ValueLabelEnumTypeHandler;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

import java.lang.reflect.Array;
import java.util.List;

@SuppressWarnings({"deprecation", "unused", "unchecked"})
@org.springframework.context.annotation.Configuration
@ConfigurationProperties(prefix = "mybatis.configuration")
@Log4j2
public class DefaultTypeHandlerRegistryConfiguration {


    private List<String> typeHandles;

    private String jsonTypeHandle;
    private List<String> jsonTypes;

    private List<String> jsonArrayTypes;

    public List<String> getTypeHandles() {
        return typeHandles;
    }

    public void setTypeHandles(List<String> typeHandles) {
        this.typeHandles = typeHandles;
    }

    public String getJsonTypeHandle() {
        return jsonTypeHandle;
    }

    public void setJsonTypeHandle(String jsonTypeHandle) {
        this.jsonTypeHandle = jsonTypeHandle;
    }

    public List<String> getJsonTypes() {
        return jsonTypes;
    }

    public void setJsonTypes(List<String> jsonTypes) {
        this.jsonTypes = jsonTypes;
    }

    public List<String> getJsonArrayTypes() {
        return jsonArrayTypes;
    }

    public void setJsonArrayTypes(List<String> jsonArrayTypes) {
        this.jsonArrayTypes = jsonArrayTypes;
    }

    @Bean
    public ConfigurationCustomizer configurationCustomizer() {

        return new ConfigurationCustomizer() {

            @Override
            public void customize(Configuration configuration) {
                configuration.setDefaultEnumTypeHandler(ValueLabelEnumTypeHandler.class);
                TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
                ClassLoader loader = configuration.getClass().getClassLoader();
                Class<?> typeHandlerclass = JsonWithClassTypeHandler.class;
                //设置JsonTypeHandles基础类
                if (StringUtils.hasLength(jsonTypeHandle)) {
                    try {
                        typeHandlerclass = loader.loadClass(jsonTypeHandle);
                        JsonTypeHandlerFactory.setObjectType(typeHandlerclass);
                    } catch (ClassNotFoundException e1) {
                        e1.printStackTrace();
                    }
                }

                //配置转换成Json的相关类型的JsonTypeHandles
                if (null != jsonTypes && !jsonTypes.isEmpty()) {
                    for (String type : jsonTypes) {
                        try {
                            Class<?> class1 = loader.loadClass(type);
                            //不能共用一个JsonTypeHandler，必须每个类型创造一个动态类处理，否则@Result指定的JsonTypeHandler时，会使用这里注册类型的实例，导致反序列化错误。
                            //typeHandlerRegistry.register(class1, typeHandlerclass);
                            Class<? extends TypeHandler> typeHandler = new JsonTypeHandlerFactory(class1).createInstance().getClass();
                            typeHandlerRegistry.register(class1, typeHandler);
                        } catch (ClassNotFoundException e) {
                            log.error("加载typeHandler出错：{}类型的jsonArrayType无法识别!", type);
                            e.printStackTrace();
                        } catch (Exception e) {
                            log.error("加载typeHandler出错", e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }

                //配置以数组形式转换成Json的相关类型的JsonTypeHandles
                if (null != jsonArrayTypes && !jsonArrayTypes.isEmpty()) {
                    for (String type : jsonArrayTypes) {
                        try {
                            Class<?> class1 = loader.loadClass(type);
                            Object clzs = Array.newInstance(class1, 0);
                            //不能共用一个JsonTypeHandler，必须每个类型创造一个动态类处理，否则@Result指定的JsonTypeHandler时，会使用这里注册类型的实例，导致反序列化错误。
                            //typeHandlerRegistry.register(clzs.getClass(), typeHandlerclass);
                            Class<? extends TypeHandler> typeHandler = new JsonTypeHandlerFactory(clzs.getClass()).createInstance().getClass();
                            typeHandlerRegistry.register(clzs.getClass(), typeHandler);
                        } catch (ClassNotFoundException e) {
                            log.error("加载typeHandler出错：{}类型的jsonType无法识别!", type);
                            e.printStackTrace();
                        } catch (Exception e) {
                            log.error("加载typeHandler出错", e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }

                //配置其他TypeHandles
                if (null != typeHandles && !typeHandles.isEmpty()) {
                    for (String type : typeHandles) {
                        try {
                            Class<?> class1 = loader.loadClass(type);
                            if (BaseTypeHandler.class.isAssignableFrom(class1)) {
                                typeHandlerRegistry.register(class1);
                            }
                            //以下兼容老得配置，但是typeHandles应该只放TypeHandle，建议需要转换的类型使用jsonTypes和jsonArrayTypes
                            else {
                                Object clzs = Array.newInstance(class1, 0);
                                //不能共用一个JsonTypeHandler，必须每个类型创造一个动态类处理，否则@Result指定的JsonTypeHandler时，会使用这里注册类型的实例，导致反序列化错误。
                                //typeHandlerRegistry.register(clzs.getClass(), typeHandlerclass);
                                Class<? extends TypeHandler> typeHandler = new JsonTypeHandlerFactory(clzs.getClass()).createInstance().getClass();
                                typeHandlerRegistry.register(clzs.getClass(), typeHandler);
                            }
                        } catch (ClassNotFoundException e) {
                            log.error("加载typeHandler出错：{}类型的typeHandler无法识别!", type);
                            e.printStackTrace();
                        } catch (Exception e) {
                            log.error("加载typeHandler出错", e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }

            }
        };
    }


}
