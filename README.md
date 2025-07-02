# ZzzTime Backend API - Sleep Monitoring REST API

## License

This code is published under a proprietary license with all rights reserved. See LICENSE for details.

## Overview

ZzzTime Backend API is a comprehensive REST API built with Spring Boot that powers the ZzzTime sleep monitoring mobile application. It provides intelligent sleep analysis, AI-powered recommendations, wearable device integration, and personalized health insights through a secure and scalable backend architecture.

## Key Features

- **JWT Authentication & Authorization**: Secure user authentication with role-based access control
- **AI-Powered Chat System**: Integration with Google Gemini AI for personalized sleep recommendations
- **Fitbit Integration**: OAuth-based connection with Fitbit API for physiological data synchronization
- **Sleep Analytics**: Comprehensive sleep pattern analysis and disorder risk management (DRM)
- **Content Management**: Dynamic tips, relaxation sounds, and personalized recommendations
- **Cloud Storage**: Seamless media upload and management through Cloudinary integration
- **User Configuration**: Flexible flag-based system for personalized user experiences
- **Real-time Data Processing**: Advanced sleep log processing and trend analysis

## Technology Stack

- **Framework**: Spring Boot 3.4.2 with Java 17
- **Database**: PostgreSQL with JPA/Hibernate
- **Security**: Spring Security with JWT authentication
- **AI Integration**: Google Gemini AI API
- **External APIs**: Fitbit OAuth 2.0, Cloudinary Cloud Storage
- **Documentation**: Swagger/OpenAPI 3.0
- **Build Tool**: Maven
- **Testing**: Spring Boot Test Suite

## API Architecture

### Core Modules

- **Authentication System**: JWT-based secure authentication with refresh tokens
- **User Management**: Complete user lifecycle management with profile customization
- **Chat & AI Services**: Intelligent conversational AI for sleep recommendations
- **Sleep Monitoring**: Comprehensive sleep tracking and analysis
- **Fitbit Integration**: Real-time health data synchronization
- **Content Management**: Dynamic tips and audio content delivery
- **Analytics Engine**: Sleep pattern analysis and reporting

### Database Design

```
├── Users & Authentication
├── Sleep Logs & Analytics
├── Chat System & Messages
├── Tips & Content Management
├── Fitbit Integration Data
├── User Configuration Flags
└── DRM Reports & Analysis
```

## Main Controllers & Endpoints

- **AuthController**: User authentication, registration, and token management
- **UserController**: User profile management and account operations
- **ChatController**: AI-powered chat functionality and message handling
- **SleepLogController**: Sleep data tracking and analysis
- **FitbitController**: Wearable device integration and data sync
- **TipController**: Personalized recommendations and content delivery
- **DrmController**: Sleep disorder risk assessment and reporting
- **SoundController**: Audio content management and streaming

## Core Services

- **GeminiService**: AI integration for intelligent sleep recommendations
- **FitbitService**: OAuth authentication and data synchronization with Fitbit
- **ChatService**: Conversation management and AI response processing
- **SleepLogService**: Sleep data analysis and pattern recognition
- **UserService**: User management and profile operations
- **TipService**: Content personalization and recommendation engine
- **CloudinaryService**: Media upload and cloud storage management
- **DrmService**: Sleep disorder risk assessment and report generation

## Security Features

- **JWT Authentication**: Secure token-based authentication system
- **Role-based Authorization**: Granular access control with method-level security
- **Password Encryption**: BCrypt-based secure password hashing
- **CORS Configuration**: Cross-origin resource sharing setup for mobile clients
- **Request Validation**: Comprehensive input validation and sanitization
- **Ownership Verification**: User-specific data access control

## External Integrations

### Google Gemini AI

- Personalized sleep recommendations
- Intelligent chat responses
- Context-aware health insights

### Fitbit API

- OAuth 2.0 authentication flow
- Real-time health data synchronization
- Sleep metrics and physiological data

### Cloudinary

- Secure media upload and storage
- Image optimization and transformation
- CDN-based content delivery

## Database Schema

- **User Management**: Users, authentication tokens, and profile data
- **Sleep Tracking**: Sleep logs, answers, and quality metrics
- **Chat System**: Conversations, messages, and AI responses
- **Content**: Tips, sounds, and personalized recommendations
- **Configuration**: User flags and preference settings
- **Health Data**: Fitbit integration and physiological metrics

## Development Features

- **RESTful Design**: Clean and intuitive API endpoints
- **Swagger Documentation**: Comprehensive API documentation and testing interface
- **Exception Handling**: Centralized error handling and response formatting
- **Data Validation**: Bean validation with custom validators
- **Logging**: Comprehensive application logging and monitoring
- **Hot Reload**: Development-time automatic restart and reload

## Configuration

- **Profile-based Configuration**: Environment-specific property files
- **External Service Configuration**: API keys and integration settings
- **Database Configuration**: PostgreSQL connection and JPA settings
- **Security Configuration**: JWT settings and authentication rules

---

_This REST API was developed as part of a final degree project focusing on digital health solutions and AI-powered wellness applications._
