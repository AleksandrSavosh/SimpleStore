package com.github.aleksandrsavosh.simplestore;

/**
 * Base interface for base operations
 */
public interface Crud<Model extends Base> {

    Model create(Model model);
    Model read(String pk);
    Model update(Model model);
    boolean delete(String pk);

}
