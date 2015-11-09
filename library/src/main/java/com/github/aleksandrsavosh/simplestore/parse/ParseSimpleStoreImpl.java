package com.github.aleksandrsavosh.simplestore.parse;

import com.github.aleksandrsavosh.simplestore.Base;
import com.github.aleksandrsavosh.simplestore.KeyValue;
import com.github.aleksandrsavosh.simplestore.LogUtil;
import com.github.aleksandrsavosh.simplestore.SimpleStore;
import com.github.aleksandrsavosh.simplestore.exception.CreateException;
import com.github.aleksandrsavosh.simplestore.exception.DeleteException;
import com.github.aleksandrsavosh.simplestore.exception.ReadException;
import com.github.aleksandrsavosh.simplestore.exception.UpdateException;
import com.parse.ParseObject;

import java.util.List;

public class ParseSimpleStoreImpl<Model extends Base> implements SimpleStore<Model, String> {

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
        return null;
    }

    @Override
    public Model update(Model model) {
        return null;
    }

    @Override
    public boolean delete(String s) {
        return false;
    }

    @Override
    public Model createThrowException(Model model) throws CreateException {
        Class clazz = model.getClass();
        try {
            ParseObject po = ParseUtil.createPO(clazz);
            ParseUtil.setModel2PO(model, po);
            ParseUtil.setModelData2PO(model, po);
            po.save();
            ParseUtil.setPO2Model(po, model);
            return model;

        } catch (Exception e) {
            throw new CreateException(e.getMessage(), e);
        }
    }

    @Override
    public Model readThrowException(String s) throws ReadException {
        return null;
    }

    @Override
    public Model updateThrowException(Model model) throws UpdateException {
        return null;
    }

    @Override
    public boolean deleteThrowException(String s) throws DeleteException {
        return false;
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
        return null;
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
