package com.github.aleksandrsavosh.simplestore;

import android.content.Context;
import com.github.aleksandrsavosh.simplestore.sqlite.SQLiteSimpleStoreImpl;

public class SimpleStoreFactory {

    public static <Model extends Base> SimpleStore<Model> getLocalStore(Class<Model> aClass, Context context){
        return new SQLiteSimpleStoreImpl<Model>();
    }

}
