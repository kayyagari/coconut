package io.sereen.coconut;

import java.lang.reflect.Field;

/**
 * Kiran A (kiran@sereen.io)
 */
public class FieldDescriptor {
    private Field field;
    private String columnName;

    public FieldDescriptor(Field field, String columnName) {
        this.field = field;
        this.columnName = columnName;
    }

    public Field getField() {
        return field;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getName() {
        return field.getName();
    }

    public Class<?> getFieldType() {
        return field.getType();
    }

    public void set(Object bean, Object value) {
        try {
            field.set(bean, value);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
