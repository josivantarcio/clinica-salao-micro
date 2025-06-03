import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useForm, Controller, useFieldArray } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  FormControl,
  FormControlLabel,
  FormHelperText,
  Grid,
  IconButton,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  Switch,
  TextField,
  Typography
} from '@mui/material';
import {
  Add as AddIcon,
  Delete as DeleteIcon,
  Save as SaveIcon,
  ArrowBack as ArrowBackIcon
} from '@mui/icons-material';

import Page from '../../components/layout/Page';
import Loading from '../../components/common/Loading';
import ErrorAlert from '../../components/common/ErrorAlert';
import {
  Professional,
  ProfessionalRequest,
  createProfessional,
  getProfessionalById,
  updateProfessional
} from '../../services/professionalService';

// Schema de validação com Yup
const schema = yup.object({
  name: yup.string().required('Nome é obrigatório'),
  email: yup.string().email('Email inválido').required('Email é obrigatório'),
  phone: yup.string().required('Telefone é obrigatório'),
  registrationNumber: yup.string().required('Número de registro é obrigatório'),
  specialties: yup.array().of(yup.string()).min(1, 'Pelo menos uma especialidade é obrigatória'),
  workingHours: yup.array().of(
    yup.object({
      dayOfWeek: yup.number().required('Dia da semana é obrigatório'),
      startTime: yup.string().required('Horário inicial é obrigatório'),
      endTime: yup.string().required('Horário final é obrigatório')
    })
  )
});

type FormData = Omit<ProfessionalRequest, 'workingHours'> & {
  active: boolean;
  workingHours: Array<{
    dayOfWeek: number;
    startTime: string;
    endTime: string;
    breakStart?: string;
    breakEnd?: string;
  }>;
};

const ProfessionalForm: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEditMode = !!id;
  const queryClient = useQueryClient();
  const [specialtyInput, setSpecialtyInput] = useState<string>('');

  // Form setup com react-hook-form e yup para validação
  const {
    control,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
    setValue,
    watch
  } = useForm<FormData>({
    resolver: yupResolver(schema),
    defaultValues: {
      name: '',
      email: '',
      phone: '',
      specialties: [],
      registrationNumber: '',
      active: true,
      workingHours: [
        {
          dayOfWeek: 1,
          startTime: '08:00',
          endTime: '17:00'
        }
      ]
    }
  });

  // Field array para horários de trabalho
  const { fields, append, remove } = useFieldArray({
    control,
    name: 'workingHours'
  });

  // Carregar dados do profissional se estiver em modo de edição
  const { data: professional, isLoading, isError, error } = useQuery({
    queryKey: ['professional', id],
    queryFn: () => getProfessionalById(Number(id)),
    enabled: isEditMode,
  });

  // Preencher formulário com dados do profissional quando carregado
  useEffect(() => {
    if (professional) {
      reset({
        ...professional,
        active: professional.active
      });
    }
  }, [professional, reset]);

  // Mutações para criar ou atualizar profissional
  const createMutation = useMutation({
    mutationFn: createProfessional,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['professionals'] });
      navigate('/professionals');
    }
  });

  const updateMutation = useMutation({
    mutationFn: (data: ProfessionalRequest) => updateProfessional(Number(id), data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['professionals'] });
      queryClient.invalidateQueries({ queryKey: ['professional', id] });
      navigate('/professionals');
    }
  });

  // Função para adicionar uma nova especialidade
  const handleAddSpecialty = () => {
    if (specialtyInput.trim()) {
      const currentSpecialties = watch('specialties') || [];
      if (!currentSpecialties.includes(specialtyInput.trim())) {
        setValue('specialties', [...currentSpecialties, specialtyInput.trim()]);
      }
      setSpecialtyInput('');
    }
  };

  // Função para remover uma especialidade
  const handleRemoveSpecialty = (index: number) => {
    const currentSpecialties = watch('specialties') || [];
    setValue(
      'specialties',
      currentSpecialties.filter((_, i) => i !== index)
    );
  };

  // Função para adicionar um horário de trabalho
  const handleAddWorkingHours = () => {
    append({
      dayOfWeek: 1,
      startTime: '08:00',
      endTime: '17:00'
    });
  };

  // Função para enviar o formulário
  const onSubmit = async (data: FormData) => {
    const professionalData: ProfessionalRequest = {
      name: data.name,
      email: data.email,
      phone: data.phone,
      specialties: data.specialties,
      registrationNumber: data.registrationNumber,
      imageUrl: data.imageUrl,
      workingHours: data.workingHours,
    };

    if (isEditMode) {
      updateMutation.mutate(professionalData);
    } else {
      createMutation.mutate(professionalData);
    }
  };

  // Map de dias da semana
  const weekDays = [
    { value: 1, label: 'Segunda-feira' },
    { value: 2, label: 'Terça-feira' },
    { value: 3, label: 'Quarta-feira' },
    { value: 4, label: 'Quinta-feira' },
    { value: 5, label: 'Sexta-feira' },
    { value: 6, label: 'Sábado' },
    { value: 7, label: 'Domingo' }
  ];

  if (isEditMode && isLoading) return <Loading />;
  if (isError) return <ErrorAlert error={error as Error} />;

  return (
    <Page title={isEditMode ? "Editar Profissional" : "Novo Profissional"}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Button
          startIcon={<ArrowBackIcon />}
          onClick={() => navigate('/professionals')}
        >
          Voltar
        </Button>
        <Typography variant="h4">
          {isEditMode ? "Editar Profissional" : "Cadastrar Novo Profissional"}
        </Typography>
        <Box /> {/* Espaçador */}
      </Box>

      <Paper sx={{ p: 3 }}>
        <form onSubmit={handleSubmit(onSubmit)}>
          <Grid container spacing={3}>
            <Grid item xs={12} md={6}>
              <Controller
                name="name"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    fullWidth
                    label="Nome"
                    error={!!errors.name}
                    helperText={errors.name?.message}
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
                    fullWidth
                    label="Email"
                    error={!!errors.email}
                    helperText={errors.email?.message}
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
                    fullWidth
                    label="Telefone"
                    error={!!errors.phone}
                    helperText={errors.phone?.message}
                  />
                )}
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <Controller
                name="registrationNumber"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    fullWidth
                    label="Número de Registro"
                    error={!!errors.registrationNumber}
                    helperText={errors.registrationNumber?.message}
                  />
                )}
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <Controller
                name="imageUrl"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    fullWidth
                    label="URL da Imagem"
                    error={!!errors.imageUrl}
                    helperText={errors.imageUrl?.message}
                  />
                )}
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <Controller
                name="active"
                control={control}
                render={({ field }) => (
                  <FormControlLabel
                    control={<Switch checked={field.value} {...field} />}
                    label="Ativo"
                  />
                )}
              />
            </Grid>

            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom>
                Especialidades
              </Typography>
              <Box display="flex" alignItems="center" mb={2}>
                <TextField
                  fullWidth
                  label="Adicionar especialidade"
                  value={specialtyInput}
                  onChange={(e) => setSpecialtyInput(e.target.value)}
                  onKeyPress={(e) => {
                    if (e.key === 'Enter') {
                      e.preventDefault();
                      handleAddSpecialty();
                    }
                  }}
                />
                <IconButton color="primary" onClick={handleAddSpecialty}>
                  <AddIcon />
                </IconButton>
              </Box>
              <Box display="flex" flexWrap="wrap" gap={1}>
                {watch('specialties')?.map((specialty, index) => (
                  <Chip
                    key={index}
                    label={specialty}
                    onDelete={() => handleRemoveSpecialty(index)}
                    color="primary"
                  />
                ))}
              </Box>
              {errors.specialties && (
                <FormHelperText error>{errors.specialties.message}</FormHelperText>
              )}
            </Grid>

            <Grid item xs={12}>
              <Box display="flex" justifyContent="space-between" alignItems="center">
                <Typography variant="h6" gutterBottom>
                  Horários de Trabalho
                </Typography>
                <Button
                  startIcon={<AddIcon />}
                  onClick={handleAddWorkingHours}
                  variant="outlined"
                  size="small"
                >
                  Adicionar Horário
                </Button>
              </Box>

              {fields.map((field, index) => (
                <Card key={field.id} sx={{ mb: 2, p: 2 }}>
                  <CardContent>
                    <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                      <Typography variant="subtitle1">Horário #{index + 1}</Typography>
                      {index > 0 && (
                        <IconButton color="error" onClick={() => remove(index)}>
                          <DeleteIcon />
                        </IconButton>
                      )}
                    </Box>
                    <Grid container spacing={2}>
                      <Grid item xs={12} md={4}>
                        <FormControl fullWidth error={!!errors.workingHours?.[index]?.dayOfWeek}>
                          <InputLabel>Dia da Semana</InputLabel>
                          <Controller
                            name={`workingHours.${index}.dayOfWeek`}
                            control={control}
                            render={({ field }) => (
                              <Select {...field} label="Dia da Semana">
                                {weekDays.map((day) => (
                                  <MenuItem key={day.value} value={day.value}>
                                    {day.label}
                                  </MenuItem>
                                ))}
                              </Select>
                            )}
                          />
                          {errors.workingHours?.[index]?.dayOfWeek && (
                            <FormHelperText>
                              {errors.workingHours[index]?.dayOfWeek?.message}
                            </FormHelperText>
                          )}
                        </FormControl>
                      </Grid>

                      <Grid item xs={12} md={4}>
                        <Controller
                          name={`workingHours.${index}.startTime`}
                          control={control}
                          render={({ field }) => (
                            <TextField
                              {...field}
                              fullWidth
                              label="Hora Início"
                              type="time"
                              InputLabelProps={{ shrink: true }}
                              error={!!errors.workingHours?.[index]?.startTime}
                              helperText={errors.workingHours?.[index]?.startTime?.message}
                            />
                          )}
                        />
                      </Grid>

                      <Grid item xs={12} md={4}>
                        <Controller
                          name={`workingHours.${index}.endTime`}
                          control={control}
                          render={({ field }) => (
                            <TextField
                              {...field}
                              fullWidth
                              label="Hora Fim"
                              type="time"
                              InputLabelProps={{ shrink: true }}
                              error={!!errors.workingHours?.[index]?.endTime}
                              helperText={errors.workingHours?.[index]?.endTime?.message}
                            />
                          )}
                        />
                      </Grid>

                      <Grid item xs={12} md={6}>
                        <Controller
                          name={`workingHours.${index}.breakStart`}
                          control={control}
                          render={({ field }) => (
                            <TextField
                              {...field}
                              fullWidth
                              label="Início do Intervalo (opcional)"
                              type="time"
                              InputLabelProps={{ shrink: true }}
                            />
                          )}
                        />
                      </Grid>

                      <Grid item xs={12} md={6}>
                        <Controller
                          name={`workingHours.${index}.breakEnd`}
                          control={control}
                          render={({ field }) => (
                            <TextField
                              {...field}
                              fullWidth
                              label="Fim do Intervalo (opcional)"
                              type="time"
                              InputLabelProps={{ shrink: true }}
                            />
                          )}
                        />
                      </Grid>
                    </Grid>
                  </CardContent>
                </Card>
              ))}
            </Grid>

            <Grid item xs={12}>
              <Box display="flex" justifyContent="flex-end" gap={2}>
                <Button
                  variant="outlined"
                  onClick={() => navigate('/professionals')}
                >
                  Cancelar
                </Button>
                <Button
                  type="submit"
                  variant="contained"
                  color="primary"
                  startIcon={<SaveIcon />}
                  disabled={isSubmitting}
                >
                  {isEditMode ? "Atualizar" : "Salvar"}
                </Button>
              </Box>
            </Grid>
          </Grid>
        </form>
      </Paper>
    </Page>
  );
};

export default ProfessionalForm;
