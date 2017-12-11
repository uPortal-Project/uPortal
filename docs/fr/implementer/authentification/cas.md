# Authentification Central Authentication Service (CAS)

à compléter.

## Redirection des invités vers CAS pour la connexion

Par défaut, uPortal permet aux utilisateurs non authentifiés (_ "guests" _) d'afficher le portail. Environ la moitié des adoptants d'uPortal utilisent cette fonctionnalité pour présenter un contenu général non sensible au public.  
(Les autres adopteurs uPortal préfèrent autoriser uniquement les utilisateurs authentifiés à accéder au portail.) Cette fonctionnalité est une façon de configurer uPortal pour exiger une authentification.

Pour configurer uPortal afin de rediriger les utilisateurs non authentifiés vers l'interface de connexion CAS, ajoutez 
l'option de configuration suivante à `${portal.home}/uPortal.properties`:

```properties
cas.enable.redirect.guest.to.login=true
```

### Inconvénients de la fonctionnalité de redirection CAS

:attention: Il y a quelques inconvénients à activer ce paramètre :

- Ce paramètre ne fonctionne que pour l'authentification CAS (à l'avenir, la communauté uPortal peut proposer une fonctionnalité unique prenant en charge plusieurs stratégies d'authentification)
- L'activation de cette fonctionnalité ** désactive toutes les autres formes d'authentification **
- Cette fonctionnalité ne prend pas en charge le _deep linking_ qui survit au processus d'authentification (_en d'autres termes_ si vous devez vous authentifier, vous n'atteindrez pas la page que vous essayiez d'atteindre si vous avez suivi un lien)

## CAS 5 ClearPass: Credential Caching and Replay

À partir de CAS 4, l'ancienne fonctionnalité ClearPass a été dépréciée au lieu de transmettre le mot de passe, crypté, comme tout autre attribut utilisateur. Cela nécessite une coordination supplémentaire entre CAS et uPortal avec le partage de clés.

### Configuration de CAS pour passer des mots de passe cryptés

Voir: <https://apereo.github.io/cas/5.0.x/integration/ClearPass.html> pour la configuration CAS.

### Creation de Clefs (from the above page)

La paire de clés doit être générée par l'application elle-même qui souhaite obtenir les informations d'identification de l'utilisateur.
La clé publique est partagée avec CAS. La clé privée est utilisée par uPortal pour déchiffrer les informations d'identification.

```bash
openssl genrsa -out private.key 1024
openssl rsa -pubout -in private.key -out public.key -inform PEM -outform DER
openssl pkcs8 -topk8 -inform PER -outform DER -nocrypt -in private.key -out private.p8
```

Sauvegarder `private.p8` dans un endroit connu.

### Changer le filtre de Validation CAS pour utiliser le protocole CAS 3

uPortal utilise une version plus ancienne pour son service intégré CAS. Pour utiliser cette fonctionnalité ClearPass, le filtre de validation doit être basculé de CAS Protocol 2 à 3. Ceci est simplement fait en éditant le nom de classe du filtre dans web.xml:

```xml
        <filter-class>org.jasig.cas.client.validation.Cas30ProxyReceivingTicketValidationFilter</filter-class>
```

### Configuration de uPortal pour accepter les mots de passe cryptés

La configuration d'uPortal pour cette fonctionnalité est simple. Le plus difficile est de configurer l'emplacement de la clé privée.

dans uportal-war/src/main/resources/properties/security.properties apportez les modifications suivantes (en supposant que le fichier de clé a été déplacé vers `/etc/cas/private.p8`):

```properties
## Flag to determine if the portal should convert CAS assertion attributes to user attributes - defaults to false
org.apereo.portal.security.cas.assertion.copyAttributesToUserAttributes=true
 
## Flag to determine if credential attribute from CAS should be decrypted to password - defaults to false
org.apereo.portal.security.cas.assertion.decryptCredentialToPassword=true
 
## Unsigned private key in PKCS8 format for credential decryption (for decryptCredentialToPassword)
org.apereo.portal.security.cas.assertion.decryptCredentialToPasswordPrivateKey=/etc/cas/private.p8
```

:attention : ** Attention: Impossible d'utiliser localhost ni HTTP! **
 :attention :CAS requiert que le trafic passe par une connexion **HTTPS** cryptée. En outre, un nom d'hôte autre que *localhost* est requis.
