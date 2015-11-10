package com.github.aleksandrsavosh.simplestore;

import com.github.aleksandrsavosh.simplestore.exception.CreateException;
import com.github.aleksandrsavosh.simplestore.exception.DeleteException;
import com.github.aleksandrsavosh.simplestore.exception.ReadException;
import com.github.aleksandrsavosh.simplestore.exception.UpdateException;

import java.util.List;

/**
 * Main interface for
 */
public interface SimpleStore<Model extends Base, PK> {

    Model create(Model model);
    Model read(PK pk);
    Model update(Model model);
    boolean delete(PK pk);

    Model createThrowException(Model model) throws CreateException;
    Model readThrowException(PK pk) throws ReadException;
    Model updateThrowException(Model model) throws UpdateException;
    boolean deleteThrowException(PK pk) throws DeleteException;

    Model createWithRelations(Model model);
    Model readWithRelations(PK pk);
//    Model updateWithRelations(Model model);
    boolean deleteWithRelations(PK pk);
//
    Model createWithRelationsThrowException(Model model) throws CreateException;
    Model readWithRelationsThrowException(PK pk) throws ReadException;
    Model updateWithRelationsThrowException(Model model) throws UpdateException;
    boolean deleteWithRelationsThrowException(PK pk) throws DeleteException;

    List<Model> readAll();
    List<Model> readAllThrowException() throws ReadException;
    List<Model> readAllWithRelations();
    List<Model> readAllWithRelationsThrowException() throws ReadException;

    List<Model> readBy(KeyValue... keyValues);
    List<Model> readByThrowException(KeyValue... keyValues) throws ReadException;
    List<Model> readByWithRelations(KeyValue... keyValues);
    List<Model> readByWithRelationsThrowException(KeyValue... keyValues) throws ReadException;

//    List<PK> readParentIds(Class parentClazz, PK id);
//    List<PK> readParentIdsThrowException(Class parentClazz, PK id) throws ReadException;
//    List<PK> readChildrenIds(Class childClazz, PK id);
//    List<PK> readChildrenIdsThrowException(Class childClazz, PK id) throws ReadException;

}
