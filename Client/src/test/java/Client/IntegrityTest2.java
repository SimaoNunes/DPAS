package Client;

import Exceptions.*;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

public class IntegrityTest2 extends BaseTest{

    /**
     * Integrity test of the response from the server to the user's request
     */

    @BeforeClass
    public static void populate() throws AlreadyRegisteredException, UnknownPublicKeyException, NonceTimeoutException, OperationTimeoutException,
                                                 FreshnessException, IntegrityException, MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException {
        clientEndpoint1.register();
        clientEndpoint2.register();
        clientEndpoint1.post("message from user1", null);
        clientEndpoint2.post("message from user2", null);
        clientEndpoint2.postGeneral("message general just to populate", null);
    }

    @After
    public void turnFlagOff() {
        setIntegrityFlag(false);

    }

    @Test(expected = IntegrityException.class)
    public void Should_throw_Integrity_Exception_Post() throws MessageTooBigException, InvalidAnnouncementException, NonceTimeoutException, FreshnessException, UserNotRegisteredException, IntegrityException, OperationTimeoutException {
        setIntegrityFlag(true);

        clientEndpoint1.post("a resposta a esta mensagem vai ser alterada", null);

    }

    @Test(expected = IntegrityException.class)
    public void Should_throw_Integrity_Exception_Read() throws NonceTimeoutException, FreshnessException, UserNotRegisteredException, IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException {
        setIntegrityFlag(true);

        clientEndpoint1.read("user2", 1);

    }

    @Test(expected = IntegrityException.class)
    public void Should_throw_Integrity_Exception_PostGeneral() throws MessageTooBigException, InvalidAnnouncementException, NonceTimeoutException, FreshnessException, UserNotRegisteredException, IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException {
        setIntegrityFlag(true);

        clientEndpoint1.postGeneral("a resposta vai ser alterada confiem", null);

    }

    @Test(expected = IntegrityException.class)
    public void Should_throw_Integrity_Exception_ReadGeneral() throws IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException {
        setIntegrityFlag(true);

        clientEndpoint1.readGeneral(1);

    }

    @Test(expected = IntegrityException.class)
    public void Should_throw_Integrity_Exception_Register() throws NonceTimeoutException, FreshnessException, IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException, UnknownPublicKeyException, AlreadyRegisteredException {
        setIntegrityFlag(true);

        clientEndpoint1.register();

    }


}
