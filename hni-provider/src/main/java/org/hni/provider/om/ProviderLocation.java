package org.hni.provider.om;

import org.hni.common.om.Persistable;
import org.hni.user.om.Address;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a physical location for a provider generally where
 * food is prepared and provided to clients. 
 *
 */
@Entity
@Table(name="provider_locations")
public class ProviderLocation implements Persistable, Serializable {

	private static final long serialVersionUID = 304579212529448435L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;
	
	@Column(name="name") private String name;
	@Column(name="created") private Date created;
	@Column(name="created_by") private Long createdById;

	@ManyToOne
	@JoinColumn(name="address_id", referencedColumnName = "id")
	private Address address;
	
	@ManyToOne
	@JoinColumn(name="provider_id", referencedColumnName = "id")
	private Provider provider;
	
    @OneToMany(fetch=FetchType.EAGER, mappedBy="providerLocation", cascade = {CascadeType.ALL}, orphanRemoval=true)
    private Set<ProviderLocationHour> providerLocationHours = new HashSet<ProviderLocationHour>();
    
    public ProviderLocation() {}
    public ProviderLocation(Long id) {
    	this.id = id;
    }
    
	@Override
	public Long getId() {
		return this.id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Long getCreatedById() {
		return createdById;
	}

	public void setCreatedById(Long createdById) {
		this.createdById = createdById;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public void setId(Long id) {
		this.id = id;
	}
	public Provider getProvider() {
		return provider;
	}
	public void setProvider(Provider provider) {
		this.provider = provider;
	}
	public Set<ProviderLocationHour> getProviderLocationHours() {
		return providerLocationHours;
	}
	public void setProviderLocationHours(Set<ProviderLocationHour> providerLocationHours) {
		this.providerLocationHours = providerLocationHours;
	}

	
}
