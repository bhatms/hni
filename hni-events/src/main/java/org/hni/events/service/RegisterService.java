package org.hni.events.service;

import org.hni.events.service.dao.RegistrationStateDAO;
import org.hni.events.service.om.Event;
import org.hni.events.service.om.RegistrationStep;
import org.hni.events.service.om.RegistrationState;
import org.hni.security.service.ActivationCodeService;
import org.hni.user.om.User;
import org.hni.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

@Component
public class RegisterService extends AbstractRegistrationService<User> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterService.class);

    @Inject
    private UserService customerService;

    @Inject
    private ActivationCodeService activationCodeService;

    @Inject
    private RegistrationStateDAO registrationStateDAO;

    @PostConstruct
    void init() {
        setRegistrationStateDAO(registrationStateDAO);
    }

    @Override
    protected WorkFlowStepResult performWorkFlowStep(final Event event, final RegistrationState registrationState) {
        final String returnString;
        RegistrationStep nextStateCode = registrationState.getRegistrationStep();
        final User user =
                registrationState.getPayload() != null ? deserialize(registrationState.getPayload(), User.class) : new User();
        final String textMessage = event.getTextMessage();
        switch (RegistrationStep.fromStateCode(registrationState.getRegistrationStep().getStateCode())) {
            case STATE_REGISTER_START:
                user.setMobilePhone(event.getPhoneNumber());
                nextStateCode = RegistrationStep.STATE_REGISTER_GET_FIRST_NAME;
                returnString = "Welcome to Hunger Not Impossible! Msg & data rates may apply. "
                        + "Any information you provide here will be kept private. "
                        + "Reply with PRIVACY to learn more. Let's get you registered. What's your first name?";
                break;
            case STATE_REGISTER_GET_FIRST_NAME:
                user.setFirstName(textMessage);
                // validate the first name
                if (customerService.validate(user)) {
                    nextStateCode = RegistrationStep.STATE_REGISTER_GET_LAST_NAME;
                    returnString = "Thanks " + textMessage + ". What's your last name?";
                } else {
                    returnString = "We didn't get that. Please send your first name again.";
                }
                break;
            case STATE_REGISTER_GET_LAST_NAME:
                user.setLastName(textMessage);
                // validate the last name
                if (customerService.validate(user)) {
                    nextStateCode = RegistrationStep.STATE_REGISTER_GET_EMAIL;
                    returnString = "Perfect! Lastly, I'd like to get your email address "
                            + "to verify your account in case you text me from a new "
                            + "number. So what's your email address? Type 'none' if you "
                            + "don't have an email. Thanks";
                } else {
                    returnString = "We didn't get that. Please send your last name again.";
                }
                break;
            case STATE_REGISTER_GET_EMAIL:
                user.setEmail(textMessage);
                // validate the email
                if (customerService.validate(user)) {
                    nextStateCode = RegistrationStep.STATE_REGISTER_CONFIRM_EMAIL;
                    if ("none".equalsIgnoreCase(textMessage)) {
                        returnString = "Okay! You don't have an email address. "
                                + "Is that correct? Reply 1 for yes and 2 for no";
                    } else {
                        returnString = "Okay! I have " + textMessage + " as your email address. "
                                + "Is that correct? Reply 1 for yes and 2 for no";
                    }
                } else {
                    returnString = "I'm sorry that is not a valid email. Please send a valid email addresss.";
                }
                break;
            case STATE_REGISTER_CONFIRM_EMAIL:
                switch (textMessage){
            		case "2":
            			user.setEmail(null);
                        nextStateCode = RegistrationStep.STATE_REGISTER_GET_EMAIL;
                        returnString = "So what's your email address?";
                        break;
            		case "1":
            			nextStateCode = RegistrationStep.STATE_REGISTER_GET_AUTH_CODE;
                        returnString = "Please enter the 6 digit authorization code provided to you for this program.";
                        break;
            		default:
            			nextStateCode= RegistrationStep.STATE_REGISTER_CONFIRM_EMAIL;
            			returnString="Invalid Response - Reply 1 for yes and 2 for no to confirm your email address";
            			break;
            	}          		
                
                break;
            case STATE_REGISTER_GET_AUTH_CODE:
                if (activationCodeService.validate(textMessage)) {
                    customerService.save(user);
                    //we are sure that text message is a long at this point
                    customerService.registerCustomer(user, textMessage);
                    nextStateCode = RegistrationStep.STATE_REGISTER_MORE_AUTH_CODES;
                    returnString = "Ok. You're all setup for yourself. If you have additional family"
                            + " members to register please enter the additional authorization"
                            + " codes now. When you need a meal just text MEAL back to this number.";
                } else {
                    returnString = "The authorization code you entered (" + textMessage + ") is not valid."
                            + " Please resend a valid unused authorization code";
                }
                break;
            case STATE_REGISTER_MORE_AUTH_CODES:
                if (activationCodeService.validate(textMessage)) {
                    customerService.registerCustomer(user, textMessage);
                    // link auth code with user
                    returnString = "We have added that authorization code to your family account. Please"
                            + " send any additional codes you need for your family.";
                } else {
                    returnString = "The authorization code you entered (" + textMessage + ") is not valid."
                            + " Please resend a valid unused authorization code";
                }
                // no longer need to change the state at this moment.
                break;
            default:
                LOGGER.error("Unknown state {}", registrationState.getRegistrationStep());
                returnString = "Oops, an error occured. Please start over again.";
                break;
        }
        return new WorkFlowStepResult(returnString, nextStateCode, serialize(user));
    }
}
