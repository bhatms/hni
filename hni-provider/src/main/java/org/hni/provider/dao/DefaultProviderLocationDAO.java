package org.hni.provider.dao;

import org.hni.common.dao.AbstractDAO;
import org.hni.provider.om.LocationQueryParams;
import org.hni.provider.om.Provider;
import org.hni.provider.om.ProviderLocation;
import org.springframework.stereotype.Component;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import java.util.Collection;
import java.util.Collections;

@Component
public class DefaultProviderLocationDAO extends AbstractDAO<ProviderLocation> implements ProviderLocationDAO {

	protected DefaultProviderLocationDAO() {
		super(ProviderLocation.class);
	}

	@Override
	public Collection<ProviderLocation> with(Provider provider) {
		try {
			Query q = em.createQuery("SELECT x FROM ProviderLocation x WHERE x.provider.id = :providerId")
				.setParameter("providerId", provider.getId());
			return q.getResultList();
		} catch(NoResultException e) {
			return Collections.emptyList();
		}
	}

	

    @Override
    public Collection<ProviderLocation> providersNearCustomer(LocationQueryParams locationQuery) {

        try {

            String queryString = new StringBuilder(
                            "SELECT pl.* , temp_adr.distance" 
                            + " FROM provider_locations pl JOIN " 

                            + " ( select id ,"
                            + " ( 6371 * acos( cos(radians(:custLatitude)) * cos(radians(latitude)) " 
                            + " * cos(radians(longitude) - radians(:custLongitude)) " 
                            + " + sin(radians(:custLatitude)) * sin(radians(latitude)) ) ) AS distance" 
                    
                            + " FROM addresses " 
                            + " WHERE (latitude < :custLatitude + 1 AND latitude > :custLatitude - 1) " 
                            + " AND (longitude < :custLongitude + 1 AND  longitude > :custLongitude - 1) "  
                            + " group by id having distance < :maxDistance )  temp_adr ON temp_adr.id = pl.address_id "
                            + " ORDER   BY temp_adr.distance asc "
                            + " LIMIT :resultCount")
                        .toString();
            
            

            Query q = em.createNativeQuery(queryString, ProviderLocation.class)
                    .setParameter("custLatitude", locationQuery.getCustomerLattitude())  
                    .setParameter("custLongitude", locationQuery.getCustomerLongitude())  
                    .setParameter("maxDistance", locationQuery.getMaxDistance())
                    .setParameter("resultCount", locationQuery.getResultCount());

            return q.getResultList();
        } catch(NoResultException e) {
            return Collections.emptyList();
        }
    }

}
