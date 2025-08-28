#!/bin/bash

# Test script for Roles & Permissions Service
# Make sure the application is running on http://localhost:8080

BASE_URL="http://localhost:8080"
HEADERS="-H 'Content-Type: application/json' -H 'x-app-user-uuid: test-user-123' -H 'x-app-org-uuid: test-org-456' -H 'x-app-client-user-session-uuid: session-789' -H 'x-app-trace-id: trace-123' -H 'x-app-region-id: US'"

echo "🧪 Testing Roles & Permissions Service API"
echo "=========================================="

# 1. Get System-Managed Roles
echo -e "\n1️⃣ Testing Get System-Managed Roles"
curl -X GET "$BASE_URL/role/system-managed" $HEADERS

# 2. Create a Custom Role
echo -e "\n\n2️⃣ Testing Create Custom Role"
ROLE_DATA='{
  "role_name": "Test Manager",
  "description": "Test role for API testing",
  "organization_uuid": "test-org-456",
  "role_management_type": "customer_managed",
  "policy": "{\"data\":{\"view\":[\"task\",\"user_basic_info\"],\"edit\":[\"task\"]},\"features\":{\"execute\":[\"create_task\"]}}"
}'

CREATE_RESPONSE=$(curl -s -X POST "$BASE_URL/role" $HEADERS -d "$ROLE_DATA")
echo "$CREATE_RESPONSE"

# Extract role UUID from response
ROLE_UUID=$(echo "$CREATE_RESPONSE" | grep -o '"role_uuid":"[^"]*"' | cut -d'"' -f4)
echo "Created Role UUID: $ROLE_UUID"

# 3. Get Role by UUID
echo -e "\n\n3️⃣ Testing Get Role by UUID"
curl -X GET "$BASE_URL/role/$ROLE_UUID" $HEADERS

# 4. Get Roles by Organization
echo -e "\n\n4️⃣ Testing Get Roles by Organization"
curl -X GET "$BASE_URL/role/organization/test-org-456" $HEADERS

# 5. Assign Role to User
echo -e "\n\n5️⃣ Testing Assign Role to User"
ASSIGN_DATA='{
  "role_uuid": "'$ROLE_UUID'",
  "organization_uuid": "test-org-456"
}'

ASSIGN_RESPONSE=$(curl -s -X POST "$BASE_URL/user/test-user-123/roles" $HEADERS -d "$ASSIGN_DATA")
echo "$ASSIGN_RESPONSE"

# 6. Get User Roles
echo -e "\n\n6️⃣ Testing Get User Roles"
curl -X GET "$BASE_URL/user/test-user-123/roles?organization_uuid=test-org-456" $HEADERS

# 7. Test Permission Check (Data-Level)
echo -e "\n\n7️⃣ Testing Permission Check (Data-Level)"
PERMISSION_DATA='{
  "user_uuid": "test-user-123",
  "organization_uuid": "test-org-456",
  "action": "edit",
  "resource": "task"
}'

curl -X POST "$BASE_URL/has-permission" $HEADERS -d "$PERMISSION_DATA"

# 8. Test Permission Check (Gateway-Level)
echo -e "\n\n8️⃣ Testing Permission Check (Gateway-Level)"
GATEWAY_DATA='{
  "user_uuid": "test-user-123",
  "organization_uuid": "test-org-456",
  "endpoint": "createTask"
}'

curl -X POST "$BASE_URL/check-permission" $HEADERS -d "$GATEWAY_DATA"

# 9. Test Permission Check (Should Fail)
echo -e "\n\n9️⃣ Testing Permission Check (Should Fail)"
FAIL_DATA='{
  "user_uuid": "test-user-123",
  "organization_uuid": "test-org-456",
  "action": "edit",
  "resource": "user_sensitive_info"
}'

curl -X POST "$BASE_URL/has-permission" $HEADERS -d "$FAIL_DATA"

# 10. Remove Role from User
echo -e "\n\n🔟 Testing Remove Role from User"
curl -X DELETE "$BASE_URL/user/test-user-123/roles/$ROLE_UUID?organization_uuid=test-org-456" $HEADERS

# 11. Delete Role
echo -e "\n\n1️⃣1️⃣ Testing Delete Role"
curl -X DELETE "$BASE_URL/role/$ROLE_UUID" $HEADERS

# 12. Health Check
echo -e "\n\n1️⃣2️⃣ Testing Health Check"
curl -X GET "http://localhost:8080/actuator/health"

echo -e "\n\n✅ API Testing Complete!"
