package com.comsysto;

import com.comsysto.neo4j.domain.Neo4jCustomer;
import com.comsysto.neo4j.repos.Neo4jCustomerRepository;
import com.comsysto.util.Neo4jTableBuilder;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.net.URL;
import java.util.ResourceBundle;

/** The class is the main controller for the app connected with Main.fxml
 *
 */
public class MainController implements StageController, Initializable {

    private ScreensConfiguration screensConfig;

    private FXMLStage stage;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    Neo4jCustomerRepository neo4jCustomerRepository;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    @Qualifier("aboutDialog")
    private FXMLDialog aboutDialog;

    Neo4jTableBuilder neo4jCustomerTableBuilder;

    /** constructs the mainController with the given ScreensConfiguration
     *
     * @param screensConfig The ScreensConfiguration
     */
    public MainController(ScreensConfiguration screensConfig) {

        super();
        this.screensConfig = screensConfig;

    }

    @Override
    public void setStage(FXMLStage stage) {
        this.stage = stage;
        stage.setTitle("Spring Data Neo4j JavaFX TableView Demo");
    }

    @FXML //  fx:id="about_menuitem"
    private MenuItem about_menuitem; // Value injected by FXMLLoader

    @FXML //  fx:id="file_menu"
    private Menu file_menu; // Value injected by FXMLLoader

    @FXML //  fx:id="file_quit_menuitem"
    private MenuItem file_quit_menuitem; // Value injected by FXMLLoader

    @FXML //  fx:id="help_menu"
    private Menu help_menu; // Value injected by FXMLLoader

    @FXML //  fx:id="main_data_table_anchorpane"
    private AnchorPane main_data_table_anchorpane; // Value injected by FXMLLoader

    @FXML //  fx:id="main_data_toolbar"
    private ToolBar main_data_toolbar; // Value injected by FXMLLoader

    @FXML //  fx:id="main_data_toolbar_search_textfield"
    private TextField main_data_toolbar_search_textfield; // Value injected by FXMLLoader

    @FXML //  fx:id="main_data_vbox"
    private VBox main_data_vbox; // Value injected by FXMLLoader

    @FXML //  fx:id="main_menubar"
    private MenuBar main_menubar; // Value injected by FXMLLoader

    @FXML //  fx:id="main_vbox"
    private VBox main_vbox; // Value injected by FXMLLoader

    @FXML //  fx:id="menu_data_customers_tableview"
    private TableView<?> menu_data_customers_tableview; // Value injected by FXMLLoader

    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert about_menuitem != null : "fx:id=\"about_menuitem\" was not injected: check your FXML file 'Main.fxml'.";
        assert file_menu != null : "fx:id=\"file_menu\" was not injected: check your FXML file 'Main.fxml'.";
        assert file_quit_menuitem != null : "fx:id=\"file_quit_menuitem\" was not injected: check your FXML file 'Main.fxml'.";
        assert help_menu != null : "fx:id=\"help_menu\" was not injected: check your FXML file 'Main.fxml'.";
        assert main_data_table_anchorpane != null : "fx:id=\"main_data_table_anchorpane\" was not injected: check your FXML file 'Main.fxml'.";
        assert main_data_toolbar != null : "fx:id=\"main_data_toolbar\" was not injected: check your FXML file 'Main.fxml'.";
        assert main_data_toolbar_search_textfield != null : "fx:id=\"main_data_toolbar_search_textfield\" was not injected: check your FXML file 'Main.fxml'.";
        assert main_data_vbox != null : "fx:id=\"main_data_vbox\" was not injected: check your FXML file 'Main.fxml'.";
        assert main_menubar != null : "fx:id=\"main_menubar\" was not injected: check your FXML file 'Main.fxml'.";
        assert main_vbox != null : "fx:id=\"main_vbox\" was not injected: check your FXML file 'Main.fxml'.";
        assert menu_data_customers_tableview != null : "fx:id=\"menu_data_customers_tableview\" was not injected: check your FXML file 'Main.fxml'.";

        // initialize your logic here: all @FXML variables will have been injected

        initNeo4jDataTables();

    }


    /**  inits the tableViews and the neo4j inside
     *
     */
    private void initNeo4jDataTables()
    {
        neo4jCustomerTableBuilder = new Neo4jTableBuilder(Neo4jCustomer.class, neo4jCustomerRepository, menu_data_customers_tableview);
        neo4jCustomerTableBuilder.initDataTable();
        neo4jCustomerTableBuilder.setTableData(neo4jCustomerRepository.findAll());
    }

    /** cunducts the search with the search expression entered in the search field
     *
     */
    @FXML
    public void searchTextfieldKeyReleased() {
        System.out.println("searchTextfieldKeyReleased original");
        neo4jCustomerTableBuilder.setTableData(neo4jCustomerRepository.findByCustomerNameLike("*" + main_data_toolbar_search_textfield.getText() + "*"));

    }

    /** exits the programme
     */
    @FXML
    public void quit() {

        stage.close();
    }

    /** shows the about dialog
     */
    @FXML
    public void about() {

        //aboutDialog.centerOnScreen();
        aboutDialog.showAndWait();

    }
}
