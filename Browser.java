import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.*;
import javax.swing.event.*;

public class Browser extends JFrame
{
    private JTextField  enterField; // JTextField to enter site name
    private JEditorPane contentsArea; // to display website
    private JButton     backButton;
    private JButton     forwardButton;
    private JButton     historyButton;
    private JButton     favoriteButton;
    private JPanel      toolbarPanel;
    public ArrayDeque   history;

    // set up GUI
    public Browser()
    {
        super( "Simple Web Browser" );

        toolbarPanel    = new JPanel((LayoutManager) new FlowLayout(FlowLayout.LEFT));
        backButton      = new JButton("Back");
        forwardButton   = new JButton("Forward");
        historyButton   = new JButton("History");
        favoriteButton = new JButton("Favorites");
        enterField      = new JTextField( "Enter file URL here" );
        contentsArea    = new JEditorPane(); // create contentsArea
        history         = new ArrayDeque();

        // create enterField and register its listener
        enterField.addActionListener(new ActionListener()
            {
                // get document specified by user
                public void actionPerformed( ActionEvent event )
                {
                    getThePage( event.getActionCommand() );
                    history.add (event.getActionCommand());
                } // end actionPerformed

            }); // end addActionListener


        contentsArea.setEditable( false );
        contentsArea.addHyperlinkListener(new HyperlinkListener()
            {
                // if user clicked hyperlink, go to specified page
                public void hyperlinkUpdate( HyperlinkEvent event )
                {
                    if ( event.getEventType() == HyperlinkEvent.EventType.ACTIVATED ) {
                        getThePage( event.getURL().toString() );
                    }
                } // end hyperlinkUpdate
            }); // end addHyperlinkListener

        add(toolbarPanel, BorderLayout.PAGE_START);
        toolbarPanel.add(backButton, BorderLayout.LINE_START);
        toolbarPanel.add(forwardButton, BorderLayout.LINE_START);
        toolbarPanel.add( enterField, BorderLayout.CENTER);
        toolbarPanel.add(historyButton, BorderLayout.LINE_START);
        toolbarPanel.add(favoriteButton, BorderLayout.LINE_START);
        add( new JScrollPane( contentsArea ), BorderLayout.CENTER );

        setSize( 400, 300 ); // set size of window
        setVisible( true ); // show window
    } // end Browser constructor

    // load document
    private void getThePage( String location )
    {
        try // load document and display location
            {
                contentsArea.setPage( location ); // set the page
                enterField.setText( location ); // set the text
            } // end try
        catch ( IOException ioException )
            {
                JOptionPane.showMessageDialog( this,
                                               "Error retrieving specified URL", "Bad URL",
                                               JOptionPane.ERROR_MESSAGE );
            } // end catch
    } // end method getThePage
    public static void main( String[] args )
    {
        Browser application = new Browser();
        application.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    } // end main
} // end class Browser
