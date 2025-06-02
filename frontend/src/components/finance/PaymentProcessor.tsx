import React, { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useSnackbar } from 'notistack';
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
  FormControl,
  Grid,
  InputLabel,
  MenuItem,
  Select,
  TextField,
  Typography,
  Divider,
  IconButton,
  Tooltip,
} from '@mui/material';
import {
  Payment as PaymentIcon,
  Link as LinkIcon,
  Refresh as RefreshIcon,
  Cancel as CancelIcon,
  Send as SendIcon,
  FileCopy as FileCopyIcon,
} from '@mui/icons-material';

import { Transaction } from '../../types/transaction';
import paymentGatewayService from '../../services/paymentGatewayService';

interface PaymentProcessorProps {
  transaction: Transaction;
  onStatusChange: (updatedTransaction: Transaction) => void;
}

const PAYMENT_METHODS = [
  { value: 'CREDIT_CARD', label: 'Cartão de Crédito' },
  { value: 'DEBIT_CARD', label: 'Cartão de Débito' },
  { value: 'CASH', label: 'Dinheiro' },
  { value: 'PIX', label: 'PIX' },
  { value: 'BANK_SLIP', label: 'Boleto Bancário' },
];

const PaymentProcessor: React.FC<PaymentProcessorProps> = ({ transaction, onStatusChange }) => {
  const [paymentMethod, setPaymentMethod] = useState('');
  const [refundReason, setRefundReason] = useState('');
  const [paymentUrl, setPaymentUrl] = useState('');
  const [openRefundDialog, setOpenRefundDialog] = useState(false);
  const [openLinkDialog, setOpenLinkDialog] = useState(false);
  const [loadingStatus, setLoadingStatus] = useState(false);
  const [paymentStatus, setPaymentStatus] = useState<any>(null);
  
  const { enqueueSnackbar } = useSnackbar();
  const queryClient = useQueryClient();

  // Mutação para processar pagamento
  const processPaymentMutation = useMutation({
    mutationFn: () => paymentGatewayService.processPayment(transaction.id, paymentMethod),
    onSuccess: (data) => {
      enqueueSnackbar('Pagamento processado com sucesso!', { variant: 'success' });
      queryClient.invalidateQueries({ queryKey: ['transaction', transaction.id] });
      queryClient.invalidateQueries({ queryKey: ['transactions'] });
      onStatusChange(data);
    },
    onError: (error: any) => {
      enqueueSnackbar(`Erro ao processar pagamento: ${error.response?.data?.message || error.message}`, { variant: 'error' });
    },
  });

  // Mutação para reembolso
  const refundPaymentMutation = useMutation({
    mutationFn: () => paymentGatewayService.refundPayment(transaction.id, refundReason),
    onSuccess: (data) => {
      enqueueSnackbar('Reembolso solicitado com sucesso!', { variant: 'success' });
      setOpenRefundDialog(false);
      setRefundReason('');
      queryClient.invalidateQueries({ queryKey: ['transaction', transaction.id] });
      queryClient.invalidateQueries({ queryKey: ['transactions'] });
      onStatusChange(data);
    },
    onError: (error: any) => {
      enqueueSnackbar(`Erro ao solicitar reembolso: ${error.response?.data?.message || error.message}`, { variant: 'error' });
    },
  });

  // Mutação para cancelar pagamento
  const cancelPaymentMutation = useMutation({
    mutationFn: () => paymentGatewayService.cancelPayment(transaction.id),
    onSuccess: (data) => {
      enqueueSnackbar('Pagamento cancelado com sucesso!', { variant: 'success' });
      queryClient.invalidateQueries({ queryKey: ['transaction', transaction.id] });
      queryClient.invalidateQueries({ queryKey: ['transactions'] });
      onStatusChange(data);
    },
    onError: (error: any) => {
      enqueueSnackbar(`Erro ao cancelar pagamento: ${error.response?.data?.message || error.message}`, { variant: 'error' });
    },
  });

  // Mutação para gerar link de pagamento
  const generateLinkMutation = useMutation({
    mutationFn: () => paymentGatewayService.generatePaymentLink(transaction.id),
    onSuccess: (data) => {
      setPaymentUrl(data.paymentUrl);
      setOpenLinkDialog(true);
      enqueueSnackbar('Link de pagamento gerado com sucesso!', { variant: 'success' });
    },
    onError: (error: any) => {
      enqueueSnackbar(`Erro ao gerar link de pagamento: ${error.response?.data?.message || error.message}`, { variant: 'error' });
    },
  });

  // Mutação para enviar lembrete
  const sendReminderMutation = useMutation({
    mutationFn: () => paymentGatewayService.sendPaymentReminder(transaction.id),
    onSuccess: (data) => {
      enqueueSnackbar(data.message || 'Lembrete enviado com sucesso!', { variant: 'success' });
    },
    onError: (error: any) => {
      enqueueSnackbar(`Erro ao enviar lembrete: ${error.response?.data?.message || error.message}`, { variant: 'error' });
    },
  });

  // Verificar status do pagamento
  const handleCheckStatus = async () => {
    setLoadingStatus(true);
    try {
      const status = await paymentGatewayService.checkPaymentStatus(transaction.id);
      setPaymentStatus(status);
      enqueueSnackbar('Status atualizado com sucesso!', { variant: 'success' });
    } catch (error: any) {
      enqueueSnackbar(`Erro ao verificar status: ${error.response?.data?.message || error.message}`, { variant: 'error' });
    } finally {
      setLoadingStatus(false);
    }
  };

  // Copiar link para área de transferência
  const handleCopyLink = () => {
    navigator.clipboard.writeText(paymentUrl);
    enqueueSnackbar('Link copiado para a área de transferência!', { variant: 'success' });
  };

  return (
    <Card variant="outlined" sx={{ mb: 3 }}>
      <CardContent>
        <Typography variant="h6" gutterBottom>
          <PaymentIcon sx={{ verticalAlign: 'middle', mr: 1 }} />
          Gerenciamento de Pagamento
        </Typography>
        <Divider sx={{ my: 2 }} />

        {/* Status atual do pagamento */}
        <Box sx={{ mb: 3 }}>
          <Grid container spacing={2} alignItems="center">
            <Grid item xs={12} sm={6}>
              <Typography variant="subtitle1">
                Status atual: 
                <Typography 
                  component="span" 
                  sx={{ 
                    ml: 1,
                    fontWeight: 'bold',
                    color: transaction.status === 'PAID' ? 'success.main' : 
                           transaction.status === 'PENDING' ? 'warning.main' : 
                           transaction.status === 'REFUNDED' ? 'error.main' : 'text.secondary'
                  }}
                >
                  {transaction.status === 'PAID' ? 'Pago' : 
                   transaction.status === 'PENDING' ? 'Pendente' : 
                   transaction.status === 'REFUNDED' ? 'Reembolsado' : 
                   transaction.status === 'CANCELLED' ? 'Cancelado' : transaction.status}
                </Typography>
              </Typography>
              {transaction.paymentMethod && (
                <Typography variant="body2" color="text.secondary">
                  Método de pagamento: {PAYMENT_METHODS.find(m => m.value === transaction.paymentMethod)?.label || transaction.paymentMethod}
                </Typography>
              )}
              {transaction.paymentDate && (
                <Typography variant="body2" color="text.secondary">
                  Data do pagamento: {new Date(transaction.paymentDate).toLocaleString()}
                </Typography>
              )}
            </Grid>
            <Grid item xs={12} sm={6} sx={{ textAlign: { xs: 'left', sm: 'right' } }}>
              <Button 
                startIcon={<RefreshIcon />}
                onClick={handleCheckStatus}
                disabled={loadingStatus}
                variant="outlined"
                size="small"
              >
                {loadingStatus ? <CircularProgress size={20} /> : 'Verificar Status'}
              </Button>
            </Grid>
          </Grid>

          {/* Exibir informações de status, se disponíveis */}
          {paymentStatus && (
            <Box sx={{ mt: 2, p: 2, bgcolor: 'background.paper', borderRadius: 1 }}>
              <Typography variant="subtitle2" gutterBottom>
                Informações do Gateway de Pagamento:
              </Typography>
              <Typography variant="body2">
                Status externo: {paymentStatus.status}
              </Typography>
              {paymentStatus.externalReference && (
                <Typography variant="body2">
                  Referência: {paymentStatus.externalReference}
                </Typography>
              )}
              <Typography variant="body2">
                Última atualização: {new Date(paymentStatus.lastUpdate).toLocaleString()}
              </Typography>
            </Box>
          )}
        </Box>

        <Divider sx={{ my: 2 }} />

        {/* Ações de pagamento */}
        <Grid container spacing={2}>
          {/* Processar pagamento - disponível para pendentes */}
          {transaction.status === 'PENDING' && (
            <Grid item xs={12} md={6}>
              <Box sx={{ bgcolor: 'background.paper', p: 2, borderRadius: 1 }}>
                <Typography variant="subtitle1" gutterBottom>
                  Processar Pagamento
                </Typography>
                <FormControl fullWidth size="small" sx={{ mb: 2 }}>
                  <InputLabel>Método de Pagamento</InputLabel>
                  <Select
                    value={paymentMethod}
                    onChange={(e) => setPaymentMethod(e.target.value)}
                    label="Método de Pagamento"
                  >
                    {PAYMENT_METHODS.map((method) => (
                      <MenuItem key={method.value} value={method.value}>
                        {method.label}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
                <Button
                  variant="contained"
                  color="primary"
                  fullWidth
                  disabled={!paymentMethod || processPaymentMutation.isPending}
                  onClick={() => processPaymentMutation.mutate()}
                >
                  {processPaymentMutation.isPending ? (
                    <CircularProgress size={24} color="inherit" />
                  ) : (
                    'Confirmar Pagamento'
                  )}
                </Button>
              </Box>
            </Grid>
          )}

          {/* Gerar Link - disponível para pendentes */}
          {transaction.status === 'PENDING' && (
            <Grid item xs={12} md={6}>
              <Box sx={{ bgcolor: 'background.paper', p: 2, borderRadius: 1 }}>
                <Typography variant="subtitle1" gutterBottom>
                  Pagamento Online
                </Typography>
                <Typography variant="body2" sx={{ mb: 2 }}>
                  Gere um link de pagamento para enviar ao cliente.
                </Typography>
                <Button
                  variant="outlined"
                  color="primary"
                  fullWidth
                  startIcon={<LinkIcon />}
                  disabled={generateLinkMutation.isPending}
                  onClick={() => generateLinkMutation.mutate()}
                >
                  {generateLinkMutation.isPending ? (
                    <CircularProgress size={24} />
                  ) : (
                    'Gerar Link de Pagamento'
                  )}
                </Button>
              </Box>
            </Grid>
          )}

          {/* Cancelar pagamento - disponível para pendentes */}
          {transaction.status === 'PENDING' && (
            <Grid item xs={12} md={6}>
              <Box sx={{ bgcolor: 'background.paper', p: 2, borderRadius: 1 }}>
                <Typography variant="subtitle1" gutterBottom>
                  Cancelar Transação
                </Typography>
                <Typography variant="body2" sx={{ mb: 2 }}>
                  Cancele a transação pendente.
                </Typography>
                <Button
                  variant="outlined"
                  color="error"
                  fullWidth
                  startIcon={<CancelIcon />}
                  disabled={cancelPaymentMutation.isPending}
                  onClick={() => cancelPaymentMutation.mutate()}
                >
                  {cancelPaymentMutation.isPending ? (
                    <CircularProgress size={24} />
                  ) : (
                    'Cancelar Transação'
                  )}
                </Button>
              </Box>
            </Grid>
          )}

          {/* Enviar lembrete - disponível para pendentes */}
          {transaction.status === 'PENDING' && (
            <Grid item xs={12} md={6}>
              <Box sx={{ bgcolor: 'background.paper', p: 2, borderRadius: 1 }}>
                <Typography variant="subtitle1" gutterBottom>
                  Lembrete de Pagamento
                </Typography>
                <Typography variant="body2" sx={{ mb: 2 }}>
                  Envie um lembrete de pagamento para o cliente.
                </Typography>
                <Button
                  variant="outlined"
                  color="info"
                  fullWidth
                  startIcon={<SendIcon />}
                  disabled={sendReminderMutation.isPending}
                  onClick={() => sendReminderMutation.mutate()}
                >
                  {sendReminderMutation.isPending ? (
                    <CircularProgress size={24} />
                  ) : (
                    'Enviar Lembrete'
                  )}
                </Button>
              </Box>
            </Grid>
          )}

          {/* Reembolso - disponível para pagos */}
          {transaction.status === 'PAID' && (
            <Grid item xs={12} md={6}>
              <Box sx={{ bgcolor: 'background.paper', p: 2, borderRadius: 1 }}>
                <Typography variant="subtitle1" gutterBottom>
                  Reembolso
                </Typography>
                <Typography variant="body2" sx={{ mb: 2 }}>
                  Solicite o reembolso desta transação.
                </Typography>
                <Button
                  variant="outlined"
                  color="warning"
                  fullWidth
                  onClick={() => setOpenRefundDialog(true)}
                >
                  Solicitar Reembolso
                </Button>
              </Box>
            </Grid>
          )}
        </Grid>

        {/* Diálogo de reembolso */}
        <Dialog open={openRefundDialog} onClose={() => setOpenRefundDialog(false)}>
          <DialogTitle>Solicitar Reembolso</DialogTitle>
          <DialogContent>
            <Typography variant="body2" paragraph>
              Você está solicitando o reembolso da transação no valor de 
              {new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(transaction.amount)}.
            </Typography>
            <TextField
              autoFocus
              margin="dense"
              label="Motivo do Reembolso"
              fullWidth
              multiline
              rows={3}
              value={refundReason}
              onChange={(e) => setRefundReason(e.target.value)}
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setOpenRefundDialog(false)}>Cancelar</Button>
            <Button 
              onClick={() => refundPaymentMutation.mutate()} 
              color="warning"
              disabled={!refundReason || refundPaymentMutation.isPending}
            >
              {refundPaymentMutation.isPending ? <CircularProgress size={24} /> : 'Confirmar Reembolso'}
            </Button>
          </DialogActions>
        </Dialog>

        {/* Diálogo de link de pagamento */}
        <Dialog open={openLinkDialog} onClose={() => setOpenLinkDialog(false)}>
          <DialogTitle>Link de Pagamento</DialogTitle>
          <DialogContent>
            <Typography variant="body2" paragraph>
              Compartilhe este link com o cliente para que ele possa realizar o pagamento online:
            </Typography>
            <Box sx={{ 
              display: 'flex',
              alignItems: 'center', 
              bgcolor: 'background.paper', 
              p: 2, 
              borderRadius: 1,
              border: '1px solid',
              borderColor: 'divider'
            }}>
              <Typography 
                variant="body2" 
                sx={{ 
                  flexGrow: 1,
                  overflow: 'hidden',
                  textOverflow: 'ellipsis',
                  whiteSpace: 'nowrap'
                }}
              >
                {paymentUrl}
              </Typography>
              <Tooltip title="Copiar link">
                <IconButton onClick={handleCopyLink} size="small">
                  <FileCopyIcon fontSize="small" />
                </IconButton>
              </Tooltip>
            </Box>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setOpenLinkDialog(false)}>Fechar</Button>
            <Button
              onClick={() => {
                window.open(paymentUrl, '_blank');
              }}
              color="primary"
              variant="contained"
            >
              Abrir Link
            </Button>
          </DialogActions>
        </Dialog>
      </CardContent>
    </Card>
  );
};

export default PaymentProcessor;
