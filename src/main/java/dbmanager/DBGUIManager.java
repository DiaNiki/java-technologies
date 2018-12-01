package dbmanager;

import database.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

class DBGUIManager extends JFrame {
    private Database database;

    private final JLabel dbNameLabel = new JLabel("Please open some database");
    private final JList tableList = new JList();
    private final DefaultListModel tableListModel = new DefaultListModel();
    private final JLabel resultMessage = new JLabel();
    private final MyTableModel resultTableModel = new MyTableModel();
    private final JFileChooser fileChooser = new JFileChooser();

    DBGUIManager() {
        super("Database Manager");

        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setSize(1600,1000);

        this.getContentPane().add(BorderLayout.NORTH, initializeMenuBar());
        this.getContentPane().add(BorderLayout.WEST, initializeMenuPanel());
        this.getContentPane().add(BorderLayout.CENTER, initializeResultPanel());
        this.getContentPane().add(BorderLayout.SOUTH, initializeControlPanel());
    }

    private JMenuBar initializeMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menuFile = new JMenu("File");
        JMenu menuAction = new JMenu("Action");
        JMenu menuHelp = new JMenu("Help");
        menuBar.add(menuFile);
        menuBar.add(menuAction);
        menuBar.add(menuHelp);
        JMenuItem menuFileOpen = new JMenuItem("Open");
        menuFileOpen.addActionListener(e -> {
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                String databasePath = fileChooser.getSelectedFile().getAbsolutePath();
                try {
                    database = new DatabaseReader(databasePath).read();
                } catch (Exception ex) {
                    database = new Database(databasePath);
                    ex.printStackTrace();
                }
                dbNameLabel.setText(fileChooser.getSelectedFile().getName());
                populateTableList();
            }
        });
        JMenuItem menuFileSaveAs = new JMenuItem("Save as");
        menuFileSaveAs.addActionListener(e -> {
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
            {
                database.setFilePath(fileChooser.getSelectedFile().getAbsolutePath());
                try {
                    database.save();
                    dbNameLabel.setText(fileChooser.getSelectedFile().getName());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        menuFile.add(menuFileOpen);
        menuFile.add(menuFileSaveAs);
        JMenuItem menuCreateTable = new JMenuItem("Create table");
        JMenuItem menuDeleteTable = new JMenuItem("Delete table");
        JMenuItem menuCartesianProduct = new JMenuItem("Cartesian product");
        menuCreateTable.addActionListener(e -> {
            if  (database == null) {
                JOptionPane.showMessageDialog(this, "No open database");
                return;
            }
            TableAddPanel tableAdd = new TableAddPanel();
            if (JOptionPane.showConfirmDialog(this, tableAdd,
                    "Enter data for the new table:", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                displayQueryResults(database.query(tableAdd.getDBQuery()));
                populateTableList();
            }
        });
        menuDeleteTable.addActionListener(e -> {
            String tableName = (String) tableList.getSelectedValue();
            if  (tableName == null || database == null) {
                JOptionPane.showMessageDialog(this, "Table is not selected");
                return;
            }
            if (JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete " + tableName + "?", "Please confirm",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                displayQueryResults(database.query("drop table " + tableName));
                populateTableList();
            }
        });
        menuCartesianProduct.addActionListener(e -> {
            java.util.List tableNames = tableList.getSelectedValuesList();
            if (tableNames.size() != 2 || database == null) {
                JOptionPane.showMessageDialog(this, "Two tables must be selected");
                return;
            }
            displayQueryResults(database.query(
                    String.format("cartesian product %s by %s", tableNames.get(0), tableNames.get(1))));
        });
        menuAction.add(menuCreateTable);
        menuAction.add(menuDeleteTable);
        menuAction.add(menuCartesianProduct);
        JMenuItem menuAbout = new JMenuItem("About");
        menuAbout.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Created by Diana Nykytiuk");
        });
        menuHelp.add(menuAbout);
        return menuBar;
    }

    private JPanel initializeControlPanel() {
        JPanel controlPanel = new JPanel();
        JLabel label = new JLabel("Enter Query Text: ");
        JTextField queryTextField = new JTextField(100);
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> {
            queryTextField.setText("");
        });
        JButton run = new JButton("Run!");
        Action action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (database != null) {
                    displayQueryResults(database.query(queryTextField.getText()));
                    populateTableList();
                }
            }
        };
        queryTextField.addActionListener(action);
        run.addActionListener(action);
        controlPanel.add(label);
        controlPanel.add(queryTextField);
        controlPanel.add(clearButton);
        controlPanel.add(run);
        return controlPanel;
    }

    private JPanel initializeMenuPanel() {
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BorderLayout());
        tableList.setModel(tableListModel);
        tableList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tableList.setLayoutOrientation(JList.VERTICAL);
        tableList.setVisibleRowCount(-1);
        tableList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = tableList.locationToIndex(e.getPoint());
                    String tableName = tableListModel.getElementAt(index).toString();
                    displayQueryResults(database.query("select * from " + tableName));
                }
            }
        });
        JScrollPane listScroll = new JScrollPane(tableList);
        listScroll.setPreferredSize(new Dimension(300, 100));
        menuPanel.add(BorderLayout.NORTH, dbNameLabel);
        menuPanel.add(BorderLayout.CENTER, listScroll);
        return menuPanel;
    }

    private JPanel initializeResultPanel() {
        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new BorderLayout());
        resultPanel.add(BorderLayout.NORTH, resultMessage);
        JTable resultTable = new JTable(resultTableModel);
        JScrollPane tableScroll = new JScrollPane(resultTable);
        resultTable.setFillsViewportHeight(true);
        resultPanel.add(BorderLayout.CENTER, tableScroll);
        return resultPanel;
    }

    private void populateTableList() {
        if (database != null) {
            Result tables = database.query("list tables");
            tableListModel.clear();
            for (Row row : tables.getRows()) {
                tableListModel.addElement(row.getElement("table_name").getValue());
            }
        }
    }

    private void displayQueryResults(Result result) {
        resultMessage.setText("<html>Result: " + result.getStatus() +
                (result.getStatus() == Result.Status.FAIL  ? "<br/>" + result.getReport() : "") +
                (result.getRows() == null || result.getRows().size() == 0 ? "<br/>Result rows empty" : "") +
                "</html>");
        resultTableModel.setResult(result);
    }
}

class MyTableModel extends AbstractTableModel {
    ArrayList<Row> rows = new ArrayList<>();
    ArrayList<String> columns = new ArrayList<>();

    void setResult(Result result) {
        rows.clear();
        columns.clear();
        if (result.getRows() != null && result.getRows().size() > 0) {
            rows.addAll(result.getRows());
            for (Element element : result.getRows().iterator().next().getElements()) {
                columns.add(element.getColumn());
            }
        }
        fireTableStructureChanged();
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return columns.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return rows.get(rowIndex).getElement(columns.get(columnIndex)).getValue();
    }

    @Override
    public String getColumnName(int column) {
        return columns.get(column);
    }
}

class TableAddPanel extends JPanel {
    private JTextField tableName = new JTextField();
    private TableAddTableModel tableAddModel = new TableAddTableModel();

    TableAddPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(new JLabel("Database name:"));
        this.add(tableName);
        JTable columnsTable = new JTable();
        columnsTable.setPreferredScrollableViewportSize(new Dimension(200, 200));
        columnsTable.setFillsViewportHeight(true);
        columnsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        columnsTable.setModel(tableAddModel);
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
