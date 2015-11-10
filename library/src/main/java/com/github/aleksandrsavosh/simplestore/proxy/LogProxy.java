package com.github.aleksandrsavosh.simplestore.proxy;

import com.github.aleksandrsavosh.simplestore.LogUtil;
import com.github.aleksandrsavosh.simplestore.SimpleStore;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Date;

public class LogProxy implements InvocationHandler {

    SimpleStore simpleStore;
    public LogProxy(SimpleStore simpleStore) {
        this.simpleStore = simpleStore;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String header = " " + simpleStore.getClass().getSimpleName() + "->" + method.getName() + "() ";
        LogUtil.toLog("====================" + header + "====================");

        //write args
        for(Object arg : args){
            LogUtil.toLog("ARG (" + arg.getClass().getSimpleName() + "): " + arg.toString());
        }

        Date start = new Date();

        Object result = method.invoke(simpleStore, args);

        LogUtil.toLog("TIME: " + (new Date().getTime() - start.getTime()) + "ms.");
        LogUtil.toLog("RESULT: " + result.toString());

        int length = header.length();
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < length; i++){ sb.append("="); }
        LogUtil.toLog("========================================" + sb.toString());

        return result;
    }
}
