package org.example;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;

public class MyTable extends JDialog{
    private final DefaultTableModel tableModel;
    private final ArrayList<Shape> shapes;
    private int selectedRow;
    private static MyTable instance;
    private final ShapeEditor shapeEditor;
    private final JFrame parentFrame;
    private MyTable(JFrame parentFrame, ShapeEditor shapeEditor){
        super(parentFrame, "Table of objects", false);
        super.setBounds(100, 100, 500, 300);
        String[] columnNames = {"Name", "x1", "y1", "x2", "y2"};
        this.shapeEditor = shapeEditor;
        this.parentFrame = parentFrame;
        tableModel = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        JPanel buttonPanel = getButtonPanel(shapeEditor, table);
        shapes = shapeEditor.getShapes();

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    shapes.get(selectedRow).setColor(Color.RED);
                    repaint();
                }
            }
        });
        parentFrame.repaint();
        super.setLayout(new BorderLayout());
        super.add(buttonPanel, BorderLayout.SOUTH);
        super.add(scrollPane, BorderLayout.CENTER);
    }

    public static void setInstance(MyTable table_m) {
        instance = table_m;
    }

    private JPanel getButtonPanel(ShapeEditor shapeEditor, JTable table) {
        JButton delete = new JButton("Delete");
        delete.addActionListener(e -> {
            int selected = table.getSelectedRow();
            tableModel.removeRow(selected);
            shapeEditor.deleteShape(selected);
            repaint();
        });
        JButton saveToFile = new JButton("Save to File");
        saveToFile.addActionListener(e -> saveTableToTxt(tableModel));
        JButton openFile = new JButton("Open File");
        openFile.addActionListener(e -> openFile(tableModel));
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(delete);
        buttonPanel.add(saveToFile);
        buttonPanel.add(openFile);
        return buttonPanel;
    }
    public static synchronized MyTable getInstance(JFrame parentFrame, ShapeEditor shapeEditor) {
        if (instance == null) {
            instance = new MyTable(parentFrame, shapeEditor);
        }
        return instance;
    }
    public void addObjectTable (String name, Point start, Point end){
        int x1 = start.x;
        int y1 = start.y;
        int x2 = end.x;
        int y2 = end.y;
        Object[] row = {name, x1, y1, x2, y2};
        tableModel.addRow(row);
    }
    public void clearTable(){
        tableModel.setRowCount(0);
        super.repaint();
    }
    private void saveTableToTxt(DefaultTableModel tableModel) {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile))) {
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    writer.write(tableModel.getColumnName(i) + "\t");
                }
                writer.newLine();

                for (int row = 0; row < tableModel.getRowCount(); row++) {
                    for (int col = 0; col < tableModel.getColumnCount(); col++) {
                        writer.write(tableModel.getValueAt(row, col).toString() + "\t");
                    }
                    writer.newLine();
                }

                JOptionPane.showMessageDialog(null, "Saved successfully\n" + selectedFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error saving file");
            }
        }
    }
    private void openFile (DefaultTableModel tableModel){
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Text files", "txt");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
                String line;
                int lineNumber = 0;
                shapeEditor.clearShapes();
                while ((line = reader.readLine()) != null) {
                    lineNumber++;

                    if (lineNumber > 1) {
                        String[] words = line.split("\t");
                        tableModel.addRow(words);
                        drawFromFile(words);
                        parentFrame.repaint();
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error opening file");
            }
        }
    }
    private void drawFromFile(String[] words){
        String name = words[0];
        int x1 = Integer.parseInt(words[1]);
        int y1 = Integer.parseInt(words[2]);
        int x2 = Integer.parseInt(words[3]);
        int y2 = Integer.parseInt(words[4]);
        Point start = new Point(x1, y1);
        Point end = new Point(x2, y2);
        Shape newShape = null;
        switch (name){
            case "Rectangle":
                newShape = new Rectangle(start, end, Color.BLACK);
                Shape.setFillColor(Color.BLUE);
                break;
            case "Ellipse":
                newShape = new Ellipse(start, end, Color.BLACK);
                Shape.setFillColor(null);
                break;
            case "Line":
                newShape = new Line(start, end, Color.BLACK);
                break;
            case "Dot":
                newShape = new Dot(start, end, Color.BLACK);
                break;
            case "Cube":
                newShape = new Cube(start, end, Color.BLACK);
                break;
            case "Line with two ellipses":
                newShape = new LineWithEllipse(start, end, Color.BLACK);
                break;
        };
        if (newShape != null) {
            shapeEditor.addShape(newShape);
            repaint();
        }
        parentFrame.repaint();
    }
    public ShapeEditor getShapeEditor(){
        return shapeEditor;
    }
}
