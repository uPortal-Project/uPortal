package org.jasig.portal.jgroups.protocols;

import java.util.Collection;
import java.util.Map;

import org.jgroups.Address;
import org.jgroups.PhysicalAddress;


/**
 * Shared persistent store used by {@link DAO_PING} to discover other cluster members. 
 * 
 * @author Eric Dalquist
 */
public interface PingDao {
    /**
     * Add an address pair to the data store
     * 
     * @param clusterName Name of the cluster the address is from
     * @param memberAddress The "unique within the cluster" address for the member
     * @param physicalAddress The physical address that the member can be contacted at
     */
    void addAddress(String clusterName, Address memberAddress, PhysicalAddress physicalAddress);
    
    /**
     * Get all of the members listed for the specified cluster.
     * 
     * @param clusterName Name of the cluster to get member address information for
     * @return Map of unique member address to physical address
     */
    Map<Address, PhysicalAddress> getAddresses(String clusterName);
    
    /**
     * Remove all cluster members that are not listed in includedAddresses.
     * 
     * @param clusterName Name of the cluster to purge addresses in
     * @param includedAddresses The addresses to keep
     */
    void purgeOtherAddresses(String clusterName, Collection<Address> includedAddresses);
}
