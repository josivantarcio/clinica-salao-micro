import api from './api';
import { Client, CreateClientDto, UpdateClientDto, ClientsResponse, ClientResponse } from '../types/client';

export const getClients = async (page: number = 0, limit: number = 10): Promise<ClientsResponse> => {
  const response = await api.get<ClientsResponse>('/clients', {
    params: { page, size: limit }
  });
  return response.data;
};

export const getClientById = async (id: number): Promise<Client> => {
  const response = await api.get<ClientResponse>(`/clients/${id}`);
  return response.data.data;
};

export const createClient = async (clientData: CreateClientDto): Promise<Client> => {
  const response = await api.post<ClientResponse>('/clients', clientData);
  return response.data.data;
};

export const updateClient = async (id: number, clientData: UpdateClientDto): Promise<Client> => {
  const response = await api.put<ClientResponse>(`/clients/${id}`, clientData);
  return response.data.data;
};

export const deleteClient = async (id: number): Promise<void> => {
  await api.delete(`/clients/${id}`);
};

export const activateClient = async (id: number): Promise<Client> => {
  const response = await api.patch<ClientResponse>(`/clients/${id}/activate`);
  return response.data.data;
};

export const deactivateClient = async (id: number): Promise<Client> => {
  const response = await api.patch<ClientResponse>(`/clients/${id}/deactivate`);
  return response.data.data;
};

/**
 * Pesquisa clientes por termo (nome, email ou telefone)
 * Útil para integração com outros módulos como o financeiro
 */
export const searchClients = async (searchTerm: string, limit: number = 10): Promise<Client[]> => {
  const response = await api.get<ClientsResponse>('/clients/search', {
    params: { q: searchTerm, size: limit }
  });
  return response.data.data;
};

/**
 * Busca clientes para um seletor/autocomplete
 * Retorna dados mínimos necessários para seleção (id, nome, email)
 */
export const getClientsForSelector = async (searchTerm: string = ''): Promise<Array<{id: number, name: string, email: string}>> => {
  try {
    if (searchTerm && searchTerm.length < 3) {
      return [];
    }
    
    const response = await api.get<ClientsResponse>(searchTerm ? '/clients/search' : '/clients', {
      params: searchTerm ? { q: searchTerm, size: 10 } : { page: 0, size: 10 }
    });
    
    return response.data.data.map(client => ({
      id: client.id,
      name: client.name,
      email: client.email
    }));
  } catch (error) {
    console.error('Erro ao buscar clientes para seletor:', error);
    return [];
  }
};
