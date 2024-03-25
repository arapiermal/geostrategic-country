package com.erimali.compute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.erimali.cntrygame.TESTING;

enum DataType {
    DOUBLE, INTEGER, STRING;
}

class Data<T> {
    T data;

    public Data(T data) {
        this.data = data;
    }

    public void set(T data) {
        this.data = data;
    }

    public T get() {
        return data;
    }

    @SuppressWarnings("unchecked")
    public void add(Object... values) {
        for (Object value : values) {
            switch (data) {
                case Double v when value instanceof Number ->
                        data = (T) Double.valueOf(v + ((Number) value).doubleValue());
                case Integer i when value instanceof Number ->
                        data = (T) Integer.valueOf(i + ((Number) value).intValue());
                case String s -> data = (T) (data.toString() + value.toString());
                case null, default -> throw new IllegalArgumentException("Unsupported operation for this data type.");
            }
        }
    }

    public int toInt() {
        if (data instanceof Number) {
            return ((Number) data).intValue();
        } else {
            throw new IllegalArgumentException("Unsupported operation for this data type.");
        }
    }

    public double toDouble() {
        if (data instanceof Number) {
            return ((Number) data).doubleValue();
        } else {
            throw new IllegalArgumentException("Unsupported operation for this data type.");
        }
    }

    @Override
    public String toString() {
        return data.toString();
    }

}

class ColData {
    int col;
    DataType type;

    public ColData(int col, DataType type) {
        this.col = col;
        this.type = type;
    }
}

class Table {
    private static final String[] COLSEPARATOR = {",", "\t"};
    private static final String[] ROWSEPARATOR = {";", System.lineSeparator()};

    List<List<Data<Object>>> table;
    Map<String, ColData> colNames;

    public Table() {
        table = new LinkedList<>();
        colNames = new LinkedHashMap<>();
    }

    public Table(String cols, String rows) {
        table = new LinkedList<>();
        colNames = new LinkedHashMap<>();
        this.addManyColumns(cols);
        this.addManyRows(rows);
    }

    public String colNamesToString() {
        return colNames.toString();
    }

    public void addRow(String parse) {
        String[] cols = parse.trim().split("\\s*" + COLSEPARATOR[0] + "\\s*");
        addRow(cols);
    }

    public void addRow(String[] cols) {
        if (cols.length != colNames.size())
            throw new IllegalArgumentException("NUMBER OF COLUMNS NOT EQUAL");
        List<Data<Object>> row = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, ColData> colName : colNames.entrySet()) {
            if (cols[i].isEmpty()) {
                row.add(null);
            } else {
                // entry loop
                row.add(new Data<Object>(parseData(cols[i], colName.getValue().type)));
            }
            i++;
        }

        table.add(row);
    }

    public void addManyRows(String parse) {
        String[] rows = parse.split(ROWSEPARATOR[0]);
        for (int i = 0; i < rows.length; i++) {
            addRow(rows[i]);
        }
    }

    public void addRow(List<Data<Object>> row) {
        // error handled with nulls
        while (row.size() < colNames.size()) {
            row.add(null);
        }
        while (row.size() > colNames.size()) {
            row.removeLast();
        }
        table.add(row);
    }

    private static final String TYPESEPARATOR = ":";

    public void addManyColumns(String parse) {
        String[] s = parse.split(COLSEPARATOR[0]);
        for (int i = 0; i < s.length; i++) {
            addColumn(s[i]);
        }
    }

    public void addColumn(String parse) {
        String[] s = parse.trim().split("\\s*" + TYPESEPARATOR + "+\\s*");
        if (s.length == 2) {
            addColumn(s[0], s[1]);
        } else {
            throw new IllegalArgumentException("MORE/LESS THAN 2 ARGUMENTS FOR COLUMN");
        }
    }

    public void addColumn(String name, String typeString) {
        DataType type = DataType.valueOf(typeString.toUpperCase());
        addColumn(name, type);
    }

    public void addColumn(String name, DataType type) {
        colNames.put(name, new ColData(colNames.size(), type));
        for (List<Data<Object>> row : table) {
            row.add(null);
        }
    }

    public Data<Object> getData(int r, int c) {
        if (r < 0 || r >= table.size()) {
            throw new IllegalArgumentException("INVALID ROW INDEX");
        }
        if (c < 0 || c >= colNames.size()) {
            throw new IllegalArgumentException("INVALID COLUMN INDEX");
        }
        return table.get(r).get(c);
    }

    public Data<Object> getData(int r, String c) {
        if (colNames.containsKey(c))
            return table.get(r).get(colNames.get(c).col);
        else
            throw new IllegalArgumentException("INVALID COLUMN NAME");
    }

    public String toStringCol(String colName) {
        int c = colNames.get(colName).col;
        return toStringCol(c);
    }

    public String toStringCol(int c) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (i = 0; i < table.size() - 1; i++) {
            sb.append(table.get(i).get(c).toString()).append(ROWSEPARATOR[1]);
        }
        sb.append(table.get(i).get(c).toString());
        return sb.toString();
    }

    public String toStringRow(int r) {
        StringBuilder sb = new StringBuilder();
        List<Data<Object>> row = table.get(r);
        int i = 0;
        for (i = 0; i < colNames.size() - 1; i++) {
            sb.append(row.get(i).toString()).append(COLSEPARATOR[0]);
        }
        sb.append(row.get(i).toString());
        return sb.toString();
    }

    // input separator index
    public String toStringTable() {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Map.Entry<String, ColData> colName : colNames.entrySet()) {
            sb.append(colName.getKey());
            if (i++ < colNames.size() - 1) {
                sb.append(COLSEPARATOR[0]);
            }
        }
        sb.append(System.lineSeparator());
        for (i = 0; i < table.size() - 1; i++) {
            sb.append(toStringRow(i)).append(System.lineSeparator());
        }
        sb.append(toStringRow(i));
        return sb.toString();
    }

    public Object parseData(String parse, DataType colName) {
        switch (colName) {
            case DOUBLE:
                try {
                    Double d = Double.parseDouble(parse);
                    return (Object) d;
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Cannot parse as Double: " + parse);
                }
            case INTEGER:
                try {
                    Integer i = Integer.parseInt(parse);
                    return (Object) i;
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Cannot parse as Integer: " + parse);
                }
            case STRING:
                return (Object) parse;
            default:
                throw new IllegalArgumentException("Unsupported data type");
        }
    }

    public int sumColInt(String colName) {
        return sumColInt(colIndex(colName));
    }

    public int sumColInt(int c) {
        int sum = 0;
        for (List<Data<Object>> row : table) {
            sum += row.get(c).toInt();
        }
        return sum;
    }

    public double sumColDouble(String colName) {
        return sumColDouble(colIndex(colName));
    }

    public int colIndex(String colName) {
        return colNames.get(colName).col;
    }

    public double sumColDouble(int c) {
        double sum = 0;
        for (List<Data<Object>> row : table) {
            sum += row.get(c).toDouble();
        }
        return sum;
    }

    public double maxColDouble(int c) {
        double max = Double.MIN_VALUE;
        for (List<Data<Object>> row : table) {
            double v = row.get(c).toDouble();
            if(max < v)
                max = v;
        }
        return max;
    }
    public double minColDouble(int c) {
        double min = Double.MAX_VALUE;
        for (List<Data<Object>> row : table) {
            double v = row.get(c).toDouble();
            if(min > v)
                min = v;
        }
        return min;
    }
    public double minColDouble(String colName) {
        return minColDouble(colIndex(colName));
    }
    public double maxColDouble(String colName) {
        return maxColDouble(colIndex(colName));
    }
    public double avgCol(String colName) {
        return avgCol(colIndex(colName));
    }

    public double avgCol(int c) {
        double sum = sumColDouble(c);
        return sum / table.size();
    }
    // avg(Age)

    public double parseFunc(String in) {
        int a = in.indexOf('(');
        int b = in.indexOf(')');
        String func = in.substring(0, a).toUpperCase();
        String colName = in.substring(a + 1, b).trim();
        return switch (func) {
            case "SUM" -> sumColDouble(colName);
            case "AVG" -> avgCol(colName);
            case "MIN" -> minColDouble(colName);
            case "MAX" -> maxColDouble(colName);
            default -> throw new IllegalArgumentException("INVALID FUNCTION");
        };
    }

}

public class Database {
    String name;
    Map<String, Table> tables;

    public Database(String name) {
        this.name = name;
        this.tables = new HashMap<>();
    }

    public Table getTable(String name) {
        if (tables.containsKey(name)) {
            return tables.get(name);
        } else {
            throw new IllegalArgumentException("TABLE DOESN'T EXIST");
        }
    }

    public void addTable(String name) {
        if (!tables.containsKey(name))
            tables.put(name, new Table());
        else
            throw new IllegalArgumentException("TABLE ALREADY EXISTS");
    }

    public void addTable(String name, String cols, String rows) {
        if (!tables.containsKey(name))
            tables.put(name, new Table(cols, rows));
        else
            throw new IllegalArgumentException("TABLE ALREADY EXISTS");
    }

    public void removeTable(String name) {
        if (tables.remove(name) == null) {
            throw new IllegalArgumentException("TABLE DOESN'T EXIST");
        }
    }

    public static void main(String[] args) {
        Database d = new Database("Name");
        String name = "tab";
        String c = "Hello:Integer,There:Double,You:String";
        String r = "2,4.0,Test;3,5.00,Test2";
        d.addTable(name, c, r);
        Table t = d.getTable(name);
        TESTING.print(t.toStringTable());
        TESTING.print(t.parseFunc("min(Hello)"));
        TESTING.print(t.parseFunc("max(Hello)"));
    }
}
