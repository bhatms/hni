package org.hni.user.service;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.hni.common.service.BaseService;
import org.hni.user.om.User;

import java.util.List;

public interface UserService extends BaseService<User> {

    /**
     * Validates user fields if present
     */
    default boolean validate(User user) {
        if (user == null) {
            return false;
        }
        if (user.getMobilePhone() != null
                && (!StringUtils.isNumeric(user.getMobilePhone()) || user.getMobilePhone().length() != 10)) {
            return false;
        }
        if (user.getEmail() != null
                && (!"none".equalsIgnoreCase(user.getEmail()) && !EmailValidator.getInstance().isValid(user.getEmail()))) {
            return false;
        }

        return true;
    }

    default User registerCustomer(User user, String authCode) {
        //do nothing
        return user;
    }

    List<User> byMobilePhone(String byMobilePhone);

    List<User> byLastName(String lastName);

    User byEmailAddress(String emailAddress);
}
