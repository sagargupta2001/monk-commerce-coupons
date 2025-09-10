# Coupons Management API for Monk Commerce

This project is a RESTful API for managing and applying various types of discount coupons for an e-commerce platform. It is built with Spring Boot, Java 17, and uses an embedded SQLite database.

## Table of Contents
1.  [Core Concepts & Architecture](#core-concepts--architecture)
2.  [Tech Stack](#tech-stack)
3.  [API Endpoints](#api-endpoints)
4.  [How to Run the Application](#how-to-run-the-application)
5.  [Coupon Use Cases & Scenarios](#coupon-use-cases--scenarios)
6.  [Assumptions](#assumptions)
7.  [Limitations & Future Improvements](#limitations--future-improvements)

---

## Core Concepts & Architecture

The application is designed using **Clean Architecture** principles to ensure a clear separation of concerns, making it highly maintainable and scalable.

-   **Domain**: Contains the core business logic and models, free from any framework dependencies.
-   **Usecase/Application**: Orchestrates the business logic. This is where the primary application services reside.
-   **Infrastructure**: Deals with external concerns like REST APIs (Web), database interactions (Persistence), and error handling.

### The Strategy Design Pattern
To support different coupon types and allow for future extensibility, the **Strategy Design Pattern** is used.
-   An `DiscountStrategy` interface defines the common operations for all coupon types: `isApplicable` and `apply`.
-   Concrete classes (`CartWiseStrategy`, `ProductWiseStrategy`, `BxGyStrategy`) implement this interface for each coupon type.
-   This design makes it easy to add new coupon types (e.g., `FirstTimeUserDiscount`) without modifying existing code, adhering to the **Open/Closed Principle**.

---

## Tech Stack
-   **Framework**: Spring Boot 3.3.4
-   **Language**: Java 17
-   **Build Tool**: Gradle
-   **Database**: SQLite (embedded via `sqlite-jdbc`)
-   **ORM**: Spring Data JPA / Hibernate
-   **API**: RESTful

---

## API Endpoints

(Note: `{{host}}` is `http://localhost:8080`)

### 1. Create a new Coupon
-   **Endpoint**: `POST /coupons`
-   **Description**: Creates a new coupon. The structure of the `details` object changes based on the `type`.
-   **Sample Payloads**:
    -   **Cart-wise**:
        ```json
        {
          "type": "CART_WISE",
          "details": {
            "minCartValue": 500,
            "discountPercentage": 10
          },
          "active": true,
          "expiryDate": "2025-12-31"
        }
        ```
    -   **Product-wise**:
        ```json
        {
          "type": "PRODUCT_WISE",
          "details": {
            "productId": "prod-123",
            "discountPercentage": 25
          },
          "active": true
        }
        ```
    -   **BxGy (Buy X, Get Y)**:
        ```json
        {
          "type": "BXGY",
          "details": {
            "buyProductIds": ["prod-abc", "prod-def"],
            "buyQuantity": 2,
            "getProductIds": ["prod-ghi"],
            "getQuantity": 1,
            "repetitionLimit": 3
          },
          "active": true
        }
        ```

### 2. Retrieve All Coupons
-   **Endpoint**: `GET /coupons`

### 3. Retrieve a Specific Coupon
-   **Endpoint**: `GET /coupons/{id}`

### 4. Update a Coupon
-   **Endpoint**: `PUT /coupons/{id}`

### 5. Delete a Coupon
-   **Endpoint**: `DELETE /coupons/{id}`

### 6. Fetch Applicable Coupons for a Cart
-   **Endpoint**: `POST /applicable-coupons`
-   **Description**: Takes a cart object and returns a list of all coupons that can be applied, along with the calculated discount for each.
-   **Sample Payload**:
    ```json
    {
      "items": [
        { "productId": "prod-123", "quantity": 2, "price": 200.0 },
        { "productId": "prod-456", "quantity": 1, "price": 350.0 }
      ]
    }
    ```
-   **Sample Response**:
    ```json
    [
      {
        "couponId": 1,
        "type": "CART_WISE",
        "discountAmount": 75.0
      }
    ]
    ```

### 7. Apply a Coupon to a Cart
-   **Endpoint**: `POST /apply-coupon/{id}`
-   **Description**: Applies a specific coupon to a cart and returns the updated cart state with discounts applied.
-   **Sample Response**:
    ```json
    {
      "items": [
        {
          "productId": "prod-123",
          "quantity": 2,
          "price": 200.0,
          "discount": 0.0
        },
        {
          "productId": "prod-456",
          "quantity": 1,
          "price": 350.0,
          "discount": 0.0
        }
      ],
      "originalTotal": 750.0,
      "totalDiscount": 75.0,
      "finalTotal": 675.0
    }
    ```
---

## How to Run the Application

1.  **Prerequisites**: JDK 17 and Gradle installed.
2.  **Clone the repository**.
3.  **Run the application** using the Gradle wrapper:
    ```bash
    ./gradlew bootRun
    ```
4.  The application will start on port `8080`, and a `coupons.db` SQLite file will be created in the root directory.

---

## Coupon Use Cases & Scenarios

### Implemented Cases ✅

1.  **Cart-Wise Coupons**
    -   **Scenario**: 10% off on cart totals over ₹500.
    -   **Implementation**: The `CartWiseStrategy` checks if `cart.total >= minCartValue`. If so, it calculates a percentage-based discount on the entire cart.

2.  **Product-Wise Coupons**
    -   **Scenario**: 20% off on "Product ID: prod-123".
    -   **Implementation**: The `ProductWiseStrategy` checks if the specified product ID exists in the cart. The discount is applied only to the total price of that specific product line item.

3.  **BxGy (Buy X, Get Y) Coupons**
    -   **Scenario**: Buy 2 items from products [A, B], get 1 item of product [C] free. Repetition limit of 2.
    -   **Implementation**:
        -   The `BxGyStrategy` first counts the total quantity of eligible "buy" items in the cart.
        -   It calculates how many times the offer can be claimed (`num_applications = total_buy_items / buyQuantity`).
        -   This is capped by the `repetitionLimit`.
        -   The number of free "get" items is `num_applications * getQuantity`.
        -   To maximize customer benefit, the discount is applied to the **cheapest eligible "get" items** present in the cart.

### Unimplemented Cases & Future Considerations 🤔

The following use cases were considered but not implemented due to time constraints. The current architecture is designed to support them in the future.

1.  **Coupon Stacking / Combination Rules**
    -   **Scenario**: Can a user apply a 20% product-wise coupon *and* a 10% cart-wise coupon?
    -   **Challenge**: This requires a complex rules engine.
    -   **Possible Rules**:
        -   Allow only one cart-wise coupon per order.
        -   Allow multiple product-wise coupons on different products.
        -   Define an order of application (e.g., product discounts first, then cart discount on the remaining total).
        -   Prohibit combining certain coupon types.

2.  **Advanced Coupon Types**
    -   **Fixed Amount Discount**: e.g., "₹100 off on orders over ₹1000".
    -   **Percentage with Cap**: e.g., "20% off, up to a maximum of ₹150".
    -   **Category-Wise Discount**: e.g., "15% off all items in the 'Electronics' category".
    -   **Free Shipping**: On orders meeting certain criteria.

3.  **Advanced Coupon Constraints**
    -   **Usage Limits**:
        -   **Global**: "Valid for the first 500 users". This requires a usage counter on the coupon entity.
        -   **Per-User**: "One use per customer". This requires associating orders/coupon usage with a `userId`.
    -   **User/Segment Specific**: Coupons valid only for specific user IDs or user segments (e.g., "new users", "gold members").
    -   **Payment Method Specific**: e.g., "Extra 5% off with HDFC Bank cards".
    -   **Tiered Discounts**: "10% off on ₹1000+, 15% off on ₹2000+".

---

## Assumptions

1.  **Single Coupon Application**: The `/apply-coupon/{id}` endpoint assumes only one coupon can be applied to a cart at a time. The system does not currently support coupon stacking.
2.  **Client-Side Cart State**: The API is stateless. The entire cart object is passed in each request. The server does not maintain cart state between calls.
3.  **Authoritative Pricing**: The `price` for each item is provided in the request payload. The API does not have its own product price catalog.
4.  **BxGy Discount Logic**: For BxGy coupons, when multiple eligible "get" items are in the cart, the discount is always applied to the cheapest ones first to maximize customer savings.

---

## Limitations & Future Improvements

1.  **No Coupon Stacking**: As mentioned, the most significant limitation is the inability to combine multiple coupons. A dedicated `CouponApplicationService` with a rules engine would be needed to implement this.
2.  **Stateless Cart**: For a production system, cart state should be persisted on the server, likely linked to a user session.
3.  **Basic Validation**: Input validation is basic. It could be enhanced with more specific business rule validations (e.g., ensuring discount percentages are between 1 and 100).
4.  **Database**: The embedded SQLite database is for development convenience and is not suitable for a production environment. It should be replaced with a robust database like PostgreSQL or MySQL.
5.  **Security**: The API is not secured. In a real-world scenario, endpoints should be protected using Spring Security (e.g., JWT, OAuth2).
6.  **Asynchronous Operations**: For high-traffic scenarios, operations like checking coupon applicability could be optimized, potentially with caching layers (e.g., Redis).