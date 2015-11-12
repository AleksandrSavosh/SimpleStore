package com.github.aleksandrsavosh.simplestoretest;

import com.github.aleksandrsavosh.simplestore.*;
import com.github.aleksandrsavosh.simplestore.exception.CreateException;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

public class Application extends android.app.Application {

    public static class CCC extends Base {
        public String test = "test333";

        @Override
        public String toString() {
            return "CCC{" +
                    "test='" + test + '\'' +
                    "} " + super.toString();
        }
    }

    public static class BBB extends Base {
        public String str = "test2";
        public AAA AAA = new AAA();
        public List<CCC> testList = new ArrayList<CCC>(){{
            add(new CCC());
            add(new CCC());
            add(new CCC());
        }};

        @Override
        public String toString() {
            return "BBB{" +
                    "str='" + str + '\'' +
                    ", AAA=" + AAA +
                    ", testList=" + testList +
                    "} " + super.toString();
        }
    }

    public static class AAA extends Base {
        private Integer ints = 50;
        private String str;
        private Date date = new Date();

        public Integer getInts() {
            return ints;
        }

        public void setInts(Integer ints) {
            this.ints = ints;
        }

        public String getStr() {
            return str;
        }

        public void setStr(String str) {
            this.str = str;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        @Override
        public String toString() {
            return "AAA{" +
                    "ints=" + ints +
                    ", str='" + str + '\'' +
                    ", date=" + date +
                    "} " + super.toString();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        SimpleStoreManager manager = SimpleStoreManager.instance(this, new HashSet<Class>(){{
            add(AAA.class);
            add(BBB.class);
            add(CCC.class);
        }});

//        manager.useLog(true);

//        manager.initLocalStore(13);
        SimpleStore<Long> localStore = manager.getLocalStore();
//
        BBB test = new BBB();
        test = localStore.createWithRelations(test);

        manager.initCloudStore("cv5X8Il8up7Y4YvrBz6nM6icaf7lBYXfPlwQSmAR", "6fDQLSh7mmIqoZEU5V0BNOrFxHavGEFkVnNDZlrZ");
        SimpleStore<String> cloudStore = manager.getCloudStore();

        cloudStore.readWithRelations("t5BvWOE4DX", BBB.class);

//        BBB test2 = new BBB();
//        test2 = cloudStore.createWithRelations(test2);




        manager.destroy();
    }
}
