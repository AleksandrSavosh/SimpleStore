package com.github.aleksandrsavosh.simplestore.sqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
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
    public <Model extends Base> boolean createFast(List<Model> models, Class<Model> modelClass) {
        if(models == null || models.isEmpty()){
            return false;
        }

        try {
            String tableName = SimpleStoreUtil.getTableName(modelClass);
            Set<Field> fields = ReflectionUtil.getFields(modelClass, Const.fields);

            int batchCount = 200 / fields.size();
            int countFullInserts = (models.size() / batchCount);
            int countRestRows = models.size() % batchCount;

            if (countFullInserts > 0) {
                String query = getBatchInsertQuery(batchCount, fields, tableName);
//                LogUtil.toLog(query, true);
                SQLiteStatement statement = database.compileStatement(query);

                for (int i = 0; i < countFullInserts; i++) {
                    int index = 1;
                    for (int j = 0; j < batchCount; j++) {
                        statement.bindLong(index++, new Date().getTime());
                        statement.bindLong(index++, new Date().getTime());
                        Model model = models.get(i * batchCount + j);
                        for (Field field : fields) {
                            if (field.getType().equals(Integer.class)){
                                statement.bindLong(index++, (Long) field.get(model));
                            } else if(field.getType().equals(Long.class)) {
                                statement.bindLong(index++, (Long) field.get(model));
                            } else if (field.getType().equals(Date.class)) {
                                statement.bindLong(index++, ((Date) field.get(model)).getTime());
                            } else if (field.getType().equals(String.class)) {
                                statement.bindString(index++, (String) field.get(model));
                            }
                        }
                    }
                    statement.execute();
                    LogUtil.toLog("INSERTED: " + (i * batchCount));
                }
                statement.close();
            }

            if (countRestRows > 0) {
                String query = getBatchInsertQuery(countRestRows, fields, tableName);
                LogUtil.toLog(query, true);
                SQLiteStatement statement = database.compileStatement(query);

                int index = 1;
                for (int j = 0; j < countRestRows; j++) {
                    statement.bindLong(index++, new Date().getTime());
                    statement.bindLong(index++, new Date().getTime());
                    Model model = models.get(countFullInserts * batchCount + j);
                    for (Field field : fields) {
                        if (field.getType().equals(Integer.class)){
                            statement.bindLong(index++, (Long) field.get(model));
                        } else if(field.getType().equals(Long.class)) {
                            statement.bindLong(index++, (Long) field.get(model));
                        } else if (field.getType().equals(Date.class)) {
                            statement.bindLong(index++, ((Date) field.get(model)).getTime());
                        } else if (field.getType().equals(String.class)) {
                            statement.bindString(index++, (String) field.get(model));
                        }
                    }
                }
                statement.execute();
                LogUtil.toLog("INSERTED: " + (countFullInserts * batchCount + countRestRows));
                statement.close();
            }

            return true;
        } catch(Exception e){
            LogUtil.toLog("Fast insert exception", e);
        }

        return false;
    }

    private String getBatchInsertQuery(int batchCount, Set<Field> fields, String tableName){
        final String lineSeparator = System.getProperty("line.separator");
        Iterator<Field> iterator = fields.iterator();
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append(lineSeparator)
                .append("INSERT INTO ").append(tableName).append(" (createdAt, updatedAt");

        while (iterator.hasNext()) {
            queryBuilder.append(", ").append(iterator.next().getName());
        }

        queryBuilder.append(")").append(lineSeparator);

        for (int i = 0; i < batchCount; i++) {
            queryBuilder.append("SELECT ?, ?, ");
            for (int j = 0; j < fields.size(); j++) {
                queryBuilder.append("?");
                if (j + 1 != fields.size()) {
                    queryBuilder.append(",");
                }
            }
            queryBuilder.append(" ").append(lineSeparator);
            if (i + 1 != batchCount) {
                queryBuilder.append("UNION ALL ").append(lineSeparator);
            }
        }

        return queryBuilder.toString();
    }

    @Override
    public <Model extends Base> Model createThrowException(Model model) throws CreateException {
        try {
            ContentValues createContentValues = SimpleStoreUtil.getContentValuesForCreate(model);

            //create files
            for(Field field : ReflectionUtil.getFields(model.getClass(), Const.dataFields)){
                field.setAccessible(true);
                byte[] bytes = (byte[]) field.get(model);
                if(bytes != null && bytes.length > 0){
                    String fileName = SimpleStoreUtil.createFile(bytes, SimpleStoreUtil.getFileName(model.getClass(), field));
                    createContentValues.put(field.getName(), fileName);
                }
            }

            Long localId = database.insert(
                    SimpleStoreUtil.getTableName(model.getClass()),
                    null,
                    createContentValues
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
            throw new ReadException("Create model exception", e);
        } finally {
            cursor.close();
        }
    }

    @Override
    public <Model extends Base> List<Model> readByThrowException(Class<Model> clazz, KeyValue... keyValues) throws ReadException {
        Cursor cursor = database.query(
                SimpleStoreUtil.getTableName(clazz),
                SimpleStoreUtil.getColumns(clazz),
                SimpleStoreUtil.getSelectionFilter(keyValues),
                SimpleStoreUtil.getSelectionFilterArguments(keyValues),
                null, null, null
        );
        List<Model> models = new ArrayList<Model>();
        try {
            while (cursor.moveToNext()) {
                models.add((Model) SimpleStoreUtil.getModel(cursor, clazz));
            }
        } catch (Exception e) {
            throw new ReadException("Create model exception", e);
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
                if(0 == models.size() % 1000){
                    LogUtil.toLog("READ: " + models.size());
                }
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
            ContentValues contentValuesForUpdate = SimpleStoreUtil.getContentValuesForUpdate(model);

            Cursor cursor = database.query(
                    SimpleStoreUtil.getTableName(model.getClass()),
                    SimpleStoreUtil.getDataColumns(model.getClass()),
                    "_id=?",
                    new String[]{Long.toString(model.getLocalId())},
                    null, null, null
            );

            while(cursor.moveToNext()) {
                for(Field field : ReflectionUtil.getFields(model.getClass(), Const.dataFields)) {
                    field.setAccessible(true);
                    String oldFileName = cursor.getString(cursor.getColumnIndex(field.getName()));
                    byte[] file = (byte[]) field.get(model);
                    if(oldFileName != null && oldFileName.length() > 0 && file != null && file.length > 0) {//update file
                        SimpleStoreUtil.deleteFile(oldFileName);
                        String newFileName = SimpleStoreUtil.getFileName(model.getClass(), field);
                        SimpleStoreUtil.createFile(file, newFileName);
                        contentValuesForUpdate.put(field.getName(), newFileName);
                    } else if(oldFileName != null && oldFileName.length() > 0 && (file == null || file.length == 0)){//delete file
                        SimpleStoreUtil.deleteFile(oldFileName);
                        contentValuesForUpdate.putNull(field.getName());
                    } else if((oldFileName == null || oldFileName.length() == 0) && file != null && file.length > 0) {//create file
                        String newFileName = SimpleStoreUtil.getFileName(model.getClass(), field);
                        SimpleStoreUtil.createFile(file, newFileName);
                        contentValuesForUpdate.put(field.getName(), newFileName);
                    }
                }
            }

            cursor.close();

            row = database.update(
                    SimpleStoreUtil.getTableName(model.getClass()),
                    contentValuesForUpdate,
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

        //get file names from database
        Cursor cursor = database.query(
                SimpleStoreUtil.getTableName(clazz),
                SimpleStoreUtil.getDataColumns(clazz),
                "_id=?",
                new String[]{Long.toString(pk)},
                null, null, null
        );
        List<String> fileNames = new ArrayList<String>();
        while(cursor.moveToNext()) {
            for (Field field : ReflectionUtil.getFields(clazz, Const.dataFields)) {
                field.setAccessible(true);
                String fileName = cursor.getString(cursor.getColumnIndex(field.getName()));
                if(fileName != null && fileName.length() > 0) {
                    fileNames.add(fileName);
                }
            }
        }

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
        //delete files
        for(String fileName : fileNames){
            SimpleStoreUtil.deleteFile(fileName);
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

            try {
                List<Long> ids = readRelationsThrowException(model.getLocalId(), clazz, childClazz);
                for(Long id : ids){
                    Base child = readWithRelationsThrowException(id, childClazz);
                    collection.add(child);
                }
                field.set(model, collection);
            } catch (DataNotFoundException unused) {
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
                models.add(readWithRelationsThrowException(cursor.getLong(0), clazz));
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
            Class childClazz = ReflectionUtil.getGenericType(field);
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
        Collection<Long> subPks = new ArrayList<Long>();
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
        return deleteRelationThrowException(model.getLocalId(), model.getClass(), subModel.getLocalId(), subModel.getClass());
    }

    @Override
    public <M extends Base, C extends Base> boolean deleteRelationThrowException(Long pk, Class<M> clazz, Long subPk, Class<C> subClazz) throws DeleteException {
        int count = database.delete(
                SimpleStoreUtil.getTableName(clazz, subClazz),
                SimpleStoreUtil.getRelationTableColumn(clazz) + "=? and " + SimpleStoreUtil.getRelationTableColumn(subClazz) + "=?",
                new String[]{ Long.toString(pk), Long.toString(subPk)  }
        );
        if(count != 1){
            throw new DeleteException("delete count not equal 1");
        }
        return true;
    }

    @Override
    public <M extends Base, C extends Base> boolean deleteRelationsThrowException(M model, Collection<C> subModels) throws DeleteException {
        Class subClazz = null;
        List<Long> subPks = new ArrayList<Long>();
        for (C subModel : subModels){
            subPks.add(subModel.getLocalId());
            subClazz = subModel.getClass();
        }
        if(subClazz == null){
            throw new DeleteException("doesn't define subClass, may be subModels is empty");
        }
        return deleteRelationsThrowException(model.getLocalId(), model.getClass(), subPks, subClazz);
    }

    @Override
    public <M extends Base, C extends Base> boolean deleteRelationsThrowException(Long pk, Class<M> clazz, Collection<Long> subPks, Class<C> subClazz) throws DeleteException {
        List<String> allPks = new ArrayList<String>();
        allPks.add(Long.toString(pk));

        StringBuilder whereClause = new StringBuilder();
        whereClause.append(SimpleStoreUtil.getRelationTableColumn(clazz));
        whereClause.append(" =? ");
        whereClause.append(" and ");
        whereClause.append(SimpleStoreUtil.getRelationTableColumn(subClazz));
        whereClause.append(" in ( ");
        Iterator<Long> it = subPks.iterator();
        while(it.hasNext()){
            allPks.add(Long.toString(it.next()));
            whereClause.append("?");
            if(it.hasNext()){
                whereClause.append(",");
            }
        }
        whereClause.append(" ) ");

        int count = database.delete(
                SimpleStoreUtil.getTableName(clazz, subClazz),
                whereClause.toString(),
                allPks.toArray(new String[allPks.size()])
        );

        if(count == 0){
            throw new DeleteException("delete count not equal 0");
        }
        return true;
    }
}
