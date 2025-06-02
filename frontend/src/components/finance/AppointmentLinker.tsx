import React, { useState, useEffect } from 'react';
import {
  Box,
  Button,
  Card,
  CardContent,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Divider,
  Grid,
  IconButton,
  List,
  ListItem,
  ListItemAvatar,
  ListItemSecondaryAction,
  ListItemText,
  Paper,
  TextField,
  Tooltip,
  Typography
} from '@mui/material';
import {
  Event as EventIcon,
  Link as LinkIcon,
  Search as SearchIcon,
  Add as AddIcon,
  DeleteOutline as DeleteIcon,
  EventBusy as EventBusyIcon,
  Check as CheckIcon
} from '@mui/icons-material';
import { useSnackbar } from 'notistack';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { getPendingPaymentAppointments, linkAppointmentToPayment, getAppointmentPaymentStatus, createTransactionFromAppointment } from '../../services/appointmentService';
import { formatCurrency } from '../../utils/formatters';
import ClientSelector from './ClientSelector';

interface AppointmentLinkerProps {
  transactionId: string;
  clientId?: number | null;
  onAppointmentLinked?: () => void;
}

const AppointmentLinker: React.FC<AppointmentLinkerProps> = ({
  transactionId,
  clientId,
  onAppointmentLinked
}) => {
  const [openDialog, setOpenDialog] = useState(false);
  const [selectedAppointment, setSelectedAppointment] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedClient, setSelectedClient] = useState<{ id: number; name: string; email: string } | null>(
    clientId ? { id: clientId, name: '', email: '' } : null
  );
  const [page, setPage] = useState(0);
  const [isCreatingTransaction, setIsCreatingTransaction] = useState(false);
  const [createTransactionDialogOpen, setCreateTransactionDialogOpen] = useState(false);
  const [appointmentIdForTransaction, setAppointmentIdForTransaction] = useState<string | null>(null);
  const [transactionDescription, setTransactionDescription] = useState('');

  const { enqueueSnackbar } = useSnackbar();
  const queryClient = useQueryClient();

  // Query para buscar agendamentos pendentes de pagamento
  const {
    data: appointmentsData,
    isLoading,
    refetch
  } = useQuery({
    queryKey: ['pendingPaymentAppointments', selectedClient?.id, page],
    queryFn: () => getPendingPaymentAppointments(
      selectedClient?.id ? String(selectedClient.id) : undefined,
      page,
      10
    ),
    enabled: openDialog
  });

  // Mutação para vincular agendamento a pagamento
  const linkMutation = useMutation({
    mutationFn: (appointmentId: string) => linkAppointmentToPayment(appointmentId, transactionId),
    onSuccess: () => {
      enqueueSnackbar('Agendamento vinculado com sucesso!', { variant: 'success' });
      queryClient.invalidateQueries({ queryKey: ['pendingPaymentAppointments'] });
      queryClient.invalidateQueries({ queryKey: ['transaction', transactionId] });
      if (onAppointmentLinked) {
        onAppointmentLinked();
      }
      setOpenDialog(false);
    },
    onError: (error: any) => {
      enqueueSnackbar(`Erro ao vincular agendamento: ${error.response?.data?.message || error.message}`, { variant: 'error' });
    }
  });

  // Mutação para criar transação a partir de agendamento
  const createTransactionMutation = useMutation({
    mutationFn: (appointmentId: string) => createTransactionFromAppointment(appointmentId, {
      description: transactionDescription || undefined
    }),
    onSuccess: (data) => {
      enqueueSnackbar('Transação criada com sucesso!', { variant: 'success' });
      setCreateTransactionDialogOpen(false);
      setTransactionDescription('');
      setAppointmentIdForTransaction(null);
      refetch();
    },
    onError: (error: any) => {
      enqueueSnackbar(`Erro ao criar transação: ${error.response?.data?.message || error.message}`, { variant: 'error' });
    }
  });

  // Filtrar agendamentos baseado no termo de pesquisa
  const filteredAppointments = appointmentsData?.data.filter(appointment => {
    if (!searchTerm) return true;
    const term = searchTerm.toLowerCase();
    return (
      appointment.service.toLowerCase().includes(term) ||
      appointment.clientName.toLowerCase().includes(term) ||
      appointment.professionalName.toLowerCase().includes(term) ||
      appointment.id.toLowerCase().includes(term)
    );
  });

  // Função para abrir o diálogo de criação de transação
  const handleOpenCreateTransactionDialog = (appointmentId: string) => {
    setAppointmentIdForTransaction(appointmentId);
    setCreateTransactionDialogOpen(true);
    // Preencher descrição padrão com base no agendamento
    const appointment = appointmentsData?.data.find(a => a.id === appointmentId);
    if (appointment) {
      setTransactionDescription(`Pagamento de ${appointment.service} - ${appointment.date} ${appointment.startTime}`);
    }
  };

  // Função para criar transação
  const handleCreateTransaction = () => {
    if (!appointmentIdForTransaction) return;
    createTransactionMutation.mutate(appointmentIdForTransaction);
  };

  return (
    <>
      <Card variant="outlined" sx={{ mb: 3 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center' }}>
            <EventIcon sx={{ mr: 1 }} />
            Agendamentos Vinculados
          </Typography>
          <Divider sx={{ my: 2 }} />

          {/* Lista de agendamentos vinculados ou botão para vincular */}
          <Box sx={{ textAlign: 'center', py: 2 }}>
            <Button
              variant="outlined"
              color="primary"
              startIcon={<LinkIcon />}
              onClick={() => setOpenDialog(true)}
            >
              Vincular Agendamento
            </Button>
          </Box>
        </CardContent>
      </Card>

      {/* Diálogo para selecionar agendamento */}
      <Dialog
        open={openDialog}
        onClose={() => setOpenDialog(false)}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>
          Vincular Agendamento à Transação
        </DialogTitle>

        <DialogContent dividers>
          <Box sx={{ mb: 3 }}>
            <Typography variant="subtitle2" gutterBottom>
              Selecione um cliente para filtrar os agendamentos:
            </Typography>
            <ClientSelector
              value={selectedClient}
              onChange={setSelectedClient}
              label="Filtrar por Cliente"
            />
          </Box>
          
          <Box sx={{ mb: 3, display: 'flex' }}>
            <TextField
              fullWidth
              placeholder="Pesquisar agendamentos..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              InputProps={{
                startAdornment: <SearchIcon sx={{ mr: 1, color: 'text.secondary' }} />
              }}
              size="small"
              variant="outlined"
            />
          </Box>

          {isLoading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
              <CircularProgress />
            </Box>
          ) : filteredAppointments && filteredAppointments.length > 0 ? (
            <List sx={{ bgcolor: 'background.paper' }}>
              {filteredAppointments.map((appointment) => (
                <Paper key={appointment.id} sx={{ mb: 2 }}>
                  <ListItem
                    alignItems="flex-start"
                    selected={selectedAppointment === appointment.id}
                    onClick={() => setSelectedAppointment(appointment.id)}
                    sx={{
                      cursor: 'pointer',
                      borderLeft: selectedAppointment === appointment.id ? '4px solid' : 'none',
                      borderColor: 'primary.main'
                    }}
                  >
                    <ListItemAvatar>
                      <Box sx={{ 
                        width: 40, 
                        height: 40, 
                        bgcolor: 'primary.light', 
                        borderRadius: '50%',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center'
                      }}>
                        <EventIcon color="primary" />
                      </Box>
                    </ListItemAvatar>
                    <ListItemText
                      primary={
                        <Typography variant="subtitle1" sx={{ fontWeight: 'medium' }}>
                          {appointment.service}
                        </Typography>
                      }
                      secondary={
                        <>
                          <Typography component="span" variant="body2" color="text.primary">
                            Cliente: {appointment.clientName}
                          </Typography>
                          <Typography component="div" variant="body2">
                            Profissional: {appointment.professionalName}
                          </Typography>
                          <Typography component="div" variant="body2">
                            Data: {new Date(appointment.date).toLocaleDateString()} {appointment.startTime} - {appointment.endTime}
                          </Typography>
                          <Typography component="div" variant="body2" sx={{ fontWeight: 'bold', color: 'primary.main' }}>
                            Valor: {formatCurrency(appointment.price)}
                          </Typography>
                        </>
                      }
                    />
                    <ListItemSecondaryAction>
                      <Tooltip title="Vincular este agendamento">
                        <IconButton 
                          edge="end" 
                          color="primary"
                          onClick={() => linkMutation.mutate(appointment.id)}
                          disabled={linkMutation.isPending}
                        >
                          <LinkIcon />
                        </IconButton>
                      </Tooltip>
                      <Tooltip title="Criar transação para este agendamento">
                        <IconButton 
                          edge="end" 
                          color="secondary" 
                          sx={{ ml: 1 }}
                          onClick={() => handleOpenCreateTransactionDialog(appointment.id)}
                        >
                          <AddIcon />
                        </IconButton>
                      </Tooltip>
                    </ListItemSecondaryAction>
                  </ListItem>
                </Paper>
              ))}
            </List>
          ) : (
            <Box sx={{ textAlign: 'center', py: 4 }}>
              <EventBusyIcon sx={{ fontSize: 60, color: 'text.secondary', mb: 2 }} />
              <Typography variant="h6" color="text.secondary">
                Nenhum agendamento pendente encontrado
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Todos os agendamentos já estão pagos ou não há agendamentos para este cliente.
              </Typography>
            </Box>
          )}
        </DialogContent>

        <DialogActions>
          <Button onClick={() => setOpenDialog(false)}>Cancelar</Button>
          <Button
            variant="contained"
            color="primary"
            onClick={() => selectedAppointment && linkMutation.mutate(selectedAppointment)}
            disabled={!selectedAppointment || linkMutation.isPending}
            startIcon={linkMutation.isPending ? <CircularProgress size={20} /> : <CheckIcon />}
          >
            {linkMutation.isPending ? 'Processando...' : 'Vincular Selecionado'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Diálogo para criar transação a partir de agendamento */}
      <Dialog
        open={createTransactionDialogOpen}
        onClose={() => setCreateTransactionDialogOpen(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>
          Criar Transação a partir do Agendamento
        </DialogTitle>
        <DialogContent dividers>
          <Typography variant="body2" paragraph>
            Preencha os detalhes para criar uma nova transação financeira vinculada ao agendamento selecionado.
          </Typography>
          <TextField
            label="Descrição da Transação"
            fullWidth
            value={transactionDescription}
            onChange={(e) => setTransactionDescription(e.target.value)}
            margin="normal"
            variant="outlined"
            multiline
            rows={2}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCreateTransactionDialogOpen(false)}>Cancelar</Button>
          <Button
            variant="contained"
            color="primary"
            onClick={handleCreateTransaction}
            disabled={createTransactionMutation.isPending}
            startIcon={createTransactionMutation.isPending ? <CircularProgress size={20} /> : <AddIcon />}
          >
            {createTransactionMutation.isPending ? 'Processando...' : 'Criar Transação'}
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
};

export default AppointmentLinker;
