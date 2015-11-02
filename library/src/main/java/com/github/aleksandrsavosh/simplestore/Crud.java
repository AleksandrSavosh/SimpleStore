package com.github.aleksandrsavosh.simplestore;

import com.github.aleksandrsavosh.simplestore.exception.CreateException;
import com.github.aleksandrsavosh.simplestore.exception.ReadException;

/**
 * Base interface for base operations
 */
public interface Crud<Model extends Base, PK> {

    Model create(Model model);
    Model read(PK pk);
//    Model update(Model model);
//    boolean delete(String pk);

    public Model createThrowException(Model model) throws CreateException;
    public Model readThrowException(PK pk) throws ReadException;
//    public Model updateThrowException(Model model) throws DataNotFoundException, OtherException;
//    public boolean deleteThrowException(PK pk) throws DataNotFoundException, OtherException;

}
