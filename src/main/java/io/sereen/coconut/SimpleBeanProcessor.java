package io.sereen.coconut;

import org.apache.commons.dbutils.BeanProcessor;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A bean processor that doesn't require setter and getter methods.
 * Kiran A (kiran@sereen.io)
 */
public class SimpleBeanProcessor extends BeanProcessor {
    // TODO cache the FieldDescriptorS of a Class, this requires some ClassLoader magic
    //private static final ConcurrentHashMap<String, FieldDescriptor[]> beanMetaMap = new ConcurrentHashMap<>();

    @Override
    public <T> T toBean(ResultSet rs, Class<? extends T> type) throws SQLException {
        T bean = newInstance(type);
        FieldDescriptor[] fields = gatherFieldDescriptors(type);
        return populateBean(rs, bean, fields);
    }

    @Override
    public <T> List<T> toBeanList(ResultSet rs, Class<? extends T> type) throws SQLException {
        List<T> results = new ArrayList<T>();

        if (!rs.next()) {
            return results;
        }

        FieldDescriptor[] fields = gatherFieldDescriptors(type);
        do {
            T bean = newInstance(type);
            populateBean(rs, bean, fields);
            results.add(bean);
        } while (rs.next());

        return results;
    }

    private  <T> T populateBean(ResultSet rs, T bean, FieldDescriptor[] fields) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int[] columnToProperty = mapColumnsToProperties(rsmd, fields);
        for (int i = 1; i < columnToProperty.length; i++) {
            if (columnToProperty[i] == PROPERTY_NOT_FOUND) {
                continue;
            }
            FieldDescriptor prop = fields[columnToProperty[i]];
            Class<?> propType = prop.getFieldType();

            Object value = null;
            if(propType != null) {
                value = this.processColumn(rs, i, propType);
                if (value == null && propType.isPrimitive()) {
                    value = primitiveDefaults.get(propType);
                }
            }

            prop.set(bean, value);
        }

        return bean;
    }

    /*
    private<T> FieldDescriptor[] getFields(Class<T> beanClass) throws SQLException {
        String beanClassName = beanClass.getName();

        FieldDescriptor[] fields = beanMetaMap.get(beanClassName);
        if(fields == null) {
            fields = gatherFieldDescriptors(beanClass);
            beanMetaMap.put(beanClassName, fields);
        }
        return fields;
    }*/

    private<T> int[] mapColumnsToProperties(ResultSetMetaData rsmd, FieldDescriptor[] fields) throws SQLException {
        int cols = rsmd.getColumnCount();
        int[] columnToProperty = new int[cols + 1];
        Arrays.fill(columnToProperty, PROPERTY_NOT_FOUND);

        for (int col = 1; col <= cols; col++) {
            String columnName = rsmd.getColumnLabel(col);
            if (null == columnName || columnName.isEmpty()) {
                columnName = rsmd.getColumnName(col);
            }
            for (int i = 0; i < fields.length; i++) {
                if (columnName.equalsIgnoreCase(fields[i].getColumnName()) || columnName.equalsIgnoreCase(fields[i].getName())) {
                    columnToProperty[col] = i;
                    break;
                }
            }
        }

        return columnToProperty;
    }

    private FieldDescriptor[] gatherFieldDescriptors(Class<?> beanClass) throws SQLException {
        List<FieldDescriptor> fds = new ArrayList<>();
        for(Field field : beanClass.getDeclaredFields()) {
            int mod = field.getModifiers();
            if(Modifier.isStatic(mod) || Modifier.isFinal(mod) || Modifier.isVolatile(mod) || Modifier.isTransient(mod)) {
                continue;
            }
            field.setAccessible(true);
            Column column = field.getAnnotation(Column.class);
            String columnName = null;
            if(column != null) {
                columnName = column.value();
            }
            else {
                columnName = field.getName();
            }

            FieldDescriptor fd = new FieldDescriptor(field, columnName);
            fds.add(fd);
        }
        return fds.toArray(new FieldDescriptor[0]);
    }

    //* ******* taken directly from commons-dbutils *******
    private static final Map<Class<?>, Object> primitiveDefaults = new HashMap<Class<?>, Object>();

    static {
        primitiveDefaults.put(Integer.TYPE, Integer.valueOf(0));
        primitiveDefaults.put(Short.TYPE, Short.valueOf((short) 0));
        primitiveDefaults.put(Byte.TYPE, Byte.valueOf((byte) 0));
        primitiveDefaults.put(Float.TYPE, Float.valueOf(0f));
        primitiveDefaults.put(Double.TYPE, Double.valueOf(0d));
        primitiveDefaults.put(Long.TYPE, Long.valueOf(0L));
        primitiveDefaults.put(Boolean.TYPE, Boolean.FALSE);
        primitiveDefaults.put(Character.TYPE, Character.valueOf((char) 0));
    }
}
