import java.rmi.*;
import java.rmi.server.*;

public class Server extends UnicastRemoteObject
    implements ServerInterface
{
    public Server() throws RemoteException
    {
        super();
    }

    protected void getPage( String location )
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

        // Create an instance of our power service server ...
        MatrixServiceServer svr = new MatrixServiceServer();

        // ... and bind it with the RMI Registry
        Naming.bind ("MatrixService", svr);

        System.out.println ("Service bound....");
    }
}
