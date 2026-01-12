# Devoteam Interview — Login Anomaly Detector (Spring Boot + Couchbase + ML simple)

Ce projet est une petite démo de détection d’anomalies de connexion.  
L’idée : on enregistre des événements de login (IP, pays, device, date/heure), puis on calcule quelques indicateurs sur l’historique récent d’un utilisateur pour estimer un **score de risque**. L’API renvoie ensuite une décision **ALLOW / BLOCK**.

---

## Ce que fait l’application

### 1) Ingestion d’un événement de login
À chaque login, l’API reçoit un payload contenant par exemple :
- `userId` : identifiant utilisateur
- `ip` : IP de connexion
- `country` : pays détecté
- `deviceId` : identifiant du device (ou navigateur/app)
- `timestamp` : date au format ISO 8601 (optionnelle : si vide, le serveur prend “now”)

L’événement est ensuite stocké dans Couchbase.

### 2) Calcul des “features”
Quand on demande un score de risque, le service relit les événements récents et calcule des features simples, par exemple :
- **distinctIp10m** : nombre d’IPs différentes sur les 10 dernières minutes
- **distinctCountries30m** : nombre de pays différents sur les 30 dernières minutes
- **eventsPerMin30m** : rythme moyen d’événements sur 30 minutes
- **deviceChanges30m** : nombre de changements de device sur 30 minutes
- **impossibleTravel** : indicateur “trajet impossible” (pays A puis pays B trop vite)

Objectif : rester lisible, compréhensible, et facile à faire évoluer.

### 3) Scoring (régression logistique)
Les features passent dans un modèle léger (régression logistique) qui renvoie une probabilité entre 0 et 1 :
- proche de 0 → comportement normal
- proche de 1 → comportement suspect

Ensuite on applique un seuil (ex: 0.7) pour décider :
- **ALLOW** si le risque est faible
- **BLOCK** si le risque est élevé  
(Optionnel : ajouter un mode **REVIEW**)

---

## Stockage Couchbase

### Structure utilisée
- **bucket** : `security`
- **scope** : `_default`
- **collection** : `login`
  
## Lancer en local avec Docker Compose (recommandé)

Le plus simple est d’utiliser **Docker Compose** pour démarrer Couchbase + l’application.

### Prérequis
- Docker + Docker Compose
- 

## Notes / améliorations possibles

Index N1QL dédiés (userId + tsEpoch) pour accélérer les requêtes.

Ajout d’un mode REVIEW et d’un audit trail (ex: log des décisions).

Tests plus complets (edge cases) + tests d’intégration avec Testcontainers.

Monitoring (actuator metrics, traces, etc.).

Tests plus complets (edge cases) + tests d’intégration avec Testcontainers.

Monitoring (actuator metrics, traces, etc.).
