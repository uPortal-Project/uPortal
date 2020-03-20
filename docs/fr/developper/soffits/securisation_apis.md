# Sécurisation des API

Les projets de Soffit fournissent souvent des API basées sur HTTP pour fournir des données aux composants d'interface utilisateur 
ou pour effectuer des opérations sur des banques de données persistantes. En fait, cette pratique est préférable et est recommandée par rapport aux 
stratégies de _rendu côté serveur_ qui génèrent du HTML dynamique avec des données déjà intégrées. Les modules 
qui fournissent une logique "frontale" (UI) et "back-end" (côté serveur) distincte sont plus faciles à comprendre, 
à maintenir et à étendre.

Dans la plupart des cas, ces API fournies par un module suivent les principes de REST (<i>Representational State Transfer</i>) 
et utilisent JSON comme format d'échange de données. Ces deux pratiques sont également préférables et recommandées.

Souvent, ces API requièrent des informations provenant d'une source fiable. Par exemple, ils peuvent avoir besoin de connaître 
l'identité et/ou les caractéristiques de l'utilisateur, ou peuvent fournir un accès à des données sensibles, voire des deux.

uPortal fournit un moyen simple et sécurisé de partager ces informations avec les API fournies par les modules (c'est-à-dire 
les API des modules uPortal). Cette fonctionnalité est basée sur 
`SoffitApiPreAuthenticatedProcessingFilter` et [Spring Security][]. Les fichiers de classe Java pour cette 
fonctionnalité sont disponibles dans la dépendance `uPortal-soffit-renderer`.

:notebook:  Un module n'a pas besoin d'être une Soffit pour utiliser ces fonctionnalités, mais il _doit_ vraiment être un module.
En d'autres termes, les API qui sont sécurisées en utilisant cette approche doivent fournir des données et des services 
_dans le portail_ comme but originel et essentiel. La raison de cette distinction est 
que l'information fournie par cette approche est un proxy du profil de l'utilisateur _dans le portail_, et 
que cela peut ne pas être (cela n'est pas souvent) la même chose que l'identité de l'utilisateur au sein de l'organisation.

## Exemple d'implémentation

Cette fonctionnalité est basée sur `SoffitApiPreAuthenticatedProcessingFilter` et [Spring Security][].
Les fichiers de classe Java pour cette fonctionnalité sont disponibles dans la dépendance `uPortal-soffit-renderer`.

Le `SoffitApiPreAuthenticatedProcessingFilter` repose sur un jeton `Bearer` dans l'En-tête HTTP `Authorization`. 
Vous pouvez obtenir un jeton approprié à partir de l'endpoint d'uPortal `userinfo`,  qui se trouve
à l'adresse suivante : `/uPortal/api/v5-1/userinfo`.   Tout utilisateur du portail (y compris les utilisateurs 
non authentifiés) peut obtenir un jeton personnel à partir de cet endpoint. Ce jeton doit être obtenu 
avec JavaScript et transmis (également avec JavaScript) en tant que jeton `Bearer` dans l'en-tête` Authorization` 
des requètes sur l'API.

Reportez-vous aux sections ci-dessous pour sécuriser les endpoints d'API et accéder aux informations sur l'utilisateur du portail.

### Dépendance `uPortal-soffit-renderer` (Exemple Gradle)

```groovy
compile "org.jasig.portal:uPortal-soffit-renderer:${uPortalVersion}"
```

### Configuration Spring Security 

Cet exemple définit les règles suivantes pour accéder aux endpoints d'API commençant par `/api`: 
  - Les utilisateurs authentifiés peuvent envoyer des requêtes `GET` et `POST` 
  - Aucun utilisateur ne peut envoyer d'autre type de requête.

```java
@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Value("${" + SIGNATURE_KEY_PROPERTY + ":" + DEFAULT_SIGNATURE_KEY + "}")
    private String signatureKey;

    @Override
    public void configure(WebSecurity web) throws Exception {
        /*
         * Since this module includes portlets, we only want to apply Spring Security to requests
         * targeting our REST APIs.
         */
        final RequestMatcher pathMatcher = new AntPathRequestMatcher("/api/**");
        final RequestMatcher inverseMatcher = new NegatedRequestMatcher(pathMatcher);
        web.ignoring().requestMatchers(inverseMatcher);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        /*
         * Provide a SoffitApiPreAuthenticatedProcessingFilter (from uPortal) that is NOT a
         * top-level bean in the Spring Application Context.
         */
        final AbstractPreAuthenticatedProcessingFilter filter =
                new SoffitApiPreAuthenticatedProcessingFilter(signatureKey);
        filter.setAuthenticationManager(authenticationManager());

        http
            .addFilter(filter)
            .authorizeRequests()
                .antMatchers(HttpMethod.GET,"/api/**").authenticated()
                .antMatchers(HttpMethod.POST,"/api/**").authenticated()
                .antMatchers(HttpMethod.DELETE,"/api/**").denyAll()
                .antMatchers(HttpMethod.PUT,"/api/**").denyAll()
                .anyRequest().permitAll()
            .and()
            /*
             * Session fixation protection is provided by uPortal.  Since portlet tech requires
             * sessionCookiePath=/, we will make the portal unusable if other modules are changing
             * the sessionId as well.
             */
            .sessionManagement()
                .sessionFixation().none();

    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new SoffitApiAuthenticationManager();
    }

    @Bean
    public ErrorPageFilter errorPageFilter() {
        return new ErrorPageFilter();
    }

    @Bean
    public FilterRegistrationBean disableSpringBootErrorFilter() {
        /*
         * The ErrorPageFilter (Spring) makes extra calls to HttpServletResponse.flushBuffer(),
         * and this behavior produces many warnings in the portal logs during portlet requests.
         */
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(errorPageFilter());
        filterRegistrationBean.setEnabled(false);
        return filterRegistrationBean;
    }

}
```

### Accès aux informations utilisateur dans l'API

Les informations sur l'utilisateur peuvent être obtenues à partir du Spring `SecurityContextHolder` :

```java
final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
final String username = (String) authentication.getPrincipal();
final Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
```

[Spring Security]: https://projects.spring.io/spring-security/
