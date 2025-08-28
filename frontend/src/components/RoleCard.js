import React from 'react';
import { Edit, Trash2, Eye, Shield, Calendar } from 'lucide-react';
import { cn } from '../utils/cn';

const RoleCard = ({ 
  role, 
  onEdit, 
  onDelete, 
  onView,
  className = '' 
}) => {
  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const getRoleTypeColor = (type) => {
    switch (type) {
      case 'CUSTOMER_MANAGED':
        return 'bg-blue-100 text-blue-800';
      case 'SYSTEM_MANAGED':
        return 'bg-purple-100 text-purple-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const getRoleTypeIcon = (type) => {
    switch (type) {
      case 'CUSTOMER_MANAGED':
        return <Shield className="w-4 h-4" />;
      case 'SYSTEM_MANAGED':
        return <Eye className="w-4 h-4" />;
      default:
        return <Shield className="w-4 h-4" />;
    }
  };

  return (
    <div className={cn("bg-white rounded-lg border border-gray-200 p-6 hover:shadow-md transition-shadow", className)}>
      <div className="flex items-start justify-between mb-4">
        <div className="flex-1">
          <div className="flex items-center gap-2 mb-2">
            <h3 className="text-lg font-semibold text-gray-900">{role.role_name}</h3>
            <span className={cn(
              "inline-flex items-center gap-1 px-2 py-1 text-xs font-medium rounded-full",
              getRoleTypeColor(role.role_management_type)
            )}>
              {getRoleTypeIcon(role.role_management_type)}
              {role.role_management_type.replace('_', ' ')}
            </span>
          </div>
          
          {role.description && (
            <p className="text-gray-600 text-sm mb-3">{role.description}</p>
          )}
          
          <div className="flex items-center gap-4 text-xs text-gray-500">
            <div className="flex items-center gap-1">
              <Calendar className="w-3 h-3" />
              Created: {formatDate(role.created_at)}
            </div>
            {role.updated_at !== role.created_at && (
              <div className="flex items-center gap-1">
                <Calendar className="w-3 h-3" />
                Updated: {formatDate(role.updated_at)}
              </div>
            )}
          </div>
        </div>
        
        <div className="flex items-center gap-2">
          {onView && (
            <button
              onClick={() => onView(role)}
              className="p-2 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-md transition-colors"
              title="View Role"
            >
              <Eye className="w-4 h-4" />
            </button>
          )}
          
          {onEdit && (
            <button
              onClick={() => onEdit(role)}
              className="p-2 text-gray-400 hover:text-green-600 hover:bg-green-50 rounded-md transition-colors"
              title="Edit Role"
            >
              <Edit className="w-4 h-4" />
            </button>
          )}
          
          {onDelete && (
            <button
              onClick={() => onDelete(role)}
              className="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-md transition-colors"
              title="Delete Role"
            >
              <Trash2 className="w-4 h-4" />
            </button>
          )}
        </div>
      </div>
      
      <div className="border-t border-gray-100 pt-4">
        <div className="flex items-center justify-between text-sm">
          <span className="text-gray-500">Role UUID:</span>
          <code className="text-gray-700 bg-gray-100 px-2 py-1 rounded text-xs">
            {role.role_uuid}
          </code>
        </div>
        
        {role.organization_uuid && (
          <div className="flex items-center justify-between text-sm mt-2">
            <span className="text-gray-500">Organization:</span>
            <code className="text-gray-700 bg-gray-100 px-2 py-1 rounded text-xs">
              {role.organization_uuid}
            </code>
          </div>
        )}
      </div>
    </div>
  );
};

export default RoleCard;
