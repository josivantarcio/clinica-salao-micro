import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { QueryProvider } from './providers/QueryProvider';
import { AuthProvider } from './contexts/AuthContext';
import theme from './styles/theme';
import MainLayout from './components/layout/MainLayout';

// Pages
const Login = React.lazy(() => import('./pages/Login'));
const Dashboard = React.lazy(() => import('./pages/Dashboard'));
const Clients = React.lazy(() => import('./pages/clients/Clients'));
const ClientDetails = React.lazy(() => import('./pages/clients/ClientDetails'));
const ClientForm = React.lazy(() => import('./pages/clients/ClientForm'));

// Finance Pages
const Transactions = React.lazy(() => import('./pages/finance/Transactions'));
const TransactionDetails = React.lazy(() => import('./pages/finance/TransactionDetails'));
const TransactionForm = React.lazy(() => import('./pages/finance/TransactionForm'));
const Revenue = React.lazy(() => import('./pages/finance/Revenue'));

// Protected Route Component
const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated } = useAuth();
  return isAuthenticated ? <>{children}</> : <Navigate to="/login" replace />;
};

// App Component
function App() {
  return (
    <QueryProvider>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <Router>
          <AuthProvider>
            <React.Suspense fallback={<div>Carregando...</div>}>
              <Routes>
                {/* Public Routes */}
                <Route path="/login" element={<Login />} />
                
                {/* Protected Routes */}
                <Route
                  path="/"
                  element={
                    <ProtectedRoute>
                      <MainLayout />
                    </ProtectedRoute>
                  }
                >
                  <Route index element={<Dashboard />} />
                  <Route path="clients" element={<Clients />} />
                  <Route path="clients/new" element={<ClientForm />} />
                  <Route path="clients/:id" element={<ClientDetails />} />
                  <Route path="clients/:id/edit" element={<ClientForm />} />
                  
                  {/* Rotas de Finanças */}
                  <Route path="finance" element={<Transactions />} />
                  <Route path="finance/transactions/new" element={<TransactionForm />} />
                  <Route path="finance/transactions/:id" element={<TransactionDetails />} />
                  <Route path="finance/transactions/:id/edit" element={<TransactionForm />} />
                  <Route path="finance/revenue" element={<Revenue />} />
                </Route>
                
                {/* 404 Route */}
                <Route path="*" element={<Navigate to="/" replace />} />
              </Routes>
            </React.Suspense>
          </AuthProvider>
        </Router>
      </ThemeProvider>
    </QueryProvider>
  );
}

export default App;
