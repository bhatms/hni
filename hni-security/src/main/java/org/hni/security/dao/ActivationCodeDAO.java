package org.hni.security.dao;

import org.hni.common.dao.BaseDAO;
import org.hni.security.om.ActivationCode;

public interface ActivationCodeDAO extends BaseDAO<ActivationCode> {
    ActivationCode getByActivationCode(String actCode);
}
