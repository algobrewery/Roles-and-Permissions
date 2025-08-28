import React, { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { PermissionProvider } from './contexts/PermissionContext';
import Navigation from './components/Navigation';
import Dashboard from './pages/Dashboard';
import RoleManagement from './pages/RoleManagement';
import PermissionTester from './pages/PermissionTester';

function App() {
  useEffect(() => {
    // Set default user and organization for testing
    if (!localStorage.getItem('userUuid')) {
      localStorage.setItem('userUuid', 'test-user-123');
    }
    if (!localStorage.getItem('organizationUuid')) {
      localStorage.setItem('organizationUuid', 'test-org-456');
    }
  }, []);

  return (
    <PermissionProvider>
      <Router>
        <div className="App">
          <Navigation />
          <main>
            <Routes>
              <Route path="/" element={<Dashboard />} />
              <Route path="/roles" element={<RoleManagement />} />
              <Route path="/permissions" element={<PermissionTester />} />
              <Route path="/users" element={<div className="p-8 text-center">User Management - Coming Soon</div>} />
              <Route path="/settings" element={<div className="p-8 text-center">Settings - Coming Soon</div>} />
            </Routes>
          </main>
          <Toaster 
            position="top-right"
            toastOptions={{
              duration: 4000,
              style: {
                background: '#363636',
                color: '#fff',
              },
              success: {
                duration: 3000,
                iconTheme: {
                  primary: '#10B981',
                  secondary: '#fff',
                },
              },
              error: {
                duration: 5000,
                iconTheme: {
                  primary: '#EF4444',
                  secondary: '#fff',
                },
              },
            }}
          />
        </div>
      </Router>
    </PermissionProvider>
  );
}

export default App;
