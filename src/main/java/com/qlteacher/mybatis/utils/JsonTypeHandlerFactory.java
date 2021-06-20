package com.qlteacher.mybatis.utils;

import org.apache.ibatis.javassist.ClassClassPath;
import org.apache.ibatis.javassist.ClassPool;
import org.apache.ibatis.javassist.CtClass;
import org.apache.ibatis.javassist.bytecode.AnnotationsAttribute;
import org.apache.ibatis.javassist.bytecode.ClassFile;
import org.apache.ibatis.javassist.bytecode.ConstPool;
import org.apache.ibatis.javassist.bytecode.Descriptor;
import org.apache.ibatis.javassist.bytecode.annotation.Annotation;
import org.apache.ibatis.javassist.bytecode.annotation.ArrayMemberValue;
import org.apache.ibatis.javassist.bytecode.annotation.ClassMemberValue;
import org.apache.ibatis.javassist.bytecode.annotation.MemberValue;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"rawtypes", "deprecation"})
public class JsonTypeHandlerFactory extends AbstractFactoryBean<TypeHandler> {
    private static JsonTypeHandlerClassLoader loader = new JsonTypeHandlerClassLoader(JsonTypeHandlerFactory.class.getClassLoader());
    private static Class objectType = JsonWithClassTypeHandler.class;
    protected static Map<Class, TypeHandler> handlerCache = new HashMap<Class, TypeHandler>();

    public static JsonTypeHandlerClassLoader getLoader() {
        return loader;
    }

    public static void setLoader(JsonTypeHandlerClassLoader loader) {
        JsonTypeHandlerFactory.loader = loader;
    }

    public static void setObjectType(Class objectType) {
        JsonTypeHandlerFactory.objectType = objectType;
    }

    private Class type;

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    public JsonTypeHandlerFactory(Class type) {
        this.type = type;
    }


    @SuppressWarnings("unchecked")
    @Override
    public Class<TypeHandler> getObjectType() {
        return JsonTypeHandlerFactory.objectType;
    }

    @SuppressWarnings("unchecked")
    @Override
    public TypeHandler createInstance() throws Exception {
        //多数据源情况下，会多次生成相应类型的JsonTypeHandler类，导致错误
        // 建立单例模式，每个类型只会创造一个JsonTypeHandler实例
        if (!handlerCache.containsKey(type)) {

            ClassPool pool = ClassPool.getDefault();
            pool.insertClassPath(new ClassClassPath(this.getClass()));
            String newClassName =
                    objectType.getCanonicalName() + "For" + type.getSimpleName().replace("[]", "Array");

            // create the annotation
            CtClass cc = pool.makeClass(newClassName);
            cc.setSuperclass(pool.getCtClass(objectType.getCanonicalName()));

            ClassFile ccFile = cc.getClassFile();
            ConstPool constpool = ccFile.getConstPool();

            AnnotationsAttribute attr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
            Annotation annot = new Annotation(MappedTypes.class.getCanonicalName(), constpool);
            ArrayMemberValue annoValues = new ArrayMemberValue(constpool);
            List<MemberValue> mvList = new ArrayList<MemberValue>();

            String typeName = type.getCanonicalName();
            if (typeName.endsWith("[]")) {
                mvList.add(new ClassMemberValue(
                        constpool.addUtf8Info("[" + Descriptor.of(typeName.substring(0, typeName.length() - 2))), constpool));
            } else {
                mvList.add(new ClassMemberValue(typeName, constpool));
            }

            annoValues.setValue(mvList.toArray(new MemberValue[]{}));
            annot.addMemberValue("value", annoValues);
            attr.addAnnotation(annot);
            ccFile.addAttribute(attr);
            // transform the ctClass to java class
            Class dynamiqueBeanClass = cc.toClass(loader, null);

            // instanciating the updated class
            handlerCache.put(type, (TypeHandler) dynamiqueBeanClass.getDeclaredConstructor(new Class[]{Class.class}).newInstance(type));
        }
        return handlerCache.get(type);
    }

}
