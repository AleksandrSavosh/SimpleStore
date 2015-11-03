package com.github.aleksandrsavosh.simplestore;

import com.github.aleksandrsavosh.simplestore.exception.CreateException;
import com.github.aleksandrsavosh.simplestore.exception.DeleteException;
import com.github.aleksandrsavosh.simplestore.exception.ReadException;
import com.github.aleksandrsavosh.simplestore.exception.UpdateException;

/**
 * Main inferface for
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
//    Model readWithRelations(PK pk);
//    Model updateWithRelations(Model model);
//    boolean deleteWithRelations(PK pk);
//
    Model createWithRelationsThrowException(Model model) throws CreateException;
//    Model readWithRelationsThrowException(PK pk) throws ReadException;
//    Model updateWithRelationsThrowException(Model model) throws UpdateException;
//    boolean deleteWithRelationsThrowException(PK pk) throws DeleteException;

}
