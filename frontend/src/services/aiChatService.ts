import api from './api';

export interface ChatMessage {
  role: 'user' | 'assistant' | 'system';
  content: string;
}

export interface ChatResponse {
  id: string;
  message: ChatMessage;
  suggestedActions?: {
    type: 'appointment' | 'service' | 'payment';
    data: any;
  }[];
}

export interface ChatHistory {
  messages: ChatMessage[];
  conversationId?: string;
}

/**
 * Serviço para comunicação com a API de IA para o chat assistente
 */
export const sendChatMessage = async (
  message: string,
  history: ChatMessage[] = [],
  clientId?: string,
  conversationId?: string
): Promise<ChatResponse> => {
  try {
    const response = await api.post('/ai-service/chat', {
      message,
      history,
      clientId,
      conversationId
    });
    
    return response.data;
  } catch (error) {
    console.error('Erro ao enviar mensagem para a IA:', error);
    
    // Fallback para desenvolvimento/demonstração
    // Simula uma resposta da IA baseada em palavras-chave simples
    const input = message.toLowerCase();
    
    let content = 'Desculpe, não consegui processar sua solicitação no momento.';
    let suggestedActions = undefined;
    
    if (input.includes('agendar') || input.includes('marcar') || input.includes('hora')) {
      content = 'Claro! Posso ajudar a agendar um horário. Qual serviço você gostaria e em qual data?';
      
      // Se a mensagem incluir um serviço e data específicos, sugerir ação
      if ((input.includes('cabelo') || input.includes('corte') || input.includes('manicure')) 
          && (input.includes('amanhã') || input.includes('hoje') || /\d{1,2}\/\d{1,2}/.test(input))) {
        
        // Extrair serviço da mensagem (simplificado)
        let service = 'Corte de Cabelo';
        if (input.includes('manicure')) service = 'Manicure';
        if (input.includes('pedicure')) service = 'Pedicure';
        
        // Extrair data (simplificado)
        let date = new Date();
        if (input.includes('amanhã')) {
          date.setDate(date.getDate() + 1);
        } else if (input.includes('hoje')) {
          // Mantém a data atual
        } else {
          // Tenta extrair data do formato DD/MM
          const dateMatch = input.match(/(\d{1,2})\/(\d{1,2})/);
          if (dateMatch) {
            const day = parseInt(dateMatch[1]);
            const month = parseInt(dateMatch[2]) - 1; // Mês em JS é 0-indexed
            date = new Date(date.getFullYear(), month, day);
          }
        }
        
        suggestedActions = [{
          type: 'appointment' as const,
          data: {
            service,
            date: date.toISOString().split('T')[0]
          }
        }];
      }
    } else if (input.includes('preço') || input.includes('valor') || input.includes('custo')) {
      content = 'Temos diversos serviços com preços variados. Por exemplo: corte de cabelo a partir de R$ 50, manicure a partir de R$ 35. Posso te enviar um link de pagamento se desejar.';
      
      if (input.includes('corte') || input.includes('cabelo')) {
        suggestedActions = [{
          type: 'payment' as const,
          data: {
            amount: 50,
            description: 'Corte de Cabelo'
          }
        }];
      } else if (input.includes('manicure')) {
        suggestedActions = [{
          type: 'payment' as const,
          data: {
            amount: 35,
            description: 'Manicure'
          }
        }];
      }
    } else if (input.includes('pagamento') || input.includes('pagar')) {
      content = 'Aceitamos diversas formas de pagamento: cartão de crédito, débito, PIX e dinheiro. Posso te enviar um link de pagamento antecipado se preferir.';
    } else if (input.includes('horário') || input.includes('funcionamento')) {
      content = 'Estamos abertos de segunda a sexta das 9h às 19h, e aos sábados das 9h às 17h. Domingo fechado para descanso.';
    }
    
    return {
      id: `local-${Date.now()}`,
      message: {
        role: 'assistant',
        content
      },
      suggestedActions
    };
  }
};

/**
 * Salva uma conversa no histórico
 */
export const saveConversation = async (
  clientId: string, 
  messages: ChatMessage[]
): Promise<{ conversationId: string }> => {
  try {
    const response = await api.post('/ai-service/conversations', {
      clientId,
      messages
    });
    
    return response.data;
  } catch (error) {
    console.error('Erro ao salvar conversa:', error);
    
    // Fallback para desenvolvimento
    return {
      conversationId: `local-conv-${Date.now()}`
    };
  }
};

/**
 * Carrega o histórico de conversa de um cliente
 */
export const loadConversationHistory = async (
  clientId: string
): Promise<ChatHistory> => {
  try {
    const response = await api.get(`/ai-service/conversations/${clientId}`);
    
    return response.data;
  } catch (error) {
    console.error('Erro ao carregar histórico de conversa:', error);
    
    // Fallback para desenvolvimento
    return {
      messages: [{
        role: 'system',
        content: 'Bem-vindo à Clínica Salão. Como posso ajudar?'
      }]
    };
  }
};
