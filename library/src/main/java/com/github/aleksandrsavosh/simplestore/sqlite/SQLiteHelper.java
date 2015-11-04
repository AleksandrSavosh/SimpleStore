package com.github.aleksandrsavosh.simplestore.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.github.aleksandrsavosh.simplestore.*;

import java.lang.reflect.Field;
import java.util.*;

public class SQLiteHelper extends SQLiteOpenHelper {

    public SQLiteHelper(Context context, String name, int version) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final List<String> queries = new ArrayList<String>();
        for(Class<? extends Base> clazz : Const.modelClasses){
            queries.add(SimpleStoreUtil.getCreateTableQuery(clazz));
        }

        //create tables for model
        execInTransaction(db, new QueryExecutor() {
            @Override
            public void exec(SQLiteDatabase db) {
                for (String query : queries) {
                    LogUtil.toLog(query);
                    db.execSQL(query);
                }
            }
        });

        //create table for relations between models
        final List<String> queriesOfCreateRelationTables = new ArrayList<String>();
        for(Class<? extends Base> clazz : Const.modelClasses){
            for(Field field : ReflectionUtil.getFields(clazz, new HashSet<Class>(){{ addAll(Const.modelClasses); }})){
                field.setAccessible(true);
                Class<? extends Base> type = (Class<? extends Base>) field.getType();
                queriesOfCreateRelationTables.add(SimpleStoreUtil.getCreateRelationTableQuery(clazz, type));
            }

            for(Field field : ReflectionUtil.getFields(clazz, new HashSet<Class>(){{
                add(Collection.class);
                add(List.class);
                add(Set.class);
            }})){
                field.setAccessible(true);
                Class<? extends Base> type = ReflectionUtil.getGenericType(field);
                queriesOfCreateRelationTables.add(SimpleStoreUtil.getCreateRelationTableQuery(clazz, type));
            }
        }

        execInTransaction(db, new QueryExecutor() {
            @Override
            public void exec(SQLiteDatabase db) {
                for (String query : queriesOfCreateRelationTables) {
                    LogUtil.toLog(query);
                    db.execSQL(query);
                }
            }
        });
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        //get all table names
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        //drop all tables
        if (c.moveToFirst()) {
            while ( !c.isAfterLast() ) {
                db.execSQL("DROP TABLE IF EXISTS " + c.getString(0));
                c.moveToNext();
            }
        }

        c.close();

        onCreate(db);
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


}
