package com.github.aleksandrsavosh.simplestore;

import com.github.aleksandrsavosh.simplestore.exception.CreateException;
import com.github.aleksandrsavosh.simplestore.exception.DeleteException;
import com.github.aleksandrsavosh.simplestore.exception.ReadException;
import com.github.aleksandrsavosh.simplestore.exception.UpdateException;

import java.util.Collection;
import java.util.List;

public interface SimpleStore<Pk> {

    <Model extends Base> Model create(Model model);
    <Model extends Base> boolean createFast(List<Model> model, Class<Model> modelClass);
    <Model extends Base> Model read(Pk pk, Class<Model> clazz);
    <Model extends Base> List<Model> readBy(Class<Model> clazz, KeyValue... keyValues);
    <Model extends Base> List<Model> readAll(Class<Model> clazz);
    <Model extends Base> Model update(Model model);
    <Model extends Base> boolean delete(Pk pk, Class<Model> clazz);
    <Model extends Base> boolean deleteBy(Class<Model> clazz, KeyValue... keyValues);

    <Model extends Base> Model createWithRelations(Model model);
    <Model extends Base> Model readWithRelations(Pk pk, Class<Model> clazz);
    <Model extends Base> List<Model> readByWithRelations(Class<Model> clazz, KeyValue... keyValues);
    <Model extends Base> List<Model> readAllWithRelations(Class<Model> clazz);
    <Model extends Base> Model updateWithRelations(Model model);
    <Model extends Base> boolean deleteWithRelations(Pk pk, Class<Model> clazz);


    <Model extends Base> Model createThrowException(Model model) throws CreateException;
    <Model extends Base> Model readThrowException(Pk pk, Class<Model> clazz) throws ReadException;
    <Model extends Base> List<Model> readByThrowException(Class<Model> clazz, KeyValue... keyValues) throws ReadException;
    <Model extends Base> List<Model> readAllThrowException(Class<Model> clazz) throws ReadException;
    <Model extends Base> Model updateThrowException(Model model) throws UpdateException;
    <Model extends Base> boolean deleteThrowException(Pk pk, Class<Model> clazz) throws DeleteException;
    <Model extends Base> boolean deleteByThrowException(Class<Model> clazz, KeyValue... keyValues) throws DeleteException;

    <Model extends Base> Model createWithRelationsThrowException(Model model) throws CreateException;
    <Model extends Base> Model readWithRelationsThrowException(Pk pk, Class<Model> clazz) throws ReadException;
    <Model extends Base> List<Model> readByWithRelationsThrowException(Class<Model> clazz, KeyValue... keyValues) throws ReadException;
    <Model extends Base> List<Model> readAllWithRelationsThrowException(Class<Model> clazz) throws ReadException;
    <Model extends Base> Model updateWithRelationsThrowException(Model model) throws UpdateException;
    <Model extends Base> boolean deleteWithRelationsThrowException(Pk pk, Class<Model> clazz) throws DeleteException;


    <M extends Base, C extends Base> boolean createRelation(M model, C subModel);
    <M extends Base, C extends Base> boolean createRelation(Pk pk, Class<M> clazz, Pk subPk, Class<C> subClazz);
    <M extends Base, C extends Base> boolean createRelations(M model, Collection<C> subModels);
    <M extends Base, C extends Base> boolean createRelations(Pk pk, Class<M> clazz, Collection<Pk> subPks, Class<C> subClazz);

    <M extends Base, C extends Base> C readRelation(M model, Class<C> subClazz);
    <M extends Base, C extends Base> Pk readRelation(Pk pk, Class<M> modelClazz, Class<C> subClazz);
    <M extends Base, C extends Base> List<C> readRelations(M model, Class<C> subClazz);
    <M extends Base, C extends Base> List<Pk> readRelations(Pk pk, Class<M> modelClazz, Class<C> subClazz);

    <M extends Base, C extends Base> boolean deleteRelation(M model, C subModel);
    <M extends Base, C extends Base> boolean deleteRelation(Pk pk, Class<M> clazz, Pk subPk, Class<C> subClazz);
    <M extends Base, C extends Base> boolean deleteRelations(M model, Collection<C> subModels);
    <M extends Base, C extends Base> boolean deleteRelations(Pk pk, Class<M> clazz, Collection<Pk> subPks, Class<C> subClazz);


    <M extends Base, C extends Base> boolean createRelationThrowException(M model, C subModel) throws CreateException;
    <M extends Base, C extends Base> boolean createRelationThrowException(Pk pk, Class<M> clazz, Pk subPk, Class<C> subClazz) throws CreateException;
    <M extends Base, C extends Base> boolean createRelationsThrowException(M model, Collection<C> subModels) throws CreateException;
    <M extends Base, C extends Base> boolean createRelationsThrowException(Pk pk, Class<M> clazz, Collection<Pk> subPks, Class<C> subClazz) throws CreateException;

    <M extends Base, C extends Base> C readRelationThrowException(M model, Class<C> subClazz) throws ReadException;
    <M extends Base, C extends Base> Pk readRelationThrowException(Pk pk, Class<M> modelClazz, Class<C> subClazz) throws ReadException;
    <M extends Base, C extends Base> List<C> readRelationsThrowException(M model, Class<C> subClazz) throws ReadException;
    <M extends Base, C extends Base> List<Pk> readRelationsThrowException(Pk pk, Class<M> modelClazz, Class<C> subClazz) throws ReadException;

    <M extends Base, C extends Base> boolean deleteRelationThrowException(M model, C subModel) throws DeleteException;
    <M extends Base, C extends Base> boolean deleteRelationThrowException(Pk pk, Class<M> clazz, Pk subPk, Class<C> subClazz) throws DeleteException;
    <M extends Base, C extends Base> boolean deleteRelationsThrowException(M model, Collection<C> subModels) throws DeleteException;
    <M extends Base, C extends Base> boolean deleteRelationsThrowException(Pk pk, Class<M> clazz, Collection<Pk> subPks, Class<C> subClazz) throws DeleteException;


}
