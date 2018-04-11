# Configurer uPortal derrière un Load-Balancer F5

## Table des matières

1. [Introduction](#introduction)
2. [Pré-Requis](#pré-requis)
3. [Configurer un LTM](#configurer-un-ltm)
4. [Configurer le GTM](#configurer-le-gtm)
5. [Configuration de Tomcat](#configuration-de-tomcat)

## Introduction

La plate-forme BIG-IP de F5 (communément appelée F5) est une solution populaire pour un équilibreur de charge (load-balancer) dédié. Il est riche en fonctionnalités avec de nombreuses options. Voici une
approche utilisée à l'Université de Californie, Merced.


Cette installation F5 possède un gestionnaire de trafic global (GTM) et deux gestionnaires de trafic locaux (LTM). Le GTM gère le DNS et
le trafic initial, redirigeant vers un LTM basé sur la configuration. Les LTM effectuent en réalité une grande partie du traitement de
paquets réseau avant de les transmettre à uPortal. En outre, F5 va gérer le cryptage SSL.

Dans cet exemple, le service DNS principal est configuré pour "aliasé" le service uPortal au F5 GTM.

## Pré-Requis

Vous devrez d'abord coordonner avec votre équipe réseau quelques modifications IP/DNS.

| éléments                                                | Example value for this install        |
| --------------------------------------------------- | ------------------------------------- |
| CNAME of uPortal URL to F5 managed A record         | my.ucmerced.edu -> my.gl.ucmerced.edu |
| CNAME of F5 managed A record in DNS as external     | my.gl.ucmerced.edu -> F5 DNS services |
| uPortal Virtual IPs (one per LTM)                   | 169.236.5.27, 169.236.79.27           |
| SSL key and certificate                             | my.key, my.cert                       |
| String to grep from the landing page of the tomcats | "portal"                              |

Les détails pour la configuration de F5 sont au-delà de ce document. Ces éléments servent à configurer un nouveau service uPortal.

## Configurer un LTM

Les premiers systèmes à configurer sont les LTM.

Connectez-vous à chaque client Web LTM (nécessite un accès administrateur).

### Installer la clé SSL et le certificat

Cette étape rend disponible la clé et le certificat pour le service Web uPortal.
Cela est similaire à la configuration d'Apache en front d'uPortal et le trafic SSL.
Les fichiers de clé et de certificat sont les mêmes que ceux attendus par Apache pour SSL.

1. Naviguer à System > File Management > SSL Certificate List > Import...
2. Importer Type: Key
3. Key Name: utiliser URL (par exemple : my.ucmerced.edu)
4. Cliquer sur Choose File
5. Rechercher le fichier de Clef
6. Cliquer sur Import
7. Cliquer sur URL link (i.e. my.ucmerced.edu)
8. Cliquer sur Import...
9. Cliquer sur Choose File
10. Rechercher et selectionner le fichier de certificat
11. Cliquer sur Import

### Créer un moniteur (monitor)

Un moniteur (monitor) vérifie la disponibilité des serveurs uPortal. Il interroge essentiellement chaque serveur, à la recherche d'une
réponse, pour confirmer qu'il est opérationnel.

1. Déterminer la string à chercher depuis la page initiale (i.e. "portal")
2. Naviguer à LTM > Virtual Servers > Monitors
3. Ajouter un Moniteur (*Monitor*) :
    1. Name: qqchose reférençant le service et "mon" (i.e. portal-http-mon)
    2. Description: des détails si vous le souhaitez
    3. Type: HTTP (ouvre plus de champs)
    4. Send String: `GET / HTTP/1.1\r\nHost: \r\n\r\n`
    5. Receive String: du texte que vous attendez de la page de destination (see étape #1)
    
### Créer des Pools

Un pool est une liste des serveurs qui forment le service uPortal pour un LTM. Une approche commune est de loger
un LTM et chaque data center. Le pool comprendrait alors les serveurs uPortal dans ce data center.

1. Déterminer le nom du pool (i.e. prod_portal_pool)
2. Naviguer à LTM > Virtual Servers > Pools
3. Ajouter un Pool:
    1. Configuration: Advanced
    2. Name: qqchose reférençant le service et "pool" (i.e. prod_portal_pool)
    3. Description: des détails si vous le souhaitez
    4. Health Monitors: selectionner le monitor créer auparavant
    5. Action On Service Down: Reject
    6. Load Balancing Method: Predictive (member), ou Dynamic Ratio (member)
        - Lire l'Aide pour faire une meilleur choix initial, puis tester.
    7. New Members: ajouter nodes = IPs + ports des serveurs uPortal
    
### Créer le profil SSL

Le profil SSL connecte la clé SSL et le certificat avec le(s) pool(s) uPortal.

1. Naviguer à LTM > Virtual Servers > Profiles > SSL > client
2. Cliquer sur Create ...
3. Entrer les valeurs suivantes :
    1. Name: un nom approprié (par exemple : portal_clientssl)
    2. Parent Profile: clientssl
    3. Selectionner 'Advanced' depuis le menu
    4. Certificate: vérifier et changer pour le profil SSL créé auparavant
    5. Key: vérifier et changer pour le profil SSL créé auparavant
    6. Chain: vérifier et changer pour une intermédiaire si cela est requis par votre certificat

### Create Port 80 Redirect Virtual Server

Cette étape crée une redirection pour le trafic sur l'adresse IP virtuelle d'uPortal, port 80, vers HTTPS (port 443). 

1. Naviguer à Virtual Servers > Virtual Server List
2. Cliquer sur Create ...
3. Entrer les valeurs suivantes :
    1. Name: Quelque chose qui combine "vs" plus le service portal (i.e. vs_portal_80)
    2. Destination: Virtual IP pour cette LTM (voir pré-requis)
    3. Service Port: HTTP (80)
    4. HTTP Profile: http
    5. VLAN and Tunnels: external_vip_vlan
    6. iRules: _sys_https_redirect

### Create Port 443 Virtual Server

Cette étape achemine le trafic entrant sur l'adresse IP virtuelle pour uPortal vers les serveurs uPortal.

1. Naviguer à Virtual Servers > Virtual Server List
2. Cliquer sur Create ...
3. Entrer les valeurs suivantes :
    1. Name: Quelque chose qui combine "vs" plus le service portal plus le port (i.e. vs_portal_443)
    2. Destination: Virtual IP pour cette LTM (voir pré-requis)
    3. Service Port: HTTPS (443)
    4. HTTP Profile: http
    5. SSL Profile (Client): sélectionner le profil SSL créé auparavant
    6. VLAN and Tunnels: external_vip_vlan
    7. SNAT Pool: Auto Map
    8. Default Pool: sélectionner le pool créé auparavant
    9. Default Persistence Profile: cookie

## Configurer le GTM

Une fois les LTM configurés, nous pouvons configurer le GTM. La configuration de GTM est beaucoup plus facile.
En outre, la configuration du GTM à partir d'un seul client Web LTM est suffisante car ils pointent tous vers un seul GTM.

Connectez-vous au client Web GTM (nécessite un accès administrateur).

### Créer un Pool

Le pool est la liste définitive des adresses IP virtuelles uPortal pointant vers tous les LTM actifs.

1. Naviguer à Pools
2. Cliquer sur Create ...
3. Entrer les valeurs suivantes :
    1. Name: Quelque chose avec le nom du service et "pool" (i.e. uportal_pool)
    2. Load Balancing Method: Topology, Global Availability, Return to DNS
        - Voir l'aide pour des options additionnelles
    3. Members: Virtual IPs + ports pour tous les LTM avec pools uPortal

### Créer des adresses IP larges

Cette étape suppose que le DNS est correctement configuré à la fois sur le DNS global et le service DNS GTM.

1. Naviguer à Wide IPs
2. Cliquer sur Create ...
3. Entrer les valeurs suivantes :
    1. Name: CNAME (DNS) de l'enregistrement A géré par le F5
    2. Pool: sélectionner le pool créer auparavant

## Configuration de Tomcat

Les changement sont a réaliser dans `<TOMCAT_HOME>/conf/server.xml`.

### Modifier le connecteur pour prendre en charge le décryptage sur le F5

Pour prendre en charge le décryptage au niveau du F5, certains attributs supplémentaires doivent être définis pour le(s) connecteur(s)
recevant le trafic du F5. Ce changement configure Tomcat pour accepter des 
paquets non cryptés en les considèrant comme sécurisés.

``` xml
    <Connector port="8080" protocol="HTTP/1.1"
        ...
        proxyPort="443"
        emptySessionPath="true"
        scheme="https"
        secure="true"
    />
```
### Ajouter `Remote IP Valve` pour la génération de la journalisation (Log) et de la Session ID

Il est important que Tomcat et uPortal reçoivent les adresses IP de l'utilisateur plutôt que les adresses IP de l'équilibreur de charge. C'est utilisé pour la journalisation et la génération de la clé de session utilisateur (Session ID). uPortal utilisera l'IP _plus_ un petit nombre aléatoire pour créer des clés de session. Donc, sans cette valve, il y aura une forte probabilité que des utilisateurs partagent des sessions par inadvertance. Ajoutez la `Remote IP Valve` à côté des autres valves proches en bas du fichier `server.xml`:

``` xml
        <Valve className="org.apache.catalina.valves.RemoteIpValve"
                internalProxies="10\.22\.1\.196, 10\.22\.2\.252"
                remoteIpHeader="x-forwarded-for"
                remoteIpProxiesHeader="X-Forwarded-For"
                protocolHeader="x-forwarded-proto" />
```

Ajuster les adresses IP pour qu'elles correspondent à celles du ou des équilibreurs de charge.

Voir [plate-forme BIG-IP](https://f5.com/products/big-ip)