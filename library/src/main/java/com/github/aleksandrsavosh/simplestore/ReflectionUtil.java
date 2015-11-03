package com.github.aleksandrsavosh.simplestore;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.HashSet;
import java.util.Set;

/**
 * Util for some reflection handles
 */
public class ReflectionUtil {

    public static Set<Field> getFields(final Class clazz, Set<Class> fieldTypes){
        Set<Field> result = new HashSet<Field>();
        for(Field field : clazz.getFields()){
            if(fieldTypes.contains(field.getType())){
                result.add(field);
            }
        }
        for(Field field : clazz.getDeclaredFields()){
            if(fieldTypes.contains(field.getType())){
                result.add(field);
            }
        }
        return result;
    }


    public static Class getGenericType(Field field) {
        ParameterizedType stringListType = (ParameterizedType) field.getGenericType();
        return (Class) stringListType.getActualTypeArguments()[0];
    }


}
