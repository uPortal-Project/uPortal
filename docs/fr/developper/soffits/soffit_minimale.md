# Soffit Minimale

L'interactivité d'une soffit avec le portail est basée sur HTTP. Il est possible d'écrire une
soffit dans n'importe quel langage ou plate-forme pouvant accepter, traiter et répondre à une
connexion via HTTP. À l'heure actuelle, les créateurs de Soffit s'attendent à
développer les soffits principalement avec [Java][] et [Spring Boot][].

## Minimal Soffit Setup Instructions Using Spring Boot

1.  Utilisez le [Spring Initializer][] pour créer un nouveau projet Spring Boot avec le paramétrage suivant :

    * Gradle Project (recommandé)
    * Packaging=*War* (recommandé)
    * Dependencies=*Cache* (recommandé) & *Web* (requis)
    * Additional dependencies you intend to use (optional -- you can add them later)

    Une fois prêt, cliquer sur le bouton `Generate Project` et télécharger les sources de votre projet en `.tar.gz` ou en `.zip`. 
    Décompresser et copier le contenu de l'archive dans un endroit adéquat de votre système de fichiers. 
    Ouvrir les fichiers de votre projet dans un outil ou éditeur, comme [IntelliJ IDEA][],
    [Eclipse][], ou possiblement [Atom][].
2.  Ajouter Soffit comme une dépendence de votre projet (voir _Ajout de la dépendance Soofit_ ci-dessous)
3.  Ajouter la dépendence `tomcat-embed-jasper` à votre projet (voir _Ajout de la dépendence `tomcat-embed-jasper`_ ci-dessous)
4.  Ajouter l'annotation `@SoffitApplication` à la application class (celle déjà annotée avec `@SpringBootApplication`) **NOTE:**  n'oubliez pas d'ajouter `import org.apereo.portal.soffit.renderer.SoffitApplication;` de manière appropriée en haut du fichier.
5.  Créer le répertoire `src/main/webapp/WEB-INF/soffit/`
6.  Choisisser un nom pour votre soffit et créer un répertoire avec ce nom à l'intérieur
    `/soffit/` (ci-dessus); recommandé: utiliser uniquement des lettres minuscules et des tirets
    ('-') dans le nom
7.  Créer un fichier `view.jsp` dans le répertoire nommé pour votre soffit; ajouter
    votre balisage (_par exemple_ `<h2> Hello World! </ h2>`)
8.  Dans `src/main/resources/application.properties`, définir la propriété `server.port`
    et attribuer la sur un port inutilisé (comme 8090)
9.  Lancer la commande `$ ./gradlew assemble` (sur \*-nix) ou `$ gradlew.bat assemble`
    (sur Windows) pour builder votre application
10. Lancer la commande `$ java -jar build/libs/{filename}.war` pour lancer votre
    application

C'est tout! Vous avez maintenant une application Soffit minimale fonctionnant sur
`localhost` sur le port` server.port`.

### Ajout de la dépendance Soffit

Vous devrez modifier le fichier de construction du projet dans l'éditeur de votre choix.
**NOTE :** assurez-vous de spécifier la version de dépendance correcte; Il se peut que cela ne soit
plus `5.0.0-SNAPSHOT` au moment où vous lisez ce guide.

Exemple Gradle (`build.gradle`):

``` gradle
repositories {
    mavenLocal()  // Add this line if not already present!
    mavenCentral()
}

[...]

compile('org.jasig.portal:uPortal-soffit-renderer:5.0.0-SNAPSHOT')
```

Exemple Maven (`pom.xml`):

``` xml
<dependency>
    <groupId>org.jasig.portal</groupId>
    <artifactId>uPortal-soffit-renderer</artifactId>
    <version>5.0.0-SNAPSHOT</version>
</dependency>
```

### Ajout de la dépendance `tomcat-embed-jasper`

Vous devrez modifier le fichier de Build du projet dans l'éditeur de votre choix.

Exemple Gradle (`build.gradle`):

``` gradle
configurations {
    providedRuntime  // Add this line if not already present!
}

[...]

providedRuntime('org.apache.tomcat.embed:tomcat-embed-jasper')
```

Exemple Maven (`pom.xml`):

``` xml
<dependency>
    <groupId>org.apache.tomcat.embed</groupId>
    <artifactId>tomcat-embed-jasper</artifactId>
    <scope>provided</scope>
</dependency>
```

[Java]: http://www.oracle.com/technetwork/java/index.html
[Spring Boot]: http://projects.spring.io/spring-boot/
[Spring Initializer]: https://start.spring.io/
[IntelliJ IDEA]: https://www.jetbrains.com/idea/
[Eclipse]: https://eclipse.org/ide/
[Atom]: https://atom.io/
