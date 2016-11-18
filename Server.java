import java.rmi.*;
import java.rmi.server.*;

public class Server extends UnicastRemoteObject
    implements ServerInterface
{
    public Server() throws RemoteException
    {
        super();
    }

    protected void getHTML( String location )
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
    } // end method getPage

    public static void main ( String args[] ) throws Exception
    {

    }
}
