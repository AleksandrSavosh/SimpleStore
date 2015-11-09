package com.github.aleksandrsavosh.simplestore.parse;

import com.github.aleksandrsavosh.simplestore.*;
import com.github.aleksandrsavosh.simplestore.exception.CreateException;
import com.github.aleksandrsavosh.simplestore.exception.DeleteException;
import com.github.aleksandrsavosh.simplestore.exception.ReadException;
import com.github.aleksandrsavosh.simplestore.exception.UpdateException;
import com.parse.ParseException;
import com.parse.ParseObject;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
            return (Model) createThrowExceptionCommon(model);
        } catch (Exception e) {
            throw new CreateException(e.getMessage(), e);
        }
    }

    public Base createThrowExceptionCommon(Base model) throws ParseException, IllegalAccessException {
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
            ParseObject po = ParseUtil.getPO(clazz, s);
            Model model = ParseUtil.createModel(clazz);
            ParseUtil.setPO2Model(po, model);
            ParseUtil.setPOData2Model(po, model);
            return model;
        } catch (Exception e){
            throw new ReadException(e);
        }
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
            ParseObject po = ParseUtil.getPO(clazz, s);
            po.delete();
            return true;
        } catch (Exception e){
            throw new DeleteException(e);
        }
    }

    @Override
    public Model createWithRelations(Model model) {
        return null;
    }

    @Override
    public Model readWithRelations(String s) {
        return null;
    }

    @Override
    public boolean deleteWithRelations(String s) {
        return false;
    }

    @Override
    public Model createWithRelationsThrowException(Model model) throws CreateException {
        try {




//            //мапа где инт глубина
//            Map<Integer, List<ModelPONode>> map =
//                    Util.getModelPoTreeRec(clazz, 1, model, null, true, isCloudStorage);
//            Util.createRelations(map);
//            Util.save(map, isCloudStorage);
//            Util.setPO2Model(map, isCloudStorage);
            return model;
        } catch(Exception e){
            throw new CreateException(e);
        }
    }

    public Base createWithRelationsThrowExceptionOnlyModels(Base model) throws ParseException, IllegalAccessException {
        model = createThrowExceptionCommon(model);
        for(Field field : ReflectionUtil.getFields(model.getClass(), new HashSet<Class>(){{addAll(Const.modelClasses);}})){
            field.setAccessible(true);
            Base child = (Base) field.get(model);
            if(child == null){ continue; }
            field.set(model, createWithRelationsThrowExceptionOnlyModels(child));
        }
        for(Field field : ReflectionUtil.getFields(model.getClass(), Const.collections)){
            field.setAccessible(true);
            Collection<Base> children = (Collection<Base>) field.get(model);
            if(children == null){ continue; }
            for(Base child : children){
                child = createWithRelationsThrowExceptionOnlyModels(child);
            }

        }

        return model;
    }

    @Override
    public Model readWithRelationsThrowException(String s) throws ReadException {
        return null;
    }

    @Override
    public boolean deleteWithRelationsThrowException(String s) throws DeleteException {
        return false;
    }

    @Override
    public List<Model> readAll() {
        return null;
    }

    @Override
    public List<Model> readAllThrowException() throws ReadException {
        return null;
    }

    @Override
    public List<Model> readAllWithRelations() {
        return null;
    }

    @Override
    public List<Model> readAllWithRelationsThrowException() throws ReadException {
        return null;
    }

    @Override
    public List<Model> readBy(KeyValue... keyValues) {
        return null;
    }

    @Override
    public List<Model> readByThrowException(KeyValue... keyValues) throws ReadException {
        return null;
    }

    @Override
    public List<Model> readByWithRelations(KeyValue... keyValues) {
        return null;
    }

    @Override
    public List<Model> readByWithRelationsThrowException(KeyValue... keyValues) throws ReadException {
        return null;
    }

    @Override
    public List<String> readParentIds(Class parentClazz, String id) {
        return null;
    }

    @Override
    public List<String> readParentIdsThrowException(Class parentClazz, String id) {
        return null;
    }

    @Override
    public List<String> readChildrenIds(Class childClazz, String id) {
        return null;
    }

    @Override
    public List<String> readChildrenIdsThrowException(Class childClazz, String id) {
        return null;
    }
}
