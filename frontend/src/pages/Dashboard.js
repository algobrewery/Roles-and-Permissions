import React, { useState, useEffect } from 'react';
import { Shield, Users, Activity, Plus, Eye, Edit, Trash2 } from 'lucide-react';
import { Link } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import { roleAPI, userRoleAPI, healthAPI } from '../services/api';
import { usePermissions } from '../contexts/PermissionContext';
import { ProtectedButton } from '../components/ProtectedComponent';

const Dashboard = () => {
  const { userPolicy, userRoles, hasPermission } = usePermissions();
  const [stats, setStats] = useState({
    totalRoles: 0,
    totalUsers: 0,
    systemHealth: 'unknown'
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    setLoading(true);
    try {
      // Load roles count
      const rolesResponse = await roleAPI.getRolesByOrganization('test-org-456');
      
      // Load system health
      const healthResponse = await healthAPI.getHealth();
      
      setStats({
        totalRoles: rolesResponse.data.length,
        totalUsers: userRoles.length,
        systemHealth: healthResponse.data.status
      });
    } catch (error) {
      console.error('Error loading dashboard data:', error);
      toast.error('Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
  };

  const StatCard = ({ title, value, icon: Icon, color, href }) => {
    const content = (
      <div className={`bg-white rounded-lg border border-gray-200 p-6 ${href ? 'hover:shadow-md transition-shadow cursor-pointer' : ''}`}>
        <div className="flex items-center">
          <div className={`flex-shrink-0 p-3 rounded-md ${color}`}>
            <Icon className="w-6 h-6 text-white" />
          </div>
          <div className="ml-4">
            <p className="text-sm font-medium text-gray-500">{title}</p>
            <p className="text-2xl font-semibold text-gray-900">{value}</p>
          </div>
        </div>
      </div>
    );

    return href ? <Link to={href}>{content}</Link> : content;
  };

  const QuickAction = ({ title, description, icon: Icon, href, permission }) => {
    if (permission && !hasPermission(permission.action, permission.resource)) {
      return null;
    }

    return (
      <Link
        to={href}
        className="bg-white rounded-lg border border-gray-200 p-6 hover:shadow-md transition-shadow"
      >
        <div className="flex items-center">
          <div className="flex-shrink-0 p-3 rounded-md bg-blue-100">
            <Icon className="w-6 h-6 text-blue-600" />
          </div>
          <div className="ml-4">
            <h3 className="text-lg font-medium text-gray-900">{title}</h3>
            <p className="text-sm text-gray-500">{description}</p>
          </div>
        </div>
      </Link>
    );
  };

  const PermissionCard = ({ title, permissions, icon: Icon }) => {
    const hasAnyPermission = permissions.some(p => hasPermission(p.action, p.resource));
    
    if (!hasAnyPermission) {
      return null;
    }

    return (
      <div className="bg-white rounded-lg border border-gray-200 p-6">
        <div className="flex items-center mb-4">
          <Icon className="w-5 h-5 text-gray-600 mr-2" />
          <h3 className="text-lg font-medium text-gray-900">{title}</h3>
        </div>
        <div className="space-y-2">
          {permissions.map((permission, index) => (
            <div key={index} className="flex items-center justify-between">
              <span className="text-sm text-gray-600">
                {permission.action} {permission.resource}
              </span>
              {hasPermission(permission.action, permission.resource) ? (
                <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800">
                  <Eye className="w-3 h-3 mr-1" />
                  Granted
                </span>
              ) : (
                <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-red-100 text-red-800">
                  <Trash2 className="w-3 h-3 mr-1" />
                  Denied
                </span>
              )}
            </div>
          ))}
        </div>
      </div>
    );
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading dashboard...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Dashboard</h1>
          <p className="mt-2 text-gray-600">
            Welcome to the Roles & Permissions Management System
          </p>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          <StatCard
            title="Total Roles"
            value={stats.totalRoles}
            icon={Shield}
            color="bg-blue-500"
            href="/roles"
          />
          <StatCard
            title="Your Roles"
            value={userRoles.length}
            icon={Users}
            color="bg-green-500"
          />
          <StatCard
            title="System Health"
            value={stats.systemHealth.toUpperCase()}
            icon={Activity}
            color={stats.systemHealth === 'UP' ? 'bg-green-500' : 'bg-red-500'}
          />
        </div>

        {/* Quick Actions */}
        <div className="mb-8">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">Quick Actions</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <QuickAction
              title="Create Role"
              description="Create a new role with custom permissions"
              icon={Plus}
              href="/roles"
              permission={{ action: 'execute', resource: 'create_role' }}
            />
            <QuickAction
              title="Test Permissions"
              description="Test your permission checks"
              icon={Activity}
              href="/permissions"
            />
            <QuickAction
              title="Manage Users"
              description="Assign roles to users"
              icon={Users}
              href="/users"
              permission={{ action: 'view', resource: 'user_basic_info' }}
            />
          </div>
        </div>

        {/* Current User Permissions */}
        {userPolicy && (
          <div className="mb-8">
            <h2 className="text-xl font-semibold text-gray-900 mb-4">Your Current Permissions</h2>
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              <PermissionCard
                title="Data Permissions"
                permissions={[
                  { action: 'view', resource: 'task' },
                  { action: 'edit', resource: 'task' },
                  { action: 'delete', resource: 'task' },
                  { action: 'view', resource: 'user_basic_info' },
                  { action: 'edit', resource: 'user_basic_info' }
                ]}
                icon={Eye}
              />
              <PermissionCard
                title="Feature Permissions"
                permissions={[
                  { action: 'execute', resource: 'create_task' },
                  { action: 'execute', resource: 'delete_task' },
                  { action: 'execute', resource: 'create_role' },
                  { action: 'execute', resource: 'delete_role' }
                ]}
                icon={Activity}
              />
            </div>
          </div>
        )}

        {/* System Status */}
        <div className="bg-white rounded-lg border border-gray-200 p-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">System Status</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <h3 className="text-lg font-medium text-gray-900 mb-2">API Health</h3>
              <div className="flex items-center">
                <div className={`w-3 h-3 rounded-full mr-2 ${
                  stats.systemHealth === 'UP' ? 'bg-green-500' : 'bg-red-500'
                }`}></div>
                <span className="text-sm text-gray-600">
                  {stats.systemHealth === 'UP' ? 'All systems operational' : 'System issues detected'}
                </span>
              </div>
            </div>
            
            <div>
              <h3 className="text-lg font-medium text-gray-900 mb-2">Policy Status</h3>
              <div className="flex items-center">
                <div className={`w-3 h-3 rounded-full mr-2 ${
                  userPolicy ? 'bg-green-500' : 'bg-yellow-500'
                }`}></div>
                <span className="text-sm text-gray-600">
                  {userPolicy ? 'Policy loaded successfully' : 'No policy loaded'}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
