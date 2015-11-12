package com.github.aleksandrsavosh.simplestore;

import com.github.aleksandrsavosh.simplestore.exception.CreateException;
import com.github.aleksandrsavosh.simplestore.exception.DeleteException;
import com.github.aleksandrsavosh.simplestore.exception.ReadException;
import com.github.aleksandrsavosh.simplestore.exception.UpdateException;

import java.util.ArrayList;
import java.util.Collection;
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

    @Override
    public <M extends Base, C extends Base> boolean createRelation(M model, C subModel) {
        try {
            return createRelationThrowException(model, subModel);
        } catch (CreateException e) {
            LogUtil.toLog("create relations error", e);
        }
        return false;
    }

    @Override
    public <M extends Base, C extends Base> boolean createRelation(Pk pk, Class<M> clazz, Pk subPk, Class<C> subClazz) {
        try {
            return createRelationThrowException(pk, clazz, subPk, subClazz);
        } catch (CreateException e) {
            LogUtil.toLog("create relations error", e);
        }
        return false;
    }

    @Override
    public <M extends Base, C extends Base> boolean createRelations(M model, Collection<C> subModels) {
        try {
            return createRelationsThrowException(model, subModels);
        } catch (CreateException e) {
            LogUtil.toLog("create relations error", e);
        }
        return false;
    }

    @Override
    public <M extends Base, C extends Base> boolean createRelations(Pk pk, Class<M> clazz, Collection<Pk> subPks, Class<C> subClazz) {
        try {
            return createRelationsThrowException(pk, clazz, subPks, subClazz);
        } catch (CreateException e) {
            LogUtil.toLog("create relations error", e);
        }
        return false;
    }

    @Override
    public <M extends Base, C extends Base> C readRelation(M model, Class<C> subClazz) {
        try {
            return readRelationThrowException(model, subClazz);
        } catch (ReadException e) {
            LogUtil.toLog("read relations error", e);
        }
        return null;
    }

    @Override
    public <M extends Base, C extends Base> Pk readRelation(Pk pk, Class<M> modelClazz, Class<C> subClazz) {
        try {
            return readRelationThrowException(pk, modelClazz, subClazz);
        } catch (ReadException e) {
            LogUtil.toLog("read relations error", e);
        }
        return null;
    }

    @Override
    public <M extends Base, C extends Base> List<C> readRelations(M model, Class<C> subClazz) {
        try {
            return readRelationsThrowException(model, subClazz);
        } catch (ReadException e) {
            LogUtil.toLog("read relations error", e);
        }
        return new ArrayList<>();
    }

    @Override
    public <M extends Base, C extends Base> List<Pk> readRelations(Pk pk, Class<M> modelClazz, Class<C> subClazz) {
        try {
            return readRelationsThrowException(pk, modelClazz, subClazz);
        } catch (ReadException e) {
            LogUtil.toLog("read relations error", e);
        }
        return new ArrayList<>();
    }

    @Override
    public <M extends Base, C extends Base> boolean deleteRelation(M model, C subModel) {
        try {
            return deleteRelationThrowException(model, subModel);
        } catch (DeleteException e) {
            LogUtil.toLog("delete relations error", e);
        }
        return false;
    }

    @Override
    public <M extends Base, C extends Base> boolean deleteRelation(Pk pk, Class<M> clazz, Pk subPk, Class<C> subClazz) {
        try {
            return deleteRelationThrowException(pk, clazz, subPk, subClazz);
        } catch (DeleteException e) {
            LogUtil.toLog("delete relations error", e);
        }
        return false;
    }

    @Override
    public <M extends Base, C extends Base> boolean deleteRelations(M model, Collection<C> subModels) {
        try {
            return deleteRelationsThrowException(model, subModels);
        } catch (DeleteException e) {
            LogUtil.toLog("delete relations error", e);
        }
        return false;
    }

    @Override
    public <M extends Base, C extends Base> boolean deleteRelations(Pk pk, Class<M> clazz, Collection<Pk> subPks, Class<C> subClazz) {
        try {
            return deleteRelationsThrowException(pk, clazz, subPks, subClazz);
        } catch (DeleteException e) {
            LogUtil.toLog("delete relations error", e);
        }
        return false;
    }

}
