# Platformă web de gestiune a unei firme de fotografie

Aplicație web monolitică Spring Boot pentru planificarea ședințelor foto/video, gestionarea clienților, locațiilor, echipamentelor, facturilor și statisticilor unei firme de fotografie.

## Cerințele sistemului

1. Gestionarea clienților cu detaliile lor de bază (nume, email, telefon)
2. Gestionarea locațiilor cu nume și, opțional, detalii de geolocalizare
3. Crearea și actualizarea ședințelor foto/video cu titlu, programare, notițe și status
4. Atribuirea fiecărei ședințe unui fotograf
5. Asocierea opțională a echipamentelor necesare la ședințe pentru planificare mai eficientă
6. Atașarea elementelor media (fotografii/video) la o ședință și actualizarea metadatelor
7. Crearea și actualizarea unei facturi pentru fiecare ședință (fiecare factură are un singur client)
8. Urmărirea statusului plății și a sumelor pentru facturi
9. Obținerea detaliilor unei ședințe, fișierele media asociate și detaliile facturii
10. Obținerea statisticilor pentru un interval de timp setat de utilizator (venituri încasate, sume restante, ședințe planificate/finalizate)

## Tehnologii


| Layer        | Tehnologie                                    |
| ------------ | --------------------------------------------- |
| Backend      | Spring Boot 4, Java 21, Spring MVC            |
| Persistență  | Spring Data JPA, Hibernate                    |
| Baze de date | H2 (in-memory), PostgreSQL 16                 |
| Securitate   | Spring Security, BCrypt, roluri ADMIN / CLIENT |
| UI           | Thymeleaf, Bootstrap 5 (WebJars)              |
| Mapping      | MapStruct, Bean Validation                    |
| Build        | Gradle                                        |
| Containere   | Docker Compose (PostgreSQL)                   |


## Arhitectură

Aplicația urmează o arhitectură în straturi:

```
Browser (Thymeleaf)
       ↓
Controller  — rute HTTP, formulare, validare @Valid
       ↓
Service     — logică de business, tranzacții
       ↓
Repository  — Spring Data JPA
       ↓
Entity      — model relațional
```

DTO-urile (`request` / `response`) și mapper-ele MapStruct separă entitățile JPA de datele expuse în UI.

### Pachete principale


| Pachet         | Rol                                        |
| -------------- | ------------------------------------------ |
| `controller`   | Endpoints MVC                              |
| `service`      | Business logic                             |
| `repository`   | Acces la date                              |
| `model.entity` | Entități JPA                               |
| `model.dto`    | Formulare și răspunsuri                    |
| `config`       | Security, Web, profiluri                   |
| `exception`    | Excepții custom + `GlobalExceptionHandler` |


## Model de date (ER)

### Entități (8)


| Entitate    | Descriere                                  |
| ----------- | ------------------------------------------ |
| `User`      | Utilizator autentificat (admin, fotograf sau cont client) |
| `Authority` | Rol (`ROLE_ADMIN`, `ROLE_CLIENT`)          |
| `Client`    | Client al firmei                           |
| `Location`  | Locație de shoot (cu geolocație opțională) |
| `GearItem`  | Echipament foto deținut de un fotograf     |
| `Shoot`     | Ședință foto/video                         |
| `Media`     | Metadata media atașată unei ședințe        |
| `Invoice`   | Factură pentru o ședință                   |


### Relații JPA


| Tip                         | Relație                                                                                                                                 |
| --------------------------- | --------------------------------------------------------------------------------------------------------------------------------------- |
| `@OneToOne`                 | `Shoot` ↔ `Invoice`, `User` ↔ `Client` (cont portal client)                                                                             |
| `@OneToMany` / `@ManyToOne` | `Shoot` → `Media`, `Shoot` → `Location`, `Shoot` → `User` (fotograf), `Shoot` → `Client`, `Invoice` → `Client`, `GearItem` → `User` |
| `@ManyToMany`               | `Shoot` ↔ `GearItem`, `User` ↔ `Authority`                                                                                              |


### Diagramă ER

```mermaid
erDiagram
    USER ||--o{ GEAR_ITEM : owns
    USER }o--o{ AUTHORITY : has
    USER ||--o{ SHOOT : photographs
    USER ||--|| CLIENT : account
    CLIENT ||--o{ SHOOT : books
    LOCATION ||--o{ SHOOT : hosts
    CLIENT ||--o{ INVOICE : billed
    SHOOT ||--|| INVOICE : has
    SHOOT ||--o{ MEDIA : contains
    SHOOT }o--o{ GEAR_ITEM : uses

    USER {
        bigint id PK
        string username
        string password
    }
    AUTHORITY {
        bigint id PK
        string role
    }
    CLIENT {
        bigint id PK
        string name
        string email
        string phone
        bigint user_id FK
    }
    LOCATION {
        bigint id PK
        string name
        double latitude
        double longitude
    }
    GEAR_ITEM {
        bigint id PK
        string brand
        string model
        string type
    }
    SHOOT {
        bigint id PK
        string title
        string status
        datetime start_at
    }
    MEDIA {
        bigint id PK
        string media_type
        string file_ref
    }
    INVOICE {
        bigint id PK
        decimal amount
        string status
    }
```



## Medii și profiluri Spring

Conform cerinței proiectului (Lab 2): **2 profiluri Spring** cu **2 baze de date diferite** și fișiere de configurare separate.

| Profil | Fișier | Bază de date | Utilizare |
|--------|--------|--------------|-----------|
| `test` | `application-test.yml` | H2 in-memory | Testare automată și rulare rapidă locală |
| `dev` | `application-dev.yml` | PostgreSQL 16 (Docker) | Dezvoltare / mediu apropiat de producție |

Profilul implicit la pornire (fără argumente): **`test`**.

Scripturile SQL (`schema-h2.sql`, `schema-postgresql.sql`) creează schema la pornire. `DataLoader` inserează utilizatorii impliciți dacă baza este goală.

## Cerințe

- **JDK 21**
- **Gradle** (wrapper inclus: `gradlew` / `gradlew.bat`)
- **Docker** (opțional, doar pentru profilul `dev`)

## Rulare locală

### 1. Profil `test` — H2 (implicit, fără Docker)

```bash
./gradlew bootRun --args='--spring.profiles.active=test'
```

Windows (PowerShell):

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=test"
```

Sau simplu (profil implicit):

```powershell
.\gradlew.bat bootRun
```


| Câmp     | Valoare              |
| -------- | -------------------- |
| JDBC URL | `jdbc:h2:mem:testdb` |
| User     | `sa`                 |
| Password | *(gol)*              |


### 2. Profil `dev` — PostgreSQL (Docker)

Pornește doar PostgreSQL:

```bash
docker compose up -d postgres
```

Rulează aplicația:

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

Windows (PowerShell):

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=dev"
```

Dacă schema este incompletă sau coruptă, resetează volumul:

```bash
docker compose down -v
docker compose up -d postgres
```

Profilul `dev` a fost testat cu PostgreSQL în Docker (schema + login + CRUD).

### 3. IDE

Configurări incluse în `.run/`:

- `ProiectApplication-Test` — profil `test` (H2)
- `ProiectApplication-Dev` — profil `dev` (PostgreSQL)

## Autentificare


| Utilizator | Parolă   | Rol    | Drepturi                                      |
| ---------- | -------- | ------ | --------------------------------------------- |
| `admin`    | `admin`  | ADMIN  | CRUD complet                                  |
| `client`   | `client` | CLIENT | Vizualizarea ședințelor proprii               |
| `client2`  | `client2`| CLIENT | Vizualizarea ședințelor proprii               |
| `client3`  | `client3`| CLIENT | Vizualizarea ședințelor proprii               |


Utilizatorii neautorizați pentru o acțiune sunt redirecționați către `/access_denied` (403).

## Rute aplicație (MVC)

Aplicația expune pagini Thymeleaf (nu REST JSON). Rolurile provin din Spring Security; filtrarea ședințelor pentru CLIENT (doar cele proprii) se face suplimentar în servicii.

### Autentificare și pagini generale

| Metodă | Rută | Rol | Descriere |
| ------ | ---- | --- | --------- |
| GET | `/` | ADMIN, CLIENT | Dashboard |
| GET | `/login` | public | Formular login |
| POST | `/perform_login` | public | Procesare login (Spring Security) |
| POST | `/logout` | autentificat | Deconectare |
| GET | `/access_denied` | public | Pagină 403 |

### Clienți (admin)

| Metodă | Rută | Rol | Descriere |
| ------ | ---- | --- | --------- |
| GET | `/clients` | ADMIN | Listă paginată (+ sortare) |
| GET | `/clients/{id}` | ADMIN | Detaliu client |
| GET | `/clients/new` | ADMIN | Formular client nou |
| POST | `/clients` | ADMIN | Creare client |
| GET | `/clients/{id}/edit` | ADMIN | Formular editare |
| POST | `/clients/{id}` | ADMIN | Actualizare client |
| POST | `/clients/{id}/delete` | ADMIN | Ștergere client |

### Locații (admin)

| Metodă | Rută | Rol | Descriere |
| ------ | ---- | --- | --------- |
| GET | `/locations` | ADMIN | Listă paginată |
| GET | `/locations/{id}` | ADMIN | Detaliu |
| GET | `/locations/new` | ADMIN | Formular nou |
| POST | `/locations` | ADMIN | Creare |
| GET | `/locations/{id}/edit` | ADMIN | Editare |
| POST | `/locations/{id}` | ADMIN | Actualizare |
| POST | `/locations/{id}/delete` | ADMIN | Ștergere |

### Echipament / gear (admin)

| Metodă | Rută | Rol | Descriere |
| ------ | ---- | --- | --------- |
| GET | `/gear` | ADMIN | Listă paginată |
| GET | `/gear/{id}` | ADMIN | Detaliu |
| GET | `/gear/new` | ADMIN | Formular nou |
| POST | `/gear` | ADMIN | Creare |
| GET | `/gear/{id}/edit` | ADMIN | Editare |
| POST | `/gear/{id}` | ADMIN | Actualizare |
| POST | `/gear/{id}/delete` | ADMIN | Ștergere |

### Ședințe foto (shoots)

| Metodă | Rută | Rol | Descriere |
| ------ | ---- | --- | --------- |
| GET | `/shoots` | ADMIN, CLIENT | Listă (CLIENT: doar ședințele proprii) |
| GET | `/shoots/{id}` | ADMIN, CLIENT | Detaliu ședință |
| GET | `/shoots/new` | ADMIN | Formular ședință nouă |
| POST | `/shoots` | ADMIN | Creare |
| GET | `/shoots/{id}/edit` | ADMIN | Editare |
| POST | `/shoots/{id}` | ADMIN | Actualizare |
| POST | `/shoots/{id}/delete` | ADMIN | Ștergere |

### Media (nested under shoot)

| Metodă | Rută | Rol | Descriere |
| ------ | ---- | --- | --------- |
| GET | `/shoots/{shootId}/media/{mediaId}` | ADMIN, CLIENT | Detaliu media |
| GET | `/shoots/{shootId}/media/new` | ADMIN | Formular media nou |
| POST | `/shoots/{shootId}/media` | ADMIN | Creare |
| GET | `/shoots/{shootId}/media/{mediaId}/edit` | ADMIN | Editare |
| POST | `/shoots/{shootId}/media/{mediaId}` | ADMIN | Actualizare |
| POST | `/shoots/{shootId}/media/{mediaId}/delete` | ADMIN | Ștergere |

### Factură (nested under shoot, admin)

| Metodă | Rută | Rol | Descriere |
| ------ | ---- | --- | --------- |
| GET | `/shoots/{shootId}/invoice/new` | ADMIN | Formular factură |
| POST | `/shoots/{shootId}/invoice` | ADMIN | Creare |
| GET | `/shoots/{shootId}/invoice/edit` | ADMIN | Editare |
| POST | `/shoots/{shootId}/invoice/edit` | ADMIN | Actualizare |
| POST | `/shoots/{shootId}/invoice/delete` | ADMIN | Ștergere |

### Statistici (admin)

| Metodă | Rută | Rol | Descriere |
| ------ | ---- | --- | --------- |
| GET | `/stats` | ADMIN | Statistici pe interval (`from`, `to`) |

### Dezvoltare (profil `test`)

| Metodă | Rută | Rol | Descriere |
| ------ | ---- | --- | --------- |
| GET | `/h2-console/**` | ADMIN | Consolă H2 (doar profil `test`) |

## Validare și gestionarea erorilor

### Validare formulare (server + client)


| Mecanism                                                         | Unde                                                       |
| ---------------------------------------------------------------- | ---------------------------------------------------------- |
| **Bean Validation** (`@NotBlank`, `@NotNull`, `@Email`, `@Size`) | DTO-uri în `model/dto/request/`                            |
| `**@Valid` + `BindingResult`**                                   | Controllere CRUD — la erori se reafișează formularul       |
| `**th:errors` + `is-invalid`**                                   | Template-uri Thymeleaf (`client/form`, `shoot/form`, etc.) |
| **Bootstrap `needs-validation`**                                 | Validare HTML5 în browser înainte de submit                |


Exemplu: la creare client fără nume, mesajul de eroare apare lângă câmp; email invalid este respins de `@Email`.

### Pagini și handler-e de eroare


| Situație                                          | HTTP | View                |
| ------------------------------------------------- | ---- | ------------------- |
| Resursă inexistentă (`ResourceNotFoundException`) | 404  | `error/error.html`  |
| URL invalid / resursă statică lipsă               | 404  | `error/404.html`    |
| Parametru invalid în URL                          | 400  | `error/error.html`  |
| Conflict business / integritate DB                | 409  | `error/error.html`  |
| Acces interzis (Spring Security)                  | 403  | `accessDenied.html` |
| Eroare neașteptată                                | 500  | `error/error.html`  |


`GlobalExceptionHandler` (`@ControllerAdvice`) centralizează excepțiile custom (`BadRequestException`, `DuplicateResourceException`, etc.) și loghează la WARN/ERROR.

Pagina **Stats** validează intervalul de timp în controller: dacă `from` este după `to`, se afișează un alert Bootstrap pe aceeași pagină (fără redirect).

## Capturi de ecran

#### 1. Login

![Login page](docs/screenshots/login.png)

#### 2. Dashboard admin

![Dashboard](docs/screenshots/dashboard.png)

#### 3. Listă clienți

![Lista clienti](docs/screenshots/clients.png)

#### 4. Validări formular

![Validare photo shoot](docs/screenshots/shoot_validation.png)

#### 5. Detalii ședință

![Sedinta](docs/screenshots/sedinta.png)

#### 6. Statistici

![Statistici](docs/screenshots/stats.png)

#### 7. Vizualizare client

Login cu user `client` și parolă `client`: dashboard „My shoots” afișează doar ședințele proprii.

![Vizualizare client](docs/screenshots/guests.png)

#### 8. Acces interzis

![Acces interzis](docs/screenshots/forbidden.png)

#### 9. Eroare ștergere

![Eroare ștergere](docs/screenshots/delete_error.png)

## Logging

Aplicația folosește **SLF4J** cu **Logback** (`logback-spring.xml`).


| Destinație     | Nivel | Fișier           |
| -------------- | ----- | ---------------- |
| Consolă        | INFO  | —                |
| Jurnal general | INFO  | `logs/app.log`   |
| Erori          | ERROR | `logs/error.log` |


În profilul `test`, pachetul `ro.fmi.awbd` este la nivel **DEBUG** (citiri, autentificare, seed). Operațiile CRUD din servicii sunt logate la **INFO**; erorile de business la **WARN**, excepțiile neașteptate la **ERROR**.

Directorul `logs/` este creat automat la pornire. Poți schimba locația cu variabila de mediu `LOG_PATH`.

## Teste

```bash
./gradlew test
./gradlew jacocoTestReport
```

Testele folosesc profilul `test` cu bază de date in-memory izolată (`jdbc:h2:mem:awbd-${random.uuid}`).


| Tip                                       | Locație                                                | Ce verifică                                 |
| ----------------------------------------- | ------------------------------------------------------ | ------------------------------------------- |
| **Unit** (Mockito)                        | `src/test/java/ro/fmi/awbd/service/`                   | Logică de business pentru toate serviciile  |
| **Integrare** (MockMvc + `@WithMockUser`) | `src/test/java/ro/fmi/awbd/integration/` + smoke tests | Securitate roluri, fluxuri MVC, persistență |
| **Coverage**                              | JaCoCo (min. **70%** linii pe servicii)                | `./gradlew jacocoTestCoverageVerification`  |


### Scenarii integrare (MockMvc)

- Utilizator neautentificat este redirecționat la login (`SecurityRouteIntegrationTest`)
- CLIENT poate deschide dashboard și lista propriilor ședințe; nu poate accesa rute admin (`SecurityRouteIntegrationTest`, `ShootControllerIntegrationTest`)
- CLIENT nu poate șterge clienți sau face POST-uri de scriere (`ClientControllerSecurityTest`, `SecurityRouteIntegrationTest`)
- CLIENT vede media doar de la ședințele proprii (`MediaControllerIntegrationTest`)
- ADMIN poate crea client și deschide formulare CRUD (`ClientControllerIntegrationTest`, `ShootControllerIntegrationTest`)
- Stats: ADMIN vede erori de interval invalid; CLIENT nu accesează `/stats` (`StatsControllerIntegrationTest`)

## Structură proiect

```
src/main/java/ro/fmi/awbd/
├── controller/       # MVC
├── service/          # Business logic
├── repository/       # JPA
├── model/            # entity, dto, enums, mapper
├── config/           # Security, Web
└── exception/        # Error handling

src/main/resources/
├── schema-h2.sql
├── schema-postgresql.sql
├── application-test.yml
├── application-dev.yml
├── logback-spring.xml
└── templates/        # Thymeleaf
```

## Contributii echipa

Contribuțiile de mai jos reflectă commit-urile din repository; fiecare membru este responsabil să livreze cod curat, fara regresii, și să verifice manual fluxurile afectate înainte de push.
Chirita George
•	Strat de servicii CRUD pentru entitățile principale
•	Controllere MVC si view-uri Thymeleaf
•	Spring Security: roluri, logout, remember-me, restricții de acces
•	Corecții schema PostgreSQL și integrare
•	Calitate: menținerea stabilității stratului de business și a regulilor de securitate; testare CRUD si autentificare înainte de commit.
Alexandru Mihai
•	Structura inițială a proiectului, dependențe și configurare Spring Security
•	Integrarea modelului client cu cont utilizator (ROLE_CLIENT) si sedinte
•	Formatare date/timp în UI, pagină detaliu media, validări
•	Teste de integrare pentru noile componente și actualizări README
•	Calitate: verificarea fluxurilor end-to-end (admin + client) și a testelor automate pentru modificările de UI și securitate.
Tudor Bogdannn
•	Scheme SQL complete (H2 + PostgreSQL) si seed utilizatori
•	Configurare logging (Logback), suite de teste unitare/integrare, JaCoCo
•	Profiluri Spring dev / test, documentație README (ER, rute, capturi)
•	Gestionare erori, pagină access denied, verificare CRUD
•	Calitate: rularea ./gradlew.bat check înainte de commit și evitarea regresiilor în DB, teste și documentație.



