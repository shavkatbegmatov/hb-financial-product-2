# API Руководство - HB Financial Product

## Оглавление
1. [Общая информация](#общая-информация)
2. [Технологии](#технологии)
3. [Структура базы данных](#структура-базы-данных)
4. [Запуск приложения](#запуск-приложения)
5. [Аутентификация](#аутентификация)
6. [API Эндпоинты](#api-эндпоинты)
   - [Аутентификация и регистрация](#аутентификация-и-регистрация)
   - [Управление пользователями](#управление-пользователями)
   - [Управление транзакциями](#управление-транзакциями)
7. [Тестовые данные](#тестовые-данные)
8. [Обработка ошибок](#обработка-ошибок)
9. [Docker](#docker)
10. [Postman коллекция](#postman-коллекция)

---

## Общая информация

**HB Financial Product** - это финансовое REST API приложение, разработанное на Spring Boot, которое предоставляет полный функционал для управления пользователями и финансовыми транзакциями.

### Основной функционал:
- Регистрация и аутентификация пользователей
- Управление профилями пользователей
- Автоматический расчет баланса
- Система финансовых транзакций (пополнение, списание, переводы)
- Фильтрация и поиск транзакций
- JWT токены для безопасности
- Подробная обработка ошибок

---

## Технологии

- **Java 21**
- **Spring Boot 3.5.6**
- **Spring Security** (JWT аутентификация)
- **Spring Data JPA** (работа с БД)
- **PostgreSQL** (основная БД)
- **Flyway** (миграции БД)
- **Maven** (сборка проекта)
- **Docker** (контейнеризация)

---

## Структура базы данных

### Таблица `users`
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    balance DECIMAL(15,2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Таблица `transactions`
```sql
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    to_user_id BIGINT,  -- для переводов
    amount DECIMAL(15,2) NOT NULL CHECK (amount > 0),
    type VARCHAR(10) NOT NULL CHECK (type IN ('DEBIT', 'CREDIT', 'TRANSFER')),
    description VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (to_user_id) REFERENCES users(id) ON DELETE SET NULL
);
```

### Типы транзакций:
- **CREDIT** - пополнение счета
- **DEBIT** - списание со счета
- **TRANSFER** - перевод между пользователями

### Статусы транзакций:
- **PENDING** - ожидает обработки
- **COMPLETED** - завершена успешно
- **FAILED** - завершена с ошибкой

---

## Запуск приложения

### Локальный запуск:
```bash
# Клонирование проекта
git clone <repository-url>
cd hb-financial-product

# Установка зависимостей и запуск
./mvnw spring-boot:run
```

**URL приложения:** http://localhost:8081

### Требования:
- PostgreSQL сервер на localhost:5432
- База данных `finance_product_2`
- Пользователь: `postgres`, пароль: `f3300955#F123456`

---

## Аутентификация

API использует JWT токены для аутентификации. Все эндпоинты (кроме регистрации и логина) требуют валидный токен в заголовке:

```
Authorization: Bearer <your-jwt-token>
```

### Схема аутентификации:
1. Регистрация пользователя `/api/auth/register`
2. Получение токенов `/api/auth/login`
3. Использование access token для API запросов
4. Обновление токенов `/api/auth/refresh`

**Время жизни токенов:**
- Access Token: 1 час
- Refresh Token: 7 дней

---

## API Эндпоинты

### Аутентификация и регистрация

#### 1. Регистрация пользователя
```http
POST /api/auth/register
Content-Type: application/json

{
    "username": "testuser",
    "email": "test@example.com",
    "fullName": "Test User",
    "password": "password123"
}
```

**Ответ:**
```json
{
    "id": 6,
    "username": "testuser",
    "email": "test@example.com",
    "fullName": "Test User",
    "balance": 0.00,
    "createdAt": "2025-09-22T04:00:00",
    "updatedAt": "2025-09-22T04:00:00"
}
```

#### 2. Вход в систему
```http
POST /api/auth/login
Content-Type: application/json

{
    "username": "john_doe",
    "password": "password123"
}
```

**Ответ:**
```json
{
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "userId": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "fullName": "John Doe"
}
```

#### 3. Обновление токена
```http
POST /api/auth/refresh?refreshToken=<refresh-token>
```

**Ответ:** новая пара токенов в том же формате.

---

### Управление пользователями

#### 1. Получить пользователя по ID
```http
GET /api/users/1
Authorization: Bearer <token>
```

**Ответ:**
```json
{
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "fullName": "John Doe",
    "balance": 975.00,
    "createdAt": "2025-09-22T04:00:00",
    "updatedAt": "2025-09-22T04:00:00"
}
```

#### 2. Получить всех пользователей (с пагинацией)
```http
GET /api/users?page=0&size=10&sortBy=fullName&sortDir=asc
Authorization: Bearer <token>
```

**Параметры:**
- `page` - номер страницы (по умолчанию: 0)
- `size` - размер страницы (по умолчанию: 10)
- `sortBy` - поле для сортировки (по умолчанию: fullName)
- `sortDir` - направление сортировки: asc/desc (по умолчанию: asc)

#### 3. Поиск пользователей
```http
GET /api/users/search?searchTerm=john&page=0&size=10
Authorization: Bearer <token>
```

Поиск выполняется по имени пользователя, email и полному имени.

#### 4. Обновить пользователя
```http
PUT /api/users/1
Authorization: Bearer <token>
Content-Type: application/json

{
    "username": "john_updated",
    "email": "john_new@example.com",
    "fullName": "John Updated Doe",
    "password": "newpassword123"
}
```

#### 5. Удалить пользователя
```http
DELETE /api/users/1
Authorization: Bearer <token>
```

---

### Управление транзакциями

#### 1. Создать транзакцию (пополнение/списание)
```http
POST /api/transactions
Authorization: Bearer <token>
Content-Type: application/json

{
    "userId": 1,
    "amount": 100.00,
    "type": "CREDIT",
    "description": "Пополнение счета"
}
```

**Типы транзакций:**
- `CREDIT` - пополнение
- `DEBIT` - списание

**Ответ:**
```json
{
    "id": 15,
    "userId": 1,
    "userName": "John Doe",
    "toUserId": null,
    "toUserName": null,
    "amount": 100.00,
    "type": "CREDIT",
    "description": "Пополнение счета",
    "status": "COMPLETED",
    "createdAt": "2025-09-22T04:00:00",
    "processedAt": "2025-09-22T04:00:00"
}
```

#### 2. Перевод между пользователями
```http
POST /api/transactions/transfer
Authorization: Bearer <token>
Content-Type: application/json

{
    "fromUserId": 1,
    "toUserId": 2,
    "amount": 50.00,
    "description": "Перевод за услуги"
}
```

**Ответ:**
```json
{
    "id": 16,
    "userId": 1,
    "userName": "John Doe",
    "toUserId": 2,
    "toUserName": "Jane Smith",
    "amount": 50.00,
    "type": "TRANSFER",
    "description": "Transfer from John Doe to Jane Smith: Перевод за услуги",
    "status": "COMPLETED",
    "createdAt": "2025-09-22T04:00:00",
    "processedAt": "2025-09-22T04:00:00"
}
```

#### 3. Получить транзакцию по ID
```http
GET /api/transactions/1
Authorization: Bearer <token>
```

#### 4. Получить все транзакции (с пагинацией)
```http
GET /api/transactions?page=0&size=10&sortBy=createdAt&sortDir=desc
Authorization: Bearer <token>
```

#### 5. Получить транзакции пользователя
```http
GET /api/transactions/user/1?page=0&size=10
Authorization: Bearer <token>
```

#### 6. Получить транзакции по статусу
```http
GET /api/transactions/status/COMPLETED?page=0&size=10
Authorization: Bearer <token>
```

**Возможные статусы:** `PENDING`, `COMPLETED`, `FAILED`

#### 7. Получить транзакции по типу
```http
GET /api/transactions/type/TRANSFER?page=0&size=10
Authorization: Bearer <token>
```

**Возможные типы:** `DEBIT`, `CREDIT`, `TRANSFER`

#### 8. Получить транзакции пользователя за период
```http
GET /api/transactions/user/1/date-range?startDate=2025-09-15T00:00:00&endDate=2025-09-22T23:59:59&page=0&size=10
Authorization: Bearer <token>
```

#### 9. Фильтрация транзакций (комбинированный поиск)
```http
GET /api/transactions/filter?userId=1&type=CREDIT&status=COMPLETED&startDate=2025-09-01T00:00:00&endDate=2025-09-30T23:59:59&page=0&size=10&sortBy=amount&sortDir=desc
Authorization: Bearer <token>
```

**Параметры фильтрации:**
- `userId` - ID пользователя
- `type` - тип транзакции
- `status` - статус транзакции
- `startDate` - дата начала периода
- `endDate` - дата окончания периода
- `page`, `size` - пагинация
- `sortBy`, `sortDir` - сортировка

---

## Тестовые данные

В системе предустановлены тестовые пользователи:

| Username | Email | Полное имя | Пароль | Баланс |
|----------|-------|------------|--------|---------|
| john_doe | john@example.com | John Doe | password123 | 975.00 |
| jane_smith | jane@example.com | Jane Smith | password123 | 1575.50 |
| bob_johnson | bob@example.com | Bob Johnson | password123 | 520.25 |
| alice_brown | alice@example.com | Alice Brown | password123 | 1670.00 |
| charlie_davis | charlie@example.com | Charlie Davis | password123 | 235.75 |

### Примеры для быстрого тестирования:

1. **Вход в систему:**
```json
{
    "username": "john_doe",
    "password": "password123"
}
```

2. **Пополнение счета:**
```json
{
    "userId": 1,
    "amount": 200.00,
    "type": "CREDIT",
    "description": "Тестовое пополнение"
}
```

3. **Перевод денег:**
```json
{
    "fromUserId": 1,
    "toUserId": 2,
    "amount": 100.00,
    "description": "Тестовый перевод"
}
```

---

## Обработка ошибок

### Коды ошибок HTTP:

| Код | Описание | Примеры |
|-----|----------|---------|
| 400 | Bad Request | Недостаточно средств, неверные данные |
| 401 | Unauthorized | Неверный токен, истекший токен |
| 404 | Not Found | Пользователь не найден, транзакция не найдена |
| 500 | Internal Server Error | Ошибка сервера |

### Формат ошибок:
```json
{
    "status": 400,
    "error": "Insufficient Balance",
    "message": "Insufficient balance for transfer. Available: 100.00, Required: 150.00",
    "path": "/api/transactions/transfer",
    "timestamp": "2025-09-22T04:00:00"
}
```

### Типы кастомных ошибок:

1. **InsufficientBalanceException** - недостаточно средств
2. **InvalidTransferException** - неверные параметры перевода
3. **UserNotFoundException** - пользователь не найден
4. **TransactionNotFoundException** - транзакция не найдена

### Примеры ошибок:

**Недостаточно средств:**
```json
{
    "status": 400,
    "error": "Insufficient Balance",
    "message": "Insufficient balance for transfer. Available: 50.00, Required: 100.00",
    "path": "/api/transactions/transfer"
}
```

**Пользователь не найден:**
```json
{
    "status": 404,
    "error": "Not Found",
    "message": "User not found with id: 999",
    "path": "/api/users/999"
}
```

**Неверные данные валидации:**
```json
{
    "status": 400,
    "error": "Validation Failed",
    "message": "Validation failed for fields: {amount=Amount must be greater than 0}",
    "path": "/api/transactions"
}
```

---

## Docker

### Запуск с Docker Compose:

```yaml
# docker-compose.yml
version: '3.8'
services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: finance_product_2
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: f3300955#F123456
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: f3300955#F123456
    depends_on:
      - postgres

volumes:
  postgres_data:
```

**Команды:**
```bash
# Сборка и запуск
docker-compose up --build

# Только запуск
docker-compose up -d

# Остановка
docker-compose down
```

**URL в Docker:** http://localhost:8080

---

## Postman коллекция

### Переменные окружения:
```json
{
    "baseUrl": "http://localhost:8081",
    "accessToken": "",
    "refreshToken": "",
    "userId": ""
}
```

### Примеры запросов:

#### 1. Регистрация
```
POST {{baseUrl}}/api/auth/register
Content-Type: application/json

{
    "username": "newuser",
    "email": "newuser@example.com",
    "fullName": "New User",
    "password": "password123"
}
```

#### 2. Логин (с автоматическим сохранением токена)
```
POST {{baseUrl}}/api/auth/login
Content-Type: application/json

{
    "username": "john_doe",
    "password": "password123"
}

# В Tests секции Postman:
pm.test("Save tokens", function () {
    var jsonData = pm.response.json();
    pm.environment.set("accessToken", jsonData.accessToken);
    pm.environment.set("refreshToken", jsonData.refreshToken);
    pm.environment.set("userId", jsonData.userId);
});
```

#### 3. Получить пользователя
```
GET {{baseUrl}}/api/users/{{userId}}
Authorization: Bearer {{accessToken}}
```

#### 4. Создать транзакцию
```
POST {{baseUrl}}/api/transactions
Authorization: Bearer {{accessToken}}
Content-Type: application/json

{
    "userId": {{userId}},
    "amount": 100.00,
    "type": "CREDIT",
    "description": "Тестовое пополнение"
}
```

#### 5. Перевод денег
```
POST {{baseUrl}}/api/transactions/transfer
Authorization: Bearer {{accessToken}}
Content-Type: application/json

{
    "fromUserId": {{userId}},
    "toUserId": 2,
    "amount": 50.00,
    "description": "Тестовый перевод"
}
```

#### 6. Получить транзакции пользователя
```
GET {{baseUrl}}/api/transactions/user/{{userId}}?page=0&size=10
Authorization: Bearer {{accessToken}}
```

#### 7. Фильтрация транзакций
```
GET {{baseUrl}}/api/transactions/filter?userId={{userId}}&type=TRANSFER&status=COMPLETED&page=0&size=10
Authorization: Bearer {{accessToken}}
```

### Автоматическое обновление токена:
```javascript
// В Pre-request Script для всех запросов:
const accessToken = pm.environment.get("accessToken");
const refreshToken = pm.environment.get("refreshToken");

if (!accessToken && refreshToken) {
    pm.sendRequest({
        url: pm.environment.get("baseUrl") + "/api/auth/refresh?refreshToken=" + refreshToken,
        method: 'POST',
    }, function (err, response) {
        if (response.code === 200) {
            const jsonData = response.json();
            pm.environment.set("accessToken", jsonData.accessToken);
            pm.environment.set("refreshToken", jsonData.refreshToken);
        }
    });
}
```

---

## Дополнительные возможности

### Мониторинг и здоровье приложения:
```
GET /actuator/health - состояние приложения
GET /actuator/info - информация о приложении
```

### Логирование:
- **DEBUG уровень** для SQL запросов
- **INFO уровень** для основных операций
- **ERROR уровень** для ошибок

### Безопасность:
- Пароли шифруются с помощью BCrypt
- JWT токены подписываются секретным ключом
- CORS настроен для разработки
- SQL инъекции предотвращаются через JPA

### Производительность:
- Индексы на часто запрашиваемые поля
- Пагинация для больших наборов данных
- Ленивая загрузка связанных сущностей
- Connection pooling с HikariCP

---

## Заключение

Это API предоставляет полный функционал для финансового приложения с надежной системой аутентификации, гибкой системой транзакций и удобными возможностями поиска и фильтрации.

Для получения дополнительной помощи или сообщения об ошибках, пожалуйста, обратитесь к разработчикам проекта.

**Версия API:** 1.0
**Дата последнего обновления:** 22 сентября 2025
**Автор:** HB Financial Team (Shavkat Begmatov)