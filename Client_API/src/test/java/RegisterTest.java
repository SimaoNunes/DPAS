import Exceptions.AlreadyRegisteredException;
import Library.Request;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

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
        clientAPI.register(publicKey, "nome a toa");
    }
}
