# Opis Projektu:

YouTube Data Fetcher to aplikacja napisana w języku Java 17, której celem jest pobieranie i zarządzanie danymi z YouTube. Aplikacja wykorzystuje framework Spring Boot, narzędzie do zarządzania zależnościami Maven oraz bazę danych SQLite (z planowanym przejściem na MySQL). W przyszłości projekt będzie również zintegrowany z Dockerem oraz Apache Spark, aby zapewnić skalowalność i wydajność przetwarzania danych.

# Cel Projektu:

Automatyczne pobieranie danych z YouTube, takich jak informacje o filmach, komentarzach, liczbach wyświetleń, polubieniach, itd.
Przechowywanie pobranych danych w bazie danych.
Udostępnianie interfejsu API do przeglądania i zarządzania danymi.
Zwiększenie skalowalności i wydajności poprzez użycie Dockera i Apache Spark.
# Technologie:

- Język programowania: Java 17
- Framework: Spring Boot
- Zarządzanie zależnościami: Maven
- Baza danych: SQLite (z planowanym przejściem na MySQL)
- Konteneryzacja: Docker (wkrótce)
- Przetwarzanie danych: Apache Spark (wkrótce)

# Funkcje:

Pobieranie danych:
Automatyczne pobieranie danych z YouTube przy użyciu YouTube Data API.
Możliwość pobierania danych o filmach, kanałach, komentarzach i innych metadanych.
Przechowywanie danych:
Przechowywanie pobranych danych w bazie danych SQLite, z planowanym przejściem na MySQL.
API RESTful:
Udostępnianie interfejsu API do przeglądania, wyszukiwania i zarządzania pobranymi danymi.
Przyszłe funkcje:
Integracja z Dockerem w celu konteneryzacji aplikacji.
Wdrożenie Apache Spark w celu wydajnego przetwarzania danych.

# kontrolerów:

**GET /channels:**

Endpoint, który zwraca listę wszystkich kanałów przechowywanych w bazie danych.
Używa DaoService do pobrania danych kanałów.
Loguje odebranie żądania GET do konsoli.


**GET /videos:**

Endpoint, który zwraca listę wszystkich filmów przechowywanych w bazie danych.
Używa DaoService do pobrania danych filmów.
Loguje odebranie żądania GET do konsoli.


**POST /start/{channelId}:**

Endpoint, który uruchamia proces pobierania i zapisywania filmów dla określonego identyfikatora kanału.
Używa ytService do pobrania i zapisania danych filmów.
Zwraca komunikat potwierdzający wykonanie operacji.



