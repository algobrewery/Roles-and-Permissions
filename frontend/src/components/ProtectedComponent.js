import React from 'react';
import { usePermissions } from '../contexts/PermissionContext';

const ProtectedComponent = ({ 
  action, 
  resource, 
  children, 
  fallback = null,
  className = '',
  ...props 
}) => {
  const { hasPermission } = usePermissions();

  if (!hasPermission(action, resource)) {
    return fallback;
  }

  return (
    <div className={className} {...props}>
      {children}
    </div>
  );
};

// Higher-order component for protecting components
export const withPermission = (WrappedComponent, action, resource) => {
  return function ProtectedComponent(props) {
    const { hasPermission } = usePermissions();
    
    if (!hasPermission(action, resource)) {
      return null;
    }
    
    return <WrappedComponent {...props} />;
  };
};

// Protected button component
export const ProtectedButton = ({ 
  action, 
  resource, 
  children, 
  className = '',
  disabled = false,
  ...props 
}) => {
  const { hasPermission } = usePermissions();

  if (!hasPermission(action, resource)) {
    return null;
  }

  return (
    <button 
      className={className} 
      disabled={disabled}
      {...props}
    >
      {children}
    </button>
  );
};

// Protected link component
export const ProtectedLink = ({ 
  action, 
  resource, 
  children, 
  className = '',
  ...props 
}) => {
  const { hasPermission } = usePermissions();

  if (!hasPermission(action, resource)) {
    return null;
  }

  return (
    <a className={className} {...props}>
      {children}
    </a>
  );
};

export default ProtectedComponent;
