package com.github.aleksandrsavosh.simplestoretest;

import com.github.aleksandrsavosh.simplestore.*;
import com.github.aleksandrsavosh.simplestore.exception.CreateException;

import java.util.*;

public class Application extends android.app.Application {

    public static class CCC extends Base {
        public Long lfs = 49l;
        public String test = "test333выфывафваыыв";
        public String test2 = "testфывафывфывафыва2";

        @Override
        public String toString() {
            return "CCC{" +
                    "test='" + test + '\'' +
                    "} " + super.toString();
        }
    }

    public static class BBB extends Base {
        public Long ld = 15l;
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
        private byte[] myFile = new byte[]{ 2,5,4,5,5,9,54,4,5,5,8,4,5,5,6,6,54,4,7,8,5,9,6,3,1,1,4,5,4 };

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
                    ", myFile=" + byte2str(myFile) +
                    "} " + super.toString();
        }

        private String byte2str(byte[] bytes){
            if(bytes == null){
                return "null";
            }
            StringBuilder sb = new StringBuilder();
            for(byte byt : bytes){
                sb.append("" + byt + ",");
            }
            return sb.toString();
        }
    }


    public static class TT extends Base {


        KK kk;

        @Override
        public String toString() {
            return "TT{" +
                    "kk=" + kk +
                    "} " + super.toString();
        }
    }

    public static class KK extends Base {


        @Override
        public String toString() {
            return "KK{} " + super.toString();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        SimpleStoreManager manager = SimpleStoreManager.instance(this, new HashSet<Class>(){{
            add(AAA.class);
            add(BBB.class);
            add(CCC.class);
            add(TT.class);
            add(KK.class);
        }});

        manager.useLog(true);

        manager.initLocalStore(27);
        manager.isNeedSaveData(false);
        SimpleStore<Long> localStore = manager.getLocalStore();


        List<CCC> cccs = new ArrayList<CCC>();
        for(int i = 0; i < 65111; i++){
            cccs.add(new CCC());
        }
//
        localStore.createFast(cccs, CCC.class);

        cccs = localStore.readAll(CCC.class);
        System.out.println("FINISH: " + cccs.size());


//        manager.initCloudStore("cv5X8Il8up7Y4YvrBz6nM6icaf7lBYXfPlwQSmAR", "6fDQLSh7mmIqoZEU5V0BNOrFxHavGEFkVnNDZlrZ");
//        SimpleStore<String> cloudStore = manager.getCloudStore();
//
//        TT tt = cloudStore.create(new TT());
//        KK kk = cloudStore.create(new KK());
//        cloudStore.createRelation(tt, kk);
//        tt = cloudStore.readWithRelations(tt.getCloudId(), TT.class);

//        cloudStore.readWithRelations("t5BvWOE4DX", BBB.class);

//        BBB test2 = new BBB();
//        test2 = cloudStore.createWithRelations(test2);




//        manager.destroy();
    }
}
