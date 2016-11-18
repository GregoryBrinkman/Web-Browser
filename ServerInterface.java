import java.rmi.*;

public interface ServerInterface extends java.rmi.Remote
{
  protected void getPage( String location ) throws RemotedException;
}
