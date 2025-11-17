# Food Delivery System (Swiggy/Zomato Backend Clone)

A production-style **microservices-based Food Delivery System** built using  
**Spring Boot, MySQL, Redis, Kafka, Docker**, following real-world  
distributed system patterns used by Swiggy, Zomato, Uber Eats, DoorDash.

This project demonstrates:

- Microservices architecture  
- Event-driven communication (Kafka)  
- Redis GEO for delivery partner location tracking  
- Async order lifecycle  
- Payment workflow with idempotency  
- Service-to-Service communication  
- Proper layered architecture (Controller → Service → Repository)  

---

# Features

###  Restaurant Service  
- Restaurant CRUD  
- Menu CRUD  
- Fetch restaurants, menu items  
- Coordinates stored for delivery assignment

###  Order Service  
- Create orders  
- Validate menu items & total  
- Stores order state (CREATED → PAID → ASSIGNED → OUT_FOR_DELIVERY → DELIVERED)  
- Publishes `order_created` event  

###  Delivery Service  
- Stores rider location using **Redis GEO**  
- Rider availability: AVAILABLE / BUSY / OFFLINE  
- Consumes `order_created`  
- Finds nearest rider  
- Assigns rider atomically  
- Publishes `delivery_assigned` event  
- Exposes rider tracking endpoints  

###  Payment Service  
- Simulated payment gateway  
- Idempotent payment API  
- Publishes `payment_success` event  
- Order service updates to PAID once payment succeeds  

---

#Architecture Overview

                    ┌────────────────────┐
                    │   API Client /     │
                    │   Postman / UI     │
                    └─────────┬──────────┘
                              │
             ┌────────────────┴─────────────────┐
             │           API Gateway            │ (optional)
             └─────────────┬────────────────────┘
                           │
┌──────────────────────────────────────────────────────────────────────┐
│                        Microservices Layer                           │
│                                                                      │
│  ┌────────────────────┐      ┌────────────────────┐                  │
│  │ Restaurant Service │      │   Order Service    │                  │
│  └───────────┬────────┘      └──────────┬─────────┘                  │
│              │                           │                           │
│              │        (Kafka Event)      │                           │
│              └──────── order_created ────────┐                       │
│                                              ▼                       │
│                                    ┌────────────────────┐            │
│                                    │ Delivery Service   │            │
│                                    └─────────┬──────────┘            │
│                                              │                       │
│                          (Kafka Event)        │                      │
│              ┌──────── payment_success ◄──────┘                      │
│              │                                                       │
│  ┌───────────▼──────────┐                                            │
│  │   Payment Service     │                                           │
│  └───────────────────────┘                                           │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘




