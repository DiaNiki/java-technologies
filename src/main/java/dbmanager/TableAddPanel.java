package dbmanager;

import database.Column;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

class TableAddPanel extends JPanel {
    private JTextField tableName = new JTextField();
    private TableAddTableModel tableAddModel = new TableAddTableModel();

    TableAddPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(new JLabel("Table name:"));
        this.add(tableName);

        JTable columnsTable = new JTable();
        columnsTable.setPreferredScrollableViewportSize(new Dimension(200, 200));
        columnsTable.setFillsViewportHeight(true);
        columnsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        columnsTable.setModel(tableAddModel);
        JComboBox typeCompoBox = new JComboBox(Column.Type.values());
        columnsTable.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(typeCompoBox));
        this.add(new JLabel("Columns:"));
        JScrollPane tableScroll = new JScrollPane();
        tableScroll.setViewportView(columnsTable);
        this.add(tableScroll);

        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add column");
        addButton.addActionListener(e -> {
            addColumn();
        });
        buttonPanel.add(addButton);
        JButton removeButton = new JButton("Remove column");
        removeButton.addActionListener(e -> {
            if (columnsTable.getSelectedRowCount() > 0) {
                tableAddModel.remove(columnsTable.getSelectedRow());
            }
        });
        buttonPanel.add(removeButton);
        this.add(buttonPanel);

        addColumn();
    }

    void addColumn() {
        tableAddModel.add(new Column(Column.Type.INT, ""));
    }

    String getDBQuery() {
        return "create table " + tableName.getText() + " (" + tableAddModel.getColumnsAsString() + ")";
    }
}

class TableAddTableModel extends AbstractTableModel {
    private ArrayList<Column> columns;

    TableAddTableModel() {
        columns = new ArrayList<>(5);
    }

    void add(Column column) {
        columns.add(column);
        fireTableRowsInserted(columns.size() - 1, columns.size() - 1);
    }

    void remove(Column column) {
        if (columns.contains(column)) {
            int index = columns.indexOf(column);
            remove(index);
        }
    }

    void remove(int index) {
        columns.remove(index);
        fireTableRowsDeleted(index, index);
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
        return column == 0 ? "Type" : "Name";
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Column column = columns.get(rowIndex);
        return columnIndex == 0 ? column.getType().toString() : column.getName();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Column column = columns.get(rowIndex);
        if  (columnIndex == 0) {
            column.setType(aValue == null ? null : Column.Type.valueOf(aValue.toString()));
        } else {
            column.setName(aValue == null ? null : aValue.toString());
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    public String getColumnsAsString() {
        return columns.stream()
                .map(column -> column.getType().toString() + " " + column.getName())
                .collect(Collectors.joining(", "));
    }
}

