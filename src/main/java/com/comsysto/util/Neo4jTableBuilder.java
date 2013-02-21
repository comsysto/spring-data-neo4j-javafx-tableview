package com.comsysto.util;

import com.comsysto.ScreensConfiguration;
import com.comsysto.neo4j.domain.Neo4jNode;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DateTimeStringConverter;
import javafx.util.converter.DefaultStringConverter;
import javafx.util.converter.IntegerStringConverter;
import org.springframework.data.neo4j.repository.GraphRepository;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/** @author Elisabeth Engel */
public class Neo4jTableBuilder <T extends Neo4jNode> {

    private Class<T> tableClass;

    private GraphRepository tableRepository;

    private TableView<T> tableView;

    private ObservableList<T> data;

    private T newNode;

    private class Neo4jTableColumn implements Comparable<Neo4jTableColumn> {
        private TableColumn tableColumn;
        private int columnOrder;

        public Neo4jTableColumn(TableColumn tableColumn, int columnOrder) {
            this.tableColumn = tableColumn;
            this.columnOrder = columnOrder;
        }

        public TableColumn getTableColumn() {
            return tableColumn;
        }

        public int getColumnOrder() {
            return columnOrder;
        }

        public void setColumnOrder(int columnOrder) {
            this.columnOrder = columnOrder;
        }

        @Override
        public int compareTo(Neo4jTableColumn o) {
            return (this.columnOrder < o.columnOrder ) ? -1: (this.columnOrder > o.columnOrder) ? 1:0 ;
        }
    }

    /** listens to changes in the sort type of a column
     *
     */
    private class SortTypeChangeListener implements InvalidationListener {

        @Override
        public void invalidated(Observable o) {
            if (o instanceof ObjectProperty) {
                ObjectProperty objectProperty = (ObjectProperty)o;
                if (objectProperty.getValue().equals(TableColumn.SortType.DESCENDING)) {
                    // only ascending order -> workaround
                    ObservableList<TableColumn<T, ?>> newSortOrder = FXCollections.observableArrayList();
                    /*
                    ObservableList<TableColumn<T, ?>> sortOrder = tableView.getSortOrder();
                    for(TableColumn column : sortOrder) {
                        newSortOrder.add(column);
                    }
                    */
                    tableView.getSortOrder().setAll(newSortOrder);

                }
            }
        }
    }

    /** automatically generates a tableView to add, edit and remove the neo4j tableRepository fields annotated
     * with @Neo4jTableBuilderColumnField.
     *
     * @param tableClass The class of the table items.
     * @param tableRepository  The repository for the table items.
     * @param tableView The tableView that shows the table items.
     */
    public Neo4jTableBuilder(Class<T> tableClass, GraphRepository tableRepository, TableView<T> tableView) {

        this.tableClass = tableClass;
        this.tableRepository = tableRepository;
        this.tableView = tableView;
    }

    /** initializes the neo4j table
     *
     */
    public void initDataTable ()
    {
        // collect all tableColumns
        ArrayList<Neo4jTableColumn> tableColumns = new ArrayList<Neo4jTableColumn>();
        tableColumns.addAll(createDataColumnsFromAnnotatedFields());
        tableColumns.addAll(createDataColumnsFromAnnotatedMethods());

        // sort
        Collections.sort(tableColumns);

        // add them to tableView
        for (Neo4jTableColumn tableColumn : tableColumns)  {
            tableView.getColumns().add(tableColumn.getTableColumn());
        }

        // last column with save or delete botton
        TableColumn lastColumn = new TableColumn<Neo4jNode, String>();
        lastColumn.setMinWidth(80);
        lastColumn.setEditable(false);
        lastColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Neo4jNode, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Neo4jNode, String> p) {
               if (!p.getValue().isSaved()){
                    return new SimpleStringProperty("saveButton");
                }
                return new SimpleStringProperty("deleteButton");
            }
        });
        lastColumn.setCellFactory(new Callback<TableColumn<Neo4jNode, String>, TableCell<Neo4jNode, String>>() {

            public TableCell<Neo4jNode, String> call(TableColumn<Neo4jNode, String> p) {
                TableCell returnTableCell;

                //System.out.println("item 0:"+Neo4jTableBuilder.this.tableView.getItems().get(0));

                returnTableCell = new ButtonTableCell<Neo4jNode, String>();

                return returnTableCell;
            }
        });
        tableView.getColumns().add(lastColumn);

        //listen to sorting type (ASC/DESC) change
        lastColumn.sortTypeProperty().addListener(new SortTypeChangeListener());

        //listen to sortorder change
        tableView.getSortOrder().addListener(new ListChangeListener<TableColumn<T, ?>>() {
            @Override
            public void onChanged(Change<? extends TableColumn<T, ?>> change) {
                /*
                if (change.getList().size() > 0) {
                    System.out.println("getSortOrder - onChanged - column: " + change.getList().get(0).getSortType());
                } else {
                    System.out.println("getSortOrder - onChanged - empty list");
                }
                */
                resetTableData();
            }
        });

    }

    /** creates all TableColumns from the anntotated fields
     *
     * @return ArrayList with all Neo4jTableColumns
     */
    private ArrayList<Neo4jTableColumn> createDataColumnsFromAnnotatedFields() {

        ArrayList<Neo4jTableColumn> tableColumns = new ArrayList<Neo4jTableColumn>();

        // find annotated fields
        Field[] fields = tableClass.getDeclaredFields();
        for (final Field field : fields) {

            field.setAccessible(true);

            Neo4jTableBuilderColumnField annotation = field.getAnnotation(Neo4jTableBuilderColumnField.class);

            if (annotation != null)
            {
                final TableColumn newColumn;
                final String name =  field.getName();
                final String columnName = (!annotation.columnName().equals("")) ? annotation.columnName() : field.getName();
                newColumn = new TableColumn(columnName);
                //listen to sorting type (ASC/DESC) change
                newColumn.sortTypeProperty().addListener(new SortTypeChangeListener());
                newColumn.setMinWidth(columnName.length()*14);

                if (annotation.columnType() == Neo4jTableBuilderColumnField.FieldType.readAndWrite)
                {
                    createAndSetFactories(newColumn, name, field.getType());

                    newColumn.setOnEditCommit(
                            new EventHandler<TableColumn.CellEditEvent<Neo4jNode, String>>() {
                                @Override
                                public void handle(TableColumn.CellEditEvent<Neo4jNode, String> t) {
                                    Neo4jNode neo4jNode = t.getTableView().getItems().get(t.getTablePosition().getRow());

                                    try {
                                        field.set(neo4jNode, t.getNewValue());
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    }

                                    if (t.getTablePosition().getRow() != 0) {
                                        tableRepository.save(neo4jNode);
                                    }

                                    //System.out.println("EventHandler handle: "+neo4jNode.toString());
                                }
                            }
                    );

                }
                else {
                    // readonly field
                    newColumn.setCellValueFactory(new PropertyValueFactory<Neo4jNode, String>(name));

                    Callback<TableColumn, TableCell> cellFactory =
                            new Callback<TableColumn, TableCell>() {
                                public TableCell call(TableColumn p) {
                                    return new NormalCell<String>();

                                }
                            };

                    newColumn.setCellFactory(cellFactory);
                }

                tableColumns.add(new Neo4jTableColumn(newColumn, annotation.columnOrder()));
            }
        }
        return tableColumns;
    }

    /** creates all TableColumns from the annotated set methods
     *
     * @return ArrayList with all Neo4jTableColumns
     */
    private ArrayList<Neo4jTableColumn> createDataColumnsFromAnnotatedMethods() {

        ArrayList<Neo4jTableColumn> tableColumns = new ArrayList<Neo4jTableColumn>();

        // find annotated methods
        Method[] methods = tableClass.getDeclaredMethods();
        for (final Method method : methods) {

            method.setAccessible(true);

            Neo4jTableBuilderColumnSetMethod annotation = method.getAnnotation(Neo4jTableBuilderColumnSetMethod.class);

            if (annotation != null & method.getName().startsWith("set")) {
                final TableColumn newColumn;
                final String name = method.getName().substring(3).toLowerCase();
                final String columnName = (!annotation.columnName().equals("")) ? annotation.columnName() : method.getName().substring(3);
                newColumn = new TableColumn(columnName);
                //listen to sorting type (ASC/DESC) change
                newColumn.sortTypeProperty().addListener(new SortTypeChangeListener());
                newColumn.setMinWidth(columnName.length()*14);

                createAndSetFactories(newColumn, name, method.getParameterTypes()[0]);

                newColumn.setOnEditCommit(
                    new EventHandler<TableColumn.CellEditEvent<Neo4jNode, String>>() {
                        @Override
                        public void handle(TableColumn.CellEditEvent<Neo4jNode, String> t) {
                            Neo4jNode neo4jNode = t.getTableView().getItems().get(t.getTablePosition().getRow());

                            try {
                                method.invoke(neo4jNode, t.getNewValue());
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }

                            if (t.getTablePosition().getRow() != 0) {
                                tableRepository.save(neo4jNode);
                            }

                            //System.out.println("EventHandler handle: "+neo4jNode.toString());
                        }
                    }
                );

                tableColumns.add(new Neo4jTableColumn(newColumn, annotation.columnOrder()));
            }
        }

        return tableColumns;
    }


    private void createAndSetFactories(TableColumn newColumn, String name, Class parameterClass) {


        Callback<TableColumn, TableCell> cellFactory;
        if (parameterClass == Integer.class)
        {
            newColumn.setCellValueFactory(new PropertyValueFactory<Neo4jNode, Integer>(name));

            cellFactory =
                    new Callback<TableColumn, TableCell>() {
                        public TableCell call(TableColumn p) {
                            return new EditingCell<Integer>(new IntegerStringConverter());
                        }
                    };
        }
        else if (parameterClass == Date.class)
        {
            newColumn.setCellValueFactory(new PropertyValueFactory<Neo4jNode, Date>(name));


            final SimpleDateFormat sdfToDate = new SimpleDateFormat("dd.MM.yyyy");

            cellFactory =
                    new Callback<TableColumn, TableCell>() {
                        public TableCell call(TableColumn p) {
                            return new EditingCell<Date>(new DateTimeStringConverter(sdfToDate));
                        }
                    };
        }
        else {

            newColumn.setCellValueFactory(new PropertyValueFactory<Neo4jNode, String>(name));


            cellFactory =
                    new Callback<TableColumn, TableCell>() {
                        public TableCell call(TableColumn p) {
                           return new EditingCell<String>(new DefaultStringConverter());

                        }
                    };

        }

        newColumn.setCellFactory(cellFactory);

    }

    private void resetTableData () {

        data.remove(newNode);
        data.add(0, newNode);

    }

    /** removes all old neo4j and inserts the new neo4j from the result parameter
     *
     * @param result All items that shall be shown in the table.
     */
    public void setTableData(Iterable<T> result)
    {
        tableView.getItems().removeAll();

        data = FXCollections.observableArrayList();
        if (this.newNode == null) this.newNode = createNewNode();
        data.add(newNode);

        for (T item : result) {
            data.add(item);
        }

        tableView.setItems(data);

    }

    /** creates a new neo4j node that can be editted and afterwards stored
     *
     * @return new neo4j node
     */
    private T createNewNode () {
        try {
            return tableClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }


    /** This class is for TableCells with no editing functionality
     *
     * @param <T>
     */
    private class NormalCell<T> extends TableCell<Neo4jNode, T> {


        /** constructs the NormalCell
         *
         */
        public NormalCell() {
        }

        @Override
        public void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);

            setText(getString());

            // for the new column
            //if(getIndex()==(tableView.getItems().size()-1)){
            if (getIndex() == 0) {
                this.getStyleClass().add("table-row-cell-new");
            }
        }


        /** if the item isn't null the item is converted to String and returned
         *
         * @return The String value of the item.
         */
        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }
    }


    /** This class is for TableCells that can be editted.
     *
     * @param <T>
     */
    private class EditingCell<T> extends TableCell<Neo4jNode, T> {

        private TextField textField;

        private StringConverter<T> stringConverter;

        private T beforeValue;

        /** constructs the EditingCell with the stringConverter
         *
         * @param stringConverter The StringConverter for this cell.
         */
        public EditingCell(StringConverter<T> stringConverter) {
            this.stringConverter = stringConverter;
        }

        @Override
        public void startEdit() {
            if (!isEmpty()) {
                super.startEdit();
                turnToEditMode();
            }

        }

        /** changes the field to be editable
         *
         */
        private void turnToEditMode() {
            createTextField();
            setText(null);
            setGraphic(textField);
            textField.selectAll();
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();

            setText(getItem().toString());
            setGraphic(null);
        }

        @Override
        public void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getString());
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(getString());
                    setGraphic(null);
                }
            }
            // for the new column
            //if(getIndex()==(tableView.getItems().size()-1)){
            if (getIndex() == 0) {
                this.getStyleClass().add("table-row-cell-new");
                turnToEditMode();
                textField.setPrefWidth(getString().length());
            }
        }


        /** creates a text field with a minWidth that can react to changes by commiting them
         *
         */
        private void createTextField() {
            beforeValue = getItem();
            textField = new TextField(getString());
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap()* 2);
            textField.focusedProperty().addListener(new ChangeListener<Boolean>(){
                @Override
                public void changed(ObservableValue<? extends Boolean> arg0,
                                    Boolean arg1, Boolean arg2) {
                    if (!arg2) {
                        T newValue;
                        try {
                            newValue = stringConverter.fromString(textField.getText());
                        }
                        catch (RuntimeException e) {
                            newValue = beforeValue;
                        }

                        getTableView().edit(EditingCell.this.getIndex(), getTableColumn());
                        commitEdit(newValue);
                    }
                }
            });
        }

        /** if the item isn't null the item is converted to String and returned
         *
         * @return The String value of the item.
         */
        private String getString() {
            return getItem() == null ? "" : stringConverter.toString(getItem());
        }
    }

    /** This class is for TableCells that consist of a Save or Delete button
     *
     * @param <S>
     * @param <T> The type of the item contained within the Cell.
     */
    private class ButtonTableCell<S, T> extends TableCell<S, T> {
        private Button button;
        //private ObservableValue<T> observableValue;

        /** constructs the ButtonTableCell
         */
        public ButtonTableCell() {

        }

        @Override
        public void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (item.toString().equals("saveButton")) {
                    button = new Button("Save");
                    button.setStyle("-fx-base: green;");
                    button.setAlignment(Pos.CENTER);
                    button.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent t) {
                            int i = getIndex();

                            Neo4jNode neo4jNode = tableView.getItems().get(i);

                            //System.out.println("save neo4jnode: "+neo4jNode.toString());

                            tableRepository.save(neo4jNode);

                            ButtonTableCell.this.updateTableView(getTableView());

                            data.add(tableView.getItems().get(i));

                            // new node
                            newNode = createNewNode();
                            data.set(0, newNode);
                        }
                    });
                    this.getStyleClass().add("table-row-cell-new");
                }
                else {
                    button = new Button("X");
                    button.setStyle("-fx-base: red;");
                    button.setAlignment(Pos.CENTER);
                    button.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent t) {
                            int i = getIndex();

                            Neo4jNode neo4jNode = tableView.getItems().get(i);

                            FXOptionPane.Response response = FXOptionPane.showConfirmDialog(ScreensConfiguration.getPrimaryStage(), "Are you sure you want to delete this entry?", "Confirm Delete");

                            if (response == FXOptionPane.Response.YES) {
                                tableRepository.delete(neo4jNode);
                                data.remove(neo4jNode);
                            }
                        }
                    });
                    this.getStyleClass().remove("table-row-cell-new");
                }
                setAlignment(Pos.CENTER);
                setGraphic(button);
            }
        }
    }

}




