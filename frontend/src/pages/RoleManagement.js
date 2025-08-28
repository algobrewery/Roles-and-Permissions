import React, { useState, useEffect } from 'react';
import { Plus, Search, Filter, RefreshCw } from 'lucide-react';
import { toast } from 'react-hot-toast';
import { roleAPI } from '../services/api';
import RoleCard from '../components/RoleCard';
import RoleForm from '../components/RoleForm';
import { ProtectedButton } from '../components/ProtectedComponent';

const RoleManagement = () => {
  const [roles, setRoles] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showForm, setShowForm] = useState(false);
  const [editingRole, setEditingRole] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterType, setFilterType] = useState('all');
  const [organizationUuid, setOrganizationUuid] = useState('test-org-456');

  useEffect(() => {
    loadRoles();
  }, [organizationUuid]);

  const loadRoles = async () => {
    setLoading(true);
    try {
      const response = await roleAPI.getRolesByOrganization(organizationUuid);
      setRoles(response.data);
    } catch (error) {
      toast.error('Failed to load roles');
      console.error('Error loading roles:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateRole = async (roleData) => {
    try {
      await roleAPI.createRole(roleData);
      toast.success('Role created successfully');
      setShowForm(false);
      loadRoles();
    } catch (error) {
      toast.error('Failed to create role');
      console.error('Error creating role:', error);
    }
  };

  const handleUpdateRole = async (roleData) => {
    try {
      await roleAPI.updateRole(editingRole.role_uuid, roleData);
      toast.success('Role updated successfully');
      setEditingRole(null);
      setShowForm(false);
      loadRoles();
    } catch (error) {
      toast.error('Failed to update role');
      console.error('Error updating role:', error);
    }
  };

  const handleDeleteRole = async (role) => {
    if (window.confirm(`Are you sure you want to delete the role "${role.role_name}"?`)) {
      try {
        await roleAPI.deleteRole(role.role_uuid);
        toast.success('Role deleted successfully');
        loadRoles();
      } catch (error) {
        toast.error('Failed to delete role');
        console.error('Error deleting role:', error);
      }
    }
  };

  const handleEditRole = (role) => {
    setEditingRole(role);
    setShowForm(true);
  };

  const handleViewRole = (role) => {
    // You can implement a modal or navigate to a detail view
    console.log('Viewing role:', role);
  };

  const filteredRoles = roles.filter(role => {
    const matchesSearch = role.role_name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         role.description?.toLowerCase().includes(searchTerm.toLowerCase());
    
    const matchesFilter = filterType === 'all' || role.role_management_type === filterType;
    
    return matchesSearch && matchesFilter;
  });

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <div className="mb-8">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold text-gray-900">Role Management</h1>
              <p className="mt-2 text-gray-600">
                Create and manage roles with custom permissions for your organization
              </p>
            </div>
            
            <ProtectedButton
              action="execute"
              resource="create_role"
              onClick={() => setShowForm(true)}
              className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
            >
              <Plus className="w-4 h-4" />
              Create Role
            </ProtectedButton>
          </div>
        </div>

        {/* Filters and Search */}
        <div className="bg-white rounded-lg border border-gray-200 p-6 mb-6">
          <div className="flex flex-col sm:flex-row gap-4">
            <div className="flex-1">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
                <input
                  type="text"
                  placeholder="Search roles..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>
            </div>
            
            <div className="flex gap-4">
              <div className="relative">
                <Filter className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
                <select
                  value={filterType}
                  onChange={(e) => setFilterType(e.target.value)}
                  className="pl-10 pr-8 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent appearance-none bg-white"
                >
                  <option value="all">All Types</option>
                  <option value="CUSTOMER_MANAGED">Customer Managed</option>
                  <option value="SYSTEM_MANAGED">System Managed</option>
                </select>
              </div>
              
              <button
                onClick={loadRoles}
                disabled={loading}
                className="flex items-center gap-2 px-4 py-2 text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:opacity-50 transition-colors"
              >
                <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
                Refresh
              </button>
            </div>
          </div>
        </div>

        {/* Role Form Modal */}
        {showForm && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-lg max-w-4xl w-full max-h-[90vh] overflow-y-auto">
              <RoleForm
                role={editingRole}
                onSubmit={editingRole ? handleUpdateRole : handleCreateRole}
                onCancel={() => {
                  setShowForm(false);
                  setEditingRole(null);
                }}
                loading={loading}
              />
            </div>
          </div>
        )}

        {/* Roles Grid */}
        {loading ? (
          <div className="flex items-center justify-center py-12">
            <RefreshCw className="w-8 h-8 animate-spin text-blue-600" />
            <span className="ml-2 text-gray-600">Loading roles...</span>
          </div>
        ) : filteredRoles.length === 0 ? (
          <div className="text-center py-12">
            <div className="text-gray-400 mb-4">
              <Plus className="w-16 h-16 mx-auto" />
            </div>
            <h3 className="text-lg font-medium text-gray-900 mb-2">No roles found</h3>
            <p className="text-gray-600">
              {searchTerm || filterType !== 'all' 
                ? 'Try adjusting your search or filter criteria'
                : 'Get started by creating your first role'
              }
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredRoles.map((role) => (
              <RoleCard
                key={role.role_uuid}
                role={role}
                onEdit={handleEditRole}
                onDelete={handleDeleteRole}
                onView={handleViewRole}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default RoleManagement;
