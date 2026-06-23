# Task Management System

REST API для управления задачами с аутентификацией на JWT и PostgreSQL.

**GitHub:** https://github.com/TiTrix5/task-management-api

## Технологический стек

- Java 17
- Spring Boot 3.2
- Spring Security (JWT)
- Spring Data JPA / Hibernate
- PostgreSQL
- Liquibase (миграции БД)
- MapStruct (DTO-маппинг)
- Lombok
- JUnit 5 + Mockito
- Docker / Docker Compose

## Функциональность

### Аутентификация
| Метод | Endpoint | Описание |
|-------|----------|----------|
| POST | `/auth/register` | Регистрация пользователя |
| POST | `/auth/login` | Вход, выдача JWT-токена |

### Пользователи
| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/users/me` | Информация о текущем пользователе (требует JWT) |

### Задачи
| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/tasks` | Список задач текущего пользователя (фильтры: `status`, `priority`) |
| GET | `/tasks/{id}` | Задача по ID |
| POST | `/tasks` | Создание задачи |
| PUT | `/tasks/{id}` | Обновление задачи |
| DELETE | `/tasks/{id}` | Удаление задачи |

Все эндпоинты задач доступны только авторизованным пользователям. Пользователь видит и управляет только своими задачами.

## Структура проекта

```
src/main/java/com/example/taskmanagement/
├── controller/     # REST-контроллеры
├── dto/            # Request/Response DTO
├── entity/         # JPA-сущности и enum'ы
├── exception/      # Обработка ошибок
├── mapper/         # MapStruct-мапперы
├── repository/     # Spring Data JPA
├── security/       # JWT, SecurityConfig
└── service/        # Бизнес-логика
```

## База данных

### Таблица `users`
| Поле | Тип | Описание |
|------|-----|----------|
| id | UUID | PK |
| username | VARCHAR(50) | Уникальное имя |
| password | VARCHAR(255) | BCrypt-хэш |
| email | VARCHAR(100) | Уникальный email |
| roles | VARCHAR(50) | `ROLE_USER` / `ROLE_ADMIN` |

### Таблица `tasks`
| Поле | Тип | Описание |
|------|-----|----------|
| id | UUID | PK |
| title | VARCHAR(255) | Название |
| description | TEXT | Описание |
| status | VARCHAR(20) | `NEW`, `IN_PROGRESS`, `DONE` |
| priority | VARCHAR(20) | `LOW`, `MEDIUM`, `HIGH` |
| user_id | UUID | FK → users.id |

## Запуск через Docker (рекомендуется)

```bash
docker compose up --build
```

Приложение: `http://localhost:8080`  
PostgreSQL: `localhost:15432` (user: `taskuser`, password: `taskpass`, db: `taskdb`)

## Локальный запуск без Docker

1. Запустите PostgreSQL и создайте БД `taskdb`.
2. Настройте переменные (или используйте значения по умолчанию из `application.properties`).
3. Соберите и запустите:

```bash
mvn clean package
java -jar target/task-management-1.0.0.jar
```

## Тестирование

### Unit-тесты
```bash
mvn test
```

### Postman
Импортируйте коллекцию из `postman/Task-Management-API.postman_collection.json`.

Порядок тестирования:
1. **Register** — создать пользователя
2. **Login** — получить JWT (токен сохраняется автоматически)
3. **Get Current User** — проверить `/users/me`
4. **Create Task** → **Get All Tasks** → **Get Task By ID** → **Update Task** → **Delete Task**

Для запросов с `{id}` подставьте UUID из ответа создания задачи.

### Примеры curl

```bash
# Регистрация
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","password":"password123","email":"user1@mail.com"}'

# Логин
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","password":"password123"}'

# Создание задачи (подставьте TOKEN)
curl -X POST http://localhost:8080/tasks \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Task 1","description":"Desc","status":"NEW","priority":"HIGH"}'

# Список задач с фильтрацией
curl "http://localhost:8080/tasks?status=NEW&priority=HIGH" \
  -H "Authorization: Bearer TOKEN"
```

## Переменные окружения

| Переменная | По умолчанию | Описание |
|------------|--------------|----------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/taskdb` | URL БД |
| `SPRING_DATASOURCE_USERNAME` | `taskuser` | Пользователь БД |
| `SPRING_DATASOURCE_PASSWORD` | `taskpass` | Пароль БД |
| `JWT_SECRET` | (см. application.properties) | Секрет для JWT |
| `JWT_EXPIRATION_MS` | `86400000` | Время жизни токена (мс) |
| `SERVER_PORT` | `8080` | Порт приложения |

## Автор

Учебный проект — REST API для системы управления задачами.
