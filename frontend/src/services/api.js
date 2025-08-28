import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add user UUID header
api.interceptors.request.use((config) => {
  const userUuid = localStorage.getItem('userUuid');
  if (userUuid) {
    config.headers['x-app-user-uuid'] = userUuid;
  }
  return config;
});

// Response interceptor for error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', error.response?.data || error.message);
    return Promise.reject(error);
  }
);

// Role Management APIs
export const roleAPI = {
  // Create a new role
  createRole: (roleData) => api.post('/role', roleData),
  
  // Get role by UUID
  getRole: (roleUuid) => api.get(`/role/${roleUuid}`),
  
  // Update role
  updateRole: (roleUuid, roleData) => api.put(`/role/${roleUuid}`, roleData),
  
  // Delete role
  deleteRole: (roleUuid) => api.delete(`/role/${roleUuid}`),
  
  // Get roles by organization
  getRolesByOrganization: (organizationUuid) => 
    api.get(`/role/organization/${organizationUuid}`),
  
  // Get system-managed roles
  getSystemManagedRoles: () => api.get('/role/system-managed'),
};

// User-Role Assignment APIs
export const userRoleAPI = {
  // Assign role to user
  assignRoleToUser: (userUuid, roleData) => 
    api.post(`/user/${userUuid}/roles`, roleData),
  
  // Get user roles
  getUserRoles: (userUuid, organizationUuid) => 
    api.get(`/user/${userUuid}/roles?organization_uuid=${organizationUuid}`),
  
  // Remove role from user
  removeRoleFromUser: (userUuid, roleUuid, organizationUuid) => 
    api.delete(`/user/${userUuid}/roles/${roleUuid}?organization_uuid=${organizationUuid}`),
};

// Permission Check APIs
export const permissionAPI = {
  // Check permission (standard)
  checkPermission: (permissionData) => api.post('/permission/check', permissionData),
  
  // Check permission (legacy)
  hasPermission: (permissionData) => api.post('/has-permission', permissionData),
  
  // Check permission (API Gateway)
  checkPermissionByEndpoint: (permissionData) => api.post('/check-permission', permissionData),
};

// Health Check APIs
export const healthAPI = {
  // Health check
  getHealth: () => api.get('/actuator/health'),
  
  // Application info
  getInfo: () => api.get('/actuator/info'),
  
  // Metrics
  getMetrics: () => api.get('/actuator/metrics'),
};

export default api;
