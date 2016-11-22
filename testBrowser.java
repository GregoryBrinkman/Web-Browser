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
    private WebView browser; //displays websites
    private TextField urlField; //where urls are entered
    URL url;
    String htmlText;
    private Button forwardButton;
    private Button backButton;
    public static ServerInterface service;

    public void start(Stage stage) {
        stage.setTitle("Browser"); //window name

        browser = new WebView();
        engine = browser.getEngine();
        urlField = new TextField();
        backButton = new Button("back");
        forwardButton = new Button("forward");

        //define url input handling
        urlField.setOnAction(new EventHandler<ActionEvent>() {
                public void handle(ActionEvent event) {
                    //engine does what we want. maybe kill the rmi stuff?
                    // engine.load(urlField.getText());
                    getPage(urlField.getText());
                }
            });

        engine.getLoadWorker()
            .exceptionProperty()
            .addListener(new ChangeListener<Throwable>() {
                    @Override
                    public void changed(ObservableValue<? extends Throwable> observableValue,
                                        Throwable oldException, Throwable exception) {
                        System.out.println("Exception loading a page: " + exception);
                    }
                });




        //display setup

        //set Toolbar
        HBox toolbar = new HBox();
        toolbar.getChildren().setAll(
                                     urlField
                                     );

        //set Window
        VBox root = new VBox();
        root.getChildren().setAll(
                                  toolbar,
                                  browser
                                  );
        stage.setScene(new Scene(root));
        stage.show();
    }

    // load document
    protected void getPage( String location )
    {
        try // load document and display location
            {
                System.out.printf("%s\n", location);
                url = new URL(location);
                System.out.printf("%s\n", url);

                System.out.println("hello");
                htmlText = service.getHTML(url);
                System.out.println("goodbye");
                System.out.println(htmlText);
                engine.loadContent(htmlText);
                // contentsArea.setText(htmlText); // set the page
                // enterField.setText( location ); // set the text
            } // end try
        catch ( IOException ioException )
            {
                // JOptionPane.showMessageDialog( this,
                //                                "Error retrieving specified URL", "Bad URL",
                //                                JOptionPane.ERROR_MESSAGE );
            } // end catch
    } // end method getPage

    public static void main(String[] args) { launch(args); }
}
