package com.github.aleksandrsavosh.simplestore.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.github.aleksandrsavosh.simplestore.Base;
import com.github.aleksandrsavosh.simplestore.ReflectionUtil;

import java.lang.reflect.Field;
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
        for(Class clazz : dbClasses){
            queries.add(getCreateTableQuery(clazz));
        }

        execInTransaction(db, new QueryExecutor() {
            @Override
            public void exec(SQLiteDatabase db) {
                for (String query : queries) {
                    System.out.println(query);
                    db.execSQL(query);
                }
            }
        });

    }

    public String generateId(Class<? extends Base> clazz) {
        String id;
        Cursor cursor;
        do {
            id = (int) (Math.random() * Integer.MAX_VALUE) + "";
            SQLiteDatabase db = getReadableDatabase();
            cursor = db.query(
                    getTableName(clazz),
                    new String[]{"_id"},
                    "_id=?",
                    new String[]{id},
                    null,
                    null,
                    null
            );
        } while(cursor.getCount() != 0);

        return id;
    }

    public <Model extends Base> String getInsertQuery(Model model) {
        return null;
    }

    public <Model extends Base> ContentValues getContentValues(Model model) throws IllegalAccessException {
        ContentValues values = new ContentValues();
        for(Field field : ReflectionUtil.getFieldsWithAccessible(model.getClass(), simpleFields)){
//            String name = getName(field);
//            String type = getType(field);
//
//            if(type.equals("INTEGER") && name.equals("_id")){
//                values.put(name, Integer.parseInt((String) field.get(model)));
//            } else if(type.equals("INTEGER")){
//                values.put(name, field.getInt(model));
//            } else if(type.equals(""))


        }
        return null;
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

    private String getCreateTableQuery(Class<? extends Base> clazz) {
        final String pk = "primary key not null";
        final String lineSeparator = System.getProperty("line.separator");
        final String tab = "    ";
        StringBuilder sb = new StringBuilder(" CREATE TABLE " + getTableName(clazz) + " ( ");

        Set<Field> fields = ReflectionUtil.getFieldsWithAccessible(clazz, simpleFields);
        Iterator<Field> it = fields.iterator();
        while(it.hasNext()){
            Field field = it.next();
            String name = field.getName();
            String type = getType(field);
            sb.append(" " + tab + name + " " + type + " " +
                    (isPrimary(name)?pk:"") +
                    (it.hasNext()?", ":"") +
                    lineSeparator);
        }
        sb.append(" ) ");
        return sb.toString();
    }

    private String getName(Field field){
        String name = field.getName();
        if(name.equals("objectId")){
            name = "_id";
        }
        return name;
    }

    private String getType(Field field){

        if(field.getName().equals("objectId")){
            return "INTEGER";
        }

        if(field.getType().equals(Integer.class)){
            return "INTEGER";
        } else if(field.getType().equals(String.class)){
            return "TEXT";
        } else if(field.getType().equals(Date.class)){
            return "NUMERIC";
        }
        throw new RuntimeException("Not supported type");
    }

    private boolean isPrimary(String name){
        return name.equals("objectId");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
