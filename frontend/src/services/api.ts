import axios, { AxiosInstance, AxiosRequestConfig } from 'axios';

const API_URL = 'http://localhost:8080/api';

const api: AxiosInstance = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor para adicionar o token de autenticação quando disponível
api.interceptors.request.use(
  (config) => {
    // Adicionar lógica de autenticação aqui se necessário
    // const token = localStorage.getItem('token');
    // if (token) {
    //   config.headers.Authorization = `Bearer ${token}`;
    // }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor para tratamento global de erros
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      // Tratamento de erros HTTP
      console.error('Erro na resposta da API:', error.response.data);
      
      // Você pode adicionar lógica de redirecionamento para login em caso de 401
      // if (error.response.status === 401) {
      //   window.location.href = '/login';
      // }
    } else if (error.request) {
      // A requisição foi feita mas não houve resposta
      console.error('Sem resposta do servidor:', error.request);
    } else {
      // Algo aconteceu na configuração da requisição
      console.error('Erro ao configurar a requisição:', error.message);
    }
    
    return Promise.reject(error);
  }
);

export default api;
