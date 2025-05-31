import React, { useEffect, useState } from 'react';
import { useNavigate, useParams, useLocation } from 'react-router-dom';
import {
  Box,
  Button,
  Paper,
  Typography,
  TextField,
  Grid,
  FormControlLabel,
  Checkbox,
  CircularProgress,
  Alert,
  Card,
  CardContent,
  CardHeader,
  Divider,
  InputAdornment,
  IconButton,
} from '@mui/material';
import { Save as SaveIcon, ArrowBack as ArrowBackIcon } from '@mui/icons-material';
import { useForm, Controller, SubmitHandler } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { format, parseISO } from 'date-fns';
import { ptBR } from 'date-fns/locale';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { 
  getClientById, 
  createClient, 
  updateClient,
  Client,
  CreateClientDto,
  UpdateClientDto 
} from '../../../services/clientService';

// Schema de validação
const clientSchema = yup.object().shape({
  name: yup.string().required('Nome é obrigatório'),
  email: yup.string().email('E-mail inválido').required('E-mail é obrigatório'),
  phone: yup.string().required('Telefone é obrigatório'),
  cpf: yup.string().required('CPF é obrigatório'),
  birthDate: yup.string().nullable(),
  address: yup.string().nullable(),
  notes: yup.string().nullable(),
  active: yup.boolean().default(true),
});

type FormData = yup.InferType<typeof clientSchema>;

const ClientForm: React.FC = () => {
  const { id } = useParams<{ id?: string }>();
  const navigate = useNavigate();
  const location = useLocation();
  const queryClient = useQueryClient();
  const isEditMode = !!id;
  const [serverError, setServerError] = useState<string | null>(null);

  // Buscar dados do cliente se estiver no modo de edição
  const { data: clientData, isLoading: isLoadingClient } = useQuery<Client>(
    ['client', id],
    () => getClientById(Number(id)),
    { enabled: isEditMode, staleTime: 0 }
  );

  // Configuração do formulário
  const {
    control,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<FormData>({
    resolver: yupResolver(clientSchema),
    defaultValues: {
      name: '',
      email: '',
      phone: '',
      cpf: '',
      birthDate: '',
      address: '',
      notes: '',
      active: true,
    },
  });

  // Atualizar valores iniciais quando os dados do cliente forem carregados
  useEffect(() => {
    if (isEditMode && clientData) {
      const formattedData = {
        ...clientData,
        birthDate: clientData.birthDate 
          ? format(parseISO(clientData.birthDate), 'yyyy-MM-dd')
          : '',
      };
      reset(formattedData);
    }
  }, [clientData, isEditMode, reset]);

  // Mutations para criar/atualizar cliente
  const createClientMutation = useMutation(createClient, {
    onSuccess: () => {
      queryClient.invalidateQueries(['clients']);
      navigate('/clients', { state: { message: 'Cliente criado com sucesso!' } });
    },
    onError: (error: any) => {
      setServerError(error.response?.data?.message || 'Erro ao criar cliente');
    },
  });

  const updateClientMutation = useMutation(
    (data: UpdateClientDto) => updateClient(Number(id), data),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['client', id]);
        queryClient.invalidateQueries(['clients']);
        navigate('/clients', { state: { message: 'Cliente atualizado com sucesso!' } });
      },
      onError: (error: any) => {
        setServerError(error.response?.data?.message || 'Erro ao atualizar cliente');
      },
    }
  );

  const onSubmit: SubmitHandler<FormData> = (data) => {
    setServerError(null);
    
    if (isEditMode) {
      updateClientMutation.mutate(data);
    } else {
      createClientMutation.mutate(data);
    }
  };

  const isLoading = isLoadingClient || createClientMutation.isLoading || updateClientMutation.isLoading;

  // Exibir mensagem de sucesso se redirecionado de outra página
  const successMessage = location.state?.message;
  useEffect(() => {
    if (successMessage) {
      const timer = setTimeout(() => {
        navigate(location.pathname, { replace: true, state: {} });
      }, 5000);
      return () => clearTimeout(timer);
    }
  }, [successMessage, navigate, location]);

  return (
    <Box>
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
        <IconButton onClick={() => navigate(-1)} sx={{ mr: 1 }}>
          <ArrowBackIcon />
        </IconButton>
        <Typography variant="h5" component="h1">
          {isEditMode ? 'Editar Cliente' : 'Novo Cliente'}
        </Typography>
      </Box>

      {successMessage && (
        <Alert severity="success" sx={{ mb: 3 }}>
          {successMessage}
        </Alert>
      )}

      {serverError && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {serverError}
        </Alert>
      )}

      <Card>
        <CardHeader title="Dados do Cliente" />
        <Divider />
        <CardContent>
          <form onSubmit={handleSubmit(onSubmit)}>
            <Grid container spacing={3}>
              <Grid item xs={12} md={6}>
                <Controller
                  name="name"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Nome Completo"
                      fullWidth
                      margin="normal"
                      error={!!errors.name}
                      helperText={errors.name?.message}
                      disabled={isLoading}
                    />
                  )}
                />
              </Grid>
              
              <Grid item xs={12} md={6}>
                <Controller
                  name="email"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      type="email"
                      label="E-mail"
                      fullWidth
                      margin="normal"
                      error={!!errors.email}
                      helperText={errors.email?.message}
                      disabled={isLoading}
                    />
                  )}
                />
              </Grid>
              
              <Grid item xs={12} md={6}>
                <Controller
                  name="cpf"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="CPF"
                      fullWidth
                      margin="normal"
                      error={!!errors.cpf}
                      helperText={errors.cpf?.message}
                      disabled={isLoading || isEditMode}
                      InputProps={{
                        inputProps: {
                          inputMode: 'numeric',
                          pattern: '[0-9]*',
                        },
                      }}
                    />
                  )}
                />
              </Grid>
              
              <Grid item xs={12} md={6}>
                <Controller
                  name="phone"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Telefone"
                      fullWidth
                      margin="normal"
                      error={!!errors.phone}
                      helperText={errors.phone?.message}
                      disabled={isLoading}
                      InputProps={{
                        startAdornment: <InputAdornment position="start">+55</InputAdornment>,
                      }}
                    />
                  )}
                />
              </Grid>
              
              <Grid item xs={12} md={6}>
                <Controller
                  name="birthDate"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      type="date"
                      label="Data de Nascimento"
                      fullWidth
                      margin="normal"
                      InputLabelProps={{
                        shrink: true,
                      }}
                      disabled={isLoading}
                    />
                  )}
                />
              </Grid>
              
              <Grid item xs={12}>
                <Controller
                  name="address"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Endereço"
                      fullWidth
                      margin="normal"
                      disabled={isLoading}
                    />
                  )}
                />
              </Grid>
              
              <Grid item xs={12}>
                <Controller
                  name="notes"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Observações"
                      fullWidth
                      multiline
                      rows={4}
                      margin="normal"
                      disabled={isLoading}
                    />
                  )}
                />
              </Grid>
              
              {isEditMode && (
                <Grid item xs={12}>
                  <Controller
                    name="active"
                    control={control}
                    render={({ field }) => (
                      <FormControlLabel
                        control={
                          <Checkbox
                            checked={field.value}
                            onChange={(e) => field.onChange(e.target.checked)}
                            disabled={isLoading}
                          />
                        }
                        label="Cliente ativo"
                      />
                    )}
                  />
                </Grid>
              )}
              
              <Grid item xs={12} sx={{ mt: 2 }}>
                <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 2 }}>
                  <Button
                    type="button"
                    variant="outlined"
                    onClick={() => navigate(-1)}
                    disabled={isLoading}
                  >
                    Cancelar
                  </Button>
                  <Button
                    type="submit"
                    variant="contained"
                    startIcon={
                      isLoading ? <CircularProgress size={20} color="inherit" /> : <SaveIcon />
                    }
                    disabled={isLoading}
                  >
                    {isLoading ? 'Salvando...' : 'Salvar'}
                  </Button>
                </Box>
              </Grid>
            </Grid>
          </form>
        </CardContent>
      </Card>
    </Box>
  );
};

export default ClientForm;
