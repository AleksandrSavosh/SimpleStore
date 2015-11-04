package com.github.aleksandrsavosh.simplestore;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.github.aleksandrsavosh.simplestore.sqlite.SQLiteHelper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class SimpleStoreUtil {

    /**
     * метод генерирует имя таблицы в зависимости какие классы переданы
     * @param clazzes классы
     * @return имя таблицы
     */
    public static String getTableName(Class... clazzes){
        TreeSet<String> names = new TreeSet<String>();
        for(Class clazz : clazzes) {
            names.add(clazz.getSimpleName().toUpperCase());
        }
        StringBuilder builder = new StringBuilder();
        Iterator<String> it = names.iterator();
        while (it.hasNext()){
            String name = it.next();
            builder.append(name);
            if(it.hasNext()){
                builder.append("_");
            }
        }
        return builder.toString();
    }

    /**
     * метод генерирует запрос на создание таблицы в зависимости от обекта класса
     * @param clazz класс
     * @return строка запроса
     */
    public static String getCreateTableQuery(Class<? extends Base> clazz) {
        final String lineSeparator = System.getProperty("line.separator");
        StringBuilder sb = new StringBuilder(lineSeparator +
                "CREATE TABLE " + getTableName(clazz) + " ( " + lineSeparator +
                "_id INTEGER primary key not null, " + lineSeparator +
                "cloudId TEXT, " + lineSeparator +
                "createdAt INTEGER, " + lineSeparator +
                "updatedAt INTEGER, " + lineSeparator
        );
        Set<Field> fields = ReflectionUtil.getFields(clazz, Const.fields);
        Iterator<Field> it = fields.iterator();
        while(it.hasNext()){
            Field field = it.next();
            String name = field.getName();
            String type;
            if(field.getType().equals(Integer.class) || field.getType().equals(Date.class)){
                type = "INTEGER";
            } else if(field.getType().equals(String.class)) {
                type = "TEXT";
            } else {
                throw new RuntimeException("Not supported type");
            }

            sb.append(name + " " + type + " " + (it.hasNext()?", ":"") + lineSeparator);
        }
        sb.append(") ");
        return sb.toString();
    }

    /**
     * метод генерирует запрос на создание таблицы связки для сущностей, сущностей лодно быть больше одной
     * @param clazzes классы
     * @return строка запроса
     */
    public static String getCreateRelationTableQuery(Class<? extends Base>... clazzes) {
        final String lineSeparator = System.getProperty("line.separator");
        StringBuilder sb = new StringBuilder(lineSeparator +
                "CREATE TABLE " + getTableName(clazzes) + " ( " + lineSeparator +
                "_id INTEGER primary key not null, " + lineSeparator);
        for(int i = 0; i < clazzes.length; i++){
            sb.append(clazzes[i].getSimpleName().toLowerCase() + "_id INTEGER not null");
            if(i + 1 == clazzes.length){
                sb.append(lineSeparator);
            } else {
                sb.append(", " + lineSeparator);
            }
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * метод возвращает все обекты зависимых моделей внутри переданого объекта модели
     * @param model обект
     * @return лист обектов моделей
     * @throws IllegalAccessException
     */
    public static List<Base> getModelChildrenObjects(Base model) throws IllegalAccessException {
        List<Base> result = new ArrayList<Base>();

        Class clazz =  model.getClass();
        for(Field field : ReflectionUtil.getFields(clazz, new HashSet<Class>(){{ addAll(Const.modelClasses); }})){
            field.setAccessible(true);
            Base child = (Base) field.get(model);
            if(child != null){
                result.add(child);
            }
        }

        for(Field field : ReflectionUtil.getFields(clazz, Const.collections)){// one to many
            field.setAccessible(true);

            Object collection = field.get(model);
            if(collection != null && collection instanceof Collection) {
                result.addAll((Collection<Base>) collection);
            }
        }
        return result;
    }

    /**
     * метод определяет названия колонок
     * @param clazz класс модели для которой нужно определить колонки
     * @param <Model>
     * @return массив строк колонок
     */
    public static <Model extends Base> String[] getColumns(Class<Model> clazz) {
        List<String> result = new ArrayList<String>();
        result.add("_id");
        result.add("cloudId");
        result.add("createdAt");
        result.add("updatedAt");
        for(Field field : ReflectionUtil.getFields(clazz, Const.fields)){
            result.add(field.getName());
        }
        return result.toArray(new String[result.size()]);
    }

    /**
     * метод создает модель на основании класса модели и данных в курсоре
     * @return объект модели
     */
    public static <Model extends Base> Model getModel(Cursor cursor, Class<? extends Base> clazz) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Model model = (Model) clazz.getConstructor().newInstance();
        model.setLocalId(cursor.getLong(cursor.getColumnIndex("_id")));
        model.setCloudId(cursor.getString(cursor.getColumnIndex("cloudId")));
        model.setCreatedAt(new java.sql.Date(cursor.getLong(cursor.getColumnIndex("createdAt"))));
        model.setUpdatedAt(new java.sql.Date(cursor.getLong(cursor.getColumnIndex("updatedAt"))));
        for(Field field : ReflectionUtil.getFields(clazz, Const.fields)){
            field.setAccessible(true);
            Class type = field.getType();
            if(type.equals(Integer.class)){
                field.set(model, cursor.getInt(cursor.getColumnIndex(field.getName())));
            }
            if(type.equals(String.class)){
                field.set(model, cursor.getString(cursor.getColumnIndex(field.getName())));
            }
            if(type.equals(Date.class)){
                Date date = new java.sql.Date(cursor.getLong(cursor.getColumnIndex(field.getName())));
                field.set(model, date);
            }
        }
        return model;
    }

    /**
     * метод создает ContentValues на основании свойст объекта модели
     * создает или обновляет updatedAt
     * если свойство обекта модели равно налл, сеттим налл
     * @return ContentValues
     */
    public static <Model extends Base> ContentValues getContentValuesForUpdate(Model model) throws IllegalAccessException {
        ContentValues contentValues = getContentValuesForCreate(model);
        contentValues.remove("createdAt");
        return contentValues;
    }

    /**
     * метод создает ContentValues на основании свойст объекта модели
     * у модели создает createAt дату если эта дата не задана
     * также создает или обновляет updatedAt
     * если свойство обекта модели равно налл, сеттим налл
     * @return ContentValues
     */
    public static <Model extends Base> ContentValues getContentValuesForCreate(final Model model) throws IllegalAccessException {
        ContentValues values = new ContentValues();
        String cloudId = model.getCloudId();
        if(cloudId == null){
            values.putNull("cloudId");
        }
        Date createdAt = model.getCreatedAt();
        if(createdAt == null){
            createdAt = new Date();
            model.setCreatedAt(createdAt);
        }
        values.put("createdAt", createdAt.getTime());
        Date updatedAt = model.getUpdatedAt();
        if(updatedAt == null){
            updatedAt = new Date();
            model.setUpdatedAt(updatedAt);
        }
        values.put("updatedAt", updatedAt.getTime());
        Set<Field> fields = ReflectionUtil.getFields(model.getClass(), Const.fields);
        for(Field field : fields){
            field.setAccessible(true);
            String name = field.getName();
            Object data = field.get(model);
            if(data == null){
                values.putNull(name);
                continue;
            }
            if(data instanceof String){
                values.put(name, (String) data);
            }
            if(data instanceof Integer){
                values.put(name, (Integer) data);
            }
            if(data instanceof Date){
                values.put(name, ((Date) data).getTime());
            }
        }
        return values;
    }

    /**
     * метод создает ContentValues для таблицы связки исходя переданых классов
     * @param classes обекты
     * @return ContentValues
     */
    public static ContentValues getContentValuesForRelationClasses(Base... classes) {
        ContentValues contentValues = new ContentValues();
        for(Base base : classes){
            contentValues.put(base.getClass().getSimpleName() + "_id", base.getLocalId());
        }
        return contentValues;
    }

    /**
     * метод получает все таблицы которые есть а базе данных
     * @return лист таблиц
     */
    public static List<String> getTableNames(SQLiteDatabase db){
        //get all table names
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        List<String> list = new ArrayList<String>();

        //drop all tables
        if (c.moveToFirst()) {
            while ( !c.isAfterLast() ) {
                list.add(c.getString(0));
                c.moveToNext();
            }
        }
        c.close();

        return list;
    }

    /**
     * Get table data
     * @return list of lists where list.get(0) its column names
     */
    public static List<List<String>> getTableData(SQLiteDatabase db, String table){
        Cursor c = db.rawQuery("select * from " + table, null);

        List<List<String>> result = new ArrayList<List<String>>();
        List<String> columnNames = new ArrayList<String>(Arrays.asList(c.getColumnNames()));
        result.add(columnNames);

        while(c.moveToNext()){
            List<String> data = new ArrayList<String>();
            for(int i = 0; i < columnNames.size(); i++){
                String name = columnNames.get(i);
                int index = c.getColumnIndex(name);
                data.add(c.getString(index));
            }
            result.add(data);
        }
        c.close();

        return result;
    }





    static class A extends Base {

    }

    static class B extends Base {

    }

    static class C extends Base {
        private Integer integer;
        String str;
        public Date dat;
        A a = new A();
        List<B> bs = new ArrayList<B>(){{
            add(new B());
            add(new B());
            add(new B());
        }};
    }

    public static void main(String[] args) throws IllegalAccessException {
        Const.modelClasses = new HashSet<Class<? extends Base>>(){{
            add(A.class);
            add(B.class);
            add(C.class);
        }};

        System.out.println(getModelChildrenObjects(new C()).size());
        System.out.println(getCreateTableQuery(C.class));
        System.out.println(getCreateRelationTableQuery(C.class, A.class));
        System.out.println(getCreateRelationTableQuery(A.class, C.class));
        System.out.println(Arrays.asList(getColumns(C.class)));

    }

}
