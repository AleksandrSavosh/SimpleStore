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

        //create table for relations between models
        for(Class<? extends Base> clazz : Const.modelClasses){
            for(Field field : ReflectionUtil.getFields(clazz, Const.modelClasses)){
                field.setAccessible(true);
                Class<? extends Base> type = (Class<? extends Base>) field.getType();
                queries.add(SimpleStoreUtil.getCreateRelationTableQuery(clazz, type));
            }

            for(Field field : ReflectionUtil.getFields(clazz, new HashSet<Class>(){{
                add(Collection.class);
                add(List.class);
                add(Set.class);
            }})){
                field.setAccessible(true);
                Class<? extends Base> type = ReflectionUtil.getGenericType(field);
                queries.add(SimpleStoreUtil.getCreateRelationTableQuery(clazz, type));
            }
        }

        execInTransaction(db, new QueryExecutor() {
            @Override
            public void exec(SQLiteDatabase db) {
                for (String query : queries) {
                    LogUtil.toLog(query, true);
                    db.execSQL(query);
                }
            }
        });
    }

    public static boolean isNeedSaveData = false;
    @Override
    @SuppressWarnings("unchecked")
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //get all table names
        List<String> tableNames = SimpleStoreUtil.getAllTableNames(db);

        //mark all tables deprecated
        LogUtil.toLog("--- mark all tables deprecated ---");
        for (String tableName : tableNames) {
            String query = "ALTER TABLE " + tableName + " RENAME TO " + tableName + "_deprecated";
            LogUtil.toLog(query);
            db.execSQL(query);
        }
        LogUtil.toLog("------------------------------");

        //create new tables
        onCreate(db);

        if(isNeedSaveData) {
            //spill data from deprecated tables to new tables
            LogUtil.toLog("--- spill data from deprecated tables to new tables ---");
            final List<String> queries = new ArrayList<String>();
            for (String tableName : tableNames) {
                if (isExistsTable(db, tableName)) {
                    List<String> depCols = getColumns(db, tableName + "_deprecated");
                    List<String> newCols = getColumns(db, tableName);

                    depCols.retainAll(newCols);// common columns

                    if (!depCols.isEmpty()) {
                        queries.add(createSpillQuery(tableName, depCols));
                    }
                }
            }

            execInTransaction(db, new QueryExecutor() {
                @Override
                public void exec(SQLiteDatabase db) {
                    for (String query : queries) {
                        LogUtil.toLog(query, true);
                        db.execSQL(query);
                    }
                }
            });
            LogUtil.toLog("------------------------------");
        }


        //drop deprecated tables
        LogUtil.toLog("--- drop deprecated tables ---");
        for (String tableName : tableNames) {
            String query = "DROP TABLE IF EXISTS " + tableName + "_deprecated";
            LogUtil.toLog(query);
            db.execSQL(query);
        }
        LogUtil.toLog("------------------------------");
    }

    public String createSpillQuery(String tableName, List<String> cols){
        final String lineSeparator = System.getProperty("line.separator");

        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < cols.size(); i++){
            sb.append(cols.get(i));

            if(i + 1 != cols.size()){
                sb.append(",");
            }
        }

        return " " + lineSeparator +
                "INSERT INTO " + tableName + " ( " + sb.toString() + " ) " + lineSeparator +
                "SELECT " + sb.toString() + " FROM " + tableName + "_deprecated " + lineSeparator;
    }

    public List<String> getColumns(SQLiteDatabase db, String tableName){
        List<String> cols = new ArrayList<String>();
        Cursor ti = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
        if ( ti.moveToFirst() ) {
            do {
                cols.add(ti.getString(1));
            } while (ti.moveToNext());
        }
        return cols;
    }

    public boolean isExistsTable(SQLiteDatabase db, String tableName){
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", new String[]{tableName});
        try{
            return c.moveToFirst();
        } finally {
            c.close();
        }
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
