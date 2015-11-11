package com.github.aleksandrsavosh.simplestore.proxy;

import com.github.aleksandrsavosh.simplestore.LogUtil;
import com.github.aleksandrsavosh.simplestore.SimpleStore;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

public class LogProxy implements InvocationHandler {

    SimpleStore simpleStore;
    public LogProxy(SimpleStore simpleStore){
        this.simpleStore = simpleStore;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String header = " " + method.getName() + "() ";
        LogUtil.toLog("====================" + header + "====================");

        //write args
        for(Object arg : args){
            LogUtil.toLog("ARG (" + arg.getClass().getSimpleName() + "): " + arg.toString());
        }
        Date start = new Date();

        try {
            Object result = method.invoke(simpleStore, args);
            LogUtil.toLog("RESULT: " + result.toString());
            return result;
        } catch (InvocationTargetException e){
            throw e.getTargetException();
        } finally {
            LogUtil.toLog("TIME: " + (new Date().getTime() - start.getTime()) + "ms.");

            int length = header.length();
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < length; i++){ sb.append("="); }
            LogUtil.toLog("========================================" + sb.toString());
        }
    }
}
