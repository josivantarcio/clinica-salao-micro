import React, { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { 
  Box, Paper, Typography, Chip, Grid, Button, Divider, 
  Card, CardContent, CircularProgress, Link, Tab, Tabs
} from '@mui/material';
import {
  ArrowBack as BackIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  AttachMoney as MoneyIcon,
  Event as EventIcon,
  Receipt as ReceiptIcon
} from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import { useSnackbar } from 'notistack';

// Importando serviços e componentes
import { getTransactionById, deleteTransaction } from '../../services/financeService';
import PaymentProcessor from '../../components/finance/PaymentProcessor';
import AppointmentLinker from '../../components/finance/AppointmentLinker';
import { formatCurrency, formatDate } from '../../utils/formatters';

// Interface para tipagem de transações
interface Transaction {
  id: string;
  clientId: string;
  clientName: string;
  appointmentId: string | null;
  serviceName: string | null;
  professionalName: string | null;
  type: 'PAYMENT' | 'REFUND';
  amount: number;
  status: 'PENDING' | 'PAID' | 'REFUNDED' | 'CANCELLED';
  paymentMethod: string;
  paymentGatewayId: string | null;
  invoiceUrl: string | null;
  description: string;
  createdAt: string;
  updatedAt: string;
  dueDate?: string;
  paymentDate?: string;
}

// Dados simulados para desenvolvimento
const mockTransactions: { [key: string]: Transaction } = {
  '1': {
    id: '1',
    clientId: '101',
    clientName: 'Maria Silva',
    appointmentId: 'a1',
    serviceName: 'Corte de Cabelo',
    professionalName: 'Carlos Oliveira',
    type: 'PAYMENT',
    amount: 150.0,
    status: 'PAID',
    paymentMethod: 'CREDIT_CARD',
    paymentGatewayId: 'pay_123456',
    invoiceUrl: 'https://asaas.com/invoice/123456',
    description: 'Pagamento de serviço de cabelo',
    createdAt: '2025-05-30T14:30:00',
    updatedAt: '2025-05-30T14:35:00'
  },
  '2': {
    id: '2',
    clientId: '102',
    clientName: 'João Pereira',
    appointmentId: 'a2',
    serviceName: 'Barba',
    professionalName: 'Ana Souza',
    type: 'PAYMENT',
    amount: 80.0,
    status: 'PENDING',
    paymentMethod: 'BANK_SLIP',
    paymentGatewayId: 'pay_234567',
    invoiceUrl: 'https://asaas.com/invoice/234567',
    description: 'Pagamento de serviço de barba',
    createdAt: '2025-05-30T15:00:00',
    updatedAt: '2025-05-30T15:00:00'
  }
};

// Função para determinar a cor do chip de status
const getStatusColor = (status: string) => {
  switch (status) {
    case 'PAID':
      return 'success';
    case 'PENDING':
      return 'warning';
    case 'REFUNDED':
      return 'info';
    case 'CANCELLED':
      return 'error';
    default:
      return 'default';
  }
};

const TransactionDetails: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { enqueueSnackbar } = useSnackbar();
  
  // Buscando dados do backend com fallback para dados simulados
  const { data: transaction, isLoading, error, refetch } = useQuery({
    queryKey: ['transaction', id],
    queryFn: async () => {
      try {
        if (!id) throw new Error('ID não fornecido');
        // Tenta buscar dados do backend
        return await getTransactionById(id);
      } catch (error) {
        console.error('Erro ao buscar detalhes da transação:', error);
        // Fallback para dados simulados
        return mockTransactions[id || '1'] || null;
      }
    },
    enabled: !!id
  });

  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '50vh' }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Box>
        <Typography variant="h6" color="error">
          Erro ao carregar transação
        </Typography>
        <Button 
          startIcon={<BackIcon />} 
          onClick={() => navigate('/finance')}
          sx={{ mt: 2 }}
        >
          Voltar para Transações
        </Button>
      </Box>
    );
  }
  
  if (!transaction && !isLoading) {
    return (
      <Box>
        <Typography variant="h6" color="error">
          Transação não encontrada
        </Typography>
        <Button 
          startIcon={<BackIcon />} 
          onClick={() => navigate('/finance')}
          sx={{ mt: 2 }}
        >
          Voltar para Transações
        </Button>
      </Box>
    );
  }

  // Estado para controlar as abas
  const [activeTab, setActiveTab] = useState<number>(0);

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setActiveTab(newValue);
  };

  // Callback para quando o status de pagamento mudar
  const handlePaymentStatusChanged = () => {
    refetch();
    enqueueSnackbar('Status de pagamento atualizado com sucesso', { variant: 'success' });
  };

  // Callback para quando um agendamento for vinculado
  const handleAppointmentLinked = () => {
    refetch();
    enqueueSnackbar('Agendamento vinculado com sucesso', { variant: 'success' });
  };

  const handleDelete = async () => {
    if (!id) return;
    try {
      await deleteTransaction(id);
      enqueueSnackbar('Transação excluída com sucesso', { variant: 'success' });
      navigate('/finance');
    } catch (error) {
      console.error('Erro ao excluir transação:', error);
      enqueueSnackbar('Erro ao excluir transação', { variant: 'error' });
    }
  };

  return (
    <Box sx={{ width: '100%' }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3, alignItems: 'center' }}>
        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          <Button 
            startIcon={<BackIcon />} 
            onClick={() => navigate('/finance')}
            sx={{ mr: 2 }}
          >
            Voltar
          </Button>
          <Typography variant="h4">
            Detalhes da Transação
          </Typography>
        </Box>
        <Box>
          <Button 
            variant="outlined" 
            startIcon={<EditIcon />} 
            onClick={() => navigate(`/finance/transactions/${id}/edit`)}
            sx={{ mr: 1 }}
          >
            Editar
          </Button>
          <Button 
            variant="outlined" 
            color="error" 
            startIcon={<DeleteIcon />}
            onClick={handleDelete}
          >
            Excluir
          </Button>
        </Box>
      </Box>

      {/* Status e abas de funcionalidades */}
      <Paper sx={{ p: 3, mb: 3 }}>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} md={6}>
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <Typography variant="h5" sx={{ mr: 2 }}>
                Status:
              </Typography>
              <Chip
                label={
                  transaction.status === 'PAID' ? 'Pago' :
                  transaction.status === 'PENDING' ? 'Pendente' :
                  transaction.status === 'REFUNDED' ? 'Reembolsado' : 'Cancelado'
                }
                color={getStatusColor(transaction.status) as any}
                sx={{ fontSize: '1rem', padding: '8px', height: 'auto' }}
              />
            </Box>
          </Grid>
          <Grid item xs={12} md={6} sx={{ display: 'flex', justifyContent: { xs: 'flex-start', md: 'flex-end' } }}>
            <Box>
              <Tabs value={activeTab} onChange={handleTabChange} aria-label="transaction functionality tabs">
                <Tab icon={<MoneyIcon />} label="Pagamento" id="tab-0" aria-controls="tabpanel-0" />
                <Tab icon={<EventIcon />} label="Agendamentos" id="tab-1" aria-controls="tabpanel-1" />
                <Tab icon={<ReceiptIcon />} label="Comprovante" id="tab-2" aria-controls="tabpanel-2" disabled />
              </Tabs>
            </Box>
          </Grid>
        </Grid>
      </Paper>
      
      {/* Conteúdo baseado na aba selecionada */}
      <Box role="tabpanel" hidden={activeTab !== 0} id="tabpanel-0" aria-labelledby="tab-0" sx={{ mb: 3 }}>
        {activeTab === 0 && (
          <PaymentProcessor 
            transactionId={transaction.id}
            currentStatus={transaction.status}
            paymentMethod={transaction.paymentMethod}
            amount={transaction.amount}
            onStatusChange={handlePaymentStatusChanged}
          />
        )}
      </Box>
      
      <Box role="tabpanel" hidden={activeTab !== 1} id="tabpanel-1" aria-labelledby="tab-1" sx={{ mb: 3 }}>
        {activeTab === 1 && (
          <AppointmentLinker 
            transactionId={transaction.id}
            clientId={transaction.clientId ? Number(transaction.clientId) : null}
            onAppointmentLinked={handleAppointmentLinked}
          />
        )}
      </Box>
      
      <Box role="tabpanel" hidden={activeTab !== 2} id="tabpanel-2" aria-labelledby="tab-2" sx={{ mb: 3 }}>
        {activeTab === 2 && (
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" align="center" color="text.secondary">
              Funcionalidade de comprovante em desenvolvimento
            </Typography>
          </Paper>
        )}
      </Box>

      {/* Detalhes da transação */}
      <Grid container spacing={3}>
        {/* Informações básicas */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Informações da Transação
              </Typography>
              <Divider sx={{ mb: 2 }} />
              <Grid container spacing={2}>
                <Grid item xs={4}>
                  <Typography color="text.secondary">ID:</Typography>
                </Grid>
                <Grid item xs={8}>
                  <Typography>{transaction.id}</Typography>
                </Grid>
                
                <Grid item xs={4}>
                  <Typography color="text.secondary">Tipo:</Typography>
                </Grid>
                <Grid item xs={8}>
                  <Chip
                    label={transaction.type === 'PAYMENT' ? 'Pagamento' : 'Reembolso'}
                    color={transaction.type === 'PAYMENT' ? 'primary' : 'secondary'}
                    size="small"
                  />
                </Grid>
                
                <Grid item xs={4}>
                  <Typography color="text.secondary">Valor:</Typography>
                </Grid>
                <Grid item xs={8}>
                  <Typography fontWeight="bold">
                    {formatCurrency(transaction.amount)}
                  </Typography>
                </Grid>
                
                <Grid item xs={4}>
                  <Typography color="text.secondary">Método:</Typography>
                </Grid>
                <Grid item xs={8}>
                  <Typography>
                    {transaction.paymentMethod === 'CREDIT_CARD' ? 'Cartão de Crédito' :
                     transaction.paymentMethod === 'BANK_SLIP' ? 'Boleto' :
                     transaction.paymentMethod === 'PIX' ? 'PIX' : transaction.paymentMethod}
                  </Typography>
                </Grid>
                
                <Grid item xs={4}>
                  <Typography color="text.secondary">Descrição:</Typography>
                </Grid>
                <Grid item xs={8}>
                  <Typography>{transaction.description}</Typography>
                </Grid>

                {transaction.paymentGatewayId && (
                  <>
                    <Grid item xs={4}>
                      <Typography color="text.secondary">ID Gateway:</Typography>
                    </Grid>
                    <Grid item xs={8}>
                      <Typography>{transaction.paymentGatewayId}</Typography>
                    </Grid>
                  </>
                )}

                {transaction.invoiceUrl && (
                  <>
                    <Grid item xs={4}>
                      <Typography color="text.secondary">Fatura:</Typography>
                    </Grid>
                    <Grid item xs={8}>
                      <Link href={transaction.invoiceUrl} target="_blank">
                        Visualizar Fatura
                      </Link>
                    </Grid>
                  </>
                )}
              </Grid>
            </CardContent>
          </Card>
        </Grid>

        {/* Informações relacionadas */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Informações Relacionadas
              </Typography>
              <Divider sx={{ mb: 2 }} />
              <Grid container spacing={2}>
                <Grid item xs={4}>
                  <Typography color="text.secondary">Cliente:</Typography>
                </Grid>
                <Grid item xs={8}>
                  <Link href={`/clients/${transaction.clientId}`}>
                    {transaction.clientName}
                  </Link>
                </Grid>

                {transaction.appointmentId && (
                  <>
                    <Grid item xs={4}>
                      <Typography color="text.secondary">Agendamento:</Typography>
                    </Grid>
                    <Grid item xs={8}>
                      <Link href={`/appointments/${transaction.appointmentId}`}>
                        Ver detalhes do agendamento
                      </Link>
                    </Grid>

                    {transaction.serviceName && (
                      <>
                        <Grid item xs={4}>
                          <Typography color="text.secondary">Serviço:</Typography>
                        </Grid>
                        <Grid item xs={8}>
                          <Typography>{transaction.serviceName}</Typography>
                        </Grid>
                      </>
                    )}

                    {transaction.professionalName && (
                      <>
                        <Grid item xs={4}>
                          <Typography color="text.secondary">Profissional:</Typography>
                        </Grid>
                        <Grid item xs={8}>
                          <Typography>{transaction.professionalName}</Typography>
                        </Grid>
                      </>
                    )}
                  </>
                )}

                <Grid item xs={4}>
                  <Typography color="text.secondary">Criado em:</Typography>
                </Grid>
                <Grid item xs={8}>
                  <Typography>
                    {formatDate(transaction.createdAt, "dd/MM/yyyy 'às' HH:mm")}
                  </Typography>
                </Grid>

                <Grid item xs={4}>
                  <Typography color="text.secondary">Atualizado em:</Typography>
                </Grid>
                <Grid item xs={8}>
                  <Typography>
                    {formatDate(transaction.updatedAt, "dd/MM/yyyy 'às' HH:mm")}
                  </Typography>
                </Grid>
                
                {transaction.dueDate && (
                  <>
                    <Grid item xs={4}>
                      <Typography color="text.secondary">Vencimento:</Typography>
                    </Grid>
                    <Grid item xs={8}>
                      <Typography>
                        {formatDate(transaction.dueDate)}
                      </Typography>
                    </Grid>
                  </>
                )}
                
                {transaction.paymentDate && (
                  <>
                    <Grid item xs={4}>
                      <Typography color="text.secondary">Data de Pagamento:</Typography>
                    </Grid>
                    <Grid item xs={8}>
                      <Typography>
                        {formatDate(transaction.paymentDate, "dd/MM/yyyy 'às' HH:mm")}
                      </Typography>
                    </Grid>
                  </>
                )}
              </Grid>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default TransactionDetails;
