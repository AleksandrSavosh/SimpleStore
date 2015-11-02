package com.github.aleksandrsavosh.simplestoretest;

import com.github.aleksandrsavosh.simplestore.Base;
import com.github.aleksandrsavosh.simplestore.LogUtil;
import com.github.aleksandrsavosh.simplestore.SimpleStore;
import com.github.aleksandrsavosh.simplestore.SimpleStoreFactory;

import java.util.Date;
import java.util.HashSet;

public class Application extends android.app.Application {


    public static class Test extends Base {
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
            return "Test{" +
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

        factory.initLocalStore(1, new HashSet<Class<? extends Base>>(){{ add(Test.class); }});

        LogUtil.setIsUseLog(true);

        SimpleStore<Test, Long> store = factory.getLocalStore(Test.class);

        Test test = new Test();

        test = store.create(test);

        Test test1 = store.read(test.getLocalId());

        System.out.println("TEST: " + test1);

        factory.destroy();
    }
}
