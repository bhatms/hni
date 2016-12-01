package org.hni.provider.service;

import org.junit.Test;

/**
 * Created by walmart on 11/14/16.
 */
public class TestDefaultGeoCodingService {

    DefaultGeoCodingService geoCodingService = new DefaultGeoCodingService();

    @Test
    public void simpleAddress() {
        geoCodingService.resolveAddress("10790+Parkridge+Boulevard%2C+Reston%2C+VA+20191");
    }
}
