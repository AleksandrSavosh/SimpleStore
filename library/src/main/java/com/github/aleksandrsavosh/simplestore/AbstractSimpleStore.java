package com.github.aleksandrsavosh.simplestore;

import com.github.aleksandrsavosh.simplestore.exception.CreateException;
import com.github.aleksandrsavosh.simplestore.exception.DeleteException;
import com.github.aleksandrsavosh.simplestore.exception.ReadException;
import com.github.aleksandrsavosh.simplestore.exception.UpdateException;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSimpleStore<Pk> implements SimpleStore<Pk> {

    @Override
    public <Model extends Base> Model create(Model model) {
        try {
            return createThrowException(model);
        } catch (CreateException e) {
            LogUtil.toLog("create error", e);
        }
        return null;
    }

    @Override
    public <Model extends Base> Model read(Pk pk, Class<Model> clazz) {
        try {
            return readThrowException(pk, clazz);
        } catch (ReadException e) {
            LogUtil.toLog("read error", e);
        }
        return null;
    }

    @Override
    public <Model extends Base> List<Model> readBy(Class<Model> clazz, KeyValue... keyValues) {
        try {
            return readByThrowException(clazz, keyValues);
        } catch (ReadException e) {
            LogUtil.toLog("read error", e);
        }
        return new ArrayList<Model>();
    }

    @Override
    public <Model extends Base> List<Model> readAll(Class<Model> clazz) {
        try {
            return readAllThrowException(clazz);
        } catch (ReadException e) {
            LogUtil.toLog("read error", e);
        }
        return new ArrayList<Model>();
    }

    @Override
    public <Model extends Base> Model update(Model model) {
        try {
            return updateThrowException(model);
        } catch (UpdateException e) {
            LogUtil.toLog("update error", e);
        }
        return null;
    }

    @Override
    public <Model extends Base> boolean delete(Pk pk, Class<Model> clazz) {
        try {
            return deleteThrowException(pk, clazz);
        } catch (DeleteException e) {
            LogUtil.toLog("delete error", e);
        }
        return false;
    }

    @Override
    public <Model extends Base> Model createWithRelations(Model model) {
        try {
            return createWithRelationsThrowException(model);
        } catch (CreateException e) {
            LogUtil.toLog("create error", e);
        }
        return null;
    }

    @Override
    public <Model extends Base> Model readWithRelations(Pk pk, Class<Model> clazz) {
        try {
            return readWithRelationsThrowException(pk, clazz);
        } catch (ReadException e) {
            LogUtil.toLog("read error", e);
        }
        return null;
    }

    @Override
    public <Model extends Base> List<Model> readByWithRelations(Class<Model> clazz, KeyValue... keyValues) {
        try {
            return readByWithRelationsThrowException(clazz, keyValues);
        } catch (ReadException e) {
            LogUtil.toLog("read error", e);
        }
        return new ArrayList<Model>();
    }

    @Override
    public <Model extends Base> List<Model> readAllWithRelations(Class<Model> clazz) {
        try {
            return readAllWithRelationsThrowException(clazz);
        } catch (ReadException e) {
            LogUtil.toLog("read error", e);
        }
        return new ArrayList<Model>();
    }

    @Override
    public <Model extends Base> Model updateWithRelations(Model model) {
        try {
            return updateWithRelationsThrowException(model);
        } catch (UpdateException e) {
            LogUtil.toLog("update error", e);
        }
        return null;
    }

    @Override
    public <Model extends Base> boolean deleteWithRelations(Pk pk, Class<Model> clazz) {
        try {
            return deleteWithRelationsThrowException(pk, clazz);
        } catch (DeleteException e) {
            LogUtil.toLog("delete error", e);
        }
        return false;
    }
}
