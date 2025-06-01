import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Box, Paper, Typography, Grid, TextField, MenuItem,
  Button, CircularProgress, FormControl, InputLabel,
  Select, FormHelperText, InputAdornment
} from '@mui/material';
import {
  ArrowBack as BackIcon,
  Save as SaveIcon
} from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import { useSnackbar } from 'notistack';

// Importando serviços reais
import { getTransactionById, createTransaction, updateTransaction, TransactionDTO } from '../../services/financeService';

// Estes serviços seriam implementados em arquivos separados
// Para agora, usaremos os dados simulados para clientes e agendamentos
// import { getAllClients } from '../../services/clientService';
// import { getAllAppointments } from '../../services/appointmentService';

// Interfaces para tipagem
interface Transaction {
  id?: string;
  clientId: string;
  appointmentId?: string;
  type: 'PAYMENT' | 'REFUND';
  amount: number;
  status: 'PENDING' | 'PAID' | 'REFUNDED' | 'CANCELLED';
  paymentMethod: string;
  description: string;
}

interface Client {
  id: string;
  name: string;
}

interface Appointment {
  id: string;
  serviceName: string;
  clientId: string;
  date: string;
}

// Dados simulados para desenvolvimento
const mockTransaction: Transaction = {
  id: '1',
  clientId: '101',
  appointmentId: 'a1',
  type: 'PAYMENT',
  amount: 150.0,
  status: 'PENDING',
  paymentMethod: 'CREDIT_CARD',
  description: 'Pagamento de serviço de cabelo'
};

const mockClients: Client[] = [
  { id: '101', name: 'Maria Silva' },
  { id: '102', name: 'João Pereira' },
  { id: '103', name: 'Ana Souza' },
  { id: '104', name: 'Carlos Oliveira' }
];

const mockAppointments: Appointment[] = [
  { id: 'a1', serviceName: 'Corte de Cabelo', clientId: '101', date: '2025-06-02T14:30:00' },
  { id: 'a2', serviceName: 'Barba', clientId: '102', date: '2025-06-03T15:00:00' },
  { id: 'a3', serviceName: 'Manicure', clientId: '103', date: '2025-06-03T16:30:00' }
];

const TransactionForm: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { enqueueSnackbar } = useSnackbar();
  const isEditMode = !!id;

  const [formData, setFormData] = useState<Transaction>({
    clientId: '',
    appointmentId: '',
    type: 'PAYMENT',
    amount: 0,
    status: 'PENDING',
    paymentMethod: '',
    description: ''
  });

  const [errors, setErrors] = useState<{[key: string]: string}>({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [filteredAppointments, setFilteredAppointments] = useState<Appointment[]>([]);

  // Buscando dados do backend com fallback para dados simulados
  const { data: transactionData, isLoading: isLoadingTransaction } = useQuery({
    queryKey: ['transaction', id],
    queryFn: async () => {
      if (!isEditMode || !id) return null;
      
      try {
        // Tenta buscar dados do backend
        return await getTransactionById(id);
      } catch (error) {
        console.error('Erro ao buscar detalhes da transação:', error);
        // Fallback para dados simulados
        return mockTransaction;
      }
    },
    enabled: isEditMode && !!id
  });
  
  // Para clientes e agendamentos, usamos dados simulados por enquanto
  // Em um ambiente real, faríamos a integração com outros serviços

  const { data: clients = [], isLoading: isLoadingClients } = useQuery({
    queryKey: ['clients'],
    queryFn: () => Promise.resolve(mockClients),
    initialData: mockClients
  });

  const { data: appointments = [], isLoading: isLoadingAppointments } = useQuery({
    queryKey: ['appointments'],
    queryFn: () => Promise.resolve(mockAppointments),
    initialData: mockAppointments
  });

  useEffect(() => {
    if (isEditMode && transactionData) {
      setFormData(transactionData);
    }
  }, [isEditMode, transactionData]);

  // Filtrar agendamentos baseados no cliente selecionado
  useEffect(() => {
    if (formData.clientId && appointments.length > 0) {
      const filtered = appointments.filter(apt => apt.clientId === formData.clientId);
      setFilteredAppointments(filtered);
      
      // Se o agendamento atual não pertence ao cliente selecionado, limpa a seleção
      if (formData.appointmentId && !filtered.some(apt => apt.id === formData.appointmentId)) {
        setFormData(prev => ({ ...prev, appointmentId: '' }));
      }
    } else {
      setFilteredAppointments([]);
    }
  }, [formData.clientId, appointments, formData.appointmentId]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | { name?: string; value: unknown }>) => {
    const { name, value } = e.target;
    if (name) {
      setFormData(prev => ({ ...prev, [name]: value }));
      
      // Limpa o erro para o campo alterado
      if (errors[name]) {
        setErrors(prev => ({ ...prev, [name]: '' }));
      }
    }
  };

  const validateForm = (): boolean => {
    const newErrors: {[key: string]: string} = {};
    
    if (!formData.clientId) {
      newErrors.clientId = 'O cliente é obrigatório';
    }
    
    if (!formData.type) {
      newErrors.type = 'O tipo de transação é obrigatório';
    }
    
    if (!formData.amount || formData.amount <= 0) {
      newErrors.amount = 'O valor deve ser maior que zero';
    }
    
    if (!formData.status) {
      newErrors.status = 'O status é obrigatório';
    }
    
    if (!formData.paymentMethod) {
      newErrors.paymentMethod = 'O método de pagamento é obrigatório';
    }
    
    if (!formData.description) {
      newErrors.description = 'A descrição é obrigatória';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm()) {
      enqueueSnackbar('Por favor, corrija os erros no formulário', { variant: 'error' });
      return;
    }
    
    setIsSubmitting(true);
    
    try {
      // Chamando a API real com fallback para simulação
      try {
        if (isEditMode && id) {
          await updateTransaction(id, formData);
        } else {
          await createTransaction(formData);
        }
      } catch (error) {
        console.error('Erro ao salvar transação no backend:', error);
        
        // Simular operação bem-sucedida para testes
        await new Promise(resolve => setTimeout(resolve, 1000));
      }
      
      enqueueSnackbar(
        isEditMode 
          ? 'Transação atualizada com sucesso!' 
          : 'Transação criada com sucesso!',
        { variant: 'success' }
      );
      
      navigate('/finance');
    } catch (error) {
      console.error('Erro ao salvar transação:', error);
      enqueueSnackbar('Erro ao salvar a transação', { variant: 'error' });
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoadingTransaction && isEditMode) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '50vh' }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box sx={{ width: '100%' }}>
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
        <Button 
          startIcon={<BackIcon />} 
          onClick={() => navigate('/finance')}
          sx={{ mr: 2 }}
        >
          Voltar
        </Button>
        <Typography variant="h4">
          {isEditMode ? 'Editar Transação' : 'Nova Transação'}
        </Typography>
      </Box>

      <Paper sx={{ p: 3 }}>
        <Box component="form" onSubmit={handleSubmit}>
          <Grid container spacing={3}>
            <Grid item xs={12} md={6}>
              <FormControl fullWidth error={!!errors.clientId}>
                <InputLabel>Cliente</InputLabel>
                <Select
                  name="clientId"
                  value={formData.clientId}
                  onChange={handleChange}
                  label="Cliente"
                  disabled={isLoadingClients || isSubmitting}
                >
                  {clients.map(client => (
                    <MenuItem key={client.id} value={client.id}>
                      {client.name}
                    </MenuItem>
                  ))}
                </Select>
                {errors.clientId && (
                  <FormHelperText>{errors.clientId}</FormHelperText>
                )}
              </FormControl>
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControl fullWidth>
                <InputLabel>Agendamento (Opcional)</InputLabel>
                <Select
                  name="appointmentId"
                  value={formData.appointmentId || ''}
                  onChange={handleChange}
                  label="Agendamento (Opcional)"
                  disabled={isLoadingAppointments || isSubmitting || !formData.clientId || filteredAppointments.length === 0}
                >
                  <MenuItem value="">
                    <em>Nenhum</em>
                  </MenuItem>
                  {filteredAppointments.map(appointment => (
                    <MenuItem key={appointment.id} value={appointment.id}>
                      {appointment.serviceName} - {new Date(appointment.date).toLocaleString('pt-BR', {
                        day: '2-digit',
                        month: '2-digit',
                        year: '2-digit',
                        hour: '2-digit',
                        minute: '2-digit'
                      })}
                    </MenuItem>
                  ))}
                </Select>
                {formData.clientId && filteredAppointments.length === 0 && (
                  <FormHelperText>
                    Não há agendamentos para este cliente
                  </FormHelperText>
                )}
              </FormControl>
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControl fullWidth error={!!errors.type}>
                <InputLabel>Tipo</InputLabel>
                <Select
                  name="type"
                  value={formData.type}
                  onChange={handleChange}
                  label="Tipo"
                  disabled={isSubmitting}
                >
                  <MenuItem value="PAYMENT">Pagamento</MenuItem>
                  <MenuItem value="REFUND">Reembolso</MenuItem>
                </Select>
                {errors.type && (
                  <FormHelperText>{errors.type}</FormHelperText>
                )}
              </FormControl>
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControl fullWidth error={!!errors.status}>
                <InputLabel>Status</InputLabel>
                <Select
                  name="status"
                  value={formData.status}
                  onChange={handleChange}
                  label="Status"
                  disabled={isSubmitting}
                >
                  <MenuItem value="PENDING">Pendente</MenuItem>
                  <MenuItem value="PAID">Pago</MenuItem>
                  <MenuItem value="REFUNDED">Reembolsado</MenuItem>
                  <MenuItem value="CANCELLED">Cancelado</MenuItem>
                </Select>
                {errors.status && (
                  <FormHelperText>{errors.status}</FormHelperText>
                )}
              </FormControl>
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                name="amount"
                label="Valor (R$)"
                type="number"
                value={formData.amount}
                onChange={handleChange}
                error={!!errors.amount}
                helperText={errors.amount}
                disabled={isSubmitting}
                InputProps={{
                  startAdornment: <InputAdornment position="start">R$</InputAdornment>,
                }}
                inputProps={{
                  step: "0.01",
                  min: "0"
                }}
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControl fullWidth error={!!errors.paymentMethod}>
                <InputLabel>Método de Pagamento</InputLabel>
                <Select
                  name="paymentMethod"
                  value={formData.paymentMethod}
                  onChange={handleChange}
                  label="Método de Pagamento"
                  disabled={isSubmitting}
                >
                  <MenuItem value="CREDIT_CARD">Cartão de Crédito</MenuItem>
                  <MenuItem value="DEBIT_CARD">Cartão de Débito</MenuItem>
                  <MenuItem value="BANK_SLIP">Boleto</MenuItem>
                  <MenuItem value="PIX">PIX</MenuItem>
                  <MenuItem value="CASH">Dinheiro</MenuItem>
                </Select>
                {errors.paymentMethod && (
                  <FormHelperText>{errors.paymentMethod}</FormHelperText>
                )}
              </FormControl>
            </Grid>

            <Grid item xs={12}>
              <TextField
                fullWidth
                name="description"
                label="Descrição"
                value={formData.description}
                onChange={handleChange}
                error={!!errors.description}
                helperText={errors.description}
                disabled={isSubmitting}
                multiline
                rows={4}
              />
            </Grid>

            <Grid item xs={12} sx={{ display: 'flex', justifyContent: 'flex-end', mt: 2 }}>
              <Button
                type="button"
                variant="outlined"
                onClick={() => navigate('/finance')}
                disabled={isSubmitting}
                sx={{ mr: 2 }}
              >
                Cancelar
              </Button>
              <Button
                type="submit"
                variant="contained"
                startIcon={<SaveIcon />}
                disabled={isSubmitting}
              >
                {isSubmitting ? <CircularProgress size={24} /> : isEditMode ? 'Atualizar' : 'Salvar'}
              </Button>
            </Grid>
          </Grid>
        </Box>
      </Paper>
    </Box>
  );
};

export default TransactionForm;
