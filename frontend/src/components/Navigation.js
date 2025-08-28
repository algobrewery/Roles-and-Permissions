import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Shield, Users, Settings, Activity, Home } from 'lucide-react';
import { cn } from '../utils/cn';
import { usePermissions } from '../contexts/PermissionContext';

const Navigation = () => {
  const location = useLocation();
  const { userPolicy } = usePermissions();

  const navigationItems = [
    {
      name: 'Dashboard',
      href: '/',
      icon: Home,
      permission: null // Always visible
    },
    {
      name: 'Role Management',
      href: '/roles',
      icon: Shield,
      permission: { action: 'view', resource: 'role' }
    },
    {
      name: 'Permission Tester',
      href: '/permissions',
      icon: Activity,
      permission: null // Always visible for testing
    },
    {
      name: 'User Management',
      href: '/users',
      icon: Users,
      permission: { action: 'view', resource: 'user_basic_info' }
    },
    {
      name: 'Settings',
      href: '/settings',
      icon: Settings,
      permission: { action: 'view', resource: 'settings' }
    }
  ];

  const isActive = (href) => {
    if (href === '/') {
      return location.pathname === '/';
    }
    return location.pathname.startsWith(href);
  };

  return (
    <nav className="bg-white border-b border-gray-200">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16">
          <div className="flex">
            <div className="flex-shrink-0 flex items-center">
              <Link to="/" className="flex items-center">
                <Shield className="w-8 h-8 text-blue-600" />
                <span className="ml-2 text-xl font-bold text-gray-900">
                  Roles & Permissions
                </span>
              </Link>
            </div>
            
            <div className="hidden sm:ml-6 sm:flex sm:space-x-8">
              {navigationItems.map((item) => {
                const Icon = item.icon;
                
                // Check if user has permission to see this item
                if (item.permission && !userPolicy) {
                  return null; // Hide if no policy loaded
                }
                
                return (
                  <Link
                    key={item.name}
                    to={item.href}
                    className={cn(
                      "inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium transition-colors",
                      isActive(item.href)
                        ? "border-blue-500 text-gray-900"
                        : "border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700"
                    )}
                  >
                    <Icon className="w-4 h-4 mr-2" />
                    {item.name}
                  </Link>
                );
              })}
            </div>
          </div>
          
          <div className="flex items-center">
            <div className="flex items-center space-x-4">
              <div className="text-sm text-gray-500">
                {userPolicy ? (
                  <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                    Policy Loaded
                  </span>
                ) : (
                  <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
                    No Policy
                  </span>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
      
      {/* Mobile menu */}
      <div className="sm:hidden">
        <div className="pt-2 pb-3 space-y-1">
          {navigationItems.map((item) => {
            const Icon = item.icon;
            
            if (item.permission && !userPolicy) {
              return null;
            }
            
            return (
              <Link
                key={item.name}
                to={item.href}
                className={cn(
                  "block pl-3 pr-4 py-2 border-l-4 text-base font-medium transition-colors",
                  isActive(item.href)
                    ? "bg-blue-50 border-blue-500 text-blue-700"
                    : "border-transparent text-gray-500 hover:bg-gray-50 hover:border-gray-300 hover:text-gray-700"
                )}
              >
                <div className="flex items-center">
                  <Icon className="w-4 h-4 mr-3" />
                  {item.name}
                </div>
              </Link>
            );
          })}
        </div>
      </div>
    </nav>
  );
};

export default Navigation;
