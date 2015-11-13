package com.github.aleksandrsavosh.simplestore.parse;


import com.github.aleksandrsavosh.simplestore.*;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class ParseUtil {

    public static ParseObject createPO(Class clazz){
        return ParseObject.create(clazz.getSimpleName());
    }

    public static <Model extends Base> Model createModel(Class<Model> clazz) throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, InstantiationException {
        return clazz.getConstructor().newInstance();
    }

    public static void setModel2PO(Base base, ParseObject po) throws ParseException, IllegalAccessException {
        Class clazz = base.getClass();
        for(Field field : ReflectionUtil.getFields(clazz, Const.fields)){
            field.setAccessible(true);
            String fieldName = field.getName();
            if (po.has(fieldName)) {
                po.remove(fieldName);
            }
            Object value = field.get(base);
            if(value != null) {
                po.put(fieldName, value);
            }
        }
    }

    public static <Model extends Base> void setModelData2PO(Model model, ParseObject po) throws IllegalAccessException, ParseException {

        for(Field field : ReflectionUtil.getFields(model.getClass(), Const.dataFields)){
            field.setAccessible(true);
            byte[] data = (byte[]) field.get(model);
            if(data == null){ continue; }
            ParseFile file = new ParseFile(field.getName(), data);
            file.save();
            po.put(field.getName(), file);
        }
    }

    public static <Model extends Base> void setPOData2Model(ParseObject po, Model model) throws ParseException, IllegalAccessException {
        for(Field field : ReflectionUtil.getFields(model.getClass(), Const.dataFields)){
            field.setAccessible(true);
            ParseFile pf = po.getParseFile(field.getName());
            if(pf != null && pf.isDataAvailable()){
                field.set(model, pf.getData());
            }
        }
    }

    public static void setPO2Model(ParseObject po, Base base) throws IllegalAccessException {
        Class clazz = base.getClass();

        base.setCloudId(po.getObjectId());
        base.setCreatedAt(po.getCreatedAt());
        base.setUpdatedAt(po.getUpdatedAt());

        for(Field field : ReflectionUtil.getFields(clazz, Const.fields)) {
            field.setAccessible(true);
            field.set(base, po.get(field.getName()));
        }
    }

    public static ParseObject getPO(Class clazz, String id) throws ParseException {
        return ParseQuery.getQuery(clazz.getSimpleName()).get(id);
    }

    public static List<ParseObject> getPOs(Class clazz) throws ParseException {
        return ParseQuery.getQuery(clazz.getSimpleName()).find();
    }

    public static List<ParseObject> getPOsBy(Class clazz, KeyValue... keyValues) throws ParseException {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(clazz.getSimpleName());
        for(KeyValue keyValue : keyValues){
            query.whereEqualTo(keyValue.key, keyValue.value);
        }
        return query.find();
    }

}
