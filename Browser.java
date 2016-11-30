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

    private WebEngine        engine;
    private WebView          browser;
    private TextField        urlField;
    public  URL              url;
    public  String           htmlText;
    private String           errorPage;
    private Button           forwardButton;
    private Button           backButton;
    private Button           favoritesButton;
    private Button           historyButton;
    private StringBuffer     fileData;
    private BufferedReader   reader;
    private Stack            backStack;
    private Stack            forwardStack;
    private FileOutputStream logFileOut;
    private PrintWriter      logWriter;
    private BufferedReader   logReader;

    // logWriter.println("There is a tie here") ;
    // logWriter.close();

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
        backStack       = new Stack();
        forwardStack    = new Stack();

        // load history
        // load error page html into string
        try
            {
                logFileOut      = new FileOutputStream(".history", true);
                logWriter       = new PrintWriter(logFileOut, true);
                // logFileIn       = new FileInputStream(".history", true);
                // logReader       = new PrintWriter(logFile);
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
                    showPush(backStack, urlField.getText());
                    getPage(urlField.getText());
                    forwardStack = new Stack();
                }
            });

        backButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent e) {
                    if(!backStack.empty()){
                        String lastPage = showPop(backStack);
                        System.out.printf("Popping backstack: %s\n", lastPage);
                        System.out.print("Pushing forwardstack");
                        showPush(forwardStack, urlField.getText());
                        urlField.setText(lastPage);
                        getPage(lastPage);
                    }
                    else{
                        System.out.println("ELSE. nevermind about backstack");
                    }
                }
            });

        forwardButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent e) {
                    String page;
                    System.out.println("Popping forwardstack");
                    if(!forwardStack.empty()){
                        page = showPop(forwardStack);
                        showPush(backStack, urlField.getText());
                        urlField.setText(page);
                        getPage(page);
                    }
                    else{System.out.println("newvermind forwardstack is empty");}
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
                                                showPush(backStack, urlField.getText());
                                                getPage(href);
                                                forwardStack = new Stack();
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
        showPush(backStack, urlField.getText());
        getPage(urlField.getText());
        forwardStack = new Stack();
    } // end start()

    // load document
    protected void getPage( String location )
    {
        try // load document and display location
            {
                System.out.println("Location: "+location);
                if (!location.substring(0, 4).equals("http") && lastLocation != null) {
                    // catch relative links
                    if (location.charAt(0) != '/') {
                        /*
                        If the relative link doesn't start with a /, we need to find the root of the site.
                        This requires some string manipulation of the lastLocation to find the root.
                        */

                        int i = lastLocation.length() - 2;
                        // start one character from the end (before the potential trailing /)

                        while (lastLocation.charAt(i--) != '/' && i > 0) {}
                        // find the last instance of /

                        if (i > 6) {
                            // if the result isn't just "http:// or similar, alter lastLocation appropriately
                            lastLocation = lastLocation.substring(0, i+1);
                        }
                    }
                    location = lastLocation + "/" + location;
                    // back up and running
                }
                url = new URL(location);

                System.out.println("getting url " + location);
                htmlText = service.getHTML(url);
                System.out.println("got url...");

                if (htmlText.equals("Page request error")) {
                    System.err.println("Error: Can't get page - " + location);
                    engine.loadContent(errorPage);
                }
                else {
                    System.out.println("loading content...");
                    engine.loadContent(htmlText);
                    System.out.println("content loaded...");
                }

                System.out.println();
                urlField.setText(location);
                lastLocation = location; // store the last url for relative links

                // TODO addToHistory(location);
                logWriter.println(location);

            } // end try

        catch ( MalformedURLException URLException )
            {
                System.err.println("Error: Malformed URL - " + location);
                URLException.printStackTrace();
                engine.loadContent(errorPage);
            }
        catch ( Exception e)
            {
                System.err.println("Error: Can't get page - " + location);
                e.printStackTrace();
                engine.loadContent(errorPage);
            } // end catch

    } // end method getPage

    static String showPush(Stack st, String a) {
        st.push(new String(a));
        System.out.println("push(" + a + ")");
        System.out.println("stack: " + st);
        return a;
    }

    static String showPop(Stack st) {
        System.out.print("pop -> ");
        String a = (String) st.pop();
        System.out.println(a);
        System.out.println("stack: " + st);
        return a;
   }
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
