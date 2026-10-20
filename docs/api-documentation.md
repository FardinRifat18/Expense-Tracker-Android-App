# ExpenseFlow REST API Documentation

Base URL: `http://localhost:8000/api/v1/` (or `http://10.0.2.2:8000/api/v1/` for Android Emulator)

## Authentication Endpoints

### 1. Register User
`POST /api/v1/auth/register/`
```json
{
  "username": "fardin",
  "email": "ewrfardin123xyz@gmail.com",
  "password": "SecurePassword123",
  "first_name": "Fardin Hasan",
  "preferred_currency": "BDT"
}
```

### 2. Login User
`POST /api/v1/auth/login/`
```json
{
  "email": "ewrfardin123xyz@gmail.com",
  "password": "SecurePassword123"
}
```
Response:
```json
{
  "access": "<JWT_ACCESS_TOKEN>",
  "refresh": "<JWT_REFRESH_TOKEN>",
  "user": {
    "id": 1,
    "email": "ewrfardin123xyz@gmail.com",
    "first_name": "Fardin Hasan",
    "preferred_currency": "BDT",
    "opening_balance": "50000.00"
  }
}
```

## Transactions Endpoints
All endpoints require `Authorization: Bearer <JWT_ACCESS_TOKEN>`.

### 1. List Transactions
`GET /api/v1/transactions/?type=EXPENSE&ordering=-transaction_date`

### 2. Create Transaction
`POST /api/v1/transactions/`
```json
{
  "title": "Grocery Shopping at Shwapno",
  "amount": "3450.00",
  "type": "EXPENSE",
  "category": 1,
  "payment_method": "bKash",
  "transaction_date": "2026-10-20T12:00:00Z",
  "description": "Vegetables, rice, and cooking oil",
  "currency": "BDT"
}
```

### 3. Export CSV
`GET /api/v1/transactions/export-csv/`

## Budgets Endpoints
`GET /api/v1/budgets/?period=2026-10`
`POST /api/v1/budgets/`

## Analytics Endpoints
`GET /api/v1/analytics/summary/`
`GET /api/v1/analytics/category-breakdown/?period=2026-10`
