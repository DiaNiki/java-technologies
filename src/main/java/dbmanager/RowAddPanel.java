package dbmanager;

import database.Column;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

class RowAddPanel extends JPanel {
    private RowAddTableModel rowAddModel;
    private String table;

    RowAddPanel(String table, Collection<Column> columns) {
        this.table = table;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(new JLabel("Table name: " + table));

        JTable columnsTable = new JTable();
        columnsTable.setPreferredScrollableViewportSize(new Dimension(200, 200));
        columnsTable.setFillsViewportHeight(true);
        rowAddModel = new RowAddTableModel(columns);
        columnsTable.setModel(rowAddModel);
        this.add(new JLabel("Values:"));
        JScrollPane tableScroll = new JScrollPane();
        tableScroll.setViewportView(columnsTable);
        this.add(tableScroll);
    }

    String getDBQuery() {
        return String.format("insert into %s (%s) values (%s)", table,
                rowAddModel.getColumnsAsString(), rowAddModel.getValuesAsString());
    }
}

class RowAddTableModel extends AbstractTableModel {
    private ArrayList<Column> columns = new ArrayList<>();
    private ArrayList<String> values = new ArrayList<>();

    RowAddTableModel(Collection<Column> columns) {
        this.columns.addAll(columns);
        columns.forEach(column -> this.values.add(""));
    }

    @Override
    public int getRowCount() {
        return columns.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public String getColumnName(int column) {
        return column == 0 ? "Column Name" : "Value";
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return columnIndex == 0 ?
                String.format("%s (%s)", columns.get(rowIndex).getName(), columns.get(rowIndex).getType().toString()) :
                values.get(rowIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 1;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if  (columnIndex == 1) {
            values.set(rowIndex, aValue == null ? null : aValue.toString());
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    String getColumnsAsString() {
        return columns.stream().map(Column::getName).collect(Collectors.joining(", "));
    }

    public String getValuesAsString() {
        return values.stream().collect(Collectors.joining(", "));
    }
}
