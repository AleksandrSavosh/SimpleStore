package com.github.aleksandrsavosh.simplestore.parse;


import android.util.Base64;
import com.github.aleksandrsavosh.simplestore.Base;
import com.github.aleksandrsavosh.simplestore.Const;
import com.github.aleksandrsavosh.simplestore.ReflectionUtil;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashSet;

public class ParseUtil {

    public static ParseObject createPO(Class clazz){
        return ParseObject.create(clazz.getSimpleName());
    }

    public static void setModel2PO(Base base, ParseObject po) throws ParseException,
            IllegalAccessException {

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

}
