import React, { useState } from 'react';
import { 
  Box, Typography, Paper, Button, Table, TableBody, TableCell, 
  TableContainer, TableHead, TableRow, TablePagination, IconButton,
  Chip, TextField, InputAdornment, Tooltip
} from '@mui/material';
import {
  Add as AddIcon,
  Search as SearchIcon,
  Visibility as ViewIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Link as LinkIcon,
  CheckCircle as CheckCircleIcon,
  MoneyOff as RefundIcon
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useSnackbar } from 'notistack';
import { useQuery } from '@tanstack/react-query';

// Importando serviço real de finanças
import { getAllTransactions, processPayment, processRefund, deleteTransaction, generatePaymentLink, TransactionDTO } from '../../services/financeService';

// Interface para tipagem de transações
interface Transaction {
  id: string;
  clientId: string;
  clientName: string;
  appointmentId: string | null;
  type: 'PAYMENT' | 'REFUND';
  amount: number;
  status: 'PENDING' | 'PAID' | 'REFUNDED' | 'CANCELLED';
  paymentMethod: string;
  paymentGatewayId: string | null;
  invoiceUrl: string | null;
  description: string;
  createdAt: string;
  updatedAt: string;
}

// Dados simulados para desenvolvimento
const mockTransactions: Transaction[] = [
  {
    id: '1',
    clientId: '101',
    clientName: 'Maria Silva',
    appointmentId: 'a1',
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
  {
    id: '2',
    clientId: '102',
    clientName: 'João Pereira',
    appointmentId: 'a2',
    type: 'PAYMENT',
    amount: 80.0,
    status: 'PENDING',
    paymentMethod: 'BANK_SLIP',
    paymentGatewayId: 'pay_234567',
    invoiceUrl: 'https://asaas.com/invoice/234567',
    description: 'Pagamento de serviço de barba',
    createdAt: '2025-05-30T15:00:00',
    updatedAt: '2025-05-30T15:00:00'
  },
  {
    id: '3',
    clientId: '103',
    clientName: 'Ana Souza',
    appointmentId: 'a3',
    type: 'PAYMENT',
    amount: 200.0,
    status: 'PAID',
    paymentMethod: 'PIX',
    paymentGatewayId: 'pay_345678',
    invoiceUrl: 'https://asaas.com/invoice/345678',
    description: 'Pagamento de serviço de manicure',
    createdAt: '2025-05-30T15:30:00',
    updatedAt: '2025-05-30T15:33:00'
  },
  {
    id: '4',
    clientId: '101',
    clientName: 'Maria Silva',
    appointmentId: 'a4',
    type: 'REFUND',
    amount: 150.0,
    status: 'REFUNDED',
    paymentMethod: 'CREDIT_CARD',
    paymentGatewayId: 'ref_123456',
    invoiceUrl: null,
    description: 'Reembolso de serviço cancelado',
    createdAt: '2025-05-31T09:00:00',
    updatedAt: '2025-05-31T09:05:00'
  },
  {
    id: '5',
    clientId: '104',
    clientName: 'Carlos Oliveira',
    appointmentId: 'a5',
    type: 'PAYMENT',
    amount: 120.0,
    status: 'CANCELLED',
    paymentMethod: 'CREDIT_CARD',
    paymentGatewayId: null,
    invoiceUrl: null,
    description: 'Pagamento cancelado pelo cliente',
    createdAt: '2025-05-31T10:00:00',
    updatedAt: '2025-05-31T10:15:00'
  }
];

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

// Componente principal
const Transactions: React.FC = () => {
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [searchQuery, setSearchQuery] = useState('');
  const navigate = useNavigate();
  const { enqueueSnackbar } = useSnackbar();

  // Buscando dados do backend com fallback para dados simulados
  const { data: transactions, isLoading, error, refetch } = useQuery({
    queryKey: ['transactions'],
    queryFn: async () => {
      try {
        // Tenta buscar dados do backend
        return await getAllTransactions();
      } catch (error) {
        console.error('Erro ao buscar transações do backend:', error);
        // Fallback para dados simulados
        return mockTransactions;
      }
    },
    initialData: mockTransactions
  });

  const filteredTransactions = transactions.filter(
    (transaction) =>
      transaction.clientName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      transaction.description.toLowerCase().includes(searchQuery.toLowerCase()) ||
      transaction.id.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const handleChangePage = (event: unknown, newPage: number) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  const handleSearchChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setSearchQuery(event.target.value);
    setPage(0);
  };

  const handleViewTransaction = (id: string) => {
    navigate(`/finance/transactions/${id}`);
  };

  const handleEditTransaction = (id: string) => {
    navigate(`/finance/transactions/${id}/edit`);
  };

  const handleDeleteTransaction = async (id: string) => {
    try {
      await deleteTransaction(id);
      enqueueSnackbar('Transação excluída com sucesso', { variant: 'success' });
      refetch(); // Atualiza a lista após excluir
    } catch (error) {
      console.error('Erro ao excluir transação:', error);
      enqueueSnackbar('Erro ao excluir transação', { variant: 'error' });
    }
  };

  const handleGeneratePaymentLink = async (id: string) => {
    try {
      const result = await generatePaymentLink(id);
      enqueueSnackbar(
        `Link de pagamento gerado com sucesso: ${result.paymentUrl}`, 
        { variant: 'success' }
      );
      refetch(); // Atualiza a lista após gerar o link
    } catch (error) {
      console.error('Erro ao gerar link de pagamento:', error);
      enqueueSnackbar('Erro ao gerar link de pagamento', { variant: 'error' });
    }
  };

  const handleProcessPayment = async (id: string) => {
    try {
      await processPayment(id);
      enqueueSnackbar('Pagamento processado com sucesso', { variant: 'success' });
      refetch(); // Atualiza a lista após processamento
    } catch (error) {
      console.error('Erro ao processar pagamento:', error);
      enqueueSnackbar('Erro ao processar pagamento', { variant: 'error' });
    }
  };

  const handleProcessRefund = async (id: string) => {
    try {
      await processRefund(id);
      enqueueSnackbar('Reembolso processado com sucesso', { variant: 'info' });
      refetch(); // Atualiza a lista após reembolso
    } catch (error) {
      console.error('Erro ao processar reembolso:', error);
      enqueueSnackbar('Erro ao processar reembolso', { variant: 'error' });
    }
  };

  return (
    <Box sx={{ width: '100%' }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
        <Typography variant="h4">Transações Financeiras</Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => navigate('/finance/transactions/new')}
        >
          Nova Transação
        </Button>
      </Box>

      <Paper sx={{ width: '100%', mb: 3 }}>
        <Box sx={{ p: 2, pb: 0 }}>
          <TextField
            fullWidth
            variant="outlined"
            placeholder="Buscar por cliente, descrição ou ID"
            value={searchQuery}
            onChange={handleSearchChange}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon />
                </InputAdornment>
              )
            }}
            sx={{ mb: 2 }}
          />
        </Box>

        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>ID</TableCell>
                <TableCell>Cliente</TableCell>
                <TableCell>Tipo</TableCell>
                <TableCell>Valor</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Método</TableCell>
                <TableCell>Data</TableCell>
                <TableCell align="center">Ações</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {isLoading ? (
                <TableRow>
                  <TableCell colSpan={8} align="center">
                    <CircularProgress size={30} />
                  </TableCell>
                </TableRow>
              ) : error ? (
                <TableRow>
                  <TableCell colSpan={8} align="center">
                    <Typography color="error">
                      Erro ao carregar transações. Por favor, tente novamente.
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : filteredTransactions
                .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                .map((transaction) => (
                  <TableRow key={transaction.id}>
                    <TableCell>{transaction.id}</TableCell>
                    <TableCell>{transaction.clientName}</TableCell>
                    <TableCell>
                      <Chip
                        label={transaction.type === 'PAYMENT' ? 'Pagamento' : 'Reembolso'}
                        color={transaction.type === 'PAYMENT' ? 'primary' : 'secondary'}
                        size="small"
                      />
                    </TableCell>
                    <TableCell>
                      R$ {transaction.amount.toFixed(2)}
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={
                          transaction.status === 'PAID' ? 'Pago' :
                          transaction.status === 'PENDING' ? 'Pendente' :
                          transaction.status === 'REFUNDED' ? 'Reembolsado' : 'Cancelado'
                        }
                        color={getStatusColor(transaction.status) as any}
                        size="small"
                      />
                    </TableCell>
                    <TableCell>
                      {transaction.paymentMethod === 'CREDIT_CARD' ? 'Cartão de Crédito' :
                       transaction.paymentMethod === 'BANK_SLIP' ? 'Boleto' :
                       transaction.paymentMethod === 'PIX' ? 'PIX' : transaction.paymentMethod}
                    </TableCell>
                    <TableCell>
                      {new Date(transaction.createdAt).toLocaleString('pt-BR', {
                        day: '2-digit',
                        month: '2-digit',
                        year: '2-digit',
                        hour: '2-digit',
                        minute: '2-digit'
                      })}
                    </TableCell>
                    <TableCell align="center">
                      <Box sx={{ display: 'flex', justifyContent: 'center' }}>
                        <Tooltip title="Visualizar">
                          <IconButton
                            size="small"
                            onClick={() => handleViewTransaction(transaction.id)}
                          >
                            <ViewIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Editar">
                          <IconButton
                            size="small"
                            onClick={() => handleEditTransaction(transaction.id)}
                          >
                            <EditIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Excluir">
                          <IconButton
                            size="small"
                            onClick={() => handleDeleteTransaction(transaction.id)}
                          >
                            <DeleteIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>

                        {transaction.status === 'PENDING' && transaction.type === 'PAYMENT' && (
                          <>
                            <Tooltip title="Gerar Link de Pagamento">
                              <IconButton
                                size="small"
                                color="primary"
                                onClick={() => handleGeneratePaymentLink(transaction.id)}
                              >
                                <LinkIcon fontSize="small" />
                              </IconButton>
                            </Tooltip>
                            <Tooltip title="Processar Pagamento">
                              <IconButton
                                size="small"
                                color="success"
                                onClick={() => handleProcessPayment(transaction.id)}
                              >
                                <CheckCircleIcon fontSize="small" />
                              </IconButton>
                            </Tooltip>
                          </>
                        )}

                        {transaction.status === 'PAID' && transaction.type === 'PAYMENT' && (
                          <Tooltip title="Processar Reembolso">
                            <IconButton
                              size="small"
                              color="info"
                              onClick={() => handleProcessRefund(transaction.id)}
                            >
                              <RefundIcon fontSize="small" />
                            </IconButton>
                          </Tooltip>
                        )}
                      </Box>
                    </TableCell>
                  </TableRow>
                ))}
              {filteredTransactions.length === 0 && (
                <TableRow>
                  <TableCell colSpan={8} align="center">
                    <Typography variant="body1" color="text.secondary">
                      Nenhuma transação encontrada.
                    </Typography>
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
        
        <TablePagination
          component="div"
          count={filteredTransactions.length}
          rowsPerPage={rowsPerPage}
          page={page}
          onPageChange={handleChangePage}
          onRowsPerPageChange={handleChangeRowsPerPage}
          labelRowsPerPage="Itens por página"
          labelDisplayedRows={({ from, to, count }) => `${from}-${to} de ${count}`}
        />
      </Paper>
    </Box>
  );
};

export default Transactions;
