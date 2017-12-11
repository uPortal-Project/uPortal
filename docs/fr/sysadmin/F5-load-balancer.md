# Configurer uPortal derrière un Load-Balancer F5

## Table des matières

1. [Introduction](#introduction)
2. [Pre-Requisites](#pre-requisites)
3. [Configuring an LTM](#configuring-an-ltm)
4. [Configuring the GTM](#configuring-the-gtm)
5. [Configuring connectors in Tomcat](#configuring-connectors-in-tomcat)

## Introduction

La plate-forme BIG-IP de F5 (communément appelée F5) est une solution populaire pour un équilibreur de charge (load-balancer) dédié. Il est riche en fonctionnalités avec de nombreuses options. Voici une
approche utilisée à l'Université de Californie, Merced.


Cette installation F5 possède un gestionnaire de trafic global (GTM) et deux gestionnaires de trafic locaux (LTM). Le GTM gère le DNS et
le trafic initial, redirigeant vers un LTM basé sur la configuration. Les LTM effectuent en réalité une grande partie du traitement de
paquets réseau avant de les transmettre à uPortal. En outre, F5 va gérer le cryptage SSL.

Dans cet exemple, le service DNS principal est configuré pour "aliasé" le service uPortal au F5 GTM.

## Pré-Requis

Vous devrez d'abord coordonneravec votre équipe réseau quelques modifications IP/DNS.

| éléments                                                | Example value for this install        |
| --------------------------------------------------- | ------------------------------------- |
| CNAME of uPortal URL to F5 managed A record         | my.ucmerced.edu -> my.gl.ucmerced.edu |
| CNAME of F5 managed A record in DNS as external     | my.gl.ucmerced.edu -> F5 DNS services |
| uPortal Virtual IPs (one per LTM)                   | 169.236.5.27, 169.236.79.27           |
| SSL key and certificate                             | my.key, my.cert                       |
| String to grep from the landing page of the tomcats | "portal"                              |

Les détails pour la configuration de F5 sont au-delà de ce document. Ces éléments servent à configurer un nouveau service uPortal.

## Configurer un LTM (gestionnaires de trafic locaux)

Les premiers systèmes à configurer sont les LTM.

Connectez-vous à chaque client Web LTM (nécessite un accès administrateur).

### Installer la clé SSL et le certificat

Cette étape rend disponible la clé et le certificat pour le service Web uPortal.
Cela est similaire à la configuration d'Apache en front d'uPortal et le trafic SSL.
Les fichiers de clé et de certificat sont les mêmes que ceux attendus par Apache pour SSL.

1. Naviguez à System > File Management > SSL Certificate List > Import...
2. Importer Type: Key
3. Key Name: utilisez URL (par exemple : my.ucmerced.edu)
4. Cliquez sur Choose File
5. Find and select the key file
6. Cliquez sur Import
7. Cliquez sur URL link (i.e. my.ucmerced.edu)
8. Cliquez sur Import...
9. Cliquez sur Choose File
10. Rechercher et selectionnez le fichier de certificat
11. Cliquez sur Import

### Créer un moniteur (monitor)

Un moniteur (monitor) vérifie la disponibilité des serveurs uPortal. Il interroge essentiellement chaque serveur, à la recherche d'une
réponse, pour confirmer qu'il est opérationnel.

1. Déterminez la string à chercher depuis la page initiale (i.e. "portal")
2. Naviguez à LTM > Virtual Servers > Monitors
3. Ajouter un Moniteur (Monitor) :
    1. Name: qqchose reférençant le service et "mon" (i.e. portal-http-mon)
    2. Description: des détails si vous le souhaitez
    3. Type: HTTP (ouvre plus de champs)
    4. Send String: `GET / HTTP/1.1\r\nHost: \r\n\r\n`
    5. Receive String: du texte que vous attendez de la page de destination (see étape #1)
    
### Créer des Pools

Un pool est une liste des serveurs qui forment le service uPortal pour un LTM. Une approche commune est de loger
un LTM et chaque data center. Le pool comprendrait alors les serveurs uPortal dans ce data center.

1. Déterminez le nom du pool (i.e. prod_portal_pool)
2. Naviguez à LTM > Virtual Servers > Pools
3. Ajouter un Pool:
    1. Configuration: Advanced
    2. Name: qqchose reférençant le service et "pool" (i.e. prod_portal_pool)
    3. Description: des détails si vous le souhaitez
    4. Health Monitors: selectionnez le monitor créer auparavant
    5. Action On Service Down: Reject
    6. Load Balancing Method: Predictive (member), ou Dynamic Ratio (member)
        - Lisez l'Aide pour faire une meilleur choix initial, puis testez.
    7. New Members: ajoutez nodes = IPs + ports des serveurs uPortal
    
### Créer le profil SSL

Le profil SSL connecte la clé SSL et le certificat avec le(s) pool(s) uPortal.

1. Naviguez à LTM > Virtual Servers > Profiles > SSL > client
2. Cliquez sur Create ...
3. Entrez les valeurs suivantes :
    1. Name: un nom approprié (par exemple : portal_clientssl)
    2. Parent Profile: clientssl
    3. Selectionnez 'Advanced' depuis le menu
    4. Certificate: vérifier et changer pour le profil SSL créé auparavant
    5. Key: vérifier et changer pour le profil SSL créé auparavant
    6. Chain: vérifier et changer pour une intermédiaire si cela est requis par votre certificat

### Create Port 80 Redirect Virtual Server

Cette étape crée une redirection pour le trafic sur l'adresse IP virtuelle d'uPortal, port 80, vers HTTPS (port 443). 

1. Naviguez à Virtual Servers > Virtual Server List
2. Cliquez sur Create ...
3. Entrez les valeurs suivantes :
    1. Name: Quelque chose qui combine "vs" plus le service portal (i.e. vs_portal_80)
    2. Destination: Virtual IP pour cette LTM (voir pré-requis)
    3. Service Port: HTTP (80)
    4. HTTP Profile: http
    5. VLAN and Tunnels: external_vip_vlan
    6. iRules: _sys_https_redirect

### Create Port 443 Virtual Server

Cette étape achemine le trafic entrant sur l'adresse IP virtuelle pour uPortal vers les serveurs uPortal.

1. Naviguez à Virtual Servers > Virtual Server List
2. Cliquez sur Create ...
3. Entrez les valeurs suivantes :
    1. Name: Quelque chose qui combine "vs" plus le service portal plus le port (i.e. vs_portal_443)
    2. Destination: Virtual IP pour cette LTM (voir pré-requis)
    3. Service Port: HTTPS (443)
    4. HTTP Profile: http
    5. SSL Profile (Client): sélectionnez le profil SSL créé auparavant
    6. VLAN and Tunnels: external_vip_vlan
    7. SNAT Pool: Auto Map
    8. Default Pool: sélectionnez le pool créé auparavant
    9. Default Persistence Profile: cookie

## Configurer le GTM

Une fois les LTM configurés, nous pouvons configurer le GTM. La configuration de GTM est beaucoup plus facile.
En outre, la configuration du GTM à partir d'un seul client Web LTM est suffisante car ils pointent tous vers un seul GTM.

Connectez-vous au client Web GTM (nécessite un accès administrateur).

### Créer un Pool

Le pool est la liste définitive des adresses IP virtuelles uPortal pointant vers tous les LTM actifs.

1. Naviguez à Pools
2. Cliquez sur Create ...
3. Entrez les valeurs suivantes :
    1. Name: Quelque chose avec le nom du service et "pool" (i.e. uportal_pool)
    2. Load Balancing Method: Topology, Global Availability, Return to DNS
        - Voir l'aide pour des options additionnelles
    3. Members: Virtual IPs + ports pour tous les LTM avec pools uPortal

### Créer des adresses IP larges

Cette étape suppose que le DNS est correctement configuré à la fois sur le DNS global et le service DNS GTM.

1. Naviguez à Wide IPs
2. Cliquez sur Create ...
3. Entrez les valeurs suivantes :
    1. Name: CNAME (DNS) de l'enregistrement A géré par le F5
    2. Pool: sélectionnez le pool créer auparavant

## Configuration des connecteurs dans Tomcat

Pour prendre en charge le décryptage sur le F5, certains attributs supplémentaires doivent être définis pour les connecteurs du fichier server.xml dans vos installations de Tomcat d'uPortal. Cette modification configure Tomcat pour qu'il accepte les paquets non cryptés mais les considère comme sécurisés.

``` xml
<Connector port="8080" protocol="HTTP/1.1"
    ...
    proxyPort="443"
    emptySessionPath="true"
    scheme="https"
    secure="true"
/>
```

Voir [plate-forme BIG-IP](https://f5.com/products/big-ip)