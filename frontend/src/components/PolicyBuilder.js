import React, { useState, useEffect } from 'react';
import { Plus, X, Eye, Edit, Trash2, Play } from 'lucide-react';
import { cn } from '../utils/cn';

const PolicyBuilder = ({ 
  initialPolicy = null, 
  onPolicyChange, 
  className = '' 
}) => {
  const [policy, setPolicy] = useState({
    data: { view: [], edit: [], delete: [] },
    features: { execute: [] }
  });

  useEffect(() => {
    if (initialPolicy) {
      setPolicy(initialPolicy);
    }
  }, [initialPolicy]);

  const handlePolicyChange = (newPolicy) => {
    setPolicy(newPolicy);
    onPolicyChange?.(newPolicy);
  };

  const addPermission = (section, action) => {
    const newPolicy = { ...policy };
    const newResource = prompt(`Enter ${action} permission resource:`);
    
    if (newResource && newResource.trim()) {
      if (section === 'data') {
        newPolicy.data[action] = [...newPolicy.data[action], newResource.trim()];
      } else {
        newPolicy.features[action] = [...newPolicy.features[action], newResource.trim()];
      }
      handlePolicyChange(newPolicy);
    }
  };

  const removePermission = (section, action, index) => {
    const newPolicy = { ...policy };
    
    if (section === 'data') {
      newPolicy.data[action] = newPolicy.data[action].filter((_, i) => i !== index);
    } else {
      newPolicy.features[action] = newPolicy.features[action].filter((_, i) => i !== index);
    }
    
    handlePolicyChange(newPolicy);
  };

  const PermissionSection = ({ title, section, actions, icon: Icon }) => (
    <div className="bg-white rounded-lg border border-gray-200 p-6 mb-6">
      <div className="flex items-center gap-2 mb-4">
        <Icon className="w-5 h-5 text-gray-600" />
        <h3 className="text-lg font-semibold text-gray-900">{title}</h3>
      </div>
      
      <div className="space-y-4">
        {actions.map((action) => (
          <div key={action} className="border border-gray-200 rounded-lg p-4">
            <div className="flex items-center justify-between mb-3">
              <h4 className="font-medium text-gray-700 capitalize">{action}</h4>
              <button
                onClick={() => addPermission(section, action)}
                className="flex items-center gap-1 px-3 py-1 text-sm bg-blue-50 text-blue-600 rounded-md hover:bg-blue-100 transition-colors"
              >
                <Plus className="w-4 h-4" />
                Add
              </button>
            </div>
            
            <div className="flex flex-wrap gap-2">
              {policy[section][action].map((resource, index) => (
                <div
                  key={index}
                  className="flex items-center gap-2 px-3 py-1 bg-gray-100 text-gray-700 rounded-md"
                >
                  <span className="text-sm">{resource}</span>
                  <button
                    onClick={() => removePermission(section, action, index)}
                    className="text-gray-400 hover:text-red-500 transition-colors"
                  >
                    <X className="w-4 h-4" />
                  </button>
                </div>
              ))}
              
              {policy[section][action].length === 0 && (
                <p className="text-gray-500 text-sm italic">No permissions added</p>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );

  return (
    <div className={cn("space-y-6", className)}>
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
        <h3 className="text-lg font-semibold text-blue-900 mb-2">Policy Builder</h3>
        <p className="text-blue-700 text-sm">
          Define permissions for this role. Add resources that users with this role can view, edit, delete, or execute.
        </p>
      </div>

      {/* Data Permissions */}
      <PermissionSection
        title="Data Permissions"
        section="data"
        actions={['view', 'edit', 'delete']}
        icon={Eye}
      />

      {/* Feature Permissions */}
      <PermissionSection
        title="Feature Permissions"
        section="features"
        actions={['execute']}
        icon={Play}
      />

      {/* Policy Preview */}
      <div className="bg-gray-50 rounded-lg border border-gray-200 p-6">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">Policy Preview</h3>
        <pre className="bg-white border border-gray-200 rounded-lg p-4 text-sm text-gray-700 overflow-x-auto">
          {JSON.stringify(policy, null, 2)}
        </pre>
      </div>
    </div>
  );
};

export default PolicyBuilder;
