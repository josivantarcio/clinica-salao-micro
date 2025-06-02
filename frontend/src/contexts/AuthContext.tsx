import React, { createContext, useContext, ReactNode, useState, useEffect } from 'react';
import api from '../services/api';
import jwtDecode from 'jwt-decode';

interface User {
  id: string;
  username: string;
  name: string;
  email: string;
  roles: string[];
}

interface JwtPayload {
  sub: string;
  username: string;
  name: string;
  email: string;
  roles: string[];
  exp: number;
  iat: number;
}

interface AuthContextType {
  isAuthenticated: boolean;
  user: User | null;
  token: string | null;
  login: (token: string) => void;
  logout: () => void;
  hasRole: (role: string) => boolean;
  isAdmin: () => boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);

  // Função para decodificar o token JWT
  const decodeToken = (token: string): User => {
    try {
      const decoded = jwtDecode<JwtPayload>(token);
      return {
        id: decoded.sub,
        username: decoded.username,
        name: decoded.name,
        email: decoded.email,
        roles: decoded.roles || []
      };
    } catch (error) {
      console.error('Erro ao decodificar token:', error);
      return {
        id: '',
        username: '',
        name: '',
        email: '',
        roles: []
      };
    }
  };

  // Verificar token na inicialização
  useEffect(() => {
    const storedToken = localStorage.getItem('token');
    if (storedToken) {
      try {
        // Verificar se o token não está expirado
        const decoded = jwtDecode<JwtPayload>(storedToken);
        const currentTime = Date.now() / 1000;
        
        if (decoded.exp && decoded.exp > currentTime) {
          // Token válido
          setToken(storedToken);
          setUser(decodeToken(storedToken));
          setIsAuthenticated(true);
          
          // Configurar token no axios
          api.defaults.headers.common['Authorization'] = `Bearer ${storedToken}`;
        } else {
          // Token expirado
          handleLogout();
        }
      } catch (error) {
        // Token inválido
        handleLogout();
      }
    }
  }, []);

  const login = (newToken: string) => {
    localStorage.setItem('token', newToken);
    setToken(newToken);
    setUser(decodeToken(newToken));
    setIsAuthenticated(true);
    
    // Configurar token no axios
    api.defaults.headers.common['Authorization'] = `Bearer ${newToken}`;
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    setToken(null);
    setUser(null);
    setIsAuthenticated(false);
    
    // Remover token do axios
    delete api.defaults.headers.common['Authorization'];
  };
  
  // Alias para manter compatibilidade
  const logout = handleLogout;
  
  // Função para verificar se o usuário tem um papel específico
  const hasRole = (role: string): boolean => {
    if (!user) return false;
    return user.roles.includes(role);
  };
  
  // Função para verificar se o usuário é administrador
  const isAdmin = (): boolean => {
    return hasRole('ROLE_ADMIN');
  };

  return (
    <AuthContext.Provider value={{ 
      isAuthenticated, 
      user, 
      token,
      login, 
      logout, 
      hasRole,
      isAdmin
    }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
