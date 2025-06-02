import api from './api';
import { Transaction } from '../types/transaction';

/**
 * Serviço para integração com o gateway de pagamento Asaas
 * Este serviço se comunica com o backend finance-service que faz a integração
 * real com a API do Asaas
 */
export const paymentGatewayService = {
  /**
   * Gera um link de pagamento para uma transação
   * @param transactionId ID da transação
   * @returns Objeto com URL de pagamento e informações adicionais
   */
  generatePaymentLink: async (transactionId: string): Promise<{ paymentUrl: string }> => {
    try {
      const response = await api.post(`/finance-service/transactions/${transactionId}/payment-link`);
      return response.data;
    } catch (error) {
      console.error('Erro ao gerar link de pagamento:', error);
      throw error;
    }
  },

  /**
   * Processa um pagamento diretamente (para pagamentos presenciais)
   * @param transactionId ID da transação
   * @param paymentMethod Método de pagamento (CREDIT_CARD, DEBIT_CARD, CASH, PIX, etc)
   * @returns Transação atualizada
   */
  processPayment: async (
    transactionId: string, 
    paymentMethod: string
  ): Promise<Transaction> => {
    try {
      const response = await api.post(`/finance-service/transactions/${transactionId}/process`, {
        paymentMethod
      });
      return response.data;
    } catch (error) {
      console.error('Erro ao processar pagamento:', error);
      throw error;
    }
  },

  /**
   * Solicita reembolso de uma transação
   * @param transactionId ID da transação
   * @param reason Motivo do reembolso
   * @returns Transação atualizada
   */
  refundPayment: async (
    transactionId: string, 
    reason: string
  ): Promise<Transaction> => {
    try {
      const response = await api.post(`/finance-service/transactions/${transactionId}/refund`, {
        reason
      });
      return response.data;
    } catch (error) {
      console.error('Erro ao solicitar reembolso:', error);
      throw error;
    }
  },

  /**
   * Verifica o status atual de um pagamento
   * @param transactionId ID da transação
   * @returns Status do pagamento
   */
  checkPaymentStatus: async (transactionId: string): Promise<{ 
    status: string;
    externalReference: string;
    lastUpdate: string;
  }> => {
    try {
      const response = await api.get(`/finance-service/transactions/${transactionId}/payment-status`);
      return response.data;
    } catch (error) {
      console.error('Erro ao verificar status do pagamento:', error);
      throw error;
    }
  },

  /**
   * Cancela um pagamento pendente
   * @param transactionId ID da transação
   * @returns Transação atualizada
   */
  cancelPayment: async (transactionId: string): Promise<Transaction> => {
    try {
      const response = await api.post(`/finance-service/transactions/${transactionId}/cancel`);
      return response.data;
    } catch (error) {
      console.error('Erro ao cancelar pagamento:', error);
      throw error;
    }
  },

  /**
   * Envia um lembrete de pagamento para o cliente
   * @param transactionId ID da transação
   * @returns Confirmação de envio
   */
  sendPaymentReminder: async (transactionId: string): Promise<{ sent: boolean; message: string }> => {
    try {
      const response = await api.post(`/finance-service/transactions/${transactionId}/reminder`);
      return response.data;
    } catch (error) {
      console.error('Erro ao enviar lembrete de pagamento:', error);
      throw error;
    }
  }
};

export default paymentGatewayService;
