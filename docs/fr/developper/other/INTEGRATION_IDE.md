# Integration IDE

Ces scripts génèrent automatiquement la configuration du projet pour les éditeurs Intellij et Eclipse.
L'utilisation de ces scripts supprime le besoin d'exécuter une importation et une configuration personnalisées pour intégrer uPortal à l'environnement de développement intégré ( Integrated Development Environment IDE).

## Intellij

1. Ouvrez un terminal
2. `cd` jusqu'au dossier uPortal
3. Lancez
```sh
./gradlew idea
```
4. Ouvrez intellij
5. Allez sur la page d'accueil
6. Selectionnez Ouvrir
![open in intellij](../../images/intellij_open.png)
7. Naviguez vers le dossier uPortal
8. Ouvrez le

## Eclipse

1. Ouvrez un terminal
2. `cd` jusqu'au dossier uPortal
3. Lancez
```sh
./gradlew eclipse
```
4. Ouvrez eclipse
6. Selectionnez file > import
7. Recherchez  "Existing Projects into Workspace"
![open in eclipse](../../images/eclipse_import.png)
7. Naviguez vers le dossier uPortal
8. Ouvrez le
