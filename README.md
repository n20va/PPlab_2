# Система обработки заказов на Apache Kafka (Лабораторная работа №2)

Данный проект реализует событийную архитектуру на базе **Apache Kafka в Docker** с несколькими независимыми сервисами‑консьюмерами.

Проект демонстрирует:

- работу producer / consumer
- использование нескольких consumer‑групп
- бизнес‑логику обработки сообщений
- потоковую аналитику
- интеграцию REST → Kafka
- дополнительный режим генерации заказов без REST

---

##  Архитектура

```
(клиент / Postman)  →  REST API (RestOrderServer)
                           │
                           ▼
                       Kafka — topic: orders
                           │
      ┌────────────────────────┬────────────────────────┬────────────────────────┐
      │ OrderConsumer          │ BillingConsumer        │ AnalyticsService       │
      │ group: order-processor │ group: billing-service │ group: analytics       │
      │ бизнес‑правила         │ имитация биллинга      │ статистика и аналитика │
      └────────────────────────┴────────────────────────┴────────────────────────┘

Дополнительно:
OrderProducer — генерация пакета заказов (без REST)
```

Каждый сервис имеет собственный `group.id`, поэтому все получают одни и те же события,
но обрабатывают их **независимо**.

---

## Используемые технологии

- Docker + Docker Compose  
- Apache Kafka  
- Java 21 (Maven)  
- Kafka Client API  
- Jackson (JSON)  
- SLF4J Simple Logger  
- Postman (ручное тестирование REST)

---

## Запуск Kafka в Docker

```bash
docker compose up -d
```

Проверка контейнеров:

```bash
docker ps
```

Ожидаемые сервисы:

- `kafka`
- `zookeeper`

---

##Вариант 1 — генерация заказов (без REST)

Сначала запустить консюмеры (в отдельных вкладках Run):

1) `OrderConsumer`  
2) `BillingConsumer`  
3) `AnalyticsService`

Затем запустить:

```
OrderProducer
```

Он генерирует **50 случайных заказов** с разными пользователями и суммами.

Демонстрируется:

- поток сообщений
- работа consumer‑groups
- параллельная обработка
- накопление статистики и аналитики

---

## Вариант 2 — REST → Kafka

Запуск REST‑сервера:

```
RestOrderServer
```

Эндпоинт:

```
POST http://localhost:8080/orders
```

Тело запроса (JSON):

```json
{
  "orderId": 401,
  "user": "postman-user",
  "amount": 650
}
```

Запрос:

1) принимается REST‑сервисом  
2) преобразуется в объект `Order`  
3) публикуется в Kafka (topic `orders`)  
4) обрабатывается всеми консьюмерами

---

## Коллекция Postman (демо‑сценарии)

### Обычный заказ

```json
{
  "orderId": 501,
  "user": "normal-user",
  "amount": 250
}
```

###Некорректный заказ (amount = 0)

```json
{
  "orderId": 502,
  "user": "broken-user",
  "amount": 0
}
```

###Большой заказ (> 500)

```json
{
  "orderId": 503,
  "user": "big-user",
  "amount": 900
}
```


---

## Логика обработки

### OrderConsumer (group: `order-processor`)

- валидирует заказ  
- классифицирует:
  - normal order
  - BIG order (> 500)
  - rejected order
- считает общую сумму заказов

---

### BillingConsumer (group: `billing-service`)

Имитация платёжного сервиса:

- логирует «списание средств»
- демонстрирует независимую обработку

---

### AnalyticsService (group: `analytics-service`)

Выполняет аналитику в реальном времени:

- общее количество заказов
- суммарная сумма
- число BIG‑заказов
- статистика по пользователям



Варвара  
Проект для лабораторной работы №2
