# ExpenseFlow Architecture Overview

ExpenseFlow is a production-grade personal finance and expense tracking solution engineered with a modern native Android client (Kotlin + Jetpack Compose) and an accompanying Python Django REST Framework backend.

## Architectural Layers

```
                      +---------------------------------------+
                      |          Android Client (UI)          |
                      |   Jetpack Compose + Material 3 M3     |
                      +-------------------+-------------------+
                                          |
                                          v
                      +---------------------------------------+
                      |         UI Layer (ViewModels)         |
                      |   ExpenseViewModel (Flow State)       |
                      +-------------------+-------------------+
                                          |
                                          v
                      +---------------------------------------+
                      |           Repository Layer            |
                      |    ExpenseRepository (Offline-First)  |
                      +---------+-------------------+---------+
                                |                   |
                                v                   v
             +-----------------------+    +-----------------------+
             |   Local Persistence   |    |    Remote Sync (DRF)  |
             | Room Database (SQLite)|    |  Retrofit + OkHttp    |
             | BigDecimal safe math  |    |  JWT Bearer Auth      |
             +-----------------------+    +-----------+-----------+
                                                      |
                                                      v
                                          +-----------------------+
                                          |     Django REST API   |
                                          |  PostgreSQL / SQLite  |
                                          |  SimpleJWT Auth       |
                                          |  Decimal-safe fields  |
                                          +-----------------------+
```

## Financial Calculation Precision Rules
1. **No Floating Point Arithmetic for Money**: Every currency calculation utilizes `java.math.BigDecimal` on Android and `models.DecimalField` in Django.
2. **Net Balance Equation**:
   `Total Balance = Opening Balance + SUM(All Income Transactions) - SUM(All Expense Transactions)`
3. **Budget Consumption**:
   `Budget Percentage = (Actual Period Category Expenses / Category Limit) * 100%`
   Alerts are emitted at 80% (Warning Amber) and 100%+ (Danger Red).
4. **Currency Localization**:
   Native support for Bangladeshi Taka (`৳` BDT) as primary default currency, alongside global options (USD, EUR, GBP, INR, etc.) selectable dynamically in User Profile.
