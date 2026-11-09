# Testora-Desktop-API-Testing-Monitoring-Platform
A Java desktop application for API testing, automated monitoring, performance analysis and response validation, featuring concurrent request execution and historical reporting.


# Cahier des charges Testora

**Plateforme Desktop de Test, Surveillance et Analyse des API**

---

## 1. Présentation Générale du Projet

### 1.1 Contexte

Ce projet s’inscrit dans le cadre d’un projet académique de développement logiciel en Java. Il vise à concevoir et développer **Testora**, une application desktop destinée aux développeurs, permettant de créer, exécuter, tester et surveiller des requêtes vers des API REST.

L’application centralise la préparation des requêtes HTTP, l’organisation des collections, l’exécution de tests automatisés, le suivi périodique de la disponibilité des endpoints et l’analyse des performances. Les résultats et historiques sont conservés dans une base de données afin de faciliter le diagnostic et le suivi de la qualité des API.

### 1.2 Objectif

L’objectif principal est de créer une application desktop Java capable de :

- Créer, configurer et envoyer des requêtes HTTP (**GET, POST, PUT, PATCH et DELETE**) vers des API REST.
- Organiser les requêtes dans des collections et gérer plusieurs environnements (développement, test, production).
- Exécuter des scénarios de tests automatisés avec des assertions sur le statut HTTP, les en-têtes, le contenu JSON et le temps de réponse.
- Surveiller périodiquement les endpoints, détecter les échecs et analyser les performances à partir de l’historique des exécutions.
- Consulter et exporter les résultats et rapports en CSV et JSON.

### 1.3 Technologies Utilisées

| Composant | Technologie |
|---|---|
| Langage | Java 21 (ou version LTS compatible) |
| Interface desktop | JavaFX (FXML et CSS si nécessaire) |
| Communication HTTP | Java HttpClient |
| Base de données | PostgreSQL |
| Accès aux données | JDBC |
| Traitement JSON | Jackson |
| Concurrence et planification | ExecutorService / ScheduledExecutorService |
| Build et dépendances | Maven |
| Tests | JUnit 5 / Mockito |
| IDE | Eclipse IDE |

---

## 2. Besoins Fonctionnels

### 2.1 Gestion de l’Espace de Travail & Configuration

- Créer et gérer des collections de requêtes API avec un nom et une description.
- Configurer des environnements et leurs variables (URL de base, paramètres et valeurs propres à chaque environnement).
- Gérer les paramètres d’une requête : méthode HTTP, URL, paramètres, en-têtes, authentification et corps de requête.
- Modifier, dupliquer, supprimer et réorganiser les requêtes et collections.
- Réutiliser les variables d’environnement dans les URL, en-têtes et corps des requêtes.

### 2.2 Construction & Exécution des Requêtes API

- Prendre en charge les méthodes HTTP GET, POST, PUT, PATCH et DELETE.
- Configurer les paramètres de requête, les en-têtes HTTP, le corps (JSON ou texte) et les mécanismes d’authentification usuels (Bearer Token, Basic Auth).
- Envoyer les requêtes via **Java HttpClient** et afficher le code de statut, les en-têtes, le corps de réponse et la durée d’exécution.
- Analyser et présenter les réponses JSON de manière lisible.
- Gérer les erreurs réseau, délais d’attente, réponses invalides et codes HTTP d’erreur.
- Permettre l’annulation d’une exécution lorsque cela est techniquement possible.

### 2.3 Tests Automatisés — Cœur du Système

- Créer des cas de test associés aux requêtes et définir des assertions sur le code HTTP, les en-têtes, les champs JSON et le temps de réponse.
- Exécuter un cas de test ou une collection complète et produire un résultat détaillé : réussi, échoué ou en erreur.
- Permettre l’enchaînement de plusieurs tests et la réutilisation des collections pour les campagnes de validation d’API.
- Détecter les écarts entre les résultats attendus et les réponses reçues, puis conserver les détails des échecs pour faciliter le diagnostic.
- Afficher un résumé d’exécution : nombre de tests réussis, échoués et en erreur.

### 2.4 Surveillance & Analyse des Performances

- Configurer des vérifications périodiques pour surveiller la disponibilité des endpoints selon un intervalle défini.
- Détecter les indisponibilités, les erreurs HTTP et les dépassements de délai, puis signaler les changements d’état.
- Mesurer les temps de réponse et calculer des indicateurs de performance : minimum, maximum, moyenne et percentiles (P50, P95 et P99) lorsque le volume de mesures le permet.
- Exécuter plusieurs contrôles en parallèle dans des limites configurables.
- Consulter l’évolution des temps de réponse et de la disponibilité à travers des graphiques et des statistiques.
- Distinguer les tests fonctionnels classiques des exécutions de charge contrôlées.

### 2.5 Historique, Rapports & Notifications

- Enregistrer l’historique des requêtes, des campagnes de tests et des contrôles de surveillance dans Mongo
- Consulter les résultats passés, filtrer l’historique et comparer les exécutions afin d’identifier les régressions.
- Exporter les résultats et rapports en formats CSV et JSON.
- Afficher des notifications dans l’application lors d’échecs de tests, d’indisponibilités ou de changements d’état d’un endpoint.
- Journaliser les événements techniques importants et les erreurs afin de faciliter le diagnostic.
- Permettre la suppression contrôlée des anciens historiques selon les paramètres de conservation.

---

## 3. Besoins Non Fonctionnels

### 3.1 Performance & Fiabilité

- L’interface doit rester réactive pendant l’exécution des requêtes grâce à l’utilisation de tâches asynchrones et de pools de threads.
- L’application doit permettre l’exécution contrôlée de plusieurs requêtes en parallèle, avec des limites configurables, des délais d’attente et une possibilité d’annulation.
- Les opérations de base de données et les traitements longs ne doivent pas bloquer le thread JavaFX.
- Les erreurs d’exécution doivent être capturées et présentées sans provoquer la fermeture inattendue de l’application.
- Les métriques statistiques doivent être calculées à partir des mesures réellement collectées.

### 3.2 Sécurité

- Utiliser HTTPS/TLS lorsque l’API cible le prend en charge et ne jamais désactiver automatiquement la validation des certificats.
- Protéger les informations sensibles (tokens et identifiants) : éviter leur affichage dans les logs et privilégier un stockage local chiffré ou le magasin sécurisé du système.
- Utiliser des requêtes JDBC préparées, valider les entrées et gérer les erreurs de base de données.
- Ne pas enregistrer les mots de passe ou tokens en clair dans les exports, journaux ou historiques.
- Informer l’utilisateur avant les opérations potentiellement risquées, notamment les tests de charge ou l’envoi de requêtes vers un environnement de production.

### 3.3 Utilisabilité & Maintenabilité

- Proposer une interface desktop claire et intuitive, organisée autour des requêtes, collections, tests, surveillance et historiques.
- Afficher des messages d’erreur compréhensibles et des indicateurs visuels pour les états des requêtes et des tests.
- Séparer les responsabilités entre interface, services applicatifs, moteurs d’exécution, accès aux données et modèles.
- Faciliter l’évolution du logiciel grâce à une architecture modulaire, des tests unitaires et une documentation technique.
- Assurer la compatibilité avec les systèmes supportant la version Java et JavaFX retenues pour le projet.

---

## 4. Les Acteurs (leurs rôles)

| Acteur | Rôles et Responsabilités |
|---|---|
| **Développeur / Utilisateur** | - Créer, modifier et organiser les requêtes API et les collections.<br>- Configurer les environnements, paramètres, en-têtes et authentifications.<br>- Exécuter les requêtes et campagnes de tests automatisés.<br>- Consulter les réponses, résultats, métriques et historiques.<br>- Configurer la surveillance des endpoints et consulter les alertes. |
| **Responsable de l’application** | - Configurer la connexion à la base de données et les paramètres généraux.<br>- Gérer les collections, environnements et données locales de l’application.<br>- Contrôler les limites d’exécution parallèle et les intervalles de surveillance.<br>- Consulter les journaux techniques, sauvegarder et restaurer les données.<br>- Veiller à la protection des tokens et informations sensibles. |

---

## 5. Périmètre et contraintes techniques

- **Nature du produit :** application desktop, conçue comme un monolithe modulaire.
- **Langage principal :** Java.
- **Interface :** JavaFX.
- **Communication avec les API :** Java HttpClient.
- **Persistance :** Mongo db
- **Architecture :** séparation entre présentation, services applicatifs, moteurs métier et infrastructure.
- **Exécution concurrente :** `ExecutorService` pour les tâches parallèles et `ScheduledExecutorService` pour la surveillance périodique.
- **Environnement de développement :** Eclipse IDE et Maven.
- **Contrainte de périmètre :** Testora consomme des API externes depuis le poste de l’utilisateur ; l’application n’expose pas son propre backend web.
