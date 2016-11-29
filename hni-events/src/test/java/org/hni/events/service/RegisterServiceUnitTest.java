package org.hni.events.service;

import org.hni.events.service.dao.RegistrationStateDAO;
import org.hni.events.service.om.Event;
import org.hni.events.service.om.EventName;
import org.hni.events.service.om.RegistrationState;
import org.hni.events.service.om.RegistrationStep;
import org.hni.security.service.ActivationCodeService;
import org.hni.user.om.User;
import org.hni.user.service.UserService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class RegisterServiceUnitTest {

    private static final String PHONE_NUMBER = "8188461238";
    private static final String AUTH_CODE = "123456";

    @InjectMocks
    private RegisterService registerService;

    @Mock
    private RegistrationStateDAO registrationStateDAO;

    @Mock
    private UserService customerService;

    @Mock
    private ActivationCodeService activationCodeService;

    private Event event;
    private RegistrationState state = null;
    private String payload = null;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        event = Event.createEvent("text/plain", PHONE_NUMBER, "message");
        when(customerService.validate(any(User.class))).thenReturn(true);
        when(activationCodeService.validate(eq(AUTH_CODE))).thenReturn(true);
    }

    @Test
    public void testStartRegister() throws Exception {

        state = new RegistrationState(EventName.REGISTER, PHONE_NUMBER, payload, RegistrationStep.STATE_REGISTER_START);
        when(registrationStateDAO.get(eq(PHONE_NUMBER))).thenReturn(state);

        String returnString = registerService.handleEvent(event);
        Assert.assertEquals("Welcome to Hunger Not Impossible! Msg & data rates may apply. "
                + "Any information you provide here will be kept private. "
                + "Reply with PRIVACY to learn more. Let's get you registered. What's your first name?", returnString);
        verify(registrationStateDAO, times(1)).update(any(RegistrationState.class));
    }

    @Test
    public void testGetFirstName() throws Exception {
        state = new RegistrationState(EventName.REGISTER, PHONE_NUMBER, payload, RegistrationStep.STATE_REGISTER_GET_FIRST_NAME);
        event.setTextMessage("firstname");

        when(registrationStateDAO.get(eq(PHONE_NUMBER))).thenReturn(state);
        String returnString = registerService.handleEvent(event);
        Assert.assertEquals("Thanks " + "firstname" + ". What's your last name?", returnString);
        verify(registrationStateDAO, times(1)).update(any(RegistrationState.class));
    }

    @Test
    public void testGetLastName() throws Exception {
        state = new RegistrationState(EventName.REGISTER, PHONE_NUMBER, payload, RegistrationStep.STATE_REGISTER_GET_LAST_NAME);
        event.setTextMessage("lastname");

        when(registrationStateDAO.get(eq(PHONE_NUMBER))).thenReturn(state);
        String returnString = registerService.handleEvent(event);
        Assert.assertEquals("Perfect! Lastly, I'd like to get your email address "
                + "to verify your account in case you text me from a new "
                + "number. So what's your email address? Type 'none' if you don't have an email. Thanks", returnString);
        verify(registrationStateDAO, times(1)).update(any(RegistrationState.class));
    }

    @Test
    public void testGetEmail() throws Exception {
        state = new RegistrationState(EventName.REGISTER, PHONE_NUMBER, payload, RegistrationStep.STATE_REGISTER_GET_EMAIL);
        event.setTextMessage("johndoe@gmail.com");

        when(registrationStateDAO.get(eq(PHONE_NUMBER))).thenReturn(state);
        String returnString = registerService.handleEvent(event);
        Assert.assertEquals("Okay! I have " + "johndoe@gmail.com" + " as your email address. "
                + "Is that correct? Reply 1 for yes and 2 for no", returnString);
        verify(registrationStateDAO, times(1)).update(any(RegistrationState.class));
    }

    @Test
    public void testGetNoneEmail() throws Exception {
        state = new RegistrationState(EventName.REGISTER, PHONE_NUMBER, payload, RegistrationStep.STATE_REGISTER_GET_EMAIL);
        event.setTextMessage("none");

        when(registrationStateDAO.get(eq(PHONE_NUMBER))).thenReturn(state);
        String returnString = registerService.handleEvent(event);
        Assert.assertEquals("Okay! You don't have an email address. "
                + "Is that correct? Reply 1 for yes and 2 for no", returnString);
        verify(registrationStateDAO, times(1)).update(any(RegistrationState.class));
    }

    @Test
    public void testConfirmEmail() throws Exception {
        state = new RegistrationState(EventName.REGISTER, PHONE_NUMBER, payload, RegistrationStep.STATE_REGISTER_CONFIRM_EMAIL);
        event.setTextMessage("1");

        when(registrationStateDAO.get(eq(PHONE_NUMBER))).thenReturn(state);
        String returnString = registerService.handleEvent(event);
        Assert.assertEquals("Please enter the 6 digit authorization code provided to you for this program.", returnString);
        verify(registrationStateDAO, times(1)).update(any(RegistrationState.class));
    }

    @Test
    public void testGetAuthCode() throws Exception {
        state = new RegistrationState(EventName.REGISTER, PHONE_NUMBER, payload, RegistrationStep.STATE_REGISTER_GET_AUTH_CODE);
        event.setTextMessage(AUTH_CODE);

        when(registrationStateDAO.get(eq(PHONE_NUMBER))).thenReturn(state);
        String returnString = registerService.handleEvent(event);
        Assert.assertEquals("Ok. You're all setup for yourself. If you have additional family"
                + " members to register please enter the additional authorization"
                + " codes now. When you need a meal just text MEAL back to this number.", returnString);
        verify(registrationStateDAO, times(1)).update(any(RegistrationState.class));
    }

    @Test
    public void testAddMoreAuthCodes() throws Exception {
        state = new RegistrationState(EventName.REGISTER, PHONE_NUMBER, payload, RegistrationStep.STATE_REGISTER_MORE_AUTH_CODES);
        event.setTextMessage(AUTH_CODE);

        when(registrationStateDAO.get(eq(PHONE_NUMBER))).thenReturn(state);
        String returnString = registerService.handleEvent(event);
        Assert.assertEquals("We have added that authorization code to your family account. Please"
                + " send any additional codes you need for your family.", returnString);
        verify(registrationStateDAO, never()).update(any(RegistrationState.class));
    }
}
