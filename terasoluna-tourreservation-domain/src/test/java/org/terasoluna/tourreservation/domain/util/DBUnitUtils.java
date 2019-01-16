package org.terasoluna.tourreservation.domain.util;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTable;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.excel.XlsDataSet;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DBUnitを使いやすくするためのユーティリティです。
 * <ul>
 * <li>ITableの内容を格子状に可視化
 * <li>2つのITableの差分（レコード追加、レコード削除、カラム更新）の抽出、および可視化
 * <li>XlsDataSetのロードの簡易化
 * </ul>
 */
public class DBUnitUtils {

    /**
     * 2つのITableの差分を、レコード追加、レコード削除、カラム更新の3つの状態で示します。
     */
    @Data
    public static class ITableDiff {
        private final ITable original;
        private final ITable revised;

        private ITable inserted;
        private ITable updated;
        private ITable deleted;

        private String viewCache;

        public ITableDiff(ITable original, ITable revised) throws DataSetException {
            this.original = original;
            this.revised = revised;

            // pkがあるmetadataを優先する
            ITableMetaData meta = original.getTableMetaData();
            if (meta.getPrimaryKeys().length == 0) {
                meta = revised.getTableMetaData();
            }
            HashMap<String, Integer> pkMapOriginal = pkMap(original, meta);
            HashMap<String, Integer> pkMapRevised = pkMap(revised, meta);

            Set<String> pkSetOriginal = pkMapOriginal.keySet();
            Set<String> pkSetRevised = pkMapRevised.keySet();

            Set<String> pkSetDeleted = new HashSet<>(pkSetOriginal);
            pkSetDeleted.removeAll(pkSetRevised);
            ITable t = diffTable(pkSetDeleted, pkMapOriginal, original, meta);
            this.deleted = t;

            Set<String> pkSetInserted = new HashSet<>(pkSetRevised);
            pkSetInserted.removeAll(pkSetOriginal);
            t = diffTable(pkSetInserted, pkMapRevised, revised, meta);
            this.inserted = t;

            Set<String> pkSetRetain = new HashSet<>(pkSetRevised);
            pkSetRetain.retainAll(pkSetOriginal);
            t = diffColumn(pkSetRetain, pkMapOriginal, original, pkMapRevised, revised, meta);
            this.updated = t;
        }

        public boolean isNone() {
            return inserted.getRowCount() == 0 && updated.getRowCount() == 0 && deleted.getRowCount() == 0;
        }

        @Override
        public String toString() {
            try {
                if (viewCache != null) {
                    return viewCache;
                } else {
                    viewCache = createDiffToITable(this).toString();
                    return viewCache;
                }
            } catch (DataSetException e) {
                //return e.getMessage();
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * カラム内の値の差分を示します。
     */
    @Data
    @AllArgsConstructor
    public static class ValueDiff {
        private Object original;
        private Object revised;

        @Override
        public String toString() {
            return original + " >> " + revised;
        }

        public static boolean equals(Object a, Object b) {
            boolean eq = Objects.equals(a, b);
            if (!eq) {
                eq = Objects.equals(String.valueOf(a), String.valueOf(b));
            }
            return eq;
        }
    }

    /**
     * 2つのITableの差分を返します。
     * @param original 更新前
     * @param revised 更新後
     * @return 差分を示す ITableDiff
     * @throws DataSetException
     */
    public static ITableDiff diff(ITable original, ITable revised) throws DataSetException {
        ITableDiff diff = new ITableDiff(original, revised);
        return diff.isNone() ? null : diff;
    }

    /**
     * ITableDiffをITable型に変換します。
     * テーブル差分の状態は、カラム名：CUD、値：C/U/D の列で示します。
     * @param original
     * @param revised
     * @return
     * @throws DataSetException
     */
    public static ITable createDiffToITable(ITableDiff diff) throws DataSetException {
        String DIFF_COLUMN_NAME = "CUD";
        Column DIFF_COLUMN = new Column(DIFF_COLUMN_NAME, DataType.UNKNOWN, "UNKNOWN", Column.NO_NULLS, null, null,
                null);
        ITableMetaData metaRevised = diff.getRevised().getTableMetaData();

        return new ITable() {

            ITableMetaData metaCache;

            @Override
            public ITableMetaData getTableMetaData() {
                if (metaCache != null) {
                    return metaCache;
                } else {
                    metaCache = new ITableMetaData() {
                        @Override
                        public String getTableName() {
                            return metaRevised.getTableName();
                        }

                        @Override
                        public Column[] getColumns() throws DataSetException {
                            Column[] diffColumns = { DIFF_COLUMN };
                            Column[] srcColumns = metaRevised.getColumns();
                            Column[] columns = new Column[1 + srcColumns.length];
                            System.arraycopy(diffColumns, 0, columns, 0, 1);
                            System.arraycopy(srcColumns, 0, columns, 1, srcColumns.length);
                            return columns;
                        }

                        @Override
                        public Column[] getPrimaryKeys() throws DataSetException {
                            return metaRevised.getPrimaryKeys();
                        }

                        @Override
                        public int getColumnIndex(String columnName) throws DataSetException {
                            if (DIFF_COLUMN_NAME.equals(columnName)) {
                                return 0;
                            } else {
                                return metaRevised.getColumnIndex(columnName) + 1;
                            }
                        }
                    };
                    return metaCache;
                }
            }

            @Override
            public int getRowCount() {
                return diff.getDeleted().getRowCount() + diff.getInserted().getRowCount()
                        + diff.getUpdated().getRowCount();
            }

            @Override
            public Object getValue(int row, String column) throws DataSetException {
                ITable t;
                String m;
                int r;
                if (diff.getDeleted().getRowCount() - 1 >= row) {
                    t = diff.getDeleted();
                    m = " D ";
                    r = row;
                } else if (diff.getDeleted().getRowCount() + diff.getInserted().getRowCount() - 1 >= row) {
                    t = diff.getInserted();
                    m = " C ";
                    r = row - diff.getDeleted().getRowCount();
                } else {
                    t = diff.getUpdated();
                    m = " U ";
                    r = row - diff.getDeleted().getRowCount() - diff.getInserted().getRowCount();
                }

                if (DIFF_COLUMN_NAME.equals(column)) {
                    return m;
                } else {
                    return t.getValue(r, column);
                }
            }

            @Override
            public String toString() {
                return DBUnitUtils.toString(this);
            }

        };
    }

    /**
     * toString()で格子状のテーブル出力できるようなITableを返します。
     * @param table
     * @return
     */
    public static ITable printable(ITable table) {
        return new ITable() {
            @Override
            public ITableMetaData getTableMetaData() {
                return table.getTableMetaData();
            }

            @Override
            public int getRowCount() {
                return table.getRowCount();
            }

            @Override
            public Object getValue(int row, String column) throws DataSetException {
                return table.getValue(row, column);
            }

            @Override
            public String toString() {
                return DBUnitUtils.toString(table);
            }
        };
    }

    /**
     * クラスファイルからの相対位置でExcelファイルをロードします。
     * @param xlsfile
     * @param thisObject
     * @return
     * @throws DataSetException
     * @throws IOException
     */
    public static XlsDataSet loadXls(String xlsfile, Object thisObject) throws DataSetException, IOException {
        return new XlsDataSet(
                new File(thisObject.getClass().getProtectionDomain().getCodeSource().getLocation().getPath()
                        + thisObject.getClass().getPackage().getName().replace('.', '/') + '/' + xlsfile)) {
            @Override
            public ITable getTable(String tableName) throws DataSetException {
                return DBUnitUtils.printable(super.getTable(tableName));
            }
        };
    }

    /**
     * DatabaseConnectionを取得します。
     * @param jdbcTemplate
     * @return
     * @throws DatabaseUnitException
     */
    public static DatabaseConnection getConnection(NamedParameterJdbcTemplate jdbcTemplate)
            throws DatabaseUnitException {
        Connection conn = DataSourceUtils.getConnection(jdbcTemplate.getJdbcTemplate().getDataSource());
        return new DatabaseConnection(conn);
    }

    private static ITable diffColumn(Set<String> pkSet, HashMap<String, Integer> pkMapOriginal, ITable original,
            HashMap<String, Integer> pkMapRevised, ITable rivised, ITableMetaData meta)
            throws DataSetException {
        // rivisedの方のカラムベースに差分を抽出
        Column[] columns = rivised.getTableMetaData().getColumns();
        DefaultTable newTable = new DefaultTable(meta) {
            @Override
            public String toString() {
                return DBUnitUtils.toString(this);
            }
        };
        int newRow = 0;
        for (String pk : pkSet) {
            int rowOriginal = pkMapOriginal.get(pk);
            int rowRevised = pkMapRevised.get(pk);
            Map<String, Object> kv = new HashMap<>();
            boolean diffExists = false;
            for (Column column : columns) {
                String columnName = column.getColumnName();
                Object valueOriginal = original.getValue(rowOriginal, columnName);
                Object valueRivised = rivised.getValue(rowRevised, columnName);
                if (!ValueDiff.equals(valueOriginal, valueRivised)) {
                    kv.put(columnName, new ValueDiff(valueOriginal, valueRivised));
                    diffExists = true;
                } else {
                    kv.put(columnName, valueOriginal.toString());
                }
            }

            if (diffExists) {
                newTable.addRow();
                for (Column column : columns) {
                    String columnName = column.getColumnName();
                    newTable.setValue(newRow, columnName, kv.get(columnName));
                }
                newRow++;
            }
        }
        return newTable;
    }

    private static ITable diffTable(Set<String> pkSet, HashMap<String, Integer> pkMap, ITable table,
            ITableMetaData meta)
            throws DataSetException {
        Column[] columns = table.getTableMetaData().getColumns();
        DefaultTable newTable = new DefaultTable(meta) {
            @Override
            public String toString() {
                return DBUnitUtils.toString(this);
            }
        };
        int newRow = 0;
        for (String pk : pkSet) {
            newTable.addRow();
            int row = pkMap.get(pk);
            for (Column column : columns) {
                newTable.setValue(newRow, column.getColumnName(), table.getValue(row, column.getColumnName()));
            }
            newRow++;
        }
        return newTable;
    }

    private static HashMap<String, Integer> pkMap(ITable table, ITableMetaData meta) throws DataSetException {
        HashMap<String, Integer> pkMap = new HashMap<>();
        for (int row = 0; row < table.getRowCount(); row++) {
            String pk = pk(table, row, meta);
            pkMap.put(pk, row);
        }
        return pkMap;
    }

    private static String pk(ITable table, int row, ITableMetaData meta) throws DataSetException {
        StringBuilder s = new StringBuilder();
        Column[] c = table.getTableMetaData().getPrimaryKeys();
        if (c.length == 0) {
            c = meta.getPrimaryKeys();
            if (c.length == 0) {
                c = table.getTableMetaData().getColumns();
            }
        }
        Column[] columns = new Column[c.length];
        System.arraycopy(c, 0, columns, 0, c.length);
        Arrays.sort(columns, (a, b) -> a.getColumnName().compareTo(b.getColumnName()));
        for (Column column : columns) {
            String name = column.getColumnName();
            Object obj = table.getValue(row, name);
            s.append(name);
            s.append(':');
            s.append(obj != null ? obj.toString() : "null");
            s.append(',');
        }
        return s.toString();
    }

    /**
     * ITableの内容を格子状にした文字列を返します。
     * @param table
     * @return 出力文字列
     */
    public static String toString(ITable table) {
        StringBuilder s = new StringBuilder();
        try {
            Column[] columns = table.getTableMetaData().getColumns();

            Map<String, Integer> width = new HashMap<>();
            for (Column column : columns) {
                String columnName = column.getColumnName();
                // width
                int l = getFormattedWidth(columnName);
                width.put(columnName, l);
            }

            List<Map<String, String>> view = new ArrayList<Map<String, String>>();
            for (int row = 0; row < table.getRowCount(); row++) {
                Map<String, String> rowMap = new HashMap<>();
                for (Column column : columns) {
                    String columnName = column.getColumnName();
                    Object obj = table.getValue(row, columnName);
                    String value = obj != null ? obj.toString() : "null";
                    rowMap.put(columnName, value);

                    // width
                    int l = getFormattedWidth(value);
                    if (width.get(columnName) < l) {
                        width.put(columnName, l);
                    }
                }
                view.add(rowMap);
            }

            // print
            s.append(System.getProperty("line.separator"));
            s.append("|");
            for (Column column : columns) {
                String columnName = column.getColumnName();
                s.append(pad(columnName, width.get(columnName), " "));
                s.append("|");
            }
            s.append(System.getProperty("line.separator"));
            s.append("|");
            for (Column column : columns) {
                String columnName = column.getColumnName();
                s.append(pad("", width.get(columnName), "-"));
                s.append("|");
            }
            s.append(System.getProperty("line.separator"));
            view.forEach(map -> {
                s.append("|");
                for (Column column : columns) {
                    String columnName = column.getColumnName();
                    String value = map.get(columnName);
                    s.append(pad(value, width.get(columnName), " "));
                    s.append("|");
                }
                s.append(System.getProperty("line.separator"));
            });
            s.append(System.getProperty("line.separator"));
        } catch (DataSetException e) {
            s.append(e.getMessage());
        }
        return s.toString();
    }

    /**
     * 全角・半角を考慮した文字列の幅を返します。
     * @param s
     * @return
     */
    private static int getFormattedWidth(String s) {
        int l = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            //https://blogs.yahoo.co.jp/kerupani/16550170.html
            //  ( 英数字      )    ( \ 記号      )    ( ~ 記号      )    ( 半角カナ                     )
            if ((c <= '\u007e') || (c == '\u00a5') || (c == '\u203e') || (c >= '\uff61' && c <= '\uff9f')) {
                l++;
            } else { // その他 (全角)
                l += 2;
            }
        }
        return l;
    }

    /**
     * 指定した文字列で埋めた文字列を返します。
     * @param value
     * @param width
     * @param padChar
     * @return
     */
    private static String pad(String value, int width, String padChar) {
        int l = getFormattedWidth(value);
        int rest = width - l;
        if (rest < 0)
            rest = 0;
        StringBuilder s = new StringBuilder(String.join("", Collections.nCopies(rest, padChar)));
        s.append(value);
        return s.toString();
    }

}
