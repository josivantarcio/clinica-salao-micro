import React, { useState, useEffect, useRef } from 'react';
import {
  Box,
  Paper,
  Typography,
  TextField,
  IconButton,
  CircularProgress,
  Avatar,
  Badge,
  Drawer,
  useTheme,
  useMediaQuery,
  Fab
} from '@mui/material';
import {
  Send as SendIcon,
  SmartToy as BotIcon,
  Close as CloseIcon,
  ChatBubble as ChatIcon,
  CalendarMonth as CalendarIcon,
  AttachMoney as MoneyIcon
} from '@mui/icons-material';
import { styled } from '@mui/material/styles';
import { useSnackbar } from 'notistack';
import { useAuth } from '../../contexts/AuthContext';

// Interfaces para tipagem
interface Message {
  id: string;
  text: string;
  sender: 'user' | 'ai';
  timestamp: Date;
  status: 'sending' | 'sent' | 'error';
  action?: {
    type: 'appointment' | 'service' | 'payment';
    data: any;
  };
}

interface AIChatProps {
  clientId?: string;
  clientName?: string;
  onScheduleAppointment?: (date: string, service: string) => void;
  onSendPaymentLink?: (amount: number, description: string) => void;
  fixedPosition?: boolean;
}

// Componentes estilizados
const MessageBubble = styled(Paper, {
  shouldForwardProp: (prop) => prop !== 'sender'
})<{ sender: 'user' | 'ai' }>(({ theme, sender }) => ({
  padding: theme.spacing(1.5),
  maxWidth: '80%',
  borderRadius: sender === 'user' 
    ? '18px 18px 4px 18px' 
    : '18px 18px 18px 4px',
  backgroundColor: sender === 'user' 
    ? theme.palette.primary.main 
    : theme.palette.background.paper,
  color: sender === 'user' ? theme.palette.primary.contrastText : theme.palette.text.primary,
  marginBottom: theme.spacing(1.5),
  boxShadow: theme.shadows[1],
  wordBreak: 'break-word',
  position: 'relative',
  border: sender === 'ai' ? `1px solid ${theme.palette.divider}` : 'none',
}));

const ChatHeader = styled(Box)(({ theme }) => ({
  padding: theme.spacing(2),
  backgroundColor: theme.palette.primary.main,
  color: theme.palette.primary.contrastText,
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'space-between',
  borderTopLeftRadius: theme.shape.borderRadius,
  borderTopRightRadius: theme.shape.borderRadius,
}));

const AIChat: React.FC<AIChatProps> = ({
  clientId,
  clientName,
  onScheduleAppointment,
  onSendPaymentLink,
  fixedPosition = false
}) => {
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState('');
  const [isProcessing, setIsProcessing] = useState(false);
  const [isOpen, setIsOpen] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const chatContainerRef = useRef<HTMLDivElement>(null);
  const { enqueueSnackbar } = useSnackbar();
  const { user } = useAuth();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));

  // Efeito para rolar para a mensagem mais recente
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  // Efeito para carregar mensagens iniciais
  useEffect(() => {
    if (isOpen && messages.length === 0) {
      // Mensagem de boas-vindas da IA
      const welcomeMessage: Message = {
        id: Date.now().toString(),
        text: clientName 
          ? `Olá! Bem-vindo(a) de volta ${clientName}! Como posso ajudar você hoje? Posso ajudar com agendamentos, informações sobre serviços ou pagamentos.` 
          : 'Olá! Sou o assistente virtual da Clínica Salão. Como posso ajudar você hoje? Posso auxiliar com agendamentos, informações sobre serviços ou pagamentos.',
        sender: 'ai',
        timestamp: new Date(),
        status: 'sent'
      };
      
      setMessages([welcomeMessage]);
    }
  }, [isOpen, clientName, messages.length]);

  // Função para processar entrada do usuário com a IA
  const processWithAI = async (userInput: string): Promise<string> => {
    // Simulação de chamada à API de IA
    // Em produção, substituir por chamada real ao serviço de backend
    try {
      // Simular atraso de resposta da IA
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      // Lógica simples para simular respostas contextuais
      const input = userInput.toLowerCase();
      
      if (input.includes('agendar') || input.includes('marcar') || input.includes('hora')) {
        return 'Claro! Posso te ajudar a agendar um horário. Qual serviço você gostaria de agendar e para qual data?';
      } else if (input.includes('preço') || input.includes('valor') || input.includes('custo')) {
        return 'Temos diversos serviços com preços variados. Por exemplo, corte de cabelo a partir de R$ 50, manicure a partir de R$ 35. Há algo específico que você gostaria de saber?';
      } else if (input.includes('pagamento') || input.includes('pagar')) {
        return 'Aceitamos diversas formas de pagamento: cartão de crédito, débito, PIX e dinheiro. Posso te enviar um link de pagamento antecipado se preferir.';
      } else if (input.includes('horário') || input.includes('funcionamento')) {
        return 'Estamos abertos de segunda a sexta das 9h às 19h, e aos sábados das 9h às 17h. Domingo fechado para descanso.';
      } else {
        return 'Entendi. Como posso te ajudar mais especificamente com isso? Posso fornecer informações sobre nossos serviços, ajudar com agendamentos ou pagamentos.';
      }
    } catch (error) {
      console.error('Erro ao processar mensagem com IA:', error);
      throw new Error('Não foi possível processar sua mensagem no momento.');
    }
  };

  // Handler para envio de mensagem
  const handleSendMessage = async () => {
    if (!input.trim()) return;
    
    const userMessage: Message = {
      id: Date.now().toString(),
      text: input,
      sender: 'user',
      timestamp: new Date(),
      status: 'sending'
    };
    
    setMessages(prev => [...prev, userMessage]);
    setInput('');
    setIsProcessing(true);
    
    try {
      // Processar mensagem com IA
      const aiResponse = await processWithAI(input);
      
      // Atualizar status da mensagem do usuário
      setMessages(prev => 
        prev.map(msg => 
          msg.id === userMessage.id ? { ...msg, status: 'sent' } : msg
        )
      );
      
      // Adicionar resposta da IA
      const aiMessage: Message = {
        id: Date.now().toString(),
        text: aiResponse,
        sender: 'ai',
        timestamp: new Date(),
        status: 'sent'
      };
      
      setMessages(prev => [...prev, aiMessage]);
      
      // Se não estiver aberto, incrementar contador de não lidos
      if (!isOpen) {
        setUnreadCount(prev => prev + 1);
      }
    } catch (error) {
      console.error('Erro ao enviar mensagem:', error);
      
      // Atualizar status da mensagem para erro
      setMessages(prev => 
        prev.map(msg => 
          msg.id === userMessage.id ? { ...msg, status: 'error' } : msg
        )
      );
      
      enqueueSnackbar('Erro ao processar mensagem. Tente novamente.', { variant: 'error' });
    } finally {
      setIsProcessing(false);
    }
  };

  // Handler para tecla Enter
  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage();
    }
  };

  // Toggle do chat
  const toggleChat = () => {
    setIsOpen(prev => !prev);
    if (!isOpen) {
      setUnreadCount(0);
    }
  };

  // Renderizar o botão de chat flutuante ou drawer completo
  return (
    <>
      {/* Botão flutuante do chat */}
      {!isOpen && (
        <Fab
          color="primary"
          aria-label="chat"
          onClick={toggleChat}
          sx={{
            position: fixedPosition ? 'fixed' : 'absolute',
            bottom: 20,
            right: 20,
            zIndex: theme.zIndex.drawer + 1
          }}
        >
          <Badge badgeContent={unreadCount} color="error">
            <ChatIcon />
          </Badge>
        </Fab>
      )}
      
      {/* Chat completo */}
      <Drawer
        anchor="right"
        open={isOpen}
        onClose={toggleChat}
        variant="temporary"
        PaperProps={{
          sx: {
            width: isMobile ? '100%' : 380,
            height: isMobile ? '100%' : 'calc(100% - 80px)',
            bottom: isMobile ? 0 : 40,
            right: isMobile ? 0 : 40,
            borderRadius: isMobile ? 0 : 2,
            overflow: 'hidden'
          }
        }}
      >
        {/* Cabeçalho do chat */}
        <ChatHeader>
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <Avatar sx={{ bgcolor: 'primary.dark', mr: 1.5 }}>
              <BotIcon />
            </Avatar>
            <Box>
              <Typography variant="subtitle1" fontWeight="bold">
                Assistente Virtual
              </Typography>
              <Typography variant="caption">
                {clientName ? `Atendendo ${clientName}` : 'Como posso ajudar?'}
              </Typography>
            </Box>
          </Box>
          <IconButton color="inherit" onClick={toggleChat} edge="end">
            <CloseIcon />
          </IconButton>
        </ChatHeader>
        
        {/* Área de mensagens */}
        <Box
          ref={chatContainerRef}
          sx={{
            flex: 1,
            p: 2,
            overflowY: 'auto',
            backgroundColor: theme.palette.background.default,
            display: 'flex',
            flexDirection: 'column'
          }}
        >
          {messages.map(message => (
            <Box
              key={message.id}
              sx={{
                display: 'flex',
                justifyContent: message.sender === 'user' ? 'flex-end' : 'flex-start',
                mb: 1
              }}
            >
              {message.sender === 'ai' && (
                <Avatar 
                  sx={{ 
                    width: 32, 
                    height: 32, 
                    mr: 1,
                    bgcolor: 'primary.main',
                    display: { xs: 'none', sm: 'flex' }
                  }}
                >
                  <BotIcon fontSize="small" />
                </Avatar>
              )}
              
              <Box sx={{ maxWidth: '80%' }}>
                <MessageBubble sender={message.sender}>
                  <Typography variant="body2">{message.text}</Typography>
                  
                  {/* Renderizar ações sugeridas se existirem */}
                  {message.action && (
                    <Box 
                      sx={{ 
                        mt: 1.5, 
                        pt: 1.5, 
                        borderTop: `1px solid ${
                          message.sender === 'user' 
                            ? 'rgba(255,255,255,0.2)' 
                            : theme.palette.divider
                        }` 
                      }}
                    >
                      {message.action.type === 'appointment' && (
                        <Box 
                          sx={{ 
                            display: 'flex', 
                            alignItems: 'center',
                            cursor: 'pointer',
                            '&:hover': { opacity: 0.8 }
                          }}
                          onClick={() => onScheduleAppointment?.(
                            message.action?.data.date,
                            message.action?.data.service
                          )}
                        >
                          <CalendarIcon fontSize="small" sx={{ mr: 1 }} />
                          <Typography variant="body2">
                            Agendar: {message.action.data.service} - {message.action.data.date}
                          </Typography>
                        </Box>
                      )}
                      
                      {message.action.type === 'payment' && (
                        <Box 
                          sx={{ 
                            display: 'flex', 
                            alignItems: 'center',
                            cursor: 'pointer',
                            '&:hover': { opacity: 0.8 }
                          }}
                          onClick={() => onSendPaymentLink?.(
                            message.action?.data.amount,
                            message.action?.data.description
                          )}
                        >
                          <MoneyIcon fontSize="small" sx={{ mr: 1 }} />
                          <Typography variant="body2">
                            Pagar: {message.action.data.description} - R$ {message.action.data.amount.toFixed(2)}
                          </Typography>
                        </Box>
                      )}
                    </Box>
                  )}
                </MessageBubble>
                
                <Typography 
                  variant="caption" 
                  sx={{ 
                    display: 'block', 
                    textAlign: message.sender === 'user' ? 'right' : 'left',
                    color: 'text.secondary',
                    fontSize: '0.7rem',
                    mt: 0.5
                  }}
                >
                  {message.timestamp.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                  {message.status === 'sending' && ' · Enviando...'}
                  {message.status === 'error' && ' · Erro ao enviar'}
                </Typography>
              </Box>
              
              {message.sender === 'user' && (
                <Avatar 
                  sx={{ 
                    width: 32, 
                    height: 32, 
                    ml: 1,
                    bgcolor: 'secondary.main',
                    display: { xs: 'none', sm: 'flex' }
                  }}
                >
                  {user?.name?.[0]?.toUpperCase() || 'U'}
                </Avatar>
              )}
            </Box>
          ))}
          <div ref={messagesEndRef} />
        </Box>
        
        {/* Área de input */}
        <Box 
          sx={{ 
            p: 2, 
            borderTop: `1px solid ${theme.palette.divider}`,
            backgroundColor: theme.palette.background.paper
          }}
        >
          <Box sx={{ display: 'flex', alignItems: 'flex-end' }}>
            <TextField
              fullWidth
              multiline
              maxRows={4}
              placeholder="Digite sua mensagem..."
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={handleKeyDown}
              disabled={isProcessing}
              variant="outlined"
              size="small"
              InputProps={{
                sx: {
                  borderRadius: 4,
                  backgroundColor: theme.palette.background.default
                }
              }}
            />
            <IconButton 
              color="primary" 
              onClick={handleSendMessage} 
              disabled={!input.trim() || isProcessing}
              sx={{ ml: 1 }}
            >
              {isProcessing ? <CircularProgress size={24} /> : <SendIcon />}
            </IconButton>
          </Box>
          <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block', textAlign: 'center' }}>
            Assistente Virtual - ClinicaSalão
          </Typography>
        </Box>
      </Drawer>
    </>
  );
};

export default AIChat;
