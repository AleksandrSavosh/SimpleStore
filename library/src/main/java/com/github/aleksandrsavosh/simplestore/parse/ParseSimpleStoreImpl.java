package com.github.aleksandrsavosh.simplestore.parse;

import com.github.aleksandrsavosh.simplestore.*;
import com.github.aleksandrsavosh.simplestore.exception.CreateException;
import com.github.aleksandrsavosh.simplestore.exception.DeleteException;
import com.github.aleksandrsavosh.simplestore.exception.ReadException;
import com.github.aleksandrsavosh.simplestore.exception.UpdateException;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class ParseSimpleStoreImpl<Model extends Base> implements SimpleStore<Model, String> {

    private Class<Model> clazz;

    public ParseSimpleStoreImpl(Class clazz) {
        this.clazz = clazz;
    }

    @Override
    public Model create(Model model) {
        try {
            return createThrowException(model);
        } catch (CreateException e) {
            LogUtil.toLog("Create model error", e);
        }
        return null;
    }

    @Override
    public Model read(String s) {
        try {
            return readThrowException(s);
        } catch (ReadException e) {
            LogUtil.toLog("Read model error", e);
        }
        return null;
    }

    @Override
    public Model update(Model model) {
        try {
            return updateThrowException(model);
        } catch (UpdateException e) {
            LogUtil.toLog("Update model error", e);
        }
        return null;
    }

    @Override
    public boolean delete(String s) {
        try {
            return deleteThrowException(s);
        } catch (DeleteException e) {
            LogUtil.toLog("Delete model error", e);
        }
        return false;
    }

    @Override
    public Model createThrowException(Model model) throws CreateException {
        try {
            return createThrowExceptionCommon(model);
        } catch (Exception e) {
            throw new CreateException(e.getMessage(), e);
        }
    }

    public <T extends Base> T createThrowExceptionCommon(T model) throws ParseException, IllegalAccessException {
        Class clazz = model.getClass();
        ParseObject po = ParseUtil.createPO(clazz);
        ParseUtil.setModel2PO(model, po);
        ParseUtil.setModelData2PO(model, po);
        po.save();
        ParseUtil.setPO2Model(po, model);
        return model;
    }

    @Override
    public Model readThrowException(String s) throws ReadException {
        try {
            return readThrowExceptionCommon(s, clazz);
        } catch (Exception e){
            throw new ReadException(e);
        }
    }

    public <T extends Base> T readThrowExceptionCommon(String id, Class<T> forClass) throws ParseException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        ParseObject po = ParseUtil.getPO(forClass, id);
        T model = ParseUtil.createModel(forClass);
        ParseUtil.setPO2Model(po, model);
        ParseUtil.setPOData2Model(po, model);
        return model;

    }

    @Override
    public Model updateThrowException(Model model) throws UpdateException {
        try {
            ParseObject po = ParseUtil.getPO(clazz, model.getCloudId());
            ParseUtil.setModel2PO(model, po);
            po.save();
            ParseUtil.setPO2Model(po, model);
            return model;
        } catch(Exception e){
            throw new UpdateException(e);
        }
    }

    @Override
    public boolean deleteThrowException(String s) throws DeleteException {
        try {
            return deleteThrowExceptionCommon(s, clazz);
        } catch (Exception e){
            throw new DeleteException(e);
        }
    }

    public <M extends Base> boolean deleteThrowExceptionCommon(String modelPk, Class<M> modelClazz) throws ParseException {
        ParseObject po = ParseUtil.getPO(clazz, modelPk);
        po.delete();
        return true;
    }

    @Override
    public Model createWithRelations(Model model) {
        try {
            return createWithRelationsThrowException(model);
        } catch (CreateException e){
            LogUtil.toLog("Create with relations error", e);
        }
        return null;
    }

    @Override
    public Model readWithRelations(String s) {
        try {
            return readWithRelationsThrowException(s);
        } catch (ReadException e) {
            LogUtil.toLog("Read with relations error", e);
        }
        return null;
    }

    @Override
    public boolean deleteWithRelations(String s) {
        try {
            return deleteThrowException(s);
        } catch (DeleteException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Model createWithRelationsThrowException(Model model) throws CreateException {
        try {
            return createWithRelationsThrowExceptionCommon(model);
        } catch(Exception e){
            throw new CreateException(e);
        }
    }

    private <T extends Base> T createWithRelationsThrowExceptionCommon(T model) throws CreateException {
        try {
            //create parent
            model = createThrowExceptionCommon(model);

            //create children
            List<? extends Base> children = SimpleStoreUtil.getModelChildrenObjects(model);
            for(Base child : children){
                child = createWithRelationsThrowExceptionCommon(child);

                //create relations
                if(!appendChildToParentCommon(model, child)){
                    throw new CreateException("Not create relation");
                }
            }

            return model;
        } catch (Exception e) {
            throw new CreateException("Can not create model with relations", e);
        }
    }

    private <Parent extends Base, Child extends Base> boolean appendChildToParentCommon(Parent parent, Child child)
            throws ParseException {
        ParseObject parentPo = ParseUtil.getPO(parent.getClass(), parent.getCloudId());
        ParseObject childPo = ParseUtil.getPO(child.getClass(), child.getCloudId());
        childPo.put(parentPo.getClassName(), parentPo);
        childPo.save();
        return true;
    }

    @Override
    public Model readWithRelationsThrowException(String s) throws ReadException {
        try {
            return readWithRelationsThrowExceptionCommon(s, clazz);
        } catch (Exception e) {
            throw new ReadException(e);
        }
    }

    public <M extends Base, C extends Base> M readWithRelationsThrowExceptionCommon(String pk, Class<M> forClazz)
            throws ReadException, NoSuchMethodException, InstantiationException, IllegalAccessException, ParseException, InvocationTargetException {
        //read parent
        M model = readThrowExceptionCommon(pk, forClazz);
        //read one to one relations
        for(Field field : ReflectionUtil.getFields(forClazz, new HashSet<Class>(){{ addAll(Const.modelClasses); }})){
            field.setAccessible(true);
            Class<C> type = (Class<C>) field.getType();
            List<String> ids = getRelationsIds(pk, forClazz, type);
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
        for(Field field : ReflectionUtil.getFields(forClazz, Const.collections)) {
            field.setAccessible(true);
            Class collType = field.getType();
            Class<C> genType = ReflectionUtil.getGenericType(field);
            Collection<C> collection = ReflectionUtil.getCollectionInstance(collType);
            List<String> ids = getRelationsIds(pk, forClazz, genType);
            for(String id : ids){
                C child = readWithRelationsThrowExceptionCommon(id, genType);
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

    public <M extends Base, C extends Base> List<String> getRelationsIds(
            String modelPk, Class<M> modelClazz, Class<C> childClazz) throws ParseException {
        ParseObject modelPo = ParseUtil.getPO(modelClazz, modelPk);
        ParseQuery<ParseObject> query = ParseQuery.getQuery(childClazz.getSimpleName());
        query.whereEqualTo(modelPo.getClassName(), modelPo);
        List<ParseObject> list = query.find();
        List<String> ids = new ArrayList<String>();
        for(ParseObject po : list){
            ids.add(po.getObjectId());
        }
        return ids;
    }

    @Override
    public Model updateWithRelationsThrowException(Model model) throws UpdateException {
        return null;
    }

    @Override
    public boolean deleteWithRelationsThrowException(String s) throws DeleteException {
        try {
            return deleteWithRelationsThrowExceptionCommon(s, clazz);
        } catch (ParseException e) {
            throw new DeleteException(e);
        }
    }

    public <M extends Base, C extends Base> boolean deleteWithRelationsThrowExceptionCommon(String modelPk, Class<M> modelClazz) throws ParseException {
        Map<Class<C>, List<String>> childs = new HashMap<Class<C>, List<String>>();
        //find all childs one to one
        for(Field field : ReflectionUtil.getFields(modelClazz, new HashSet<Class>(){{ addAll(Const.modelClasses); }})){
            field.setAccessible(true);
            Class<C> childClazz = (Class<C>) field.getType();
            List<String> ids = getRelationsIds(modelPk, modelClazz, childClazz);
            if(ids.size() > 0){
                childs.put(childClazz, ids);
            }
        }
        //find all childs one to many
        for(Field field : ReflectionUtil.getFields(modelClazz, Const.collections)) {
            field.setAccessible(true);
            Class<C> childClazz = ReflectionUtil.getGenericType(field);
            List<String> ids = getRelationsIds(modelPk, modelClazz, childClazz);
            if(ids.size() > 0){
                childs.put(childClazz, ids);
            }
        }
        //delete parent
        boolean result = deleteThrowExceptionCommon(modelPk, modelClazz);
        //delete children
        for(Class<C> key : childs.keySet()){
            for(String id : childs.get(key)){
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
            LogUtil.toLog("read all error", e);
        }
        return null;
    }

    @Override
    public List<Model> readAllThrowException() throws ReadException {
        try {
            List<Model> models = new ArrayList<Model>();
            List<ParseObject> pos = null;
            pos = ParseUtil.getPOs(clazz);
            for(ParseObject po : pos) {
                Model model = ParseUtil.createModel(clazz);
                ParseUtil.setPO2Model(po, model);
                models.add(model);
            }
            return models;
        } catch (Exception e) {
            throw new ReadException(e);
        }
    }

    @Override
    public List<Model> readAllWithRelations() {
        try {
            return readAllWithRelationsThrowException();
        } catch(ReadException e){
            LogUtil.toLog("read all with relations exception", e);
        }
        return new ArrayList<Model>();
    }

    @Override
    public List<Model> readAllWithRelationsThrowException() throws ReadException {
        try {
            List<Model> models = new ArrayList<Model>();
            List<ParseObject> pos = ParseUtil.getPOs(clazz);
            for(ParseObject po : pos) {
                models.add(readWithRelationsThrowExceptionCommon(po.getObjectId(), clazz));
            }
            return models;
        } catch (Exception e){
            throw new ReadException(e);
        }
    }

    @Override
    public List<Model> readBy(KeyValue... keyValues) {
        try {
            return readByThrowException(keyValues);
        } catch(ReadException e){
            LogUtil.toLog("read by exception", e);
        }
        return new ArrayList<Model>();
    }

    @Override
    public List<Model> readByThrowException(KeyValue... keyValues) throws ReadException {
        try {
            List<Model> models = new ArrayList<Model>();
            List<ParseObject> pos = ParseUtil.getPOsBy(clazz, keyValues);
            for (ParseObject po : pos) {
                Model model = ParseUtil.createModel(clazz);
                ParseUtil.setPO2Model(po, model);
                models.add(model);
            }
            return models;
        } catch (Exception e){
            throw new ReadException(e);
        }
    }

    @Override
    public List<Model> readByWithRelations(KeyValue... keyValues) {
        try {
            return readByThrowException(keyValues);
        } catch(ReadException e){
            LogUtil.toLog("read by with relations exception", e);
        }
        return new ArrayList<Model>();
    }

    @Override
    public List<Model> readByWithRelationsThrowException(KeyValue... keyValues) throws ReadException {
        try {
            List<Model> models = new ArrayList<Model>();
            List<ParseObject> pos = ParseUtil.getPOsBy(clazz, keyValues);
            for (ParseObject po : pos) {
                models.add(readWithRelationsThrowException(po.getObjectId()));
            }
            return models;
        } catch (Exception e){
            throw new ReadException(e);
        }
    }

//    @Override
    public List<String> readParentIds(Class parentClazz, String modelPk) {
        try {
            return readParentIdsThrowException(parentClazz, modelPk);
        } catch(ReadException e){
            LogUtil.toLog("read parent ids exception", e);
        }
        return new ArrayList<String>();
    }

//    @Override
    public List<String> readParentIdsThrowException(Class parentClazz, String modelPk) throws ReadException {
        try {
            ParseObject po = ParseUtil.getPO(clazz, modelPk);
            ParseRelation<ParseObject> relation = po.getRelation(parentClazz.getSimpleName());
            ParseQuery<ParseObject> query = relation.getQuery();
            List<String> ids = new ArrayList<String>();
            List<ParseObject> pos = query.find();
            for(ParseObject parentPo : pos){
                ids.add(parentPo.getObjectId());
            }
            return ids;
        } catch(Exception e){
            throw new ReadException(e);
        }
    }

//    @Override
    public List<String> readChildrenIds(Class childClazz, String id) {
        return null;
    }

//    @Override
    public List<String> readChildrenIdsThrowException(Class childClazz, String id) {
        return null;
    }
}
