import java.rmi.*;
import java.net.*;
import java.io.*;
import java.rmi.server.*;

public interface ServerInterface extends java.rmi.Remote
{

    String getHTML( URL location ) throws RemoteException;
}
