package com.github.aleksandrsavosh.simplestore.sqlite;

import android.database.sqlite.SQLiteDatabase;
import com.github.aleksandrsavosh.simplestore.LogUtil;
import com.github.aleksandrsavosh.simplestore.SimpleStore;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TransactionProxy implements InvocationHandler {

    SimpleStore<Long> simpleStore;
    SQLiteHelper sqLiteHelper;
    public TransactionProxy(SimpleStore<Long> simpleStore, SQLiteHelper sqLiteHelper) {
        this.simpleStore = simpleStore;
        this.sqLiteHelper = sqLiteHelper;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        synchronized (sqLiteHelper) {
            if (method.getName().contains("create") ||
                    method.getName().contains("update") ||
                    method.getName().contains("delete")) {

                //open transaction
                SQLiteDatabase db = sqLiteHelper.getWritableDatabase();
                if (!db.inTransaction()) {
                    LogUtil.toLog("----- BEGIN TRAN -----");
                    db.beginTransaction();
                }
                try {
                    ((SQLiteSimpleStoreImpl) simpleStore).setDatabase(db);
                    Object out = method.invoke(simpleStore, args);
                    if (out != null) {
                        //commit tran
                        LogUtil.toLog("----- TRAN SUCCESS -----");
                        db.setTransactionSuccessful();
                    }
                    return out;
                } catch (InvocationTargetException e) {
                    throw e.getTargetException();
                } finally {
                    //close tran
                    LogUtil.toLog("----- END TRAN -----");
                    ((SQLiteSimpleStoreImpl) simpleStore).setDatabase(null);
                    db.endTransaction();
                    db.close();
                }
            }

            LogUtil.toLog("----- TRAN DOESN'T NEED -----");
            SQLiteDatabase db = sqLiteHelper.getReadableDatabase();
            try {
                ((SQLiteSimpleStoreImpl) simpleStore).setDatabase(db);
                Object out = method.invoke(simpleStore, args);
                return out;
            } finally {
                ((SQLiteSimpleStoreImpl) simpleStore).setDatabase(null);
                db.close();
            }
        }
    }
}
