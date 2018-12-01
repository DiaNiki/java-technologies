package dbmanager;

import database.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

class DBGUIManager extends JFrame {
    private Database database;

    private final JLabel dbNameLabel = new JLabel("Please open some database");
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
        JMenu menuHelp = new JMenu("Help");
        menuBar.add(menuFile);
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
        controlPanel.add(run);
        return controlPanel;
    }

    private JPanel initializeMenuPanel() {
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BorderLayout());
        JList tableList = new JList();
        tableList.setModel(tableListModel);
        tableList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
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