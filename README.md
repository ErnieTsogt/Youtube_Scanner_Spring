# YouTube Scanner Analytics - Backend

Backend aplikacji do skanowania kanałów YouTube, zapisywania historii zmian i udostępniania danych analitycznych dla frontendu Vue.

**Status**: aktualne względem bieżącej implementacji  
**Stack**: Java 17, Spring Boot, MySQL, Flyway, Docker Compose

Pełna wspólna dokumentacja projektu znajduje się w `../DOKUMENTACJA.md`.

## Najważniejsze funkcje

- pobieranie danych kanału i filmów z YouTube API,
- zapisywanie aktualnych danych filmów oraz ich historycznych snapshotów,
- analityka kanałów, filmów i trendów,
- ręczne skanowanie kanału,
- automatyczne skanowanie według `intervalDays + HH:mm`,
- backup danych do pliku JSON,
- restore backupu w trybie merge/upsert,
- usuwanie skanów kanału lub całego kanału razem z zależnymi danymi.

## Architektura

```text
Frontend (Vue 3, port 3000)
        ↓
Backend (Spring Boot, port 9090 w Docker / 8080 lokalnie)
        ↓
MySQL 8 (port 3308 w Docker)
```

## Moduły backendu

- `controller` - endpointy REST,
- `service` - logika biznesowa, skanowanie i harmonogram,
- `repository` - dostęp do bazy przez Spring Data JPA,
- `model/data` - encje bazy danych,
- `model/dto` - DTO używane przez API,
- `src/main/resources/db/migration` - migracje Flyway.

## Aktualne endpointy

### Skanowanie i dane podstawowe

- `GET /channels`
- `GET /videos`
- `POST /start/{channelId}`
- `POST /api/start/{channelId}`
- `GET /api/channels`
- `GET /api/videos`
- `GET /api/scan-history`

### Analityka

- `GET /api/analytics/channels`
- `GET /api/analytics/videos?channel=&from=&to=&sortBy=&sortDir=&page=&size=`
- `GET /api/analytics/video-stats-history?channel=&from=&to=&sortBy=&sortDir=&page=&size=`
- `GET /api/analytics/trends?channel=&metric=&days=`
- `GET /api/analytics/statistics?channel=`
- `GET /api/analytics/comparison?channel=&metric=`
- `DELETE /api/analytics/channel/{googleId}/videos`
- `DELETE /api/analytics/channel/{googleId}`

### Auto-skan

- `GET /api/scan-config`
- `PUT /api/scan-config?intervalDays=&time=HH:mm`

Zwracana konfiguracja zawiera m.in.:

- `intervalDays`,
- `time`,
- `cron`,
- `lastAutoScanDate`,
- `lastScanDate`,
- `nextAutoScanAt`.

Scheduler działa w strefie `Europe/Warsaw`.

### Backup i restore

- `GET /api/backup/export`
- `POST /api/backup/restore` - `multipart/form-data`, pole `file`

Restore działa jako **merge/upsert**:

- nowe rekordy są dodawane,
- istniejące duplikaty są nadpisywane,
- rekordy nieobecne w pliku backupu nie są usuwane.

## Baza danych

Najważniejsze tabele:

- `Channels`
- `YTVideos`
- `VideoSnapshots`
- `ScanHistory`

Migracje znajdują się w `src/main/resources/db/migration`.

## Uruchomienie

### Docker Compose

Z katalogu `Youtube_Scanner_Spring`:

```bash
docker compose up -d --build
```

Dostęp:

- frontend: `http://localhost:3000`
- backend: `http://localhost:9090`
- mysql: `localhost:3308`

### Tryb lokalny

Backend:

```bash
mvn clean install
mvn spring-boot:run
```

Domyślnie backend działa lokalnie na `http://localhost:8080`.

## Trwałość danych

`docker-compose.yaml` używa nazwanego wolumenu `mysql_data`, więc dane MySQL pozostają po zwykłym `docker compose down`.

Uwaga:

- `docker compose down` - zatrzymuje kontenery, zachowuje dane,
- `docker compose down -v` - usuwa także wolumen i całą bazę.

## Najczęstsze scenariusze

### Ręczny skan kanału

```bash
curl -X POST http://localhost:9090/api/start/UCX6OQ3DkcsbYNE6H8uQQuVA
```

### Odczyt konfiguracji auto-skana

```bash
curl http://localhost:9090/api/scan-config
```

### Ustawienie auto-skana co 3 dni o 02:30

```bash
curl -X PUT "http://localhost:9090/api/scan-config?intervalDays=3&time=02:30"
```

## Dokumenty powiązane

- `../DOKUMENTACJA.md` - główna dokumentacja projektu,
- `QUICK_START.md` - krótki odsyłacz do dokumentacji głównej.
