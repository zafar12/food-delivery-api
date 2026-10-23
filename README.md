# Food Delivery Service API

A robust, production-ready RESTful API backend built with **Java**, **Spring Boot**, **Spring Data JPA**, **Spring Security**, and **H2 Database** for managing multi-role food ordering workflows.

---

## 🚀 Key Features & Capabilities

- **Role-Based Access Control (RBAC)**:
  - **Customer**: Register/login, browse restaurants and menu items, place orders (single restaurant, multiple items, custom tips, and coupon percentage discounts), track order status history, and confirm delivery (`RECEIVED`).
  - **Restaurant Owner**: Manage restaurant profiles and menus (CRUD), view incoming orders, and advance order lifecycle states (`PROCESSING` $\rightarrow$ `IN_ROUTE` $\rightarrow$ `DELIVERED`).
  - **Administrator**: Full CRUD access over all users, restaurants, and meals, ability to block/unblock user accounts, and management of platform records. Includes a protected, immutable built-in admin account (`admin@toptal.com`).
- **Order Lifecycle Management**:
  - Enforced valid state transition workflows: `PLACED` $\rightarrow$ `PROCESSING` $\rightarrow$ `IN_ROUTE` $\rightarrow$ `DELIVERED` $\rightarrow$ `RECEIVED` (or `CANCELED` from `PLACED`).
  - Complete chronological audit history logging date and time of every status change.

---

## 🛠️ Technology Stack

- **Language**: Java 17
- **Framework**: Spring Boot 3.2.5
- **Persistence**: Spring Data JPA / Hibernate
- **Database**: H2 In-Memory Database (`/h2-console` enabled)
- **Security**: Spring Security (HTTP Basic & Method-level RBAC)
- **Build Tool**: Gradle

---

## 📦 Getting Started & Local Execution

### Prerequisites
- JDK 17 or higher installed.
- Gradle installed.

### 1. Clone or Open Project
Navigate to your project root folder:
```bash
cd food-delivery-api


## 🧪 API Endpoints & Testing via cURL

### Authentication Note
All protected endpoints require HTTP Basic Authentication headers (`Authorization: Basic <base64-credentials>`).

---

### 1. User & Authentication APIs

* **Register Customer**:
  ```bash
  curl -X POST http://localhost:8080/api/users/register \
    -H "Content-Type: application/json" \
    -d '{"email": "customer1@toptal.com", "password": "password123", "role": "CUSTOMER"}'
  
  * **Register Restaurant Owner**:  
    curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"email": "owner1@toptal.com", "password": "password123", "role": "RESTAURANT_OWNER"}'
  
  * **Get All Users (Admin Only):**:
  curl -u admin@toptal.com:admin123 http://localhost:8080/api/users
  
  * **Block/Unblock User (Admin Only)**:  
  curl -X PUT -u admin@toptal.com:admin123 http://localhost:8080/api/users/2/block
  
### 2. Restaurant & Meal APIs
  
  * **Create Restaurant (Owner Only)**:
  curl -X POST -u owner1@toptal.com:password123 http://localhost:8080/api/restaurants \
  -H "Content-Type: application/json" \
  -d '{"name": "Tasty Bites", "description": "Burgers and Fast Food"}'
    
  
  * **Browse All Restaurants (Authenticated)**:  
  curl -u customer1@toptal.com:password123 http://localhost:8080/api/restaurants
  
  * **Add Meal to Menu (Owner Only)**:  
  curl -X POST -u owner1@toptal.com:password123 http://localhost:8080/api/restaurants/1/meals \
  -H "Content-Type: application/json" \
  -d '{"name": "Cheeseburger", "description": "Juicy patty with cheese", "price": 9.99}'
  
### 3. Order Placement & State Lifecycle APIs  
  
  * **Place Order (Customer Only)**:  
  curl -X POST -u customer1@toptal.com:password123 http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"restaurantId": 1, "mealIds": [1], "customTip": 2.00, "couponCode": null}'
  
  * **Advance Order Status (Owner: PLACED -> PROCESSING)**: 
  curl -X PATCH -u owner1@toptal.com:password123 "http://localhost:8080/api/orders/1/status?status=PROCESSING"
  
  * **Mark Order Received (Customer: DELIVERED -> RECEIVED)**:  
  curl -X PATCH -u customer1@toptal.com:password123 "http://localhost:8080/api/orders/1/status?status=RECEIVED" 