package com.github.aleksandrsavosh.simplestore;

import android.content.Context;
import com.github.aleksandrsavosh.simplestore.sqlite.SQLiteHelper;
import com.github.aleksandrsavosh.simplestore.sqlite.SQLiteSimpleStoreImpl;

import java.util.Set;

public class SimpleStoreFactory {

    private Context context;
    private SQLiteHelper sqLiteHelper;

    private SimpleStoreFactory(Context context){
        this.context = context;
    }

    public static SimpleStoreFactory instance(Context context){
        return new SimpleStoreFactory(context);
    }

    public void initLocalStore(int appVersion, Set<Class<? extends Base>> models){
        sqLiteHelper = new SQLiteHelper(context, "SimpleStore", appVersion, models);
    }

    public void destroy(){
        sqLiteHelper.close();
    }

    public <Model extends Base> SimpleStore<Model> getLocalStore(Class<Model> aClass){
        return new SQLiteSimpleStoreImpl<Model>(aClass, sqLiteHelper);
    }

}
