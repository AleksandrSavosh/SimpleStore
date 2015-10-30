package com.github.aleksandrsavosh.simplestore.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.github.aleksandrsavosh.simplestore.Base;
import com.github.aleksandrsavosh.simplestore.SimpleStore;

public class SQLiteSimpleStoreImpl<Model extends Base> implements SimpleStore<Model> {

    class Helper extends SQLiteOpenHelper {

        public Helper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    @Override
    public Model create(Model model) {
        return null;
    }

    @Override
    public Model read(String pk) {
        return null;
    }

    @Override
    public Model update(Model model) {
        return null;
    }

    @Override
    public boolean delete(String pk) {
        return false;
    }
}
