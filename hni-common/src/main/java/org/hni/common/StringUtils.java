package org.hni.common;

import org.apache.commons.validator.routines.EmailValidator;

import javax.mail.internet.InternetAddress;

/**
 * String utility class
 */
public class StringUtils {

    /**
     * This method checks if passed String is a number or not.
     *
     * @param str
     * @return true if valid number otherwise false
     */
    public static boolean isNumber(String str) {
        try {
            Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
