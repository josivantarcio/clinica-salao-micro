import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { QueryProvider } from './providers/QueryProvider';
import { AuthProvider } from './contexts/AuthContext';
import { useAuth } from './contexts/AuthContext';
import theme from './styles/theme';
import MainLayout from './components/layout/MainLayout';
import RoleBasedRoute from './components/auth/RoleBasedRoute';

// Pages
const Login = React.lazy(() => import('./pages/Login'));
const Dashboard = React.lazy(() => import('./pages/Dashboard'));
const Unauthorized = React.lazy(() => import('./pages/Unauthorized'));
const Clients = React.lazy(() => import('./pages/clients/Clients'));
const ClientDetails = React.lazy(() => import('./pages/clients/ClientDetails'));
const ClientForm = React.lazy(() => import('./pages/clients/ClientForm'));

// Finance Pages
const Transactions = React.lazy(() => import('./pages/finance/Transactions'));
const TransactionDetails = React.lazy(() => import('./pages/finance/TransactionDetails'));
const TransactionForm = React.lazy(() => import('./pages/finance/TransactionForm'));
const Revenue = React.lazy(() => import('./pages/finance/Revenue'));

// Professional Pages
const Professionals = React.lazy(() => import('./pages/professionals/Professionals'));
const ProfessionalForm = React.lazy(() => import('./pages/professionals/ProfessionalForm'));

// Appointments Pages
const Appointments = React.lazy(() => import('./pages/appointments/Appointments'));

// Loyalty Pages
const LoyaltyPrograms = React.lazy(() => import('./pages/loyalty/LoyaltyPrograms'));

// Reports Pages
const Reports = React.lazy(() => import('./pages/reports/Reports'));

// Protected Route Component para compatibilidade com código existente
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
                <Route path="/unauthorized" element={<Unauthorized />} />
                
                {/* Protected Routes com Layout Principal */}
                <Route
                  path="/"
                  element={
                    <ProtectedRoute>
                      <MainLayout />
                    </ProtectedRoute>
                  }
                >
                  {/* Dashboard - Acessível a todos usuários autenticados */}
                  <Route index element={<Dashboard />} />
                  
                  {/* Rotas de Clientes - Acesso para atendentes e administradores */}
                  <Route element={<RoleBasedRoute requiredRoles={['ROLE_ATTENDANT', 'ROLE_ADMIN']} />}>
                    <Route path="clients" element={<Clients />} />
                    <Route path="clients/new" element={<ClientForm />} />
                    <Route path="clients/:id" element={<ClientDetails />} />
                    <Route path="clients/:id/edit" element={<ClientForm />} />
                  </Route>
                  
                  {/* Rotas de Finanças - Acesso restrito para administradores e financeiro */}
                  <Route element={<RoleBasedRoute requiredRoles={['ROLE_ADMIN', 'ROLE_FINANCE']} />}>
                    <Route path="finance" element={<Transactions />} />
                    <Route path="finance/transactions/new" element={<TransactionForm />} />
                    <Route path="finance/transactions/:id" element={<TransactionDetails />} />
                    <Route path="finance/transactions/:id/edit" element={<TransactionForm />} />
                    <Route path="finance/revenue" element={<Revenue />} />
                  </Route>

                  {/* Rotas de Profissionais - Acesso para administradores e gerentes */}
                  <Route element={<RoleBasedRoute requiredRoles={['ROLE_ADMIN', 'ROLE_MANAGER']} />}>
                    <Route path="professionals" element={<Professionals />} />
                    <Route path="professionals/new" element={<ProfessionalForm />} />
                    <Route path="professionals/:id/edit" element={<ProfessionalForm />} />
                  </Route>

                  {/* Rotas de Agendamentos - Acesso para atendentes, profissionais e administradores */}
                  <Route element={<RoleBasedRoute requiredRoles={['ROLE_ATTENDANT', 'ROLE_PROFESSIONAL', 'ROLE_ADMIN']} />}>
                    <Route path="appointments" element={<Appointments />} />
                  </Route>

                  {/* Rotas de Fidelidade - Acesso para administradores e gerentes */}
                  <Route element={<RoleBasedRoute requiredRoles={['ROLE_ADMIN', 'ROLE_MANAGER']} />}>
                    <Route path="loyalty/programs" element={<LoyaltyPrograms />} />
                  </Route>

                  {/* Rotas de Relatórios - Acesso para administradores e gerentes */}
                  <Route element={<RoleBasedRoute requiredRoles={['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_FINANCE']} />}>
                    <Route path="reports" element={<Reports />} />
                  </Route>
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
