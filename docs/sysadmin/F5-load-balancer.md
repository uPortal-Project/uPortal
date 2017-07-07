# Configuring uPortal Behind F5 Load-Balancer

## Sections

1. [Introduction](#introduction)
2. [Pre-Requisites](#pre-requisites)
3. [Configuring an LTM](#configuring-an-ltm)
4. [Configuring the GTM](#configuring-the-gtm)
5. [Configuring connectors in Tomcat](#configuring-connectors-in-tomcat)

## Introduction

F5's BIG-IP Platform (commonly called F5) is a popular solution for a dedicated load balancer. It is feature-rich with many options. Here is one
approach used at University of California, Merced.


This F5 installation has one Global Traffic Manager (GTM) and two Local Traffic Managers (LTMs). The GTM manages DNS and
initial traffic, redirecting to an LTM based on configuration. The LTMs actually perform much of the processing of the 
network packets before passing them on to uPortal. In addition, F5 will handle SSL encryption.

In this example, the main DNS service is configured to alias the uPortal service to the F5 GTM.

## Pre-Requisites

You will need to coordinate a few IP/DNS changes up front with your network team.

| Item                                                | Example value for this install        |
| --------------------------------------------------- | ------------------------------------- |
| CNAME of uPortal URL to F5 managed A record         | my.ucmerced.edu -> my.gl.ucmerced.edu |
| CNAME of F5 managed A record in DNS as external     | my.gl.ucmerced.edu -> F5 DNS services |
| uPortal Virtual IPs (one per LTM)                   | 169.236.5.27, 169.236.79.27           |
| SSL key and certificate                             | my.key, my.cert                       |
| String to grep from the landing page of the tomcats | "portal"                              |

Details for setting up F5 are beyond this document. These items are for setting up a new uPortal service.

## Configuring an LTM 
The first systems to configure are the LTMs.

Log into each LTM web client (requires admin access).

### Install SSL Key and Certificate

This step makes available the key and certificate for the uPortal web service.
This is similar to configuring Apache to front uPortal and handle SSL traffic.
The key and certificate files are the same as those expected by Apache for SSL.

1. Navigate to System > File Management > SSL Certificate List > Import...
2. Import Type: Key
3. Key Name: use URL (i.e. my.ucmerced.edu)
4. Click on Choose File
5. Find and select the key file
6. Click on Import
7. Click on URL link (i.e. my.ucmerced.edu)
8. Click on Import...
9. Click on Choose File
10. Find and select the certificate file
11. Click on Import

### Create Monitor

A monitor checks uPortal servers for availability. It essentially polls each server, looking for a specific
response, to confirm it is operational.

1. Determine that string to grep for from the initial page (i.e. "portal")
2. Navigate to LTM > Virtual Servers > Monitors
3. Add Monitor:
    1. Name: something referencing service and "mon" (i.e. portal-http-mon)
    2. Description: some detail if you want
    3. Type: HTTP (opens up more fields)
    4. Send String: `GET / HTTP/1.1\r\nHost: \r\n\r\n`
    5. Receive String: some text you expect from the landing page (see step #1)
    
### Create Pools

A pool is a list of the servers that form this uPortal service for an LTM. A common approach is to house
an LTM and each data center. The pool would then consist of the uPortal servers in that data center.

1. Determine the pool name (i.e. prod_portal_pool)
2. Navigate to LTM > Virtual Servers > Pools
3. Add Pool:
    1. Configuration: Advanced
    2. Name: something combining service and "pool" (i.e. prod_portal_pool)
    3. Description: some detail if you want
    4. Health Monitors: select monitor created above
    5. Action On Service Down: Reject
    6. Load Balancing Method: Predictive (member), or Dynamic Ratio (member)
        - read Help to make a better initial selection, then test.
    7. New Members: add nodes = IPs + ports of the uPortal servers
    
### Create SSL Profile

The SSL Profile connects the SSL key and certificate with the uPortal pool(s).

1. Navigate to LTM > Virtual Servers > Profiles > SSL > client
2. Click on Create ...
3. Enter the following values:
    1. Name: an appropriate name (i.e. portal_clientssl)
    2. Parent Profile: clientssl
    3. Select 'Advanced' from the dropdown
    4. Certificate: check and change to SSL Profile created above
    5. Key: check and change to SSL Profile created above
    6. Chain: check and change to an intermediate if required for your certificate

### Create Port 80 Redirect Virtual Server

This step creates a redirect for traffic on the uPortal virtual IP, port 80, to HTTPS (port 443). 

1. Navigate to Virtual Servers > Virtual Server List
2. Click on Create ...
3. Enter the following values:
    1. Name: Something tha combines "vs" plus service plus portl (i.e. vs_portal_80)
    2. Destination: Virtual IP for this LTM (see pre-requisites)
    3. Service Port: HTTP (80)
    4. HTTP Profile: http
    5. VLAN and Tunnels: external_vip_vlan
    6. iRules: _sys_https_redirect

### Create Port 443 Virtual Server

This step routes incoming traffic on the virtual IP for uPortal to the uPortal servers.

1. Navigate to Virtual Servers > Virtual Server List
2. Click on Create ...
1. Enter the following values:
    1. Name: Something tha combines "vs" plus service plus portl (i.e. vs_portal_443)
    2. Destination: Virtual IP for this LTM (see pre-requisites)
    3. Service Port: HTTPS (443)
    4. HTTP Profile: http
    5. SSL Profile (Client): select SSL Profile created above
    6. VLAN and Tunnels: external_vip_vlan
    7. SNAT Pool: Auto Map
    8. Default Pool: select the pool created above
    9. Default Persistence Profile: cookie

## Configuring the GTM

Once the LTMs are configured, we can configure the GTM. GTM configure is much easier.
Also, configuring the GTM from one LTM web client is sufficient as they all point to
a single GTM.

Log into the GTM (requires admin access) web client.

### Create Pool

The pool is the definitive list of the uPortal virtual IPs pointing to all active LTMs.

1. Navigate to Pools
2. Click on Create ...
3. Enter the following values:
    1. Name: Something with service name and "pool" (i.e. uportal_pool)
    2. Load Balancing Method: Topology, Global Availability, Return to DNS
        - see help for addition options
    3. Members: Virtual IPs + ports for all LTMs with uPortal pools

### Create Wide IPs

This step assumes that DNS is set up correctly both on the global DNS and the GTM DNS service.

1. Navigate to Wide IPs
2. Click on Create ...
3. Enter the following values:
    1. Name: CNAME of the F5 managed A record
    2. Pool: select pool created above

## Configuring Connectors in Tomcat

To support decryption at the F5, some additional attributes need to be set for the connectors
in server.xml in your uPortal Tomcat installs. This change configures Tomcat to accept unencrypted
packets but consider them secure.

``` xml
<Connector port="8080" protocol="HTTP/1.1"
    ...
    proxyPort="443"
    emptySessionPath="true"
    scheme="https"
    secure="true"
/>
```

See [BIG-IP Platform](https://f5.com/products/big-ip)