package com.github.aleksandrsavosh.simplestore;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;

/**
 * Util for some reflection handles
 */
public class ReflectionUtil {

    private static Map<Class, Map<Set<Class>, Set<Field>>> cache = new HashMap<Class, Map<Set<Class>, Set<Field>>>();

    public static Set<Field> getFields(final Class clazz, Set<Class> fieldTypes){
        if(cache.containsKey(clazz)){
            if(cache.get(clazz).containsKey(fieldTypes)){
                return cache.get(clazz).get(fieldTypes);
            }
        } else {
            cache.put(clazz, new HashMap<Set<Class>, Set<Field>>());
        }


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

        cache.get(clazz).put(fieldTypes, result);

        return result;
    }


    public static Class getGenericType(Field field) {
        ParameterizedType stringListType = (ParameterizedType) field.getGenericType();
        return (Class) stringListType.getActualTypeArguments()[0];
    }


    public static Collection getCollectionInstance(Class collType) {
        if(collType.equals(Collection.class) || collType.equals(List.class)){
            return new ArrayList();
        } else {
            return new HashSet();
        }
    }
}

