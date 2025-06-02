import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { CircularProgress, Box } from '@mui/material';

interface RoleBasedRouteProps {
  requiredRoles: string[];
  redirectTo?: string;
}

/**
 * Componente para proteger rotas baseado em papéis de usuário
 * Verifica se o usuário está autenticado e possui pelo menos um dos papéis requeridos
 */
const RoleBasedRoute: React.FC<RoleBasedRouteProps> = ({ 
  requiredRoles, 
  redirectTo = '/login' 
}) => {
  const { isAuthenticated, user, hasRole } = useAuth();
  
  // Se ainda estiver carregando os dados do usuário
  if (isAuthenticated && !user) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <CircularProgress />
      </Box>
    );
  }

  // Verifica se o usuário está autenticado
  if (!isAuthenticated) {
    return <Navigate to={redirectTo} />;
  }

  // Se não há papéis requeridos, apenas verifica autenticação
  if (!requiredRoles || requiredRoles.length === 0) {
    return <Outlet />;
  }

  // Verifica se o usuário tem pelo menos um dos papéis requeridos
  const hasRequiredRole = requiredRoles.some(role => hasRole(role));
  
  if (!hasRequiredRole) {
    return <Navigate to="/unauthorized" />;
  }

  // Se passou por todas as verificações, permite acesso
  return <Outlet />;
};

export default RoleBasedRoute;
