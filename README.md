# Login Anomaly Detector (Spring Boot + Couchbase + ML simple)

Ce projet est une petite démo de détection d’anomalies de connexion.  
L’idée est simple : on enregistre des événements de login (IP, pays, device, date/heure), puis on calcule quelques indicateurs sur l’historique récent d’un utilisateur pour estimer un **score de risque**. À la fin, l’API renvoie une décision **ALLOW / BLOCK**.

---

## Ce que fait l’application

### 1) Ingestion d’un événement de login
À chaque login, l’API reçoit un payload contenant par exemple :
- `userId` : identifiant utilisateur
- `ip` : IP de connexion
- `country` : pays détecté
- `deviceId` : identifiant du device (ou navigateur/app)
- `timestamp` : date ISO (optionnelle si tu veux laisser le serveur mettre “now”)

L’événement est ensuite stocké dans Couchbase.

### 2) Calcul des “features”
Quand on demande un score de risque pour un utilisateur, le service relit les événements récents et calcule des features basiques, par exemple :
- **distinctIp10m** : combien d’IPs différentes sur les 10 dernières minutes
- **distinctCountries30m** : combien de pays différents sur les 30 dernières minutes
- **eventsPerMin30m** : rythme moyen d’événements (activité anormale si trop rapide)
- **deviceChanges30m** : nombre de changements de device récemment
- **impossibleTravel** : indicateur “ça n’a pas de sens” (ex: pays A puis pays B trop vite)

L’objectif n’est pas d’être parfait, mais d’avoir des signaux simples, lisibles et faciles à améliorer.

### 3) Scoring (régression logistique)
Les features passent dans un modèle léger (régression logistique).  
Le modèle renvoie une probabilité entre 0 et 1 :
- proche de 0 → comportement normal
- proche de 1 → comportement suspect

Ensuite on applique un seuil (par exemple 0.7) pour décider :
- **ALLOW** si le risque est faible
- **BLOCK** si le risque est élevé  
(ou **REVIEW** si tu veux ajouter un mode “à vérifier”)

---

## Stockage Couchbase

### Structure utilisée
- **bucket** : `security`
- **scope** : `_default`
- **collection** : `login`

### Exemple de document enregistré
```json
{
  "_class": "tn.devoteam.demo.detector.data.LoginEventDocument",
  "country": "TN",
  "deviceId": "d1",
  "ip": "10.0.0.4",
  "tsEpoch": 1768252081,
  "type": "login_event",



## Déploiement sur AWS (Option A — ECS Fargate + Couchbase Capella)

### Prérequis
- Un compte AWS + AWS CLI configuré
- Docker installé
- Un cluster Couchbase Capella (sur AWS)
- (Optionnel) Un nom de domaine (Route 53) + certificat (ACM)

### Étapes (vue d’ensemble)
1. **Conteneuriser l’application**
   - Ajouter un `Dockerfile` et s’assurer que l’app lit la config Couchbase via des variables d’environnement.

2. **Publier l’image sur Amazon ECR**
   - Créer un repository ECR.
   - Builder l’image Docker puis la pousser sur ECR.

3. **Préparer Couchbase Capella**
   - Créer le cluster + un utilisateur.
   - Créer le bucket/scope/collection utilisés par l’app.
   - Autoriser l’accès réseau depuis AWS (selon votre configuration).

4. **Déployer sur ECS Fargate**
   - Créer un cluster ECS.
   - Créer une *Task Definition* (image ECR + port + variables d’environnement / secrets).
   - Créer un *Service* Fargate (1 instance ou plus).

5. **Exposer l’app via un Load Balancer**
   - Créer un ALB.
   - Brancher l’ALB au service ECS (target group + health check).

6. **(Optionnel) HTTPS + domaine**
   - Créer un certificat dans ACM.
   - Configurer un listener HTTPS sur l’ALB.
   - Pointer le domaine vers l’ALB via Route 53.

7. **Logs & suivi**
   - Envoyer les logs vers CloudWatch.
   - Vérifier l’état du service et les health checks.

  "userId": "u123"
}
