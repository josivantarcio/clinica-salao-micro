import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Grid,
  IconButton,
  Paper,
  Switch,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  CardGiftcard as CardGiftcardIcon,
} from '@mui/icons-material';

import Page from '../../components/layout/Page';
import Loading from '../../components/common/Loading';
import ErrorAlert from '../../components/common/ErrorAlert';
import {
  LoyaltyProgram,
  getLoyaltyPrograms,
  activateLoyaltyProgram,
  deactivateLoyaltyProgram
} from '../../services/loyaltyService';

const LoyaltyPrograms: React.FC = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  // Buscar programas de fidelidade
  const { data: programs = [], isLoading, isError, error } = useQuery({
    queryKey: ['loyaltyPrograms'],
    queryFn: getLoyaltyPrograms
  });

  // Mutations para ativar/desativar programas
  const activateMutation = useMutation({
    mutationFn: activateLoyaltyProgram,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['loyaltyPrograms'] });
    }
  });

  const deactivateMutation = useMutation({
    mutationFn: deactivateLoyaltyProgram,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['loyaltyPrograms'] });
    }
  });

  // Função para alternar status ativo/inativo do programa
  const handleToggleActive = (program: LoyaltyProgram) => {
    if (program.active) {
      deactivateMutation.mutate(program.id);
    } else {
      activateMutation.mutate(program.id);
    }
  };

  if (isLoading) return <Loading />;
  if (isError) return <ErrorAlert error={error as Error} />;

  return (
    <Page title="Programas de Fidelidade">
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4">Programas de Fidelidade</Typography>
        <Button
          variant="contained"
          color="primary"
          startIcon={<AddIcon />}
          onClick={() => navigate('/loyalty/programs/new')}
        >
          Novo Programa
        </Button>
      </Box>

      <Grid container spacing={3}>
        {programs.length > 0 ? (
          programs.map((program) => (
            <Grid item xs={12} md={6} lg={4} key={program.id}>
              <Card>
                <CardContent>
                  <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                    <Typography variant="h6">{program.name}</Typography>
                    <Switch
                      checked={program.active}
                      onChange={() => handleToggleActive(program)}
                      color="primary"
                    />
                  </Box>
                  <Typography variant="body2" color="text.secondary" mb={2}>
                    {program.description}
                  </Typography>
                  <Box mb={2}>
                    <Typography variant="body2">
                      <strong>Pontos por compra:</strong> {program.pointsPerPurchase} a cada R$ {program.minPurchaseValue.toFixed(2)}
                    </Typography>
                  </Box>
                  <Box mb={2}>
                    <Typography variant="body2">
                      <strong>Total de recompensas:</strong> {program.rewards.length}
                    </Typography>
                    <Box mt={1} display="flex" gap={1} flexWrap="wrap">
                      {program.rewards.slice(0, 3).map((reward) => (
                        <Chip
                          key={reward.id}
                          size="small"
                          icon={<CardGiftcardIcon />}
                          label={reward.name}
                          color={reward.active ? "primary" : "default"}
                          variant="outlined"
                        />
                      ))}
                      {program.rewards.length > 3 && (
                        <Chip
                          size="small"
                          label={`+${program.rewards.length - 3} mais`}
                          color="default"
                        />
                      )}
                    </Box>
                  </Box>
                  <Box display="flex" justifyContent="space-between">
                    <Chip
                      label={program.active ? 'Ativo' : 'Inativo'}
                      color={program.active ? 'success' : 'default'}
                      size="small"
                    />
                    <Box>
                      <Button
                        variant="outlined"
                        size="small"
                        startIcon={<CardGiftcardIcon />}
                        onClick={() => navigate(`/loyalty/programs/${program.id}/rewards`)}
                        sx={{ mr: 1 }}
                      >
                        Recompensas
                      </Button>
                      <IconButton
                        color="primary"
                        size="small"
                        onClick={() => navigate(`/loyalty/programs/${program.id}/edit`)}
                      >
                        <EditIcon />
                      </IconButton>
                    </Box>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          ))
        ) : (
          <Grid item xs={12}>
            <Paper sx={{ p: 3, textAlign: 'center' }}>
              <Typography variant="h6" mb={2}>
                Nenhum programa de fidelidade cadastrado
              </Typography>
              <Typography variant="body2" color="text.secondary" mb={3}>
                Crie um programa de fidelidade para recompensar seus clientes fiéis
              </Typography>
              <Button
                variant="contained"
                startIcon={<AddIcon />}
                onClick={() => navigate('/loyalty/programs/new')}
              >
                Cadastrar Programa
              </Button>
            </Paper>
          </Grid>
        )}
      </Grid>

      {programs.length > 0 && (
        <Box mt={4}>
          <Typography variant="h5" mb={3}>Visão Geral de Recompensas</Typography>
          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Programa</TableCell>
                  <TableCell>Recompensa</TableCell>
                  <TableCell>Pontos Necessários</TableCell>
                  <TableCell>Desconto</TableCell>
                  <TableCell>Status</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {programs.flatMap(program =>
                  program.rewards.map(reward => (
                    <TableRow key={`${program.id}-${reward.id}`}>
                      <TableCell>{program.name}</TableCell>
                      <TableCell>{reward.name}</TableCell>
                      <TableCell>{reward.pointsRequired}</TableCell>
                      <TableCell>{reward.discount}%</TableCell>
                      <TableCell>
                        <Chip
                          label={reward.active ? 'Ativo' : 'Inativo'}
                          color={reward.active ? 'success' : 'default'}
                          size="small"
                        />
                      </TableCell>
                    </TableRow>
                  ))
                )}
                {programs.every(program => program.rewards.length === 0) && (
                  <TableRow>
                    <TableCell colSpan={5} align="center">
                      Nenhuma recompensa cadastrada
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </Box>
      )}
    </Page>
  );
};

export default LoyaltyPrograms;
