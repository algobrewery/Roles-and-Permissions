# Roles & Permissions Frontend

A modern React application for managing roles and permissions with a visual policy builder and permission testing interface.

## Features

- **Role Management**: Create, edit, and delete roles with custom permissions
- **Policy Builder**: Visual interface for building JSON policy documents
- **Permission Testing**: Test permission checks in real-time
- **Protected Components**: UI elements that show/hide based on user permissions
- **Dashboard**: Overview of system status and user permissions
- **Responsive Design**: Works on desktop and mobile devices

## Tech Stack

- **React 18** - UI framework
- **React Router** - Client-side routing
- **Tailwind CSS** - Styling
- **React Hook Form** - Form handling
- **Axios** - HTTP client
- **Lucide React** - Icons
- **React Hot Toast** - Notifications

## Getting Started

### Prerequisites

- Node.js 16+ 
- npm or yarn
- Backend API running on `http://localhost:8080`

### Installation

1. Navigate to the frontend directory:
```bash
cd frontend
```

2. Install dependencies:
```bash
npm install
```

3. Start the development server:
```bash
npm start
```

The application will open at `http://localhost:3000`

### Building for Production

```bash
npm run build
```

## Usage

### 1. Dashboard
- View system statistics and health
- See your current permissions
- Quick access to main features

### 2. Role Management
- Create new roles with custom permissions
- Edit existing roles
- Delete roles
- Search and filter roles

### 3. Policy Builder
- Visual interface for creating policy documents
- Add/remove permissions for data (view, edit, delete)
- Add/remove permissions for features (execute)
- Real-time JSON preview

### 4. Permission Tester
- Test permission checks against the API
- Multiple test types (standard, legacy, gateway)
- View current user policy
- Quick test buttons for common scenarios

## API Integration

The frontend communicates with your Spring Boot backend API:

- **Base URL**: `http://localhost:8080`
- **Headers**: Automatically includes `x-app-user-uuid` from localStorage
- **CORS**: Configured to work with the backend

## Permission System

### Policy Structure
```json
{
  "data": {
    "view": ["task", "user_basic_info"],
    "edit": ["task"],
    "delete": ["task"]
  },
  "features": {
    "execute": ["create_task", "delete_task"]
  }
}
```

### Protected Components
```jsx
import { ProtectedButton } from './components/ProtectedComponent';

<ProtectedButton 
  action="edit" 
  resource="task" 
  onClick={handleEdit}
>
  Edit Task
</ProtectedButton>
```

### Permission Hook
```jsx
import { usePermissions } from './contexts/PermissionContext';

const { hasPermission, userPolicy } = usePermissions();

if (hasPermission('view', 'task')) {
  // Show task list
}
```

## Configuration

### Environment Variables
Create a `.env` file in the frontend directory:

```env
REACT_APP_API_URL=http://localhost:8080
```

### Default Test Data
The app automatically sets these values for testing:
- User UUID: `test-user-123`
- Organization UUID: `test-org-456`

## Project Structure

```
frontend/
├── public/
│   └── index.html
├── src/
│   ├── components/
│   │   ├── Navigation.js
│   │   ├── PolicyBuilder.js
│   │   ├── ProtectedComponent.js
│   │   ├── RoleCard.js
│   │   └── RoleForm.js
│   ├── contexts/
│   │   └── PermissionContext.js
│   ├── pages/
│   │   ├── Dashboard.js
│   │   ├── PermissionTester.js
│   │   └── RoleManagement.js
│   ├── services/
│   │   └── api.js
│   ├── utils/
│   │   └── cn.js
│   ├── App.js
│   ├── index.js
│   └── index.css
├── package.json
├── tailwind.config.js
└── README.md
```

## Development

### Adding New Components
1. Create component in `src/components/`
2. Import and use in pages
3. Add permission checks if needed

### Adding New Pages
1. Create page in `src/pages/`
2. Add route in `App.js`
3. Add navigation item in `Navigation.js`

### Styling
- Use Tailwind CSS classes
- Use the `cn()` utility for conditional classes
- Follow the existing design patterns

## Troubleshooting

### Common Issues

1. **API Connection Failed**
   - Ensure backend is running on port 8080
   - Check CORS configuration
   - Verify API endpoints

2. **Permissions Not Loading**
   - Check browser console for errors
   - Verify user UUID and organization UUID are set
   - Check network tab for API calls

3. **Build Errors**
   - Clear node_modules and reinstall
   - Check for missing dependencies
   - Verify Node.js version

## Contributing

1. Follow the existing code style
2. Add permission checks for new features
3. Test on both desktop and mobile
4. Update documentation as needed

## License

This project is part of the Roles & Permissions Management System.
