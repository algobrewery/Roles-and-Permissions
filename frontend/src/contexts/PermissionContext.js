import React, { createContext, useContext, useState, useEffect } from 'react';
import { userRoleAPI } from '../services/api';

const PermissionContext = createContext();

export const usePermissions = () => {
  const context = useContext(PermissionContext);
  if (!context) {
    throw new Error('usePermissions must be used within a PermissionProvider');
  }
  return context;
};

export const PermissionProvider = ({ children }) => {
  const [userPolicy, setUserPolicy] = useState(null);
  const [userRoles, setUserRoles] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Load user permissions on mount
  useEffect(() => {
    const userUuid = localStorage.getItem('userUuid');
    const organizationUuid = localStorage.getItem('organizationUuid');
    
    if (userUuid && organizationUuid) {
      loadUserPermissions(userUuid, organizationUuid);
    }
  }, []);

  const loadUserPermissions = async (userUuid, organizationUuid) => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await userRoleAPI.getUserRoles(userUuid, organizationUuid);
      setUserRoles(response.data);
      
      // Combine all role policies into a single policy
      const combinedPolicy = combineRolePolicies(response.data);
      setUserPolicy(combinedPolicy);
    } catch (err) {
      setError(err.message);
      console.error('Failed to load user permissions:', err);
    } finally {
      setLoading(false);
    }
  };

  const combineRolePolicies = (roles) => {
    const combinedPolicy = {
      data: { view: [], edit: [], delete: [] },
      features: { execute: [] }
    };

    roles.forEach(role => {
      try {
        const policy = JSON.parse(role.policy);
        
        // Combine data permissions
        if (policy.data) {
          Object.keys(policy.data).forEach(action => {
            if (policy.data[action]) {
              combinedPolicy.data[action] = [
                ...new Set([...combinedPolicy.data[action], ...policy.data[action]])
              ];
            }
          });
        }
        
        // Combine feature permissions
        if (policy.features) {
          Object.keys(policy.features).forEach(action => {
            if (policy.features[action]) {
              combinedPolicy.features[action] = [
                ...new Set([...combinedPolicy.features[action], ...policy.features[action]])
              ];
            }
          });
        }
      } catch (err) {
        console.error('Failed to parse role policy:', err);
      }
    });

    return combinedPolicy;
  };

  const hasPermission = (action, resource) => {
    if (!userPolicy) return false;
    
    // Check for wildcard permissions
    if (action === 'view' || action === 'edit' || action === 'delete') {
      return userPolicy.data[action]?.includes(resource) || 
             userPolicy.data[action]?.includes('*');
    }
    
    if (action === 'execute') {
      return userPolicy.features.execute?.includes(resource) || 
             userPolicy.features.execute?.includes('*');
    }
    
    return false;
  };

  const hasAnyPermission = (action, resources) => {
    return resources.some(resource => hasPermission(action, resource));
  };

  const hasAllPermissions = (action, resources) => {
    return resources.every(resource => hasPermission(action, resource));
  };

  const refreshPermissions = () => {
    const userUuid = localStorage.getItem('userUuid');
    const organizationUuid = localStorage.getItem('organizationUuid');
    
    if (userUuid && organizationUuid) {
      loadUserPermissions(userUuid, organizationUuid);
    }
  };

  const value = {
    userPolicy,
    userRoles,
    loading,
    error,
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
    refreshPermissions,
    loadUserPermissions
  };

  return (
    <PermissionContext.Provider value={value}>
      {children}
    </PermissionContext.Provider>
  );
};
