package com.github.aleksandrsavosh.simplestore.parse;

import com.github.aleksandrsavosh.simplestore.*;
import com.github.aleksandrsavosh.simplestore.exception.*;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.lang.reflect.Field;
import java.util.*;

public class ParseSimpleStoreImpl extends AbstractSimpleStore<String> {

    @Override
    public <Model extends Base> Model createThrowException(Model model) throws CreateException {
        try {
            Class clazz = model.getClass();
            ParseObject po = ParseUtil.createPO(clazz);
            ParseUtil.setModel2PO(model, po);
            ParseUtil.setModelData2PO(model, po);
            po.save();
            ParseUtil.setPO2Model(po, model);
            return model;
        } catch (Exception e){
            throw new CreateException(e);
        }
    }

    @Override
    public <Model extends Base> Model readThrowException(String pk, Class<Model> clazz) throws ReadException {
        try {
            ParseObject po = ParseUtil.getPO(clazz, pk);
            Model model = ParseUtil.createModel(clazz);
            ParseUtil.setPO2Model(po, model);
            ParseUtil.setPOData2Model(po, model);
            return model;
        } catch (Exception e) {
            if(e instanceof ParseException && ((ParseException)e).getCode() == ParseException.OBJECT_NOT_FOUND){
                throw new DataNotFoundException("Data not found");
            }
            throw new ReadException(e);
        }
    }

    @Override
    public <Model extends Base> List<Model> readByThrowException(Class<Model> clazz, KeyValue... keyValues) throws ReadException {
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
            if(e instanceof ParseException && ((ParseException)e).getCode() == ParseException.OBJECT_NOT_FOUND){
                throw new DataNotFoundException("Data not found");
            }
            throw new ReadException(e);
        }
    }

    @Override
    public <Model extends Base> List<Model> readAllThrowException(Class<Model> clazz) throws ReadException {
        try {
            List<Model> models = new ArrayList<Model>();
            List<ParseObject> pos = ParseUtil.getPOs(clazz);
            for(ParseObject po : pos) {
                Model model = ParseUtil.createModel(clazz);
                ParseUtil.setPO2Model(po, model);
                models.add(model);
            }
            return models;
        } catch (Exception e) {
            if(e instanceof ParseException && ((ParseException)e).getCode() == ParseException.OBJECT_NOT_FOUND){
                throw new DataNotFoundException("Data not found");
            }
            throw new ReadException(e);
        }
    }

    @Override
    public <Model extends Base> Model updateThrowException(Model model) throws UpdateException {
        try {
            ParseObject po = ParseUtil.getPO(model.getClass(), model.getCloudId());
            ParseUtil.setModel2PO(model, po);
            po.save();
            ParseUtil.setPO2Model(po, model);
            return model;
        } catch(Exception e){
            throw new UpdateException(e);
        }
    }

    @Override
    public <Model extends Base> boolean deleteThrowException(String pk, Class<Model> clazz) throws DeleteException {
        try {
            ParseObject po = ParseUtil.getPO(clazz, pk);
            po.delete();
            return true;
        } catch(Exception e){
            throw new DeleteException(e);
        }
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
                //create relations
                if(!createRelationThrowException(model, child)){
                    throw new CreateException("Not create relation");
                }
            }
            return model;
        } catch (Exception e) {
            throw new CreateException("Can not create model with relations", e);
        }
    }

    @Override
    public <Model extends Base> Model readWithRelationsThrowException(String pk, Class<Model> clazz) throws ReadException {
        //read parent
        Model model = readThrowException(pk, clazz);
        //read one to one relations
        for(Field field : ReflectionUtil.getFields(clazz, Const.modelClasses)){
            field.setAccessible(true);
            Class childClazz = field.getType();
            try {
                String childId = readRelationThrowException(pk, clazz, childClazz);
                Base child = readWithRelationsThrowException(childId, childClazz);
                field.set(model, child);
            } catch (DataNotFoundException unused){
            } catch (IllegalAccessException e) {
                throw new ReadException(e);
            }
        }
        //read one to many relations
        for(Field field : ReflectionUtil.getFields(clazz, Const.collections)) {
            field.setAccessible(true);
            Class collType = field.getType();
            Class childClazz = ReflectionUtil.getGenericType(field);
            Collection collection = ReflectionUtil.getCollectionInstance(collType);
            try {
                List<String> ids = readRelationsThrowException(pk, clazz, childClazz);
                for(String id : ids){
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
        try {
            List<Model> models = new ArrayList<Model>();
            List<ParseObject> pos = ParseUtil.getPOsBy(clazz, keyValues);
            for (ParseObject po : pos) {
                models.add(readWithRelationsThrowException(po.getObjectId(), clazz));
            }
            return models;
        } catch (Exception e){
            if(e instanceof ParseException && ((ParseException)e).getCode() == ParseException.OBJECT_NOT_FOUND){
                throw new DataNotFoundException("Data not found");
            }
            throw new ReadException(e);
        }
    }

    @Override
    public <Model extends Base> List<Model> readAllWithRelationsThrowException(Class<Model> clazz) throws ReadException {
        try {
            List<Model> models = new ArrayList<Model>();
            List<ParseObject> pos = ParseUtil.getPOs(clazz);
            for(ParseObject po : pos) {
                models.add(readWithRelationsThrowException(po.getObjectId(), clazz));
            }
            return models;
        } catch (Exception e){
            if(e instanceof ParseException && ((ParseException)e).getCode() == ParseException.OBJECT_NOT_FOUND){
                throw new DataNotFoundException("Data not found");
            }
            throw new ReadException(e);
        }
    }

    @Override
    public <Model extends Base> Model updateWithRelationsThrowException(Model model) throws UpdateException {
        try {
            deleteWithRelationsThrowException(model.getCloudId(), model.getClass());
            return createWithRelationsThrowException(model);
        } catch (Exception e){
            throw new UpdateException(e);
        }
    }

    @Override
    public <Model extends Base> boolean deleteWithRelationsThrowException(String pk, Class<Model> clazz) throws DeleteException {
        //delete children one to one
        for(Field field : ReflectionUtil.getFields(clazz, Const.modelClasses)){
            field.setAccessible(true);
            Class childClazz =  field.getType();
            try {
                String childId = readRelationThrowException(pk, clazz, childClazz);
                deleteWithRelationsThrowException(childId, childClazz);
            } catch (DataNotFoundException unused){
            } catch (ReadException e){
                throw new DeleteException(e);
            }
        }
        //delete children one to many
        for(Field field : ReflectionUtil.getFields(clazz, Const.collections)) {
            field.setAccessible(true);
            Class childClazz = ReflectionUtil.getGenericType(field);
            try {
                List<String> ids = readRelationsThrowException(pk, clazz, childClazz);
                for(String childId : ids){
                    deleteWithRelationsThrowException(childId, childClazz);
                }
            } catch (DataNotFoundException unused){
            } catch (ReadException e){
                throw new DeleteException(e);
            }
        }
        //delete parent
        return deleteThrowException(pk, clazz);
    }

    public <M extends Base, C extends Base> boolean createRelationThrowException(M model, C child) throws CreateException {
        try {
            ParseObject parentPo = ParseUtil.getPO(model.getClass(), model.getCloudId());
            ParseObject childPo = ParseUtil.getPO(child.getClass(), child.getCloudId());
            childPo.put(parentPo.getClassName(), parentPo);
            childPo.save();
            return true;
        } catch (Exception e){
            throw new CreateException(e);
        }
    }

    public <M extends Base, C extends Base> String readRelationThrowException(String pk, Class<M> modelClazz, Class<C> subClazz) throws ReadException {
        List<String> ids = readRelationsThrowException(pk, modelClazz, subClazz);
        if(ids.size() > 1){
            throw new ReadException("To much results for one model");
        }
        return ids.get(0);
    }

    public <M extends Base, C extends Base> List<String> readRelationsThrowException(String pk, Class<M> modelClazz, Class<C> subClazz) throws ReadException {
        List<String> ids = new ArrayList<String>();
        try {
            ParseObject modelPo = ParseUtil.getPO(modelClazz, pk);
            ParseQuery<ParseObject> query = ParseQuery.getQuery(subClazz.getSimpleName());
            query.whereEqualTo(modelPo.getClassName(), modelPo);
            List<ParseObject> list = query.find();
            for (ParseObject po : list) {
                ids.add(po.getObjectId());
            }
        } catch (Exception e){
            if(e instanceof ParseException && ((ParseException)e).getCode() == ParseException.OBJECT_NOT_FOUND){
                throw new DataNotFoundException("Data not found");
            }
            throw new ReadException(e);
        }
        if(ids.size() == 0){
            throw new DataNotFoundException("Data not found");
        }
        return ids;
    }

}
