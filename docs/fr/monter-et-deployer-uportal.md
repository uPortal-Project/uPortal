# Build uPortal

uPortal utilise Gradle pour la configuration du projet et comme système de montage. uPortal est fourni avec un Wrapper Gradle si vous ne souhaitez pas installer cet outil (`./gradlew` dans le dossier root du repo).

## uPortal-start

Sauf si vous avez une raison spécifique de construire uPortal directement depuis
ce repo, [uPortal-start](https://github.com/uPortal-project/uPortal-start) est recommandé pour déployer uPortal. En utilisant uPortal-start, vous disposez de tâches gradle qui permettent l'import et la consommation de données xml, utilisées pour personnaliser uPortal.

## Installation d'uPortal sur un Maven Local

Pour faire des modifications dans le code source et vouloir les tester localement,
faite un build d'uPortal et stockez les binary dans votre repo local Maven. Vous pouvez le faire depuis le dossier root d'uPortal :

```bash
./gradlew install
```

## Tâches Gradle

Pour une liste exhaustive des tâches Gradle disponible, faire depuis le dossier root.

```bash
./gradlew tasks
```
