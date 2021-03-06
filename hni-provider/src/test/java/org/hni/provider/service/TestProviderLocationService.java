package org.hni.provider.service;

import org.hni.provider.om.Provider;
import org.hni.provider.om.ProviderLocation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * The schema for testing is located in the hni-schema project which gets shared across
 * all test cases.
 * 
 * @author j2parke
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:test-applicationContext.xml"} )
@Transactional
public class TestProviderLocationService {

	@Inject private ProviderService providerService;
	@Inject private ProviderLocationService providerLocationService;

	@Test
	public void testGetProviderLocation() {
		ProviderLocation providerLocation = providerLocationService.get(1L);
		assertNotNull(providerLocation);
	}

	/*
	@Test
	public void testAddAddress() {
		ProviderLocation providerLocation = providerLocationService.get(1L);
		assertNotNull(providerLocation);
		assertNotNull(providerLocation.getAddress());

		providerLocation.getAddress().add( new Address("address1", "address2", "city", "AR", "zip") );
		providerLocationService.save(providerLocation);

		ProviderLocation plCheck = providerLocationService.get(1L);
		assertNotNull(plCheck);
		assertTrue(plCheck.getAddresses().size() > 0);

	}
	*/

	@Test
	public void testAddLocationToProvider() {
		Provider provider = providerService.get(1L);
		assertNotNull(provider);

		ProviderLocation pl = new ProviderLocation();
		pl.setCreated(new Date());
		pl.setCreatedById(1L);
		pl.setName("test new location");
		pl.setProvider(provider);

		pl = providerLocationService.save(pl);

		Collection<ProviderLocation> list = providerLocationService.with(provider);
		assertNotNull(list);
		assertTrue(list.size() > 0);

	}

	@Test
    public void testGetProviderLocationByCustomerAddr_nullAddress() {
	    Collection<ProviderLocation> providerLocations = providerLocationService.providersNearCustomer(null, 1, 10);
        assertTrue(providerLocations.isEmpty());
    }
    
    @Test
    public void testGetProviderLocationByCustomerAddr_emptyAddress() {
        Collection<ProviderLocation> providerLocations = providerLocationService.providersNearCustomer("  ", 1, 10);
        assertTrue(providerLocations.isEmpty());
    }
    
    @Test
    public void testGetProviderLocationByCustomerAddr_invalidAddress() {
        Collection<ProviderLocation> providerLocations = providerLocationService.providersNearCustomer("not a good addres", 1, 10);
        assertTrue(providerLocations.isEmpty());
    }

    @Test
    public void testGetProviderLocationByCustomerAddr() {
        Collection<ProviderLocation> providerLocations = providerLocationService.providersNearCustomer("bridle view way ohcolumbus", 3, 10);
        assertTrue(providerLocations.size() > 0);

        providerLocations = providerLocationService.providersNearCustomer("reston town center reston va", 1, 10);
        assertTrue(providerLocations.size() > 0);
    }
    
    
    @Test
    public void testGetProviderLocationByCustomerAddr_found_in2miles() {
        Collection<ProviderLocation> providerLocations = providerLocationService.providersNearCustomer("8914 Centreville Rd Manassas, VA", 1, 2);
        assertTrue(providerLocations.size() > 0);
        
        ProviderLocation providerLoc = providerLocations.iterator().next();
        assertEquals("MANASSAS", providerLoc.getAddress().getCity().toUpperCase());

    }
    
    @Test
    public void testGetProviderLocationByCustomerAddr_not_found_in2miles() {
        Collection<ProviderLocation> providerLocations = providerLocationService.providersNearCustomer("10864 Sudley Manor Dr, Manassas, VA", 1, 2);
        assertTrue(providerLocations.size() == 0);

    }
    
    @Test
    public void testGetProviderLocationByCustomerAddr_found_in10miles() {
        Collection<ProviderLocation> providerLocations = providerLocationService.providersNearCustomer("10864 Sudley Manor Dr, Manassas, VA", 1, 10);
        assertTrue(providerLocations.size() > 0);
        
        ProviderLocation providerLoc = providerLocations.iterator().next();
        assertEquals("MANASSAS", providerLoc.getAddress().getCity().toUpperCase());
    }
    
    
    @Test
    public void testProviderLocation_testOrder() {
        // test out of multiple providers first one is nearest
        Collection<ProviderLocation> providerLocations = providerLocationService
                    .providersNearCustomer("bridle view way columbus ohio", 3, 10);
        assertTrue(providerLocations.size() > 0);

        //nearest
        Iterator<ProviderLocation> itr = providerLocations.iterator();
        ProviderLocation providerLoc = itr.next();
        assertEquals("COLUMBUS", providerLoc.getAddress().getCity().toUpperCase());
        
        //NEXT
        providerLoc = itr.next();
        assertEquals("westerville", providerLoc.getAddress().getCity().toLowerCase());
    }
    
    
    @Test
    public void testProviderLocation_testOrder1() {
        // test out of multiple providers first one is nearest
        Collection<ProviderLocation> providerLocations = providerLocationService.
                    providersNearCustomer("126 S State St, Westerville, OH 43081", 3, 10);
        assertTrue(providerLocations.size() > 0);

        //nearest
        Iterator<ProviderLocation> itr = providerLocations.iterator();
        ProviderLocation providerLoc = itr.next();
        assertEquals("westerville", providerLoc.getAddress().getCity().toLowerCase());
        
        //NEXT
        providerLoc = itr.next();
        assertEquals("columbus", providerLoc.getAddress().getCity().toLowerCase());
        
    }
    
    
    @Test
    public void testGetProviderLocationByCustomerAddr_found_in250miles_testlimit() {
        Collection<ProviderLocation> providerLocations = providerLocationService
                    .providersNearCustomer("10864 Sudley Manor Dr, Manassas, VA", 2, 250);
        assertEquals(2, providerLocations.size());
    }
}
