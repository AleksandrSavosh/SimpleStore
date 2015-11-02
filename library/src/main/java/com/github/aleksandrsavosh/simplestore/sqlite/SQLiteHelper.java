package com.github.aleksandrsavosh.simplestore.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.github.aleksandrsavosh.simplestore.Base;
import com.github.aleksandrsavosh.simplestore.LogUtil;
import com.github.aleksandrsavosh.simplestore.ReflectionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class SQLiteHelper extends SQLiteOpenHelper {

    private Set<Class<? extends Base>> dbClasses;
    private Set<Class> simpleFields = new HashSet<Class>();
    {
        simpleFields.add(Integer.class);
        simpleFields.add(String.class);
        simpleFields.add(Date.class);
    }


    public SQLiteHelper(Context context, String name, int version, Set<Class<? extends Base>> dbClasses) {
        super(context, name, null, version);
        this.dbClasses = dbClasses;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final List<String> queries = new ArrayList<String>();
        for(Class<? extends Base> clazz : dbClasses){
            queries.add(getCreateTableQuery(clazz));
        }

        execInTransaction(db, new QueryExecutor() {
            @Override
            public void exec(SQLiteDatabase db) {
                for (String query : queries) {
                    LogUtil.toLog(query);
                    db.execSQL(query);
                }
            }
        });
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

//    public String generateId(Class<? extends Base> clazz) {
//        String id;
//        Cursor cursor;
//        do {
//            id = (int) (Math.random() * Integer.MAX_VALUE) + "";
//            SQLiteDatabase db = getReadableDatabase();
//            cursor = db.query(
//                    getTableName(clazz),
//                    new String[]{"_id"},
//                    "_id=?",
//                    new String[]{id},
//                    null,
//                    null,
//                    null
//            );
//        } while(cursor.getCount() != 0);
//
//        return id;
//    }

    public <Model extends Base> ContentValues getContentValuesForCreate(final Model model) throws IllegalAccessException {
        ContentValues values = new ContentValues();

        String cloudId = model.getCloudId();
        if(cloudId == null){
            values.putNull("cloudId");
        }

        Date createdAt = model.getCreatedAt();
        if(createdAt == null){
            createdAt = new Date();
            model.setCreatedAt(createdAt);
        }
        values.put("createdAt", createdAt.getTime());

        Date updatedAt = model.getUpdatedAt();
        if(updatedAt == null){
            updatedAt = new Date();
            model.setUpdatedAt(updatedAt);
        }
        values.put("updatedAt", updatedAt.getTime());


        Set<Field> fields = ReflectionUtil.getFields(model.getClass(), simpleFields);
        for(Field field : fields){
            field.setAccessible(true);

            String name = field.getName();
            Object data = field.get(model);

            if(data == null){
                values.putNull(name);
                continue;
            }

            if(data instanceof String){
                values.put(name, (String) data);
            }

            if(data instanceof Integer){
                values.put(name, (Integer) data);
            }

            if(data instanceof Date){
                values.put(name, ((Date) data).getTime());
            }
        }
        return values;
    }

    public <Model extends Base> String[] getColumns(Class<Model> clazz) {
        List<String> result = new ArrayList<String>();
        result.add("_id");
        result.add("cloudId");
        result.add("createdAt");
        result.add("updatedAt");
        for(Field field : ReflectionUtil.getFields(clazz, simpleFields)){
            result.add(field.getName());
        }
        return result.toArray(new String[result.size()]);
    }

    public <Model extends Base> Model getModel(Cursor cursor, Class<? extends Base> clazz) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Model model = (Model) clazz.getConstructor().newInstance();

        model.setLocalId(cursor.getLong(cursor.getColumnIndex("_id")));
        model.setCloudId(cursor.getString(cursor.getColumnIndex("cloudId")));
        model.setCreatedAt(new java.sql.Date(cursor.getLong(cursor.getColumnIndex("createdAt"))));
        model.setUpdatedAt(new java.sql.Date(cursor.getLong(cursor.getColumnIndex("updatedAt"))));

        for(Field field : ReflectionUtil.getFields(clazz, simpleFields)){
            field.setAccessible(true);
            Class type = field.getType();

            if(type.equals(Integer.class)){
                field.set(model, cursor.getInt(cursor.getColumnIndex(field.getName())));
            }

            if(type.equals(String.class)){
                field.set(model, cursor.getString(cursor.getColumnIndex(field.getName())));
            }

            if(type.equals(Date.class)){
                Date date = new java.sql.Date(cursor.getLong(cursor.getColumnIndex(field.getName())));
                field.set(model, date);
            }
        }

        return model;
    }

    interface QueryExecutor {
        void exec(SQLiteDatabase db);
    }

    private void execInTransaction(SQLiteDatabase db, QueryExecutor executor){
        db.beginTransaction();
        try {
            executor.exec(db);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public String getTableName(Class<? extends Base> clazz){
        return clazz.getSimpleName().toUpperCase();
    }

    private String getCreateTableQuery(final Class<? extends Base> clazz) {
        final String lineSeparator = System.getProperty("line.separator");
        final String tab = "    ";
        StringBuilder sb = new StringBuilder("CREATE TABLE " + getTableName(clazz) + " ( " +
            tab + "_id INTEGER primary key not null, " + lineSeparator +
            tab + "cloudId TEXT, " + lineSeparator +
            tab + "createdAt INTEGER, " + lineSeparator +
            tab + "updatedAt INTEGER, " + lineSeparator
        );

        Set<Field> fields = ReflectionUtil.getFields(clazz, simpleFields);

        Iterator<Field> it = fields.iterator();
        while(it.hasNext()){
            Field field = it.next();
            String name = field.getName();
            String type = getType(field);
            sb.append(tab + name + " " + type + " " + (it.hasNext()?", ":"") + lineSeparator);
        }
        sb.append(") ");
        return sb.toString();
    }

    private String getType(Field field){
        if(field.getType().equals(Integer.class)){
            return "INTEGER";
        } else if(field.getType().equals(String.class)){
            return "TEXT";
        } else if(field.getType().equals(Date.class)){
            return "INTEGER";
        }
        throw new RuntimeException("Not supported type");
    }


}
