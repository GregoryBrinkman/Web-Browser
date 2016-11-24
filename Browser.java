import java.rmi.*;
import java.net.*;
import java.util.*;
import java.io.*;
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
import javafx.concurrent.Worker.State;
import javafx.concurrent.Worker;
import org.w3c.dom.*;
import org.w3c.dom.events.DocumentEvent;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

public class Browser extends Application
{
    public  static final String    EVENT_TYPE_CLICK = "click";
    public  static ServerInterface service;
    private static String          lastLocation;

    private WebEngine engine;
    private WebView   browser;
    private TextField urlField;
    public  URL       url;
    public  String    htmlText;
    private String    errorPage;
    private Button    forwardButton;
    private Button    backButton;
    private Button    favoritesButton;
    private Button    historyButton;
    private StringBuffer fileData;
    private BufferedReader reader;

    public void start(Stage stage) {
        stage.setTitle("Browser"); //window name
        browser         = new WebView();
        engine          = browser.getEngine();
        urlField        = new TextField("https://news.ycombinator.com");
        backButton      = new Button("Back");
        forwardButton   = new Button("Forward");
        favoritesButton = new Button("Favorites");
        historyButton   = new Button("History");
        errorPage       = "";
        fileData        = new StringBuffer();

        // load error page html into string
        try
            {
                reader      = new BufferedReader(new FileReader("error.html"));
                char[] buf  = new char[1024];
                int numRead = 0;
                while((numRead=reader.read(buf)) != -1){
                    errorPage = String.valueOf(buf, 0, numRead);
                    fileData.append(errorPage);
                }
                reader.close();
                errorPage = fileData.toString();
            }
        catch (Exception e)
            {
                e.printStackTrace();
            }

        //define url input handling
        urlField.setOnAction(new EventHandler<ActionEvent>() {
                public void handle(ActionEvent event) {
                    getPage(urlField.getText());
                }
            });

        engine.getLoadWorker()
            .exceptionProperty()
            .addListener(new ChangeListener<Throwable>() {
                    public void changed(ObservableValue<? extends Throwable> observableValue,
                                        Throwable oldException,
                                        Throwable exception) {
                        System.out.println("Exception loading a page: " + exception);
                    }
                });



        engine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
                public void changed(ObservableValue ov, State oldState, State newState) {
                    if (newState == Worker.State.SUCCEEDED) {
                        EventListener listener = new EventListener() {
                                public void handleEvent(Event ev) {
                                    String domEventType = ev.getType();
                                    //System.err.println("EventType: " + domEventType);

                                    if (domEventType.equals(EVENT_TYPE_CLICK)) {
                                        String href = ((Element)ev.getTarget()).getAttribute("href");
                                        if (href == null) { return; }
                                        else {
                                            try {
                                                getPage(href);
                                            }
                                            catch (Exception e) {
                                                System.out.println("Hyperlink error: " + e);
                                            }
                                        } // end else
                                    } // end if
                                } // end handleEvent()
                            }; // end EventListener listener

                        Document doc = engine.getDocument();
                        NodeList nodeList = doc.getElementsByTagName("a");
                        for (int i = 0; i < nodeList.getLength(); i++) {
                            ((EventTarget) nodeList.item(i)).addEventListener(EVENT_TYPE_CLICK, listener, false);
                        }
                    } // end if
                } // end changed()
            }); // end addListener()

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
    } // end start()

    // load document
    protected void getPage( String location )
    {
        try // load document and display location
            {
                if (location.charAt(0) == '/' && lastLocation != null) {
                    // catch relative links
                    location = lastLocation + location;
                }
                url = new URL(location);
                lastLocation = location; // store the last url for relative links

                System.out.println("getting url...");
                htmlText = service.getHTML(url);
                System.out.println("got url...");

                if (htmlText.equals("Page request error")) {
                    System.err.println("Error: Can't get page");
                    engine.loadContent(errorPage);
                }
                else {
                    System.out.println("loading content...");
                    engine.loadContent(htmlText);
                    System.out.println("content loaded...");
                }

                System.out.println();
                urlField.setText(location);

                // addToHistory(location);

            } // end try

        catch ( MalformedURLException URLException )
            {
                System.err.println("Error: Malformed URL");
                URLException.printStackTrace();
                engine.loadContent(errorPage);
            }
        catch ( Exception e)
            {
                System.err.println("Error: Can't get page");
                e.printStackTrace();
                engine.loadContent(errorPage);
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
