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
