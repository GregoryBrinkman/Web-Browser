import java.rmi.*;

public interface ServerInterface extends java.rmi.Remote
{
  protected void getHTML( String location ) throws RemotedException;
}
