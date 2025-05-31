export interface Client {
  id: number;
  name: string;
  email: string;
  phone: string;
  cpf: string;
  birthDate?: string;
  address?: string;
  notes?: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateClientDto {
  name: string;
  email: string;
  phone: string;
  cpf: string;
  birthDate?: string;
  address?: string;
  notes?: string;
  active?: boolean;
}

export interface UpdateClientDto extends Partial<CreateClientDto> {}

export interface ClientResponse {
  data: Client;
  message?: string;
}

export interface ClientsResponse {
  data: Client[];
  total: number;
  page: number;
  limit: number;
  totalPages: number;
}
