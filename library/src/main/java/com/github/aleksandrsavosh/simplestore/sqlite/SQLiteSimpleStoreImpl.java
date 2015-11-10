package com.github.aleksandrsavosh.simplestore.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.github.aleksandrsavosh.simplestore.*;
import com.github.aleksandrsavosh.simplestore.exception.*;

import java.lang.reflect.Field;
import java.util.*;

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
        return readThrowExceptionCommon(pk, clazz);
    }

    public <T extends Base> T readThrowExceptionCommon(Long modelPk, Class<T> modelClazz) throws ReadException {
        SQLiteDatabase database = sqLiteHelper.getWritableDatabase();
        Cursor cursor = database.query(
                SimpleStoreUtil.getTableName(modelClazz),
                SimpleStoreUtil.getColumns(modelClazz),
                "_id=?",
                new String[]{Long.toString(modelPk)},
                null,
                null,
                null
        );

        if(!cursor.moveToNext()) {
            cursor.close();
            throw new DataNotFoundException("Date not found");
        }

        try {
            return SimpleStoreUtil.getModel(cursor, modelClazz);
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
        try {
            return updateCommonThrowException(model);
        } catch (Exception e){
            throw new UpdateException(e);
        }
    }

    public <T extends Base> T updateCommonThrowException(T model) throws UpdateException, DataNotFoundException {
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
            throw new DataNotFoundException("Not found model for update");
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
        if(!deleteCommon(pk, clazz)){
            throw new DeleteException("No models found for delete");
        }
        return true;
    }

    public boolean deleteCommon(Long pk, Class<? extends Base> clazz){
        SQLiteDatabase database = sqLiteHelper.getWritableDatabase();

        //delete from object table
        int result = database.delete(
                SimpleStoreUtil.getTableName(clazz),
                "_id=?",
                new String[]{Long.toString(pk)}
        );


        //delete from all relation table
        List<String> allRelationTableNames = SimpleStoreUtil.getAllRelationTableNames(database, clazz);

        for(String table : allRelationTableNames){
            String columnName = SimpleStoreUtil.getRelationTableColumn(clazz);
            database.delete(table, columnName + "=?", new String[]{Long.toString(pk)});
        }

        return result != 0;
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

    @Override
    public Model readWithRelations(Long aLong) {
        try {
            return readWithRelationsThrowException(aLong);
        } catch(ReadException e){
            LogUtil.toLog("Read with relations exception", e);
        }
        return null;
    }

    @Override
    public Model readWithRelationsThrowException(Long pk) throws ReadException {
        return readWithRelationsThrowExceptionCommon(pk, clazz);
    }

    public <T extends Base> T readWithRelationsThrowExceptionCommon(Long modelPk, Class<T> modelClazz) throws ReadException {
        //read parent
        T model = readThrowExceptionCommon(modelPk, modelClazz);

        //read one to one relations
        for(Field field : ReflectionUtil.getFields(modelClazz, new HashSet<Class>(){{ addAll(Const.modelClasses); }})){
            field.setAccessible(true);
            Class type = field.getType();

            List<Long> ids = getRelationsIds(modelPk, modelClazz, type);
            if(ids.size() > 1){
                throw new ReadException("To much children for one model property");
            }

            if(ids.size() == 1){
                try {
                    Base child = readWithRelationsThrowExceptionCommon(ids.get(0), type);
                    field.set(model, child);
                } catch (IllegalAccessException e) {
                    throw new ReadException(e);
                }
            }
        }

        //read one to many relations
        for(Field field : ReflectionUtil.getFields(modelClazz, Const.collections)) {
            field.setAccessible(true);
            Class collType = field.getType();
            Class genType = ReflectionUtil.getGenericType(field);

            Collection collection = ReflectionUtil.getCollectionInstance(collType);

            List<Long> ids = getRelationsIds(modelPk, modelClazz, genType);
            for(Long id : ids){
                Base child = readWithRelationsThrowExceptionCommon(id, genType);
                collection.add(child);
            }
            try {
                field.set(model, collection);
            } catch (IllegalAccessException e) {
                throw new ReadException(e);
            }
        }

        return model;
    }

    private <M extends Base, C extends Base> List<Long> getRelationsIds(Long modelPk, Class<M> modelClazz, Class<C> child){
        List<Long> ids = new ArrayList<Long>();

        SQLiteDatabase database = sqLiteHelper.getReadableDatabase();
        Cursor cursor = database.query(
                SimpleStoreUtil.getTableName(modelClazz, child),
                SimpleStoreUtil.getRelationTableColumns(modelClazz, child),
                SimpleStoreUtil.getRelationTableColumn(modelClazz) + "=?",
                new String[]{Long.toString(modelPk)},
                null,
                null,
                null
        );

        while(cursor.moveToNext()) {
            ids.add(cursor.getLong(cursor.getColumnIndex(SimpleStoreUtil.getRelationTableColumn(child))));
        }
        cursor.close();

        return ids;
    }

    @Override
    public Model updateWithRelationsThrowException(Model model) throws UpdateException {
        return null;
    }

    public <M extends Base, C extends Base> M updateWithRelationsCommonThrowException(M modelActual, M modelExists, Class<M> modelClazz)
            throws UpdateException, CreateException, ReadException {

        if(modelActual == null && modelExists == null){
            throw new UpdateException("nothing for update");
        }

        //create model, return model
        if(modelActual != null && modelExists == null){
            return createWithRelationsThrowExceptionCommon(modelActual);
        }

        //delete model, return null
        if(modelActual == null && modelExists != null){
            deleteWithRelationsThrowExceptionCommon(modelExists.getLocalId(), modelClazz);
            return null;
        }

        //update model
        M modelResult = updateCommonThrowException(modelActual);

        //update one to one relations
        for(Field field : ReflectionUtil.getFields(modelClazz, new HashSet<Class>(){{ addAll(Const.modelClasses); }})){
            field.setAccessible(true);
            Class<C> childClazz = (Class<C>) field.getType();
            List<Long> existsIds = getRelationsIds(modelExists.getLocalId(), modelClazz, childClazz);
            if(existsIds.size() > 1){
                throw new ReadException("To much children for one model property");
            }
            if(existsIds.size() == 1){
                try {
                    C childActual = (C) field.get(modelActual);
                    C childExists = readWithRelationsThrowExceptionCommon(existsIds.get(0), childClazz);
                    C childResult = updateWithRelationsCommonThrowException(childActual, childExists, childClazz);
                    if(childResult != null){
                        field.set(modelResult, childResult);
                    }
                } catch (IllegalAccessException e) {
                    throw new UpdateException(e);
                }
            }
        }


        //update one to many relations
        for(Field field : ReflectionUtil.getFields(modelClazz, Const.collections)) {
            field.setAccessible(true);
            Class collType = field.getType();
            Class<C> childClazz = ReflectionUtil.getGenericType(field);
            Collection resultCollection = ReflectionUtil.getCollectionInstance(collType);
            List<Long> existsIds = getRelationsIds(modelExists.getLocalId(), modelClazz, childClazz);

            List<C> childsForDelete = new ArrayList<C>();
            List<C> childsForCreate = new ArrayList<C>();


        }



        return modelResult;
    }


    @Override
    public boolean deleteWithRelations(Long aLong) {
        try {
            return deleteWithRelationsThrowException(aLong);
        } catch (DeleteException e) {
            LogUtil.toLog("Delete with relations exception", e);
        }
        return false;
    }

    @Override
    public boolean deleteWithRelationsThrowException(Long aLong) throws DeleteException {
        if(!deleteWithRelationsThrowExceptionCommon(aLong, clazz)){
            throw new DeleteException("No models found for delete");
        }
        return true;
    }

    public boolean deleteWithRelationsThrowExceptionCommon(Long pk, Class forClazz){
        LogUtil.toLog("DELETE FOR " + forClazz.getSimpleName() + " pk " + pk);
        Map<Class, List<Long>> childs = new HashMap<Class, List<Long>>();

        //find all childs one to one
        for(Field field : ReflectionUtil.getFields(forClazz, new HashSet<Class>(){{ addAll(Const.modelClasses); }})){
            field.setAccessible(true);
            Class type = field.getType();

            List<Long> ids = getRelationsIds(pk, forClazz, type);
            if(ids.size() > 0){
                childs.put(type, ids);
            }
        }

        //find all childs one to many
        for(Field field : ReflectionUtil.getFields(forClazz, Const.collections)) {
            field.setAccessible(true);
            Class genType = ReflectionUtil.getGenericType(field);

            List<Long> ids = getRelationsIds(pk, forClazz, genType);
            if(ids.size() > 0){
                childs.put(genType, ids);
            }
        }

        //delete parent
        boolean result = deleteCommon(pk, forClazz);

        for(Class key : childs.keySet()){
            for(Long id : childs.get(key)){
                deleteWithRelationsThrowExceptionCommon(id, key);
            }
        }

        return result;
    }


    @Override
    public List<Model> readAll() {
        try {
            return readAllThrowException();
        } catch (ReadException e) {
            LogUtil.toLog("Read all exception", e);
        }
        return new ArrayList<Model>();
    }

    @Override
    public List<Model> readAllThrowException() throws ReadException {
        SQLiteDatabase database = sqLiteHelper.getReadableDatabase();
        Cursor cursor = database.query(
                SimpleStoreUtil.getTableName(clazz),
                SimpleStoreUtil.getColumns(clazz),
                null, null, null, null, null
        );

        List<Model> models = new ArrayList<Model>();

        try {
            while(cursor.moveToNext()) {
                models.add((Model) SimpleStoreUtil.getModel(cursor, clazz));
            }
        } catch (Exception e) {
            throw new ReadException(e);
        } finally {
            cursor.close();
        }

        return models;
    }

    @Override
    public List<Model> readAllWithRelations() {
        try {
            return readAllWithRelationsThrowException();
        } catch (ReadException e) {
            LogUtil.toLog("Read all with relations exception", e);
        }
        return new ArrayList<Model>();
    }

    @Override
    public List<Model> readAllWithRelationsThrowException() throws ReadException {
        List<Model> models = new ArrayList<Model>();
        SQLiteDatabase database = sqLiteHelper.getReadableDatabase();
        Cursor cursor = database.query(
                SimpleStoreUtil.getTableName(clazz),
                new String[]{ "_id" },
                null, null, null, null, null
        );

        try {
            while (cursor.moveToNext()) {
                models.add(readWithRelationsThrowException(cursor.getLong(1)));
            }
        } finally {
            cursor.close();
        }

        return models;
    }

    @Override
    public List<Model> readBy(KeyValue... keyValues) {
        try {
            return readByThrowException(keyValues);
        } catch (ReadException e) {
            LogUtil.toLog("Read By exception", e);
        }
        return new ArrayList<Model>();
    }

    @Override
    public List<Model> readByThrowException(KeyValue... keyValues) throws ReadException {
        List<Model> models = new ArrayList<Model>();
        SQLiteDatabase database = sqLiteHelper.getReadableDatabase();
        Cursor cursor = database.query(
                SimpleStoreUtil.getTableName(clazz),
                new String[]{ "_id" },
                SimpleStoreUtil.getSelectionFilter(keyValues),
                SimpleStoreUtil.getSelectionFilterArguments(keyValues),
                null, null, null
        );

        try {
            while (cursor.moveToNext()) {
                models.add(readThrowException(cursor.getLong(1)));
            }
        } finally {
            cursor.close();
        }

        return models;
    }

    @Override
    public List<Model> readByWithRelations(KeyValue... keyValues) {
        try {
            return readByWithRelationsThrowException(keyValues);
        } catch (ReadException e) {
            LogUtil.toLog("Read By with relations exception", e);
        }
        return new ArrayList<Model>();
    }

    @Override
    public List<Model> readByWithRelationsThrowException(KeyValue... keyValues) throws ReadException {
        List<Model> models = new ArrayList<Model>();
        SQLiteDatabase database = sqLiteHelper.getReadableDatabase();
        Cursor cursor = database.query(
                SimpleStoreUtil.getTableName(clazz),
                new String[]{ "_id" },
                SimpleStoreUtil.getSelectionFilter(keyValues),
                SimpleStoreUtil.getSelectionFilterArguments(keyValues),
                null, null, null
        );

        try {
            while (cursor.moveToNext()) {
                models.add(readWithRelationsThrowException(cursor.getLong(1)));
            }
        } finally {
            cursor.close();
        }

        return models;
    }

//    @Override
    public List<Long> readParentIds(Class parentClazz, Long id) {
        try {
            return readParentIdsThrowException(parentClazz, id);
        } catch (Exception e) {
            LogUtil.toLog("Read parent ids exception", e);
        }
        return new ArrayList<Long>();
    }

//    @Override
    public List<Long> readParentIdsThrowException(Class parentClazz, Long id) {
        SQLiteDatabase database = sqLiteHelper.getReadableDatabase();
        Cursor cursor = database.query(
                SimpleStoreUtil.getTableName(clazz, parentClazz),
                new String[]{ SimpleStoreUtil.getRelationTableColumn(parentClazz) },
                SimpleStoreUtil.getRelationTableColumn(clazz) + "=?",
                new String[]{ "" + id },
                null, null, null
        );

        List<Long> ids = new ArrayList<Long>();
        try {
            while (cursor.moveToNext()) {
                ids.add(cursor.getLong(1));
            }
        } finally {
            cursor.close();
        }
        return ids;
    }

//    @Override
    public List<Long> readChildrenIds(Class childClazz, Long id) {
        try {
            return readChildrenIdsThrowException(childClazz, id);
        } catch (Exception e) {
            LogUtil.toLog("Read children ids exception", e);
        }
        return new ArrayList<Long>();
    }

//    @Override
    public List<Long> readChildrenIdsThrowException(Class childClazz, Long id) {
        SQLiteDatabase database = sqLiteHelper.getReadableDatabase();
        Cursor cursor = database.query(
                SimpleStoreUtil.getTableName(clazz, childClazz),
                new String[]{ SimpleStoreUtil.getRelationTableColumn(childClazz) },
                SimpleStoreUtil.getRelationTableColumn(clazz) + "=?",
                new String[]{ "" + id },
                null, null, null
        );

        List<Long> ids = new ArrayList<Long>();
        try {
            while (cursor.moveToNext()) {
                ids.add(cursor.getLong(1));
            }
        } finally {
            cursor.close();
        }
        return ids;
    }
}
