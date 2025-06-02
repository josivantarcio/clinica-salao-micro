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
  const [searchTerm, setSearchTerm] = useState('');
  
  // Filtros avançados
  const [filters, setFilters] = useState({
    status: '',
    startDate: '',
    endDate: '',
    minAmount: '',
    maxAmount: '',
    paymentMethod: ''
  });
  
  // Estado para controlar a exibição do painel de filtros
  const [showFilters, setShowFilters] = useState(false);
  const navigate = useNavigate();
  const { enqueueSnackbar } = useSnackbar();

  // Buscando dados do backend com fallback para dados simulados
  const { data: transactionsData, isLoading, error, refetch } = useQuery({
    queryKey: ['transactions', page, rowsPerPage, searchTerm, filters],
    queryFn: async () => {
      try {
        // Tenta buscar dados do backend
        return await getAllTransactions(
          page, 
          rowsPerPage, 
          searchTerm,
          filters.status || undefined,
          filters.startDate || undefined,
          filters.endDate || undefined,
          filters.minAmount ? parseFloat(filters.minAmount) : undefined,
          filters.maxAmount ? parseFloat(filters.maxAmount) : undefined,
          filters.paymentMethod || undefined
        );
      } catch (err) {
        console.error('Erro ao buscar transações:', err);
        // Fallback para dados simulados
        // Aplicando filtros localmente nos dados simulados
        let filteredData = Object.values(mockTransactions);
        
        if (searchTerm) {
          const searchLower = searchTerm.toLowerCase();
          filteredData = filteredData.filter(t => 
            t.description.toLowerCase().includes(searchLower) ||
            t.clientName.toLowerCase().includes(searchLower)
          );
        }
        
        if (filters.status) {
          filteredData = filteredData.filter(t => t.status === filters.status);
        }
        
        if (filters.paymentMethod) {
          filteredData = filteredData.filter(t => t.paymentMethod === filters.paymentMethod);
        }
        
        if (filters.startDate) {
          const startDate = new Date(filters.startDate);
          filteredData = filteredData.filter(t => new Date(t.createdAt) >= startDate);
        }
        
        if (filters.endDate) {
          const endDate = new Date(filters.endDate);
          endDate.setHours(23, 59, 59, 999);
          filteredData = filteredData.filter(t => new Date(t.createdAt) <= endDate);
        }
        
        if (filters.minAmount) {
          const minAmount = parseFloat(filters.minAmount);
          filteredData = filteredData.filter(t => t.amount >= minAmount);
        }
        
        if (filters.maxAmount) {
          const maxAmount = parseFloat(filters.maxAmount);
          filteredData = filteredData.filter(t => t.amount <= maxAmount);
        }
        
        // Converte array filtrado de volta para objeto com IDs como chaves
        const filteredMockTransactions: Record<string, Transaction> = {};
        filteredData.forEach(t => { filteredMockTransactions[t.id] = t; });
        
        return {
          data: filteredMockTransactions,
          page: page,
          size: rowsPerPage,
          totalElements: filteredData.length,
          totalPages: Math.ceil(filteredData.length / rowsPerPage)
        };
      }
    }
  });

  const handleChangePage = (event: unknown, newPage: number) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  // Busca de transações
  const handleSearch = (event: React.ChangeEvent<HTMLInputElement>) => {
    setSearchTerm(event.target.value);
    setPage(0); // Reset para a primeira página ao buscar
  };

  // Limpar busca
  const handleClearSearch = () => {
    setSearchTerm('');
  };
  
  // Atualizar filtros
  const handleFilterChange = (event: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = event.target;
    setFilters(prev => ({
      ...prev,
      [name]: value
    }));
    setPage(0); // Reset para a primeira página ao filtrar
  };
  
  // Limpar todos os filtros
  const handleClearFilters = () => {
    setFilters({
      status: '',
      startDate: '',
      endDate: '',
      minAmount: '',
      maxAmount: '',
      paymentMethod: ''
    });
    setSearchTerm('');
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
      </Box>

      {/* Barra de busca, filtros e botões */}
      <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 2 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', width: '100%', maxWidth: 500 }}>
          <TextField
            variant="outlined"
            placeholder="Buscar transações..."
            fullWidth
            value={searchTerm}
            onChange={handleSearch}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon />
                </InputAdornment>
              ),
              endAdornment: searchTerm && (
                <InputAdornment position="end">
                  <IconButton onClick={handleClearSearch} size="small">
                    <ClearIcon />
                  </IconButton>
                </InputAdornment>
              )
            }}
            size="small"
          />
        </Box>
        <Box>
          <Button
            variant="outlined"
            startIcon={showFilters ? <FilterListOffIcon /> : <FilterListIcon />}
            onClick={() => setShowFilters(!showFilters)}
            sx={{ mr: 1 }}
          >
            {showFilters ? 'Ocultar Filtros' : 'Filtros Avançados'}
          </Button>
          <Button
            variant="contained"
            color="primary"
            startIcon={<AddIcon />}
            onClick={() => navigate('/finance/transactions/new')}
          >
            Nova Transação
          </Button>
        </Box>
      </Box>
      
      {/* Painel de filtros avançados */}
      <Collapse in={showFilters}>
        <Paper sx={{ p: 2, mb: 3 }}>
          <Grid container spacing={2}>
            <Grid item xs={12} sm={6} md={3}>
              <FormControl fullWidth size="small">
                <InputLabel>Status</InputLabel>
                <Select
                  name="status"
                  value={filters.status}
                  onChange={handleFilterChange as any}
                  label="Status"
                >
                  <MenuItem value="">Todos</MenuItem>
                  <MenuItem value="PENDING">Pendente</MenuItem>
                  <MenuItem value="PAID">Pago</MenuItem>
                  <MenuItem value="REFUNDED">Reembolsado</MenuItem>
                  <MenuItem value="CANCELLED">Cancelado</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <FormControl fullWidth size="small">
                <InputLabel>Método de Pagamento</InputLabel>
                <Select
                  name="paymentMethod"
                  value={filters.paymentMethod}
                  onChange={handleFilterChange as any}
                  label="Método de Pagamento"
                >
                  <MenuItem value="">Todos</MenuItem>
                  <MenuItem value="CREDIT_CARD">Cartão de Crédito</MenuItem>
                  <MenuItem value="DEBIT_CARD">Cartão de Débito</MenuItem>
                  <MenuItem value="PIX">PIX</MenuItem>
                  <MenuItem value="MONEY">Dinheiro</MenuItem>
                  <MenuItem value="BANK_SLIP">Boleto</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <TextField
                label="Data Inicial"
                type="date"
                name="startDate"
                value={filters.startDate}
                onChange={handleFilterChange}
                fullWidth
                size="small"
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <TextField
                label="Data Final"
                type="date"
                name="endDate"
                value={filters.endDate}
                onChange={handleFilterChange}
                fullWidth
                size="small"
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <TextField
                label="Valor Mínimo"
                type="number"
                name="minAmount"
                value={filters.minAmount}
                onChange={handleFilterChange}
                fullWidth
                size="small"
                InputProps={{
                  startAdornment: <InputAdornment position="start">R$</InputAdornment>,
                }}
              />
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <TextField
                label="Valor Máximo"
                type="number"
                name="maxAmount"
                value={filters.maxAmount}
                onChange={handleFilterChange}
                fullWidth
                size="small"
                InputProps={{
                  startAdornment: <InputAdornment position="start">R$</InputAdornment>,
                }}
              />
            </Grid>
            <Grid item xs={12} sm={12} md={6} sx={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end' }}>
              <Button onClick={handleClearFilters} startIcon={<ClearAllIcon />}>
                Limpar Filtros
              </Button>
            </Grid>
          </Grid>
        </Paper>
      </Collapse>

      <Paper sx={{ width: '100%', mb: 3 }}>
        <Box sx={{ p: 2, pb: 0 }}>
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
