package com.github.aleksandrsavosh.simplestore.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.github.aleksandrsavosh.simplestore.*;
import com.github.aleksandrsavosh.simplestore.exception.*;

import java.lang.reflect.Field;
import java.util.*;

public class SQLiteSimpleStoreImpl extends AbstractSimpleStore<Long> {

    private SQLiteDatabase database;

    public void setDatabase(SQLiteDatabase database) {
        this.database = database;
    }

    @Override
    public <Model extends Base> Model createThrowException(Model model) throws CreateException {
        try {
            Long localId = database.insert(
                    SimpleStoreUtil.getTableName(model.getClass()),
                    null,
                    SimpleStoreUtil.getContentValuesForCreate(model)
            );
            model.setLocalId(localId);
            return model;
        } catch(Exception e){
            throw new CreateException(e);
        }
    }

    @Override
    public <Model extends Base> Model readThrowException(Long pk, Class<Model> clazz) throws ReadException {
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
            throw new DataNotFoundException("Date not found");
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
    public <Model extends Base> List<Model> readByThrowException(Class<Model> clazz, KeyValue... keyValues) throws ReadException {
        Cursor cursor = database.query(
                SimpleStoreUtil.getTableName(clazz),
                new String[]{ "_id" },
                SimpleStoreUtil.getSelectionFilter(keyValues),
                SimpleStoreUtil.getSelectionFilterArguments(keyValues),
                null, null, null
        );
        List<Model> models = new ArrayList<Model>();
        try {
            while (cursor.moveToNext()) {
                models.add(readThrowException(cursor.getLong(1), clazz));
            }
        } finally {
            cursor.close();
        }
        return models;
    }

    @Override
    public <Model extends Base> List<Model> readAllThrowException(Class<Model> clazz) throws ReadException {
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
    public <Model extends Base> Model updateThrowException(Model model) throws UpdateException {
        int row;
        try {
            row = database.update(
                    SimpleStoreUtil.getTableName(model.getClass()),
                    SimpleStoreUtil.getContentValuesForUpdate(model),
                    "_id=?",
                    new String[]{Long.toString(model.getLocalId())}
            );
        } catch (Exception e){
            throw new UpdateException("Update model exception");
        }
        if(row == 0){
            throw new UpdateException(new DataNotFoundException("Not found model for update"));
        }
        return model;
    }

    @Override
    public <Model extends Base> boolean deleteThrowException(Long pk, Class<Model> clazz) throws DeleteException {
        //delete from object table
        int result = database.delete(
                SimpleStoreUtil.getTableName(clazz),
                "_id=?",
                new String[]{Long.toString(pk)}
        );
        //delete from all relation table
        List<String> allRelationTableNames = SimpleStoreUtil.getAllRelationTableNames(database, clazz);
        String columnName = SimpleStoreUtil.getRelationTableColumn(clazz);
        for(String table : allRelationTableNames){
            database.delete(table, columnName + "=?", new String[]{Long.toString(pk)});
        }
        return result != 0;
    }

    @Override
    public <Model extends Base> Model createWithRelationsThrowException(Model model) throws CreateException {
        try {
            //create parent
            model = createThrowException(model);
            //create children
            List<? extends Base> children = SimpleStoreUtil.getModelChildrenObjects(model);
            for(Base child : children){
                child = createWithRelationsThrowException(child);

                //create relation
                createRelationThrowException(model, child);
            }
            return model;
        } catch (IllegalAccessException e) {
            throw new CreateException("Can not create model with relations", e);
        }
    }

    @Override
    public <Model extends Base> Model readWithRelationsThrowException(Long pk, Class<Model> clazz) throws ReadException {
        //read parent
        Model model = readThrowException(pk, clazz);
        //read one to one relations
        for(Field field : ReflectionUtil.getFields(clazz, Const.modelClasses)){
            field.setAccessible(true);
            Class childClazz = field.getType();
            Long childPk = null;
            try {
                childPk = readRelationThrowException(model.getLocalId(), clazz, childClazz);
            } catch (DataNotFoundException unused){}
            if(childPk != null){
                Base child = readWithRelationsThrowException(childPk, childClazz);
                try {
                    field.set(model, child);
                } catch (IllegalAccessException e) {
                    throw new ReadException(e);
                }
            }
        }
        //read one to many relations
        for(Field field : ReflectionUtil.getFields(clazz, Const.collections)) {
            field.setAccessible(true);
            Class collType = field.getType();
            Class childClazz = ReflectionUtil.getGenericType(field);

            Collection collection = ReflectionUtil.getCollectionInstance(collType);

            List<Long> ids = readRelationsThrowException(model.getLocalId(), clazz, childClazz);
            for(Long id : ids){
                Base child = readWithRelationsThrowException(id, childClazz);
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

    @Override
    public <Model extends Base> List<Model> readByWithRelationsThrowException(Class<Model> clazz, KeyValue... keyValues) throws ReadException {
        Cursor cursor = database.query(
                SimpleStoreUtil.getTableName(clazz),
                new String[]{ "_id" },
                SimpleStoreUtil.getSelectionFilter(keyValues),
                SimpleStoreUtil.getSelectionFilterArguments(keyValues),
                null, null, null
        );
        List<Model> models = new ArrayList<Model>();
        try {
            while (cursor.moveToNext()) {
                models.add(readWithRelationsThrowException(cursor.getLong(1), clazz));
            }
        } finally {
            cursor.close();
        }
        return models;
    }

    @Override
    public <Model extends Base> List<Model> readAllWithRelationsThrowException(Class<Model> clazz) throws ReadException {
        Cursor cursor = database.query(
                SimpleStoreUtil.getTableName(clazz),
                new String[]{ "_id" },
                null, null, null, null, null
        );
        List<Model> models = new ArrayList<Model>();
        try {
            while (cursor.moveToNext()) {
                models.add(readWithRelationsThrowException(cursor.getLong(1), clazz));
            }
        } finally {
            cursor.close();
        }
        return models;
    }

    @Override
    public <Model extends Base> Model updateWithRelationsThrowException(Model model) throws UpdateException {
        try {
            deleteWithRelationsThrowException(model.getLocalId(), model.getClass());
            return createWithRelationsThrowException(model);
        } catch (Exception e) {
            throw new UpdateException(e);
        }
    }

    @Override
    public <Model extends Base> boolean deleteWithRelationsThrowException(Long pk, Class<Model> clazz) throws DeleteException {
        //delete children one to one
        for(Field field : ReflectionUtil.getFields(clazz, Const.modelClasses)){
            field.setAccessible(true);
            Class childClazz = field.getType();
            Long childPk = null;
            try {
                childPk = readRelationThrowException(pk, clazz, childClazz);
            } catch (DataNotFoundException unused){
            } catch (ReadException e) {
                throw new DeleteException(e);
            }
            if(childPk != null){
                deleteWithRelationsThrowException(childPk, childClazz);
            }
        }
        //delete children one to many
        for(Field field : ReflectionUtil.getFields(clazz, Const.collections)) {
            field.setAccessible(true);
            Class collType = field.getType();
            Class childClazz = ReflectionUtil.getGenericType(field);
            Collection collection = ReflectionUtil.getCollectionInstance(collType);
            List<Long> ids = null;
            try {
                 ids = readRelationsThrowException(pk, clazz, childClazz);
            } catch (DataNotFoundException unused) {
            } catch (ReadException e) {
                throw new DeleteException(e);
            }
            if(ids != null){
                for(Long childPk : ids){
                    deleteWithRelationsThrowException(childPk, childClazz);
                }
            }
        }
        //delete parent
        return deleteThrowException(pk, clazz);
    }



    @Override
    public <M extends Base, C extends Base> boolean createRelationThrowException(M model, C child) throws CreateException {
        return createRelationThrowException(model.getLocalId(), model.getClass(), child.getLocalId(), child.getClass());
    }

    @Override
    public <M extends Base, C extends Base> boolean createRelationThrowException(Long pk, Class<M> clazz, Long subPk, Class<C> subClazz) throws CreateException {
        long rowId = database.insert(
                SimpleStoreUtil.getTableName(clazz, subClazz),
                null,
                SimpleStoreUtil.getContentValuesForRelationClasses(pk, clazz, subPk, subClazz)
        );
        if(rowId == -1){
            throw new CreateException("create relation error");
        }
        return true;
    }

    @Override
    public <M extends Base, C extends Base> boolean createRelationsThrowException(M model, Collection<C> subModels) throws CreateException {
        Collection<Long> subPks = new ArrayList<>();
        Class subClazz = null;
        for(C subModel : subModels){
            subPks.add(subModel.getLocalId());
            subClazz = subModel.getClass();
        }
        if(subClazz == null){
            throw new CreateException("doesn't define subClass, may be subModels is empty");
        }
        return createRelationsThrowException(model.getLocalId(), model.getClass(), subPks, subClazz);
    }

    @Override
    public <M extends Base, C extends Base> boolean createRelationsThrowException(Long pk, Class<M> clazz, Collection<Long> subPks, Class<C> subClazz) throws CreateException {
        long rowId = database.insert(
                SimpleStoreUtil.getTableName(clazz, subClazz),
                null,
                SimpleStoreUtil.getContentValuesForRelationClasses(pk, clazz, subPks, subClazz)
        );
        if(rowId == -1){
            throw new CreateException("create relation error");
        }
        return true;
    }


    @Override
    public <M extends Base, C extends Base> C readRelationThrowException(M model, Class<C> subClazz) throws ReadException {
        Long subId = readRelationThrowException(model.getLocalId(), model.getClass(), subClazz);
        return readThrowException(subId, subClazz);
    }

    @Override
    public <M extends Base, C extends Base> Long readRelationThrowException(Long pk, Class<M> modelClazz, Class<C> subClazz)
            throws ReadException {
        List<Long> ids = readRelationsThrowException(pk, modelClazz, subClazz);
        if(ids.size() > 1){
            throw new ReadException("Too many ids for one relation");
        }
        return ids.get(0);
    }

    @Override
    public <M extends Base, C extends Base> List<C> readRelationsThrowException(M model, Class<C> subClazz) throws ReadException {
        List<Long> subPks = readRelationsThrowException(model.getLocalId(), model.getClass(), subClazz);
        List<C> list = (List<C>) ReflectionUtil.getCollectionInstance(ArrayList.class);
        for(Long subPk : subPks){
            list.add(readThrowException(subPk, subClazz));
        }
        return list;
    }

    @Override
    public <M extends Base, C extends Base> List<Long> readRelationsThrowException(Long pk, Class<M> modelClazz, Class<C> subClazz) throws ReadException {
        List<Long> ids = new ArrayList<Long>();
        Cursor cursor = database.query(
                SimpleStoreUtil.getTableName(modelClazz, subClazz),
                SimpleStoreUtil.getRelationTableColumns(modelClazz, subClazz),
                SimpleStoreUtil.getRelationTableColumn(modelClazz) + "=?",
                new String[]{Long.toString(pk)},
                null,
                null,
                null
        );
        while(cursor.moveToNext()) {
            ids.add(cursor.getLong(cursor.getColumnIndex(SimpleStoreUtil.getRelationTableColumn(subClazz))));
        }
        cursor.close();
        if(ids.size() == 0){
            throw new DataNotFoundException("No relations for model");
        }
        return ids;
    }


    @Override
    public <M extends Base, C extends Base> boolean deleteRelationThrowException(M model, C subModel) throws DeleteException {
        return false;
    }

    @Override
    public <M extends Base, C extends Base> boolean deleteRelationThrowException(Long aLong, Class<M> clazz, Long subPk, Class<C> subClazz) throws DeleteException {
        return false;
    }

    @Override
    public <M extends Base, C extends Base> boolean deleteRelationsThrowException(M model, Collection<C> subModels) throws DeleteException {
        return false;
    }

    @Override
    public <M extends Base, C extends Base> boolean deleteRelationsThrowException(Long aLong, Class<M> clazz, Collection<Long> subPks, Class<C> subClazz) throws DeleteException {
        return false;
    }
}
