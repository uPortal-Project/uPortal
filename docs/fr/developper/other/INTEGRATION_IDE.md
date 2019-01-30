# Integration IDE

Ces scripts génèrent automatiquement la configuration du projet pour les éditeurs Intellij et Eclipse.
L'utilisation de ces scripts supprime le besoin de faire un import et une configuration personnalisée pour intégrer uPortal à l'Environnement de Développement Intégré (EDI) (Integrated Development Environment IDE).

## Intellij

1. Ouvrir un terminal
2. `cd` jusqu'au dossier uPortal
3. Lancer
```sh
./gradlew idea
```
4. Ouvrir intellij
5. Aller sur la page d'accueil
6. Selectionner Ouvrir
![open in intellij](../../../images/intellij_open.png)
7. Naviguer vers le dossier uPortal
8. Ouvrir le dossier

## Eclipse

1. Ouvrir un terminal
2. `cd` jusqu'au dossier uPortal
3. Lancer
```sh
./gradlew eclipse
```
4. Ouvrir eclipse
5. Sélectionner file > import
6. Rechercher  "Existing Projects into Workspace"
![open in eclipse](../../../images/eclipse_import.png)
7. Sélectionner le dossier uPortal comme dossier racine
8. Sélectionner l'option "Recherche les projets imbriqués"
9. L'ouvrir
