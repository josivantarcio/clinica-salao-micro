import axios, { AxiosInstance, AxiosRequestConfig, AxiosError } from 'axios';

// URL base da API - pode ser configurada no .env para diferentes ambientes
const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

// Criar instância do Axios com configurações base
const api: AxiosInstance = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000, // 10 segundos
});

// Request interceptor para adicionar o token de autenticação quando disponível
api.interceptors.request.use(
  (config) => {
    // Adicionar token de autenticação a cada requisição
    const token = localStorage.getItem('token');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Armazenar requisições que estão sendo refeitas para evitar loops infinitos
let isRefreshing = false;
let failedQueue: any[] = [];

const processQueue = (error: any, token: string | null = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  
  failedQueue = [];
};

// Response interceptor para tratamento global de erros
api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as any;
    
    // Se o erro for 401 (Não autorizado) e não for uma tentativa de refresh
    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        // Se já estiver tentando renovar o token, enfileira a requisição
        try {
          const token = await new Promise((resolve, reject) => {
            failedQueue.push({ resolve, reject });
          });
          originalRequest.headers['Authorization'] = `Bearer ${token}`;
          return axios(originalRequest);
        } catch (err) {
          return Promise.reject(err);
        }
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        // Aqui deveria chamar o endpoint de refresh token
        // Para fins de demonstração, simplesmente removemos o token e redirecionamos para login
        localStorage.removeItem('token');
        delete api.defaults.headers.common['Authorization'];
        
        // Processa a fila com erro para todas as requisições pendentes
        processQueue(error);
        
        // Redireciona para a página de login
        window.location.href = '/login';
        return Promise.reject(error);
      } catch (refreshError) {
        // Se a renovação do token falhar, desloga o usuário
        localStorage.removeItem('token');
        delete api.defaults.headers.common['Authorization'];
        
        // Processa a fila com erro
        processQueue(refreshError);
        
        // Redireciona para login
        window.location.href = '/login';
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }
    
    if (error.response) {
      // Tratamento de outros erros HTTP
      console.error(
        `Erro na resposta da API: ${error.response.status} - ${JSON.stringify(error.response.data)}`
      );
      
      // Tratamento de erros HTTP específicos
      switch (error.response.status) {
        case 403:
          console.error('Acesso negado. Você não tem permissão para acessar este recurso.');
          break;
        case 404:
          console.error('Recurso não encontrado.');
          break;
        case 500:
          console.error('Erro interno do servidor.');
          break;
        default:
          console.error(`Erro inesperado: ${error.response.status}`);
      }
    } else if (error.request) {
      // A requisição foi feita mas não houve resposta
      console.error('Sem resposta do servidor. Verifique sua conexão com a internet.', error.request);
    } else {
      // Algo aconteceu na configuração da requisição
      console.error('Erro ao configurar a requisição:', error.message);
    }
    
    return Promise.reject(error);
  }
);

export default api;
