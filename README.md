# Roles & Permissions Service

A comprehensive Spring Boot microservice for managing user roles and permissions with fine-grained access control, built with PostgreSQL, Redis caching, and Flyway migrations.

## 🎯 What is Roles & Permissions?

The Roles & Permissions Service is a centralized authorization system that provides:

- **Role-Based Access Control (RBAC)** - Define roles with specific permissions
- **Fine-Grained Permissions** - Control access to data and features at granular levels
- **Multi-Tenant Support** - Organization-scoped roles and permissions
- **Policy-Based Authorization** - JSON-based policy definitions for flexible access control
- **Caching Layer** - Redis-powered caching for high-performance permission checks
- **RESTful API** - Easy integration with other microservices

### Key Concepts

#### **Roles**
- **System-Managed Roles**: Predefined roles (Owner, Manager, User, Operator) that apply across all organizations
- **Organization-Managed Roles**: Custom roles created by organizations for their specific needs

#### **Permissions**
- **Data Permissions**: Control access to view/edit specific data types (tasks, users, clients, etc.)
- **Feature Permissions**: Control access to execute specific features (create_task, generate_reports, etc.)

#### **Policy Structure**
```json
{
  "data": {
    "view": ["task", "user_basic_info", "client"],
    "edit": ["task"]
  },
  "features": {
    "execute": ["create_task", "assign_task", "generate_reports"]
  }
}
```

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   API Gateway   │───▶│ Roles Service   │───▶│   PostgreSQL    │
│                 │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                              ▼
                       ┌─────────────────┐
                       │      Redis      │
                       │   (Caching)     │
                       └─────────────────┘
```

## 🚀 Quick Start

### Prerequisites
- Java 17+
- PostgreSQL 12+
- Redis 6+
- Gradle 7+

### Setup
1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd roles_per
   ```

2. **Configure database** (Update `src/main/resources/application.yml`)
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/your_database
       username: your_username
       password: your_password
   ```

3. **Run the application**
   ```bash
   ./gradlew bootRun
   ```

4. **Verify setup**
   ```bash
   curl http://localhost:8080/actuator/health
   ```

## 📊 Database Schema

The service uses Flyway migrations to manage the database schema:

- **V1**: Creates `roles` table with constraints and indexes
- **V2**: Creates `user_roles` table with foreign key relationships
- **V3**: Seeds system-managed roles (Owner, Manager, User, Operator)

## 🔧 API Endpoints

### Base Configuration
- **Base URL**: `http://localhost:8080`
- **Content-Type**: `application/json`

---

## 📋 Complete API Documentation

### **1. Health & Monitoring**

#### Health Check
**GET** `/actuator/health`

**Request:**
```bash
curl -X GET "http://localhost:8080/actuator/health" \
  -H "Content-Type: application/json"
```

**Response (200 OK):**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "redis": {
      "status": "UP",
      "details": {
        "version": "7.0.0"
      }
    }
  }
}
```

---

### **2. Role Management**

#### Get System-Managed Roles
**GET** `/role/system-managed`

**Request:**
```bash
curl -X GET "http://localhost:8080/role/system-managed" \
  -H "Content-Type: application/json"
```

**Response (200 OK):**
```json
[
  {
    "role_uuid": "550e8400-e29b-41d4-a716-446655440000",
    "role_name": "Owner",
    "description": "Full access to all operations across the system",
    "organization_uuid": null,
    "role_management_type": "SYSTEM_MANAGED",
    "policy": "{\"data\":{\"view\":[\"*\"],\"edit\":[\"*\"]},\"features\":{\"execute\":[\"*\"]}}",
    "created_at": "2025-08-28T16:00:00Z",
    "created_by": "system"
  },
  {
    "role_uuid": "550e8400-e29b-41d4-a716-446655440001",
    "role_name": "Manager",
    "description": "Can view/edit users, view organization, approve requests, and generate reports",
    "organization_uuid": null,
    "role_management_type": "SYSTEM_MANAGED",
    "policy": "{\"data\":{\"view\":[\"user_basic_info\",\"user_sensitive_info\",\"organization\",\"task\",\"client\"],\"edit\":[\"user_basic_info\",\"task\"]},\"features\":{\"execute\":[\"approve_requests\",\"generate_reports\",\"assign_task\"]}}",
    "created_at": "2025-08-28T16:00:00Z",
    "created_by": "system"
  },
  {
    "role_uuid": "550e8400-e29b-41d4-a716-446655440002",
    "role_name": "User",
    "description": "Can view and edit own profile only",
    "organization_uuid": null,
    "role_management_type": "SYSTEM_MANAGED",
    "policy": "{\"data\":{\"view\":[\"user_basic_info\"],\"edit\":[\"user_basic_info\"]},\"features\":{\"execute\":[]}}",
    "created_at": "2025-08-28T16:00:00Z",
    "created_by": "system"
  },
  {
    "role_uuid": "550e8400-e29b-41d4-a716-446655440003",
    "role_name": "Operator",
    "description": "System operations and monitoring capabilities",
    "organization_uuid": null,
    "role_management_type": "SYSTEM_MANAGED",
    "policy": "{\"data\":{\"view\":[\"*\"],\"edit\":[\"task\",\"client\"]},\"features\":{\"execute\":[\"system_monitoring\",\"backup_operations\",\"generate_reports\"]}}",
    "created_at": "2025-08-28T16:00:00Z",
    "created_by": "system"
  }
]
```

#### Create Custom Role
**POST** `/role`

**Request:**
```bash
curl -X POST "http://localhost:8080/role" \
  -H "Content-Type: application/json" \
  -H "x-app-user-uuid: admin-user-123" \
  -d '{
    "role_name": "Project Manager",
    "description": "Can manage projects and tasks within organization",
    "organization_uuid": "test-org-456",
    "role_management_type": "ORGANIZATION_MANAGED",
    "policy": "{\"data\":{\"view\":[\"task\",\"user_basic_info\",\"client\"],\"edit\":[\"task\"]},\"features\":{\"execute\":[\"create_task\",\"assign_task\"]}}"
  }'
```

**Response (201 Created):**
```json
{
  "role_uuid": "660e8400-e29b-41d4-a716-446655440000",
  "role_name": "Project Manager",
  "description": "Can manage projects and tasks within organization",
  "organization_uuid": "test-org-456",
  "role_management_type": "ORGANIZATION_MANAGED",
  "policy": "{\"data\":{\"view\":[\"task\",\"user_basic_info\",\"client\"],\"edit\":[\"task\"]},\"features\":{\"execute\":[\"create_task\",\"assign_task\"]}}",
  "created_at": "2025-08-28T17:00:00Z",
  "created_by": "admin-user-123"
}
```

#### Get Role by UUID
**GET** `/role/{roleUuid}`

**Request:**
```bash
curl -X GET "http://localhost:8080/role/660e8400-e29b-41d4-a716-446655440000" \
  -H "Content-Type: application/json"
```

**Response (200 OK):**
```json
{
  "role_uuid": "660e8400-e29b-41d4-a716-446655440000",
  "role_name": "Project Manager",
  "description": "Can manage projects and tasks within organization",
  "organization_uuid": "test-org-456",
  "role_management_type": "ORGANIZATION_MANAGED",
  "policy": "{\"data\":{\"view\":[\"task\",\"user_basic_info\",\"client\"],\"edit\":[\"task\"]},\"features\":{\"execute\":[\"create_task\",\"assign_task\"]}}",
  "created_at": "2025-08-28T17:00:00Z",
  "created_by": "admin-user-123"
}
```

#### Get Roles by Organization
**GET** `/role/organization/{organizationUuid}`

**Request:**
```bash
curl -X GET "http://localhost:8080/role/organization/test-org-456" \
  -H "Content-Type: application/json"
```

**Response (200 OK):**
```json
[
  {
    "role_uuid": "660e8400-e29b-41d4-a716-446655440000",
    "role_name": "Project Manager",
    "description": "Can manage projects and tasks within organization",
    "organization_uuid": "test-org-456",
    "role_management_type": "ORGANIZATION_MANAGED",
    "policy": "{\"data\":{\"view\":[\"task\",\"user_basic_info\",\"client\"],\"edit\":[\"task\"]},\"features\":{\"execute\":[\"create_task\",\"assign_task\"]}}",
    "created_at": "2025-08-28T17:00:00Z",
    "created_by": "admin-user-123"
  }
]
```

#### Update Role
**PUT** `/role/{roleUuid}`

**Request:**
```bash
curl -X PUT "http://localhost:8080/role/660e8400-e29b-41d4-a716-446655440000" \
  -H "Content-Type: application/json" \
  -d '{
    "role_name": "Senior Project Manager",
    "description": "Enhanced project management with client access",
    "organization_uuid": "test-org-456",
    "role_management_type": "ORGANIZATION_MANAGED",
    "policy": "{\"data\":{\"view\":[\"task\",\"user_basic_info\",\"client\",\"organization\"],\"edit\":[\"task\",\"client\"]},\"features\":{\"execute\":[\"create_task\",\"assign_task\",\"generate_reports\"]}}"
  }'
```

**Response (200 OK):**
```json
{
  "role_uuid": "660e8400-e29b-41d4-a716-446655440000",
  "role_name": "Senior Project Manager",
  "description": "Enhanced project management with client access",
  "organization_uuid": "test-org-456",
  "role_management_type": "ORGANIZATION_MANAGED",
  "policy": "{\"data\":{\"view\":[\"task\",\"user_basic_info\",\"client\",\"organization\"],\"edit\":[\"task\",\"client\"]},\"features\":{\"execute\":[\"create_task\",\"assign_task\",\"generate_reports\"]}}",
  "updated_at": "2025-08-28T17:15:00Z"
}
```

#### Delete Role
**DELETE** `/role/{roleUuid}`

**Request:**
```bash
curl -X DELETE "http://localhost:8080/role/660e8400-e29b-41d4-a716-446655440000" \
  -H "Content-Type: application/json"
```

**Response (200 OK):**
```json
{
  "role_uuid": "660e8400-e29b-41d4-a716-446655440000",
  "status": "deleted"
}
```

---

### **3. User Role Assignment**

#### Assign Role to User
**POST** `/user/{userUuid}/roles`

**Request:**
```bash
curl -X POST "http://localhost:8080/user/test-user-123/roles" \
  -H "Content-Type: application/json" \
  -H "x-app-user-uuid: admin-user-123" \
  -d '{
    "role_uuid": "660e8400-e29b-41d4-a716-446655440000",
    "organization_uuid": "test-org-456"
  }'
```

**Response (201 Created):**
```json
{
  "user_role_uuid": "770e8400-e29b-41d4-a716-446655440000",
  "user_uuid": "test-user-123",
  "role_uuid": "660e8400-e29b-41d4-a716-446655440000",
  "organization_uuid": "test-org-456",
  "created_at": "2025-08-28T17:05:00Z",
  "created_by": "admin-user-123"
}
```

#### Get User Roles
**GET** `/user/{userUuid}/roles?organization_uuid={organizationUuid}`

**Request:**
```bash
curl -X GET "http://localhost:8080/user/test-user-123/roles?organization_uuid=test-org-456" \
  -H "Content-Type: application/json"
```

**Response (200 OK):**
```json
[
  {
    "user_role_uuid": "770e8400-e29b-41d4-a716-446655440000",
    "user_uuid": "test-user-123",
    "role_uuid": "660e8400-e29b-41d4-a716-446655440000",
    "organization_uuid": "test-org-456",
    "created_at": "2025-08-28T17:05:00Z",
    "created_by": "admin-user-123"
  }
]
```

#### Remove Role from User
**DELETE** `/user/{userUuid}/roles/{roleUuid}?organization_uuid={organizationUuid}`

**Request:**
```bash
curl -X DELETE "http://localhost:8080/user/test-user-123/roles/660e8400-e29b-41d4-a716-446655440000?organization_uuid=test-org-456" \
  -H "Content-Type: application/json"
```

**Response (204 No Content):**
```
(Empty response body)
```

---

### **4. Permission Checking**

#### Primary Permission Check
**POST** `/permission/check`

**Request (Should PASS - Task View):**
```bash
curl -X POST "http://localhost:8080/permission/check" \
  -H "Content-Type: application/json" \
  -d '{
    "user_uuid": "test-user-123",
    "organization_uuid": "test-org-456",
    "resource": "task",
    "action": "view",
    "resource_id": "*"
  }'
```

**Response (200 OK):**
```json
{
  "has_permission": true,
  "role_uuid": "660e8400-e29b-41d4-a716-446655440000",
  "role_name": "Project Manager",
  "granted_scope": "team"
}
```

**Request (Should FAIL - Sensitive Data):**
```bash
curl -X POST "http://localhost:8080/permission/check" \
  -H "Content-Type: application/json" \
  -d '{
    "user_uuid": "test-user-123",
    "organization_uuid": "test-org-456",
    "resource": "user_sensitive_info",
    "action": "view",
    "resource_id": "*"
  }'
```

**Response (200 OK):**
```json
{
  "has_permission": false,
  "role_uuid": null,
  "role_name": null,
  "granted_scope": null
}
```

#### Legacy Permission Check
**POST** `/has-permission`

**Request:**
```bash
curl -X POST "http://localhost:8080/has-permission" \
  -H "Content-Type: application/json" \
  -d '{
    "user_uuid": "test-user-123",
    "organization_uuid": "test-org-456",
    "resource": "user_basic_info",
    "action": "view"
  }'
```

**Response (200 OK):**
```json
{
  "has_permission": true,
  "role_uuid": "660e8400-e29b-41d4-a716-446655440000",
  "role_name": "Project Manager",
  "granted_scope": "team"
}
```

#### Endpoint-Based Permission Check
**POST** `/check-permission`

**Request:**
```bash
curl -X POST "http://localhost:8080/check-permission" \
  -H "Content-Type: application/json" \
  -d '{
    "user_uuid": "test-user-123",
    "organization_uuid": "test-org-456",
    "endpoint": "GET /tasks"
  }'
```

**Response (200 OK):**
```json
{
  "has_permission": true,
  "role_uuid": "660e8400-e29b-41d4-a716-446655440000",
  "role_name": "Project Manager",
  "granted_scope": "team"
}
```

---

## 🧪 Testing

### Automated Testing Script
Run the complete test suite:
```bash
./test-api.sh
```

### Manual Testing Sequence
1. **Health Check** - Verify service is running
2. **Get System Roles** - Confirm migrations worked
3. **Create Custom Role** - Test role creation
4. **Assign Role to User** - Test user-role assignment
5. **Test Permissions** - Verify access control works
6. **Update Role** - Test role modifications
7. **Cleanup** - Remove test data

## 📊 Available Resources & Actions

### Data Resources
- `task` - Task management data
- `user_basic_info` - Basic user profile information
- `user_sensitive_info` - Sensitive user data (PII, etc.)
- `client` - Client/customer information
- `organization` - Organization settings and data

### Actions
- `view` - Read access to resources
- `edit` - Modify access to resources
- `execute` - Execute specific features/operations

### Feature Resources
- `create_task` - Create new tasks
- `assign_task` - Assign tasks to users
- `generate_reports` - Generate system reports
- `approve_requests` - Approve user requests
- `system_monitoring` - Monitor system health
- `backup_operations` - Perform system backups

### Supported Endpoints (for endpoint-based checks)
- `GET /tasks` → `view` + `task`
- `POST /tasks` → `execute` + `create_task`
- `PUT /tasks/{id}` → `edit` + `task`
- `DELETE /tasks/{id}` → `execute` + `delete_task`
- `GET /users` → `view` + `user_basic_info`
- `POST /users` → `execute` + `create_user`
- `PATCH /users/{id}` → `edit` + `user_basic_info`
- `DELETE /users/{id}` → `execute` + `delete_user`
- `GET /clients` → `view` + `client`
- `POST /clients` → `execute` + `create_client`
- `PUT /clients/{id}` → `edit` + `client`
- `DELETE /clients/{id}` → `execute` + `delete_client`
- `GET /organization` → `view` + `organization`
- `PUT /organization` → `edit` + `organization`

## 🔒 Security Features

- **JWT Integration Ready** - Configured for JWT token validation
- **CORS Support** - Cross-origin resource sharing enabled
- **Input Validation** - Request validation with detailed error messages
- **SQL Injection Protection** - Parameterized queries and JPA
- **Cache Security** - Redis-based caching with TTL
- **Audit Trail** - Created/updated by tracking

## 🚀 Performance Features

- **Redis Caching** - Permission checks cached for 1-5 minutes
- **Database Indexing** - Optimized queries with proper indexes
- **Connection Pooling** - HikariCP for database connections
- **JSON Optimization** - JSONB for policy storage with GIN indexes

## 🛠️ Configuration

### Environment Variables
```bash
# Database
POSTGRES_URL=jdbc:postgresql://localhost:5432/roles_db
POSTGRES_USERNAME=your_username
POSTGRES_PASSWORD=your_password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password

# Server
SERVER_PORT=8080
LOG_LEVEL=INFO

# JWT (if using)
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000
```

### Application Properties
Key configuration options in `application.yml`:
- Database connection settings
- Redis cache configuration
- Flyway migration settings
- Logging levels
- Security settings

## 🐛 Error Handling

### Common Error Responses

#### 400 Bad Request
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "timestamp": 1756378637.424900900,
  "details": null
}
```

#### 404 Not Found
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Resource not found",
  "timestamp": 1756378637.424900900,
  "details": null
}
```

#### 500 Internal Server Error
```json
{
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred",
  "timestamp": 1756378637.424900900,
  "details": null
}
```

## 📈 Monitoring

### Health Endpoints
- `/actuator/health` - Application health status
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus metrics

### Logging
- Structured logging with timestamps
- Configurable log levels
- SQL query logging (debug mode)
- Security event logging

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

## 📞 Support

For questions or issues:
- Create an issue in the repository
- Check the logs for detailed error messages
- Verify database and Redis connectivity
- Ensure proper environment configuration

---

**Built with ❤️ using Spring Boot, PostgreSQL, and Redis**