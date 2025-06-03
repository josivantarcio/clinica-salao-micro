import { api } from './api';

export interface LoyaltyProgram {
  id: number;
  name: string;
  description: string;
  pointsPerPurchase: number;
  minPurchaseValue: number;
  active: boolean;
  rewards: Reward[];
}

export interface Reward {
  id: number;
  name: string;
  description: string;
  pointsRequired: number;
  discount: number;
  active: boolean;
}

export interface ClientLoyalty {
  id: number;
  clientId: number;
  clientName: string;
  currentPoints: number;
  totalAccumulatedPoints: number;
  pointsHistory: PointTransaction[];
}

export interface PointTransaction {
  id: number;
  clientId: number;
  transactionType: 'EARN' | 'REDEEM';
  points: number;
  description: string;
  transactionDate: string;
  relatedPurchaseId?: number;
  relatedRewardId?: number;
}

export interface LoyaltyProgramRequest {
  name: string;
  description: string;
  pointsPerPurchase: number;
  minPurchaseValue: number;
}

export interface RewardRequest {
  name: string;
  description: string;
  pointsRequired: number;
  discount: number;
}

// API para Programas de Fidelidade
export const getLoyaltyPrograms = async (): Promise<LoyaltyProgram[]> => {
  const response = await api.get('/loyalty/programs');
  return response.data;
};

export const getLoyaltyProgramById = async (id: number): Promise<LoyaltyProgram> => {
  const response = await api.get(`/loyalty/programs/${id}`);
  return response.data;
};

export const createLoyaltyProgram = async (program: LoyaltyProgramRequest): Promise<LoyaltyProgram> => {
  const response = await api.post('/loyalty/programs', program);
  return response.data;
};

export const updateLoyaltyProgram = async (id: number, program: LoyaltyProgramRequest): Promise<LoyaltyProgram> => {
  const response = await api.put(`/loyalty/programs/${id}`, program);
  return response.data;
};

export const activateLoyaltyProgram = async (id: number): Promise<LoyaltyProgram> => {
  const response = await api.patch(`/loyalty/programs/${id}/activate`);
  return response.data;
};

export const deactivateLoyaltyProgram = async (id: number): Promise<LoyaltyProgram> => {
  const response = await api.patch(`/loyalty/programs/${id}/deactivate`);
  return response.data;
};

// API para Recompensas
export const createReward = async (programId: number, reward: RewardRequest): Promise<Reward> => {
  const response = await api.post(`/loyalty/programs/${programId}/rewards`, reward);
  return response.data;
};

export const updateReward = async (programId: number, rewardId: number, reward: RewardRequest): Promise<Reward> => {
  const response = await api.put(`/loyalty/programs/${programId}/rewards/${rewardId}`, reward);
  return response.data;
};

export const activateReward = async (programId: number, rewardId: number): Promise<Reward> => {
  const response = await api.patch(`/loyalty/programs/${programId}/rewards/${rewardId}/activate`);
  return response.data;
};

export const deactivateReward = async (programId: number, rewardId: number): Promise<Reward> => {
  const response = await api.patch(`/loyalty/programs/${programId}/rewards/${rewardId}/deactivate`);
  return response.data;
};

// API para Pontos dos Clientes
export const getClientLoyalty = async (clientId: number): Promise<ClientLoyalty> => {
  const response = await api.get(`/loyalty/clients/${clientId}`);
  return response.data;
};

export const getClientLoyaltyHistory = async (clientId: number): Promise<PointTransaction[]> => {
  const response = await api.get(`/loyalty/clients/${clientId}/history`);
  return response.data;
};

export const addManualPoints = async (clientId: number, points: number, description: string): Promise<PointTransaction> => {
  const response = await api.post(`/loyalty/clients/${clientId}/points`, {
    points,
    description,
    transactionType: 'EARN'
  });
  return response.data;
};

export const redeemReward = async (clientId: number, rewardId: number): Promise<PointTransaction> => {
  const response = await api.post(`/loyalty/clients/${clientId}/redeem`, { rewardId });
  return response.data;
};
