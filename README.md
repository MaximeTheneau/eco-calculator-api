# Eco Calculator API

API REST en Java / Spring Boot qui estime l'empreinte carbone d'une page web (poids de la page → énergie consommée → CO2 émis), avec historique des calculs en base MySQL.

Approche inspirée de la méthodologie publique **Sustainable Web Design** (sustainablewebdesign.org), utilisée entre autres par Website Carbon. Il s'agit d'une approximation basée sur des moyennes d'industrie, pas d'une reproduction exacte d'un outil existant.

## Stack technique

- Java 21, Spring Boot 4.1.1, Maven
- Spring Web, Spring Data JPA (Hibernate)
- MySQL 8.4
- Docker / Docker Compose

## Structure du projet

```
src/main/java/com/ecocalculatorapi/
├── controller/    # Endpoints REST
├── service/       # Logique métier (calcul carbone)
├── repository/     # Accès aux données (Spring Data JPA)
├── entity/        # Entités JPA
├── dto/           # Objets d'échange (requêtes/réponses)
├── exception/     # Gestion globale des erreurs
└── security/      # Filtre de clé API
```

## Prérequis

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (ou OrbStack) installé et lancé
- Java 21 et Maven, uniquement si tu veux lancer le projet sans Docker

## Installation

**1. Cloner le projet**
```bash
git clone <url-du-repo>
cd eco-calculator-api
```

**2. Configurer les variables d'environnement**

Le fichier `.env.example` liste les variables nécessaires, sans valeurs (c'est le seul fichier de config versionné dans Git). Copie-le en `.env.local` et remplis-le avec tes propres valeurs :

```bash
cp .env.example .env.local
```

Puis édite `.env.local` :
```
DB_USER=eco
DB_PASSWORD=<choisis-un-mot-de-passe-fort>
DB_ROOT_PASSWORD=<choisis-un-autre-mot-de-passe-fort>
CUSTOM_API_KEY=<génère-une-clé-aléatoire>
```

Pour générer une clé API aléatoire :
```bash
openssl rand -hex 32
```

⚠️ `.env.local` contient des secrets et ne doit **jamais** être commité. Il est déjà listé dans `.gitignore`.

**3. Lancer l'application**

Comme les valeurs sont dans `.env.local` (et non `.env`), précise le fichier à Docker Compose :
```bash
docker compose --env-file .env.local up --build
```

L'API démarre sur `http://localhost:8080`.

## Endpoints

Toutes les routes `/api/**` nécessitent l'en-tête `X-API-KEY` (valeur définie dans `CUSTOM_API_KEY`).

| Méthode | Route | Description |
|---|---|---|
| GET | `/api/calculate?url=...&greenHosting=false` | Calcule l'empreinte carbone d'une page |
| GET | `/api/history` | Liste les calculs déjà effectués |

**Exemple**
```bash
curl -H "X-API-KEY: ta-clé" \
  "http://localhost:8080/api/calculate?url=https://example.com"
```

Réponse :
```json
{
  "url": "https://example.com",
  "pageSizeBytes": 1256,
  "co2GramsPerVisit": 0.001,
  "co2GramsPerYear": 1.2,
  "greenHosting": false,
  "rating": "A+"
}
```

## Gestion des erreurs

Toutes les erreurs renvoient un JSON homogène :
```json
{
  "timestamp": "2026-09-29T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "URL invalide : ...",
  "path": "/api/calculate"
}
```

| Cas | Code HTTP |
|---|---|
| URL invalide ou vide | 400 |
| Clé API manquante ou incorrecte | 401 |
| Site cible injoignable | 502 |
| Timeout vers le site cible | 504 |

## Limites connues

- Seul le document HTML principal est mesuré, pas les images, CSS ou JS qu'un navigateur chargerait en plus. Une mesure du poids complet de la page nécessiterait un navigateur headless (ex. Playwright).
- Le nombre de visites mensuelles utilisé pour l'estimation annuelle (10 000) est une hypothèse fixe, pas une donnée réelle.

## Commandes utiles

```bash
# Arrêter les conteneurs
docker compose down

# Arrêter et supprimer les données de la base
docker compose down -v

# Rebuild sans cache
docker compose --env-file .env.local build --no-cache

# Voir les logs de l'API
docker compose logs -f api
```

## Pistes d'évolution

- Mesure du poids complet de la page (navigateur headless)
- Rate limiting par clé API
- Frontend simple pour saisir une URL et afficher le résultat
- Tests unitaires et d'intégration (JUnit, MockMvc)
