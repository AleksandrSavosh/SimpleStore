package com.github.aleksandrsavosh.simplestore.parse;

import com.github.aleksandrsavosh.simplestore.Base;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс узел, которые представляет его глубину, объект связка к который он обрабатывает и узлы которые ему подчиняются
 * @param <Model>
 */
public class ModelPONode<Model extends Base> {
    public Integer deep;
    public ModelPO<Model> modelPO;
    public List<ModelPONode> nodes = new ArrayList<ModelPONode>();

    @Override
    public String toString() {
        return "ModelPONode{" +
                "deep=" + deep +
                ", modelPO=" + modelPO +
                ", nodes=" + nodes +
                '}';
    }
}
