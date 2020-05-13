package client;

import exceptions.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IntegrityResponseTolerateFaultTest extends BaseTest{

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
        
        setIntegrityFlag(true);
    }

    @AfterClass
    public static void turnFlagOff() {
        setIntegrityFlag(false);

    }

    @Test
    public void Should_Tolerate_Post() throws MessageTooBigException, InvalidAnnouncementException, NonceTimeoutException, FreshnessException, UserNotRegisteredException, IntegrityException, OperationTimeoutException {
        
        assertEquals(1, clientEndpoint1.post("user1 announc message1", null));

    }

    // @Test(expected = IntegrityException.class)
    // public void Should_throw_Integrity_Exception_Read() throws NonceTimeoutException, FreshnessException, UserNotRegisteredException, IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException {
    //     setIntegrityFlag(true);

    //     clientEndpoint1.read("user2", 1);

    // }

    // @Test(expected = IntegrityException.class)
    // public void Should_throw_Integrity_Exception_PostGeneral() throws MessageTooBigException, InvalidAnnouncementException, NonceTimeoutException, FreshnessException, UserNotRegisteredException, IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException {
    //     setIntegrityFlag(true);

    //     clientEndpoint1.post("a resposta vai ser alterada confiem", null);

    // }

    // @Test(expected = IntegrityException.class)
    // public void Should_throw_Integrity_Exception_ReadGeneral() throws IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException, NonceTimeoutException, UserNotRegisteredException, FreshnessException {
    //     setIntegrityFlag(true);

    //     clientEndpoint1.readGeneral(1);

    // }

    // @Test(expected = IntegrityException.class)
    // public void Should_throw_Integrity_Exception_Register() throws NonceTimeoutException, FreshnessException, IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException, UnknownPublicKeyException, AlreadyRegisteredException {
    //     setIntegrityFlag(true);

    //     clientEndpoint3.register();

    // }


}
