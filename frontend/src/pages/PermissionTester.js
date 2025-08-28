import React, { useState } from 'react';
import { Shield, Check, X, Play } from 'lucide-react';
import { toast } from 'react-hot-toast';
import { permissionAPI } from '../services/api';
import { usePermissions } from '../contexts/PermissionContext';

const PermissionTester = () => {
  const { userPolicy, hasPermission } = usePermissions();
  const [testResults, setTestResults] = useState([]);
  const [loading, setLoading] = useState(false);

  const [testData, setTestData] = useState({
    user_uuid: 'test-user-123',
    organization_uuid: 'test-org-456',
    action: 'view',
    resource: 'task',
    endpoint: 'GET /tasks'
  });

  const runPermissionTest = async (testType, data) => {
    setLoading(true);
    try {
      let response;
      
      switch (testType) {
        case 'standard':
          response = await permissionAPI.checkPermission(data);
          break;
        case 'legacy':
          response = await permissionAPI.hasPermission(data);
          break;
        case 'gateway':
          response = await permissionAPI.checkPermissionByEndpoint(data);
          break;
        default:
          throw new Error('Invalid test type');
      }

      const result = {
        id: Date.now(),
        type: testType,
        data: { ...data },
        response: response.data,
        timestamp: new Date().toISOString(),
        success: true
      };

      setTestResults(prev => [result, ...prev]);
      toast.success(`${testType} permission test completed`);
    } catch (error) {
      const result = {
        id: Date.now(),
        type: testType,
        data: { ...data },
        error: error.response?.data || error.message,
        timestamp: new Date().toISOString(),
        success: false
      };

      setTestResults(prev => [result, ...prev]);
      toast.error(`${testType} permission test failed`);
    } finally {
      setLoading(false);
    }
  };

  const handleTest = (testType) => {
    const data = { ...testData };
    if (testType === 'gateway') {
      delete data.action;
      delete data.resource;
    } else {
      delete data.endpoint;
    }
    runPermissionTest(testType, data);
  };

  const quickTests = [
    { action: 'view', resource: 'task', label: 'View Tasks' },
    { action: 'edit', resource: 'task', label: 'Edit Tasks' },
    { action: 'delete', resource: 'task', label: 'Delete Tasks' },
    { action: 'view', resource: 'user_basic_info', label: 'View User Info' },
    { action: 'execute', resource: 'create_task', label: 'Create Task' },
    { action: 'execute', resource: 'delete_task', label: 'Delete Task' }
  ];

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Permission Tester</h1>
          <p className="mt-2 text-gray-600">
            Test permission checks and verify your role-based access control
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Test Configuration */}
          <div className="space-y-6">
            <div className="bg-white rounded-lg border border-gray-200 p-6">
              <h2 className="text-xl font-semibold text-gray-900 mb-4">Test Configuration</h2>
              
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    User UUID
                  </label>
                  <input
                    type="text"
                    value={testData.user_uuid}
                    onChange={(e) => setTestData({ ...testData, user_uuid: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Organization UUID
                  </label>
                  <input
                    type="text"
                    value={testData.organization_uuid}
                    onChange={(e) => setTestData({ ...testData, organization_uuid: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Action
                    </label>
                    <select
                      value={testData.action}
                      onChange={(e) => setTestData({ ...testData, action: e.target.value })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    >
                      <option value="view">View</option>
                      <option value="edit">Edit</option>
                      <option value="delete">Delete</option>
                      <option value="execute">Execute</option>
                    </select>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Resource
                    </label>
                    <input
                      type="text"
                      value={testData.resource}
                      onChange={(e) => setTestData({ ...testData, resource: e.target.value })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      placeholder="e.g., task, user_basic_info"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Endpoint (for Gateway tests)
                  </label>
                  <input
                    type="text"
                    value={testData.endpoint}
                    onChange={(e) => setTestData({ ...testData, endpoint: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    placeholder="e.g., GET /tasks"
                  />
                </div>
              </div>

              <div className="mt-6 space-y-3">
                <button
                  onClick={() => handleTest('standard')}
                  disabled={loading}
                  className="w-full flex items-center justify-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50 transition-colors"
                >
                  <Shield className="w-4 h-4" />
                  Test Standard Permission
                </button>

                <button
                  onClick={() => handleTest('legacy')}
                  disabled={loading}
                  className="w-full flex items-center justify-center gap-2 px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 disabled:opacity-50 transition-colors"
                >
                  <Check className="w-4 h-4" />
                  Test Legacy Permission
                </button>

                <button
                  onClick={() => handleTest('gateway')}
                  disabled={loading}
                  className="w-full flex items-center justify-center gap-2 px-4 py-2 bg-purple-600 text-white rounded-md hover:bg-purple-700 disabled:opacity-50 transition-colors"
                >
                  <Play className="w-4 h-4" />
                  Test Gateway Permission
                </button>
              </div>
            </div>

            {/* Quick Tests */}
            <div className="bg-white rounded-lg border border-gray-200 p-6">
              <h3 className="text-lg font-semibold text-gray-900 mb-4">Quick Tests</h3>
              <div className="grid grid-cols-2 gap-2">
                {quickTests.map((test, index) => (
                  <button
                    key={index}
                    onClick={() => {
                      setTestData({
                        ...testData,
                        action: test.action,
                        resource: test.resource
                      });
                      handleTest('standard');
                    }}
                    disabled={loading}
                    className="px-3 py-2 text-sm bg-gray-100 text-gray-700 rounded-md hover:bg-gray-200 disabled:opacity-50 transition-colors"
                  >
                    {test.label}
                  </button>
                ))}
              </div>
            </div>
          </div>

          {/* Current Policy & Test Results */}
          <div className="space-y-6">
            {/* Current User Policy */}
            <div className="bg-white rounded-lg border border-gray-200 p-6">
              <h2 className="text-xl font-semibold text-gray-900 mb-4">Current User Policy</h2>
              {userPolicy ? (
                <pre className="bg-gray-50 border border-gray-200 rounded-lg p-4 text-sm text-gray-700 overflow-x-auto">
                  {JSON.stringify(userPolicy, null, 2)}
                </pre>
              ) : (
                <p className="text-gray-500 italic">No policy loaded</p>
              )}
            </div>

            {/* Test Results */}
            <div className="bg-white rounded-lg border border-gray-200 p-6">
              <h2 className="text-xl font-semibold text-gray-900 mb-4">Test Results</h2>
              <div className="space-y-4 max-h-96 overflow-y-auto">
                {testResults.length === 0 ? (
                  <p className="text-gray-500 italic">No tests run yet</p>
                ) : (
                  testResults.map((result) => (
                    <div
                      key={result.id}
                      className={`border rounded-lg p-4 ${
                        result.success ? 'border-green-200 bg-green-50' : 'border-red-200 bg-red-50'
                      }`}
                    >
                      <div className="flex items-center justify-between mb-2">
                        <span className={`inline-flex items-center gap-1 px-2 py-1 text-xs font-medium rounded-full ${
                          result.success 
                            ? 'bg-green-100 text-green-800' 
                            : 'bg-red-100 text-red-800'
                        }`}>
                          {result.success ? <Check className="w-3 h-3" /> : <X className="w-3 h-3" />}
                          {result.type.toUpperCase()}
                        </span>
                        <span className="text-xs text-gray-500">
                          {new Date(result.timestamp).toLocaleTimeString()}
                        </span>
                      </div>
                      
                      <div className="text-sm">
                        <p className="font-medium mb-1">Request:</p>
                        <pre className="bg-white border rounded p-2 text-xs mb-2">
                          {JSON.stringify(result.data, null, 2)}
                        </pre>
                        
                        {result.success ? (
                          <>
                            <p className="font-medium mb-1">Response:</p>
                            <pre className="bg-white border rounded p-2 text-xs">
                              {JSON.stringify(result.response, null, 2)}
                            </pre>
                          </>
                        ) : (
                          <>
                            <p className="font-medium mb-1">Error:</p>
                            <pre className="bg-white border rounded p-2 text-xs text-red-600">
                              {JSON.stringify(result.error, null, 2)}
                            </pre>
                          </>
                        )}
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PermissionTester;
