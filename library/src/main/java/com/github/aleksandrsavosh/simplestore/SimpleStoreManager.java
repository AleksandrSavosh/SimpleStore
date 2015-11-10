package com.github.aleksandrsavosh.simplestore;

import android.content.Context;
import com.github.aleksandrsavosh.simplestore.parse.ParseSimpleStoreImpl;
import com.github.aleksandrsavosh.simplestore.proxy.LogProxy;
import com.github.aleksandrsavosh.simplestore.sqlite.SQLiteHelper;
import com.github.aleksandrsavosh.simplestore.sqlite.SQLiteSimpleStoreImpl;
import com.parse.Parse;

import java.lang.reflect.Proxy;
import java.util.Set;

public class SimpleStoreManager {

    public static SimpleStoreManager instance;
    private Context context;
    private SQLiteHelper sqLiteHelper;

    private SimpleStoreManager(Context context) {
        this.context = context;
    }

    public static SimpleStoreManager instance(Context context, Set<Class<? extends Base>> models){
        instance = new SimpleStoreManager(context);
        Const.modelClasses = models;
        return instance;
    }

    public void initLocalStore(int appVersion){
        sqLiteHelper = new SQLiteHelper(context, "SimpleStore", appVersion);
    }

    public void destroy(){
        sqLiteHelper.close();
    }

    public <Model extends Base> SimpleStore<Model, Long> getLocalStore(Class<Model> aClass){
        SimpleStore<Model, Long> simpleStore = new SQLiteSimpleStoreImpl<Model>(aClass, sqLiteHelper);
        return (SimpleStore<Model, Long>)
                Proxy.newProxyInstance(
                        simpleStore.getClass().getClassLoader(),
                        new Class[]{ SimpleStore.class },
                        new LogProxy(simpleStore));
    }

    public SQLiteHelper getSqLiteHelper() {
        return sqLiteHelper;
    }

    public void initCloudStore(String applicationId, String clientKey){
        Parse.initialize(context, applicationId, clientKey);
    }

    public <Model extends Base> SimpleStore<Model, String> getCloudStore(Class<Model> clazz) {
        return new ParseSimpleStoreImpl<Model>(clazz);
    }

    public void useLog(boolean b) {
        LogUtil.setIsUseLog(b);
    }
}
