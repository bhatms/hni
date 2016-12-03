package org.hni.provider.service;

import org.apache.commons.lang3.StringUtils;
import org.hni.common.service.AbstractService;
import org.hni.provider.dao.ProviderLocationDAO;
import org.hni.provider.om.AddressException;
import org.hni.provider.om.LocationQueryParams;
import org.hni.provider.om.Provider;
import org.hni.provider.om.ProviderLocation;
import org.hni.user.om.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

import java.util.Collection;

@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class DefaultProviderLocationService extends AbstractService<ProviderLocation> implements ProviderLocationService {

	private ProviderLocationDAO providerLocationDao;

	@Autowired
	private GeoCodingService geoCodingService;
	
	@Inject
	public DefaultProviderLocationService(ProviderLocationDAO providerLocationDao) {
		super(providerLocationDao);
		this.providerLocationDao = providerLocationDao;
	}

	@Override
	public Collection<ProviderLocation> with(Provider provider) {
		return this.providerLocationDao.with(provider);
	}

	@Override
    public Collection<ProviderLocation> providersNearCustomer(String customerAddress, int itemsPerPage, double maxDistance) {

        if(!StringUtils.isBlank(customerAddress)) {
            
            Address addrpoint = geoCodingService.resolveAddress(customerAddress).orElse(null);

            if (addrpoint != null) {
                LocationQueryParams locationQuery = transFormLocationToQueryObject(addrpoint, maxDistance, itemsPerPage);
                return providerLocationDao.providersNearCustomer(locationQuery);
            } else {
                throw new AddressException("Unable to resolve address");
            }
            
        } else {
            throw new AddressException("Invalid address provided");
        }
	    
    }
	
	/**
	 * method takes latitude longitude of customer
	 * find boundaries and create an object with all search parameters
	 * @param addrpoint
	 * @param distance
	 * @param itemsCount number of results
	 * @return
	 */
	protected LocationQueryParams transFormLocationToQueryObject(Address addrpoint, double distance, int itemsCount) {
	    
	    LocationQueryParams locationQueryParams = new LocationQueryParams();
	    locationQueryParams.setMaxDistance(distance * 1.60934); //convert miles to km
	    locationQueryParams.setResultCount(itemsCount);
	    locationQueryParams.setCustomerLattitude(addrpoint.getLatitude());
	    locationQueryParams.setCustomerLongitude(addrpoint.getLongitude());
        return locationQueryParams;
	}


}
