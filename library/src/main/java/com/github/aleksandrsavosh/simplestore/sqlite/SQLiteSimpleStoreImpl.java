package com.github.aleksandrsavosh.simplestore.sqlite;

import android.database.sqlite.SQLiteDatabase;
import com.github.aleksandrsavosh.simplestore.Base;
import com.github.aleksandrsavosh.simplestore.SimpleStore;

import java.util.Date;

public class SQLiteSimpleStoreImpl<Model extends Base> implements SimpleStore<Model> {

    Class<Model> clazz;
    SQLiteHelper sqLiteHelper;

    public SQLiteSimpleStoreImpl(Class<Model> clazz, SQLiteHelper sqLiteHelper) {
        this.clazz = clazz;
        this.sqLiteHelper = sqLiteHelper;
    }

    @Override
    public Model create(Model model) {

        String objectId = sqLiteHelper.generateId(clazz);
        model.setObjectId(objectId);
        model.setCreatedAt(new Date());
        model.setUpdatedAt(new Date());

        SQLiteDatabase database = sqLiteHelper.getWritableDatabase();

        database.insert(
                sqLiteHelper.getTableName(model.getClass()),
                null,
                sqLiteHelper.getContentValues(model)
        );

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
