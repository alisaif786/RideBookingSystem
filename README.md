# 🚕 RideFlow – Distributed Ride Matching Platform

> A distributed, event-driven backend for ride requests, real-time driver location tracking, and driver matching.

RideFlow is a **Java and Spring Boot based microservices project** designed to simulate the backend architecture of a modern ride-hailing platform.

The system uses **Apache Kafka for asynchronous event-driven communication**, **Redis Geospatial operations for nearby-driver discovery**, and **OpenFeign for synchronous service-to-service communication**.

The primary goal of this project is to explore how a ride-matching system can be designed using distributed systems concepts rather than building everything as a single monolithic application.

---

## 🏗️ Architecture

```text
                         ┌──────────────────┐
                         │      Client      │
                         └────────┬─────────┘
                                  │
                                  ▼
                         ┌──────────────────┐
                         │   Ride Service   │
                         │      :8081       │
                         └────────┬─────────┘
                                  │
                                  │ ride.requested
                                  ▼
                         ┌──────────────────┐
                         │      Kafka       │
                         └────────┬─────────┘
                                  │
                                  ▼
                       ┌──────────────────────┐
                       │  Matching Service    │
                       │       :8084          │
                       └──────────┬───────────┘
                                  │
                                  │ OpenFeign
                                  ▼
                       ┌──────────────────────┐
                       │  Location Service    │
                       │       :8082          │
                       └──────────┬───────────┘
                                  │
                                  ▼
                           ┌─────────────┐
                           │    Redis    │
                           │ Redis GEO   │
                           └─────────────┘
```

---

# 🧩 Microservices

## 🚕 Ride Service

Responsible for handling ride requests.

### Responsibilities

* Receive ride requests
* Create ride information
* Publish ride events to Kafka
* Initiate the ride matching workflow

### Port

```text
8081
```

### Event Published

```text
ride.requested
```

---

## 📍 Location Service

Responsible for managing driver geographical locations.

### Responsibilities

* Update driver location
* Store driver coordinates using Redis Geospatial operations
* Search for nearby drivers
* Remove driver locations
* Expose REST APIs for location-related operations

### Port

```text
8082
```

### Redis

Driver coordinates are maintained using Redis GEO functionality.

```text
Driver Location
      │
      ▼
Location Service
      │
      ▼
Redis GEO
      │
      ▼
Nearby Drivers
```

---

## 🎯 Matching Service

Responsible for processing ride requests and finding nearby drivers.

### Responsibilities

* Consume `ride.requested` events from Kafka
* Process incoming ride requests
* Communicate with Location Service using OpenFeign
* Retrieve nearby drivers
* Execute driver matching logic

### Port

```text
8084
```

### Kafka Consumer Group

```text
matching-service-group
```

---

# 📨 Event-Driven Architecture

RideFlow uses **Apache Kafka** to decouple ride creation from ride matching.

### Current Event Flow

```text
Ride Service
     │
     │  ride.requested
     ▼
   Kafka
     │
     ▼
Matching Service
     │
     │ OpenFeign
     ▼
Location Service
     │
     ▼
   Redis GEO
     │
     ▼
Nearby Drivers
```

This allows the ride request flow and driver matching flow to operate as separate services.

---

# 📍 Driver Location Matching

Redis Geospatial operations are used to find drivers close to the ride pickup location.

```text
              Pickup Location
                     │
                     ▼
              Location Service
                     │
                     ▼
                Redis GEO
                     │
             Nearby Search
                     │
                     ▼
             Nearby Drivers
```

This avoids scanning every driver manually and allows the location service to focus specifically on geographical operations.

---

# 🔗 Communication Patterns

RideFlow currently uses two different communication patterns.

## Asynchronous Communication

### Apache Kafka

```text
Ride Service
     │
     ▼
ride.requested
     │
     ▼
Kafka
     │
     ▼
Matching Service
```

Kafka is used for ride request events.

---

## Synchronous Communication

### Spring Cloud OpenFeign

```text
Matching Service
       │
       │ HTTP
       ▼
Location Service
```

The Matching Service uses OpenFeign to request nearby drivers from the Location Service.

---

# 🧱 Current Ride Matching Flow

```text
1. Passenger requests a ride
              │
              ▼
2. Ride Service receives request
              │
              ▼
3. Ride Service publishes
   ride.requested event
              │
              ▼
4. Kafka receives event
              │
              ▼
5. Matching Service consumes event
              │
              ▼
6. Matching Service requests
   nearby drivers
              │
              ▼
7. Location Service queries
   Redis GEO
              │
              ▼
8. Nearby drivers are returned
              │
              ▼
9. Matching Service processes
   driver matching logic
```

---

# 🛠️ Technology Stack

| Technology             | Purpose                          |
| ---------------------- | -------------------------------- |
| Java 17                | Programming Language             |
| Spring Boot            | Microservices Framework          |
| Spring Data JPA        | Persistence                      |
| MySQL                  | Relational Database              |
| Spring Data Redis      | Redis Integration                |
| Redis GEO              | Geospatial Driver Search         |
| Apache Kafka           | Event-Driven Communication       |
| Spring Kafka           | Kafka Integration                |
| Spring Cloud OpenFeign | Service-to-Service Communication |
| Docker                 | Infrastructure                   |
| Maven                  | Build & Dependency Management    |
| Lombok                 | Boilerplate Reduction            |
| REST APIs              | Service APIs                     |

---

# 📂 Project Structure

```text
RideFlow/
│
├── ride-service/
│   ├── src/
│   └── pom.xml
│
├── location-service/
│   ├── src/
│   └── pom.xml
│
├── matching-service/
│   ├── src/
│   └── pom.xml
│
├── docker-compose.yml
├── pom.xml
└── README.md
```

---

# ✅ Current Implementation

### Location Service

* [x] Driver location update API
* [x] Nearby driver search API
* [x] Remove driver API
* [x] Redis Geospatial integration
* [x] REST APIs
* [x] Logging

### Ride Service

* [x] Ride request API
* [x] Ride service layer
* [x] Ride request/response DTOs
* [x] Ride entity
* [x] Kafka event publishing
* [x] `ride.requested` event

### Matching Service

* [x] Matching Service
* [x] Kafka consumer
* [x] `RideRequestedEvent`
* [x] `RideEventConsumer`
* [x] Kafka consumer group
* [x] OpenFeign client
* [x] Location Service integration
* [x] Nearby driver retrieval
* [x] Initial driver matching flow

### Infrastructure

* [x] Multi-module Maven structure
* [x] Docker configuration
* [x] Redis setup
* [x] Kafka configuration
* [x] Service-specific configuration

---

# 🚧 Roadmap

The current implementation represents the **Ride Matching MVP**.

Planned improvements:

### Ride Lifecycle

* [ ] Driver matching completion
* [ ] `ride.matched` event
* [ ] Driver acceptance
* [ ] Ride start
* [ ] Ride completion
* [ ] Ride cancellation
* [ ] Ride state management

### Distributed System Reliability

* [ ] Kafka retry handling
* [ ] Dead Letter Topic
* [ ] Idempotent event processing
* [ ] Failure recovery
* [ ] Timeout handling
* [ ] Concurrency control during driver assignment

### Security

* [ ] JWT Authentication
* [ ] Role-based authorization
* [ ] Driver/Passenger roles

### Observability

* [ ] Spring Boot Actuator
* [ ] Centralized logging
* [ ] Metrics
* [ ] Prometheus
* [ ] Grafana
* [ ] Distributed tracing

### DevOps

* [ ] Complete Docker Compose environment
* [ ] CI/CD pipeline
* [ ] Containerized services
* [ ] Cloud deployment

### Testing

* [ ] Unit tests
* [ ] Integration tests
* [ ] Kafka integration tests
* [ ] Redis integration tests
* [ ] End-to-end ride flow tests

---

# 🎯 Project Objective

RideFlow is being developed to understand and implement the core concepts behind a **distributed ride-matching system**.

The project focuses on:

* Microservices architecture
* Event-driven architecture
* Apache Kafka
* Redis Geospatial operations
* Synchronous service communication
* Asynchronous service communication
* Distributed system design
* Service separation and responsibility
* Dockerized infrastructure
* Scalable backend architecture

---

# 🚀 Running the Project

Clone the repository:

```bash
git clone https://github.com/alisaif786/RideBookingSystem.git
```

Navigate to the project:

```bash
cd RideBookingSystem
```

Start the required infrastructure using Docker Compose:

```bash
docker-compose up -d
```

Then start the individual Spring Boot services.

---

# 📌 Current Status

**RideFlow is currently at the Distributed Ride Matching MVP stage.**

The core architecture is implemented:

```text
Ride Request
     ↓
Ride Service
     ↓
Kafka
     ↓
Matching Service
     ↓
Location Service
     ↓
Redis GEO
     ↓
Nearby Drivers
```

The next major milestone is completing the **driver assignment and ride lifecycle**, turning the current matching MVP into a more complete ride-booking backend.

---

# 👨‍💻 Author

**Saif Ali**

Java Backend Developer
Spring Boot | Microservices | Kafka | Redis

````

### My recommendation

**Abhi project ko “complete backend” mat bolo.**  
Is stage par strongest and most honest wording hai:

> **RideFlow – Distributed Ride Matching Platform**  
> **Distributed Ride Matching MVP**

Tumhare project ka **architecture complete enough hai to call it an MVP**, but the **business lifecycle is not complete enough to call it a complete ride-booking backend**.

Aur honestly, tumhare liye next feature **API Gateway ya JWT nahi** hona chahiye. Pehle ye complete karo:

```text
ride.requested
      ↓
matching
      ↓
ride.matched
      ↓
driver accepts
      ↓
ride starts
      ↓
ride completes
