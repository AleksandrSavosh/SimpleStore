package com.github.aleksandrsavosh.simplestore.parse;

import com.github.aleksandrsavosh.simplestore.Base;
import com.parse.ParseObject;

/**
 * Класс связка модели и парс объекта
 * @param <Model>
 */
public class ModelPO<Model extends Base> {
    public ParseObject po;
    public Model model;

    @Override
    public String toString() {
        return "ModelPO{" +
                "po=" + po +
                ", model=" + model +
                '}';
    }
}
