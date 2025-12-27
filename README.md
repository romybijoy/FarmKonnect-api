# 🌾 FarmKonnect

FarmKonnect is a social communication platform designed for farmers and agricultural communities to connect, share knowledge, and collaborate digitally.
It enables users to post updates, follow others, chat in real time, form groups, and receive personalized suggestions based on location.

## 🚀 Features

### 👤 User & Profile

- User registration and authentication (JWT based)
- Profile management (name, bio, district, profile picture)
- Email verification
- Profile statistics (posts, followers, following)

### 🤝 Social Connections

- Follow / Unfollow users
- Followers & following count
- User suggestions (based on district and activity)
- Notifications for follow actions

### 📝 Posts & Feed

- Create, edit, and delete posts
- Like and save posts
- Personalized feed
- Post count per user

### 💬 Chat & Groups

- One-to-one chat
- Group chat functionality
- Add / remove group members
- Real-time messaging using WebSocket
- Online / last-seen presence

### 📞 Audio / Video Calls

- WhatsApp-style audio & video calling
- WebRTC integration
- Incoming call popup
- Accept / reject call flow

### 🔔 Notifications

- Follow notifications


## Microservice Architecture

FarmKonnect is built using a microservice architecture for scalability and maintainability.
Backend Services

  auth-service → Authentication & JWT

  user-service → User profiles, followers, suggestions

  post-service → Posts, likes, saves

  chat-service → Messaging, groups, presence, WebSocket

  notification-service → Notifications

  story-service → Stories

  api-gateway → Central request routing

  eureka-server → Service discovery

## Communication

    REST APIs (client ↔ services)
    gRPC (service ↔ service)
    WebSocket (real-time chat & presence)

## 🖥️ Tech Stack

  ### Frontend

      React (Vite)
      Redux Toolkit
      Tailwind CSS
      WebSocket
      WebRTC
      Fetch API

  ### Backend

      Java 17
      Spring Boot
      Spring Security (JWT)
      Spring Data JPA
      Spring Cloud Gateway
      gRPC
      WebSocket (STOMP)

  ### Database & Infra

      MySQL
      MongoDB
      Redis (presence, caching)
      Docker & Docker Compose
