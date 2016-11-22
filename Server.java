import java.rmi.*;
import java.net.*;
import java.io.*;
import java.rmi.server.*;

public class Server extends UnicastRemoteObject
    implements ServerInterface
{
    InputStream html;
    HttpURLConnection connection;
    String htmlString;

    public Server() throws RemoteException
    {
        super();
    }
    // convert InputStream to String
    private static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

	}
    public String getHTML( URL location ) throws RemoteException
    {
        try // load document and display location
            {
                connection = (HttpURLConnection)location.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();
                html = connection.getInputStream();
                htmlString = getStringFromInputStream(html);
                System.out.printf("\n%s\n", html);
                return htmlString;
            } // end try
        catch ( IOException ioException )
            {
                return htmlString;
            } // end catch
    } // end method getPage

    public static void main ( String args[] ) throws Exception
    {

        // Create an instance of our power service server ...
        Server svr = new Server();

        // ... and bind it with the RMI Registry
        Naming.bind ("Server", svr);

        System.out.println ("Service bound....");
    }
}
