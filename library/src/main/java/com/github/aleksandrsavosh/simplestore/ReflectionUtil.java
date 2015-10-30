package com.github.aleksandrsavosh.simplestore;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 * Util for some reflection handles
 */
public class ReflectionUtil {

    public static Set<Field> getFieldsWithAccessible(Class clazz, Set<Class> classesType) {
        Set<Field> results = new HashSet<Field>();
        while(clazz != null){
            for(Field field : clazz.getDeclaredFields()){
                field.setAccessible(true);
                if(classesType.contains(field.getType())){
                    results.add(field);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return results;
    }




}
