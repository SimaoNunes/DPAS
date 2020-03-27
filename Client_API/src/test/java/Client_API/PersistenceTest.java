package Client_API;

import Exceptions.AlreadyRegisteredException;
import Exceptions.InvalidPublicKeyException;
import Exceptions.UnknownPublicKeyException;
import Library.Request;
import org.junit.Test;

import java.util.Scanner;

public class PersistenceTest extends BaseTest{

    public void askForReboot() {

        System.out.println("Press enter after server reboot!");

        Scanner scanner = new Scanner(System.in);
        String a = scanner.nextLine();
    }


    @Test(expected = AlreadyRegisteredException.class)
    public void PersistenceRegisterTest() throws AlreadyRegisteredException, UnknownPublicKeyException, InvalidPublicKeyException {

        clientAPI.register(publicKey1, "Simao");

        askForReboot();

        System.out.println("aiai");

        clientAPI.register(publicKey1, "Miguel");


    }



}
