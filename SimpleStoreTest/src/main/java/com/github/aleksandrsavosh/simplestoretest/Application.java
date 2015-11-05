package com.github.aleksandrsavosh.simplestoretest;

import com.github.aleksandrsavosh.simplestore.Base;
import com.github.aleksandrsavosh.simplestore.LogUtil;
import com.github.aleksandrsavosh.simplestore.SimpleStore;
import com.github.aleksandrsavosh.simplestore.SimpleStoreFactory;

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

        SimpleStoreFactory factory = SimpleStoreFactory.instance(this);

        LogUtil.setIsUseLog(true);

        factory.initLocalStore(12, new HashSet<Class<? extends Base>>(){{ add(AAA.class); add(BBB.class); add(CCC.class); }});

        SimpleStore<BBB, Long> store = factory.getLocalStore(BBB.class);

        BBB test = new BBB();

        test = store.createWithRelations(test);

        System.out.println("TEST: " + test);
        System.out.println("TEST2: " + store.readWithRelations(test.getLocalId()));


        store.deleteWithRelations(test.getLocalId());


        factory.destroy();
    }
}
