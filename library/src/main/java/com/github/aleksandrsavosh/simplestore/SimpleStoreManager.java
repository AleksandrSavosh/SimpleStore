package com.github.aleksandrsavosh.simplestore;

import android.content.Context;
import com.github.aleksandrsavosh.simplestore.parse.ParseSimpleStoreImpl;
import com.github.aleksandrsavosh.simplestore.proxy.LogProxy;
import com.github.aleksandrsavosh.simplestore.sqlite.SQLiteHelper;
import com.github.aleksandrsavosh.simplestore.sqlite.SQLiteSimpleStoreImpl;
import com.github.aleksandrsavosh.simplestore.sqlite.TransactionProxy;
import com.parse.Parse;

import java.lang.reflect.Proxy;
import java.util.Set;

public class SimpleStoreManager {

    public static SimpleStoreManager instance;
    public Context context;
    private SQLiteHelper sqLiteHelper;

    private SimpleStoreManager(Context context) {
        this.context = context;
    }

    public static SimpleStoreManager instance(Context context, Set<Class> models){
        instance = new SimpleStoreManager(context);
        Const.modelClasses = models;
        return instance;
    }

    public void initLocalStore(int appVersion){
        sqLiteHelper = new SQLiteHelper(context, "SimpleStore", appVersion);
    }

    public void destroy(){
        if(sqLiteHelper != null) {
            sqLiteHelper.close();
        }
        sqLiteHelper = null;
    }

    @SuppressWarnings("unchecked")
    public SimpleStore<Long> getLocalStore(){
        SimpleStore<Long> simpleStore = new SQLiteSimpleStoreImpl();
        simpleStore = (SimpleStore<Long>) Proxy.newProxyInstance(
                simpleStore.getClass().getClassLoader(),
                new Class[]{ SimpleStore.class },
                new TransactionProxy(simpleStore, sqLiteHelper));
        return (SimpleStore<Long>) Proxy.newProxyInstance(
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

    @SuppressWarnings("unchecked")
    public SimpleStore<String> getCloudStore() {
        SimpleStore<String> simpleStore = new ParseSimpleStoreImpl();
        return (SimpleStore<String>) Proxy.newProxyInstance(
                simpleStore.getClass().getClassLoader(),
                new Class[]{ SimpleStore.class },
                new LogProxy(simpleStore));
    }

    public void useLog(boolean b) {
        LogUtil.setIsUseLog(b);
    }
}
