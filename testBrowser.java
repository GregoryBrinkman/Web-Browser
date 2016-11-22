import java.rmi.*;
import javax.swing.text.html.HTMLDocument;
import java.net.*;
import java.util.*;
// import java.awt.*;
// import java.awt.event.*;
import java.io.IOException;
import javafx.application.Application;
import javafx.beans.value.*;
import javafx.event.*;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.web.*;
import javafx.stage.Stage;

public class testBrowser extends Application
{
    private WebEngine engine;
    private WebView   browser; //displays websites
    private TextField urlField; //where urls are entered
    public  URL       url;
    public  String    htmlText;
    private Button    forwardButton;
    private Button    backButton;
    private Button    favoritesButton;
    private Button    historyButton;
    public static ServerInterface service;

    public void start(Stage stage) {
        stage.setTitle("Browser"); //window name
        browser         = new WebView();
        engine          = browser.getEngine();
        urlField        = new TextField();
        backButton      = new Button("Back");
        forwardButton   = new Button("Forward");
        favoritesButton = new Button("Favorites");
        historyButton   = new Button("History");

        //define url input handling
        urlField.setOnAction(new EventHandler<ActionEvent>() {
                public void handle(ActionEvent event) {
                    getPage(urlField.getText());
                }
            });

        engine.getLoadWorker()
            .exceptionProperty()
            .addListener(new ChangeListener<Throwable>() {
                    @Override
                    public void changed(ObservableValue<? extends Throwable> observableValue,
                                        Throwable oldException,
                                        Throwable exception) {
                        System.out.println("Exception loading a page: " + exception);
                    }
                });

        //set Toolbar
        HBox toolbar = new HBox();
        toolbar.getChildren().setAll(backButton,
                                     forwardButton,
                                     urlField,
                                     favoritesButton,
                                     historyButton);

        //set Window
        VBox root = new VBox();
        root.getChildren().setAll(toolbar, browser);
        stage.setScene(new Scene(root));
        stage.show();
    }

    // load document
    protected void getPage( String location )
    {
        try // load document and display location
            {
                url      = new URL(location);
                htmlText = service.getHTML(url);
                engine.loadContent(htmlText);
            } // end try
        catch ( IOException ioException )
            {
                // engine.load("file:///error.html");
            } // end catch
    } // end method getPage

    public static void main(String[] args) {

        try{
            // service = (Server) Naming.lookup
            //     ("rmi://" + args[0] + "/Server");
            service = (ServerInterface) Naming.lookup ("rmi://localhost/Server");
        }
        catch(Exception e){
            System.out.println("Failed setting up registry lookup");
            e.printStackTrace();
            System.exit(1);
        }
        launch(args);
    }
}
