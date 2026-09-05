# Box Dispatch Service

A small REST API for creating boxes, loading items into boxes, and checking box information.

## Tech Stack

- Java 17
- Spring Boot 4.1.1
- PostgreSQL
- Spring Data JPA
- Flyway
- Maven
- JUnit 5
- Mockito

## Requirements

You need:

- Java 17 or newer
- PostgreSQL

## Database Setup

Create the database:

```sql
CREATE DATABASE box_dispatch;
```

Set these environment variables:

```text
DB_URL=jdbc:postgresql://localhost:5432/box_dispatch
DB_USERNAME=postgres
DB_PASSWORD=your_password
```

The database tables are created automatically by Flyway when the application starts.

## Run the Application

```bash
./mvnw spring-boot:run
```

The API runs on:

```text
http://localhost:8080
```

## Run Tests

```bash
./mvnw clean test
```

## API Endpoints

### Create a box

```http
POST /api/v1/boxes
```

Example request:

```json
{
  "txref": "BOX-004",
  "weightLimitGrams": 500,
  "batteryPercentage": 80
}
```

A new box starts with the `IDLE` state.

### Get available boxes

```http
GET /api/v1/boxes/available
```

A box is available when:

- it is `IDLE`
- battery is at least 25%

### Check battery

```http
GET /api/v1/boxes/{txref}/battery
```

Example:

```http
GET /api/v1/boxes/BOX-004/battery
```

### Get items in a box

```http
GET /api/v1/boxes/{txref}/items
```

Example:

```http
GET /api/v1/boxes/BOX-004/items
```

### Load items

```http
POST /api/v1/boxes/{txref}/items
```

Example:

```json
{
  "items": [
    {
      "name": "Item_001",
      "weightGrams": 100,
      "code": "ITEM_001"
    },
    {
      "name": "Item-002",
      "weightGrams": 150,
      "code": "ITEM_002"
    }
  ]
}
```

A successful request returns `204 No Content`.

## Rules

### Weight

The total weight of the items cannot be greater than the box weight limit.

For example, a box with a 500g limit and 300g already loaded can only accept another 200g.

If a request contains multiple items and the total is too heavy, none of the items are loaded.

### Battery

A box needs at least 25% battery before loading can start.

```text
24%  -> rejected
25%  -> allowed
26%  -> allowed
```

### Box state

A box must be `IDLE` before it can be loaded.

The states used by the service are:

```text
IDLE
  |
  v
LOADING
  |
  v
LOADED
  |
  v
DELIVERING
  |
  v
DELIVERED
  |
  v
RETURNING
  |
  v
IDLE
```

The actual delivery process is outside the scope of this task.

### Item name

Item names can contain:

- letters
- numbers
- `-`
- `_`

Examples:

```text
Item-001
Item_001
Box123
```

### Item code

Item codes can contain uppercase letters, numbers and `_`.

Examples:

```text
ITEM_001
ABC123
PART_01
```

Codes must be unique inside the same box.

### Box transaction reference

`txref` is required, must be unique, and cannot be longer than 20 characters.

## Concurrency

The box row is locked when items are being loaded.

This prevents two requests from loading the same box at the same time and accidentally going over the weight limit.

The lock is held until the database transaction finishes.

## Database

There are two main tables:

```text
boxes
  |
  | one-to-many
  |
items
```

Each item belongs to one box.

The database also checks:

- box weight is between 1g and 500g
- battery is between 0% and 100%
- item weight is greater than 0
- `txref` is unique
- item code is unique within a box
- box state is valid

## Migrations

Flyway is used for database migrations.

Current migrations:

```text
V1__create_boxes_and_items.sql
V2__seed_boxes.sql
```

The second migration adds a few sample boxes for development.

## Error Responses

Errors are returned as JSON.

Example:

```json
{
  "timestamp": "2026-09-05T06:00:00+01:00",
  "status": 409,
  "error": "Conflict",
  "message": "Box battery must be at least 25% to start loading",
  "path": "/api/v1/boxes/BOX-002/items"
}
```

Common status codes:

| Status | Meaning                                |
| ------ | --------------------------------------- |
| 200    | Request was successful                 |
| 201    | Box was created                        |
| 204    | Items were loaded                      |
| 400    | Invalid request                        |
| 404    | Box was not found                      |
| 409    | Request conflicts with a business rule |
| 500    | Unexpected server error                |

## Project Structure

The code is grouped by feature:

```text
com.example.boxdispatch
├── box
│   ├── Box.java
│   ├── BoxController.java
│   ├── BoxRepository.java
│   ├── BoxService.java
│   ├── BoxState.java
│   ├── dto
│   └── exception
├── item
│   ├── Item.java
│   ├── ItemRepository.java
│   └── dto
└── common
    └── exception
```

## Assumptions

- New boxes start as `IDLE`.
- Weight is stored in grams.
- Battery is stored as a percentage from 0 to 100.
- The maximum box weight limit is 500g.
- A box needs at least 25% battery to start loading.
- Loading a request is all or nothing.
- Item codes only need to be unique within a box.
- Loaded weight is calculated from the items in the box.
- Box communication and delivery operations are not part of this task.
- Sample boxes are added by Flyway for development.