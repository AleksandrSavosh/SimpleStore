package com.github.aleksandrsavosh.simplestore;

import com.github.aleksandrsavosh.simplestore.exception.CreateException;
import com.github.aleksandrsavosh.simplestore.exception.DeleteException;
import com.github.aleksandrsavosh.simplestore.exception.ReadException;
import com.github.aleksandrsavosh.simplestore.exception.UpdateException;

import java.util.List;

public interface SimpleStore<Pk> {

    <Model extends Base> Model create(Model model);
    <Model extends Base> Model read(Pk pk, Class<Model> clazz);
    <Model extends Base> List<Model> readBy(Class<Model> clazz, KeyValue... keyValues);
    <Model extends Base> List<Model> readAll(Class<Model> clazz);
    <Model extends Base> Model update(Model model);
    <Model extends Base> boolean delete(Pk pk, Class<Model> clazz);

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

    <Model extends Base> Model createWithRelationsThrowException(Model model) throws CreateException;
    <Model extends Base> Model readWithRelationsThrowException(Pk pk, Class<Model> clazz) throws ReadException;
    <Model extends Base> List<Model> readByWithRelationsThrowException(Class<Model> clazz, KeyValue... keyValues) throws ReadException;
    <Model extends Base> List<Model> readAllWithRelationsThrowException(Class<Model> clazz) throws ReadException;
    <Model extends Base> Model updateWithRelationsThrowException(Model model) throws UpdateException;
    <Model extends Base> boolean deleteWithRelationsThrowException(Pk pk, Class<Model> clazz) throws DeleteException;


//    <M extends Base, C extends Base> boolean createRelationThrowException(M model, C child) throws CreateException;
//    <M extends Base, C extends Base> Pk readRelationThrowException(Pk pk, Class<M> modelClazz, Class<C> subClazz) throws ReadException;
//    <M extends Base, C extends Base> List<Pk> readRelationsThrowException(Pk pk, Class<M> modelClazz, Class<C> subClazz) throws ReadException;
//    <M extends Base, C extends Base> C readRelationThrowException(M model, Class<C> subClazz) throws ReadException;

}
