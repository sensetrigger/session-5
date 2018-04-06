package ru.sbt.jschool.session5.problem1;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

class Cell {
    private String fieldName;
    private String columnType;

    Cell(String fName, String cType) {
        fieldName = fName;
        columnType = cType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getColumnType() {
        return columnType;
    }
}

public class SQLGenerator {
    private List<Cell> columns = new ArrayList<>();
    private int columnsCount = 0;
    private int primaryKeysCount = 0;

    public <T> void getFields(Class<T> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            Annotation[] annotations = field.getAnnotations();
            for (Annotation a : annotations) {
                if (a instanceof Column) {
                    columnsCount++;
                    if (((Column) a).name().equals(""))
                        columns.add(new Cell(field.getName(), a.annotationType().getSimpleName()));
                    else
                        columns.add(new Cell(((Column) a).name(), a.annotationType().getSimpleName()));
                }
                else if (a instanceof PrimaryKey) {
                    primaryKeysCount++;
                    if (((PrimaryKey) a).name().equals(""))
                        columns.add(new Cell(field.getName(), a.annotationType().getSimpleName()));
                    else
                        columns.add(new Cell(((PrimaryKey) a).name(), a.annotationType().getSimpleName()));
                }
            }
        }
    }

    public <T> String select(Class<T> clazz) {
        getFields(clazz);
        StringBuilder query = new StringBuilder();

        query.append("SELECT ");

        int count = 0;
        for (Cell cell : columns)
            if (cell.getColumnType().equals("Column")) {
                query.append(cell.getFieldName().toLowerCase());
                count++;
                if (count < columnsCount)
                    query.append(", ");
            }

        query
                .append(" FROM ")
                .append(clazz.getAnnotation(Table.class).name())
                .append(" WHERE ");

        count = 0;
        for (Cell cell : columns)
            if (cell.getColumnType().equals("PrimaryKey")) {
                query.append(cell.getFieldName().toLowerCase());
                count++;
                if (count < primaryKeysCount)
                    query.append(" = ? AND ");
                else
                    query.append(" = ?");
            }

        return query.toString();
    }

    public <T> String insert(Class<T> clazz) {
        getFields(clazz);
        StringBuilder query = new StringBuilder();

        int count = 0;
        query
                .append("INSERT INTO ")
                .append(clazz.getAnnotation(Table.class).name())
                .append("(");

        for (Cell cell : columns) {
            query.append(cell.getFieldName().toLowerCase());
            count++;
            if (count < columns.size())
                query.append(", ");
        }
        query.append(") VALUES (");

        count = 0;
        for (Cell cell : columns) {
            query.append("?");
            count++;
            if (count < columns.size())
                query.append(", ");
        }

        query.append(")");
        return query.toString();
    }

    public <T> String update(Class<T> clazz) {
        getFields(clazz);
        StringBuilder query = new StringBuilder();

        query
                .append("UPDATE ")
                .append(clazz.getAnnotation(Table.class).name())
                .append(" SET ");

        int count = 0;
        for (Cell cell : columns)
            if (cell.getColumnType().equals("Column")) {
                query.append(cell.getFieldName().toLowerCase());
                count++;
                if (count < columnsCount)
                    query.append(" = ?, ");
                else
                    query.append(" = ?");
            }

        query.append(" WHERE ");

        count = 0;
        for (Cell cell : columns)
            if (cell.getColumnType().equals("PrimaryKey")) {
                query.append(cell.getFieldName().toLowerCase());
                count++;
                if (count < primaryKeysCount)
                    query.append(" = ? AND ");
                else
                    query.append(" = ?");
            }

        return query.toString();
   }

    public <T> String delete(Class<T> clazz) {
        getFields(clazz);
        StringBuilder query = new StringBuilder();

        query
                .append("DELETE FROM ")
                .append(clazz.getAnnotation(Table.class).name())
                .append(" WHERE ");

        int count = 0;
        for (Cell cell : columns)
            if (cell.getColumnType().equals("PrimaryKey")) {
                query.append(cell.getFieldName().toLowerCase());
                count++;
                if (count < primaryKeysCount)
                    query.append(" = ? AND ");
                else
                    query.append(" = ?");
            }

        return query.toString();
    }
}
