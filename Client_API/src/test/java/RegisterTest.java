import Exceptions.AlreadyRegisteredException;
import Library.Request;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

////////////////////////////////////////////////////////////////////
//																  //
//   WARNING: Server must be running in order to run these tests  //
//                                                                //
////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////
//                                                                //
//     Only 1 user is considered for tests purposes (user1)       //
//                                                                //
////////////////////////////////////////////////////////////////////

public class RegisterTest extends BaseTest {

    @BeforeClass
    public static void setup() throws IOException {
        Socket socket = new Socket("localhost", 9000);
        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
        outputStream.writeObject(new Request("DELETEALL", null));
        outputStream.close();
        socket.close();
    }

    @Test
    public void RegisterSuccess() throws AlreadyRegisteredException {
        clientAPI.register(publicKey1, "nome a toa");
    }
}
