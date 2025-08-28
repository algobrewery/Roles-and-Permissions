import React, { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { Save, X } from 'lucide-react';
import { cn } from '../utils/cn';
import PolicyBuilder from './PolicyBuilder';

const RoleForm = ({ 
  role = null, 
  onSubmit, 
  onCancel, 
  loading = false,
  className = '' 
}) => {
  const [policy, setPolicy] = useState({
    data: { view: [], edit: [], delete: [] },
    features: { execute: [] }
  });

  const {
    register,
    handleSubmit,
    formState: { errors },
    setValue,
    watch
  } = useForm({
    defaultValues: {
      role_name: role?.role_name || '',
      description: role?.description || '',
      organization_uuid: role?.organization_uuid || 'test-org-456',
      role_management_type: role?.role_management_type || 'CUSTOMER_MANAGED'
    }
  });

  useEffect(() => {
    if (role?.policy) {
      try {
        const parsedPolicy = JSON.parse(role.policy);
        setPolicy(parsedPolicy);
      } catch (err) {
        console.error('Failed to parse role policy:', err);
      }
    }
  }, [role]);

  const handleFormSubmit = (data) => {
    const formData = {
      ...data,
      policy: JSON.stringify(policy)
    };
    onSubmit(formData);
  };

  const roleManagementType = watch('role_management_type');

  return (
    <form onSubmit={handleSubmit(handleFormSubmit)} className={cn("space-y-6", className)}>
      <div className="bg-white rounded-lg border border-gray-200 p-6">
        <h3 className="text-lg font-semibold text-gray-900 mb-6">
          {role ? 'Edit Role' : 'Create New Role'}
        </h3>

        {/* Basic Information */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Role Name *
            </label>
            <input
              type="text"
              {...register('role_name', { 
                required: 'Role name is required',
                maxLength: { value: 100, message: 'Role name must not exceed 100 characters' }
              })}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="Enter role name"
            />
            {errors.role_name && (
              <p className="mt-1 text-sm text-red-600">{errors.role_name.message}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Role Management Type *
            </label>
            <select
              {...register('role_management_type', { required: 'Role management type is required' })}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
              <option value="CUSTOMER_MANAGED">Customer Managed</option>
              <option value="SYSTEM_MANAGED">System Managed</option>
            </select>
            {errors.role_management_type && (
              <p className="mt-1 text-sm text-red-600">{errors.role_management_type.message}</p>
            )}
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Description
          </label>
          <textarea
            {...register('description', { 
              maxLength: { value: 255, message: 'Description must not exceed 255 characters' }
            })}
            rows={3}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            placeholder="Enter role description"
          />
          {errors.description && (
            <p className="mt-1 text-sm text-red-600">{errors.description.message}</p>
          )}
        </div>

        {roleManagementType === 'CUSTOMER_MANAGED' && (
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Organization UUID *
            </label>
            <input
              type="text"
              {...register('organization_uuid', { required: 'Organization UUID is required' })}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="Enter organization UUID"
            />
            {errors.organization_uuid && (
              <p className="mt-1 text-sm text-red-600">{errors.organization_uuid.message}</p>
            )}
          </div>
        )}
      </div>

      {/* Policy Builder */}
      <PolicyBuilder
        initialPolicy={policy}
        onPolicyChange={setPolicy}
      />

      {/* Form Actions */}
      <div className="flex justify-end gap-4">
        <button
          type="button"
          onClick={onCancel}
          className="px-4 py-2 text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-colors"
        >
          <X className="w-4 h-4 inline mr-2" />
          Cancel
        </button>
        <button
          type="submit"
          disabled={loading}
          className="px-4 py-2 text-white bg-blue-600 border border-transparent rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        >
          <Save className="w-4 h-4 inline mr-2" />
          {loading ? 'Saving...' : (role ? 'Update Role' : 'Create Role')}
        </button>
      </div>
    </form>
  );
};

export default RoleForm;
