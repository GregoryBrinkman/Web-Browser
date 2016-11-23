import java.rmi.*;
import java.net.*;
import java.io.*;
import java.rmi.server.*;

public class Server extends UnicastRemoteObject
    implements ServerInterface
{
    InputStream       html;
    HttpURLConnection connection;
    String            htmlString;

    public Server() throws RemoteException
    {
        super();
    }
    // convert InputStream to String
    private static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder  sb = new StringBuilder();
        String         line;

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
                html       = connection.getInputStream();
                htmlString = getStringFromInputStream(html);
                return htmlString;
            } // end try
        catch ( Exception e)
            {
                return "Page request error";
            }
    } // end method getPage

    public static void main ( String args[] ) throws Exception
    {

        try {
            // Create an instance of our power service server ...
            Server svr = new Server();

            // ... and bind it with the RMI Registry
            Naming.bind ("Server", svr);
            System.out.println ("Service bound....");

        } catch (Exception e) {
            System.out.println("Failed to register object " + e);
            e.printStackTrace();
            System.exit(1);
        }
    }
}
