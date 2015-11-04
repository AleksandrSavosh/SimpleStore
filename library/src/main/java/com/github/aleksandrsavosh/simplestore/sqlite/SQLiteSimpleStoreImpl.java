package com.github.aleksandrsavosh.simplestore.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.github.aleksandrsavosh.simplestore.*;
import com.github.aleksandrsavosh.simplestore.exception.CreateException;
import com.github.aleksandrsavosh.simplestore.exception.DeleteException;
import com.github.aleksandrsavosh.simplestore.exception.ReadException;
import com.github.aleksandrsavosh.simplestore.exception.UpdateException;

import java.util.ArrayList;
import java.util.List;

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
        return createThrowExceptionCommon(model);
    }

    private <T extends Base> T createThrowExceptionCommon(T model) throws CreateException {
        try {
            SQLiteDatabase database = sqLiteHelper.getWritableDatabase();
            Long localId = database.insert(
                    SimpleStoreUtil.getTableName(model.getClass()),
                    null,
                    SimpleStoreUtil.getContentValuesForCreate(model)
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
            SimpleStoreUtil.getTableName(clazz),
                SimpleStoreUtil.getColumns(clazz),
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
            return SimpleStoreUtil.getModel(cursor, clazz);
        } catch (Exception e){
            throw new ReadException("Create model exception");
        } finally {
            cursor.close();
        }
    }

    @Override
    public Model update(Model model) {
        try {
            return updateThrowException(model);
        } catch (UpdateException e){
            LogUtil.toLog("Update exception", e);
        }
        return null;
    }

    @Override
    public Model updateThrowException(Model model) throws UpdateException {
        int row;
        try {
            SQLiteDatabase database = sqLiteHelper.getWritableDatabase();

            row = database.update(
                    SimpleStoreUtil.getTableName(clazz),
                    SimpleStoreUtil.getContentValuesForUpdate(model),
                    "_id=?",
                    new String[]{Long.toString(model.getLocalId())}
            );

        } catch (Exception e){
            throw new UpdateException("Update model exception");
        }

        if(row == 0){
            throw new UpdateException("Not found model for update");
        }

        return model;
    }

    @Override
    public boolean delete(Long pk) {
        try {
            return deleteThrowException(pk);
        } catch (DeleteException e){
            LogUtil.toLog("Delete exception", e);
        }
        return false;
    }

    @Override
    public boolean deleteThrowException(Long pk) throws DeleteException {
        SQLiteDatabase database = sqLiteHelper.getWritableDatabase();

        int row = database.delete(
                SimpleStoreUtil.getTableName(clazz),
                "_id=?",
                new String[]{Long.toString(pk)}
        );

        if(row == 0){
            throw new DeleteException("No models found for delete");
        }

        return true;
    }

    @Override
    public Model createWithRelations(Model model) {
        try {
            return createWithRelationsThrowException(model);
        } catch(CreateException e){
            LogUtil.toLog("Create with relations exception", e);
        }
        return null;
    }

    @Override
    public Model createWithRelationsThrowException(Model model) throws CreateException {
        return createWithRelationsThrowExceptionCommon(model);
    }

    private <T extends Base> T createWithRelationsThrowExceptionCommon(T model) throws CreateException {
        try {
            //create parent
            model = createThrowExceptionCommon(model);

            //create childs
            List<? extends Base> childs = SimpleStoreUtil.getModelChildrenObjects(model);
            for(Base base : childs){
                base = createWithRelationsThrowExceptionCommon(base);

                //create relations
                if(!appendChildToParentCommon(model, base)){
                    throw new CreateException("Not create relation");
                }
            }

            return model;
        } catch (IllegalAccessException e) {
            throw new CreateException("Can not create model with relations", e);
        }
    }

    private boolean appendChildToParentCommon(Base parent, Base child) throws CreateException {
        SQLiteDatabase database = sqLiteHelper.getWritableDatabase();
        long rowId = database.insert(
                SimpleStoreUtil.getTableName(parent.getClass(), child.getClass()),
                null,
                SimpleStoreUtil.getContentValuesForRelationClasses(parent, child)
        );
        return rowId != -1;
    }
}
