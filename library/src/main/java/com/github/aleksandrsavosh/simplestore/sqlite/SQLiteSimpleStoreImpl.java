package com.github.aleksandrsavosh.simplestore.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.github.aleksandrsavosh.simplestore.Base;
import com.github.aleksandrsavosh.simplestore.LogUtil;
import com.github.aleksandrsavosh.simplestore.SimpleStore;
import com.github.aleksandrsavosh.simplestore.exception.CreateException;
import com.github.aleksandrsavosh.simplestore.exception.ReadException;

public class SQLiteSimpleStoreImpl<Model extends Base> implements SimpleStore<Model, Long> {

    Class<Model> clazz;
    SQLiteHelper sqLiteHelper;

    public SQLiteSimpleStoreImpl(Class<Model> clazz, SQLiteHelper sqLiteHelper) {
        this.clazz = clazz;
        this.sqLiteHelper = sqLiteHelper;
    }

    @Override
    public Model create(Model model) {
        try {
            return createThrowException(model);
        } catch (CreateException e){
            LogUtil.toLog("Create exception", e);
        }
        return null;
    }

    @Override
    public Model createThrowException(Model model) throws CreateException {
        try {
            SQLiteDatabase database = sqLiteHelper.getWritableDatabase();
            Long localId = database.insert(
                    sqLiteHelper.getTableName(model.getClass()),
                    null,
                    sqLiteHelper.getContentValuesForCreate(model)
            );
            model.setLocalId(localId);
        } catch(Exception e){
            throw new CreateException(e);
        }
        return model;
    }

    @Override
    public Model read(Long pk) {
        try {
            return readThrowException(pk);
        } catch (ReadException e){
            LogUtil.toLog("Read exception", e);
        }
        return null;
    }

    @Override
    public Model readThrowException(Long pk) throws ReadException {

        SQLiteDatabase database = sqLiteHelper.getWritableDatabase();
        Cursor cursor = database.query(
            sqLiteHelper.getTableName(clazz),
            sqLiteHelper.getColumns(clazz),
            "_id=?",
            new String[]{Long.toString(pk)},
            null,
            null,
            null
        );

        if(!cursor.moveToNext()) {
            cursor.close();
            throw new ReadException("Date not found");
        }

        try {
            return sqLiteHelper.getModel(cursor, clazz);
        } catch (Exception e){
            throw new ReadException("Create model exception");
        } finally {
            cursor.close();
        }
    }

}
