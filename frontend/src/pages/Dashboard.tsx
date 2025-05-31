import React from 'react';
import { Box, Typography, Grid, Paper, Button } from '@mui/material';
import { Add as AddIcon } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';

const Dashboard: React.FC = () => {
  const navigate = useNavigate();

  // Dados de exemplo para os cards
  const stats = [
    { title: 'Total de Clientes', value: '1.234', change: '+12%', period: 'este mês' },
    { title: 'Agendamentos', value: '89', change: '+5%', period: 'esta semana' },
    { title: 'Faturamento', value: 'R$ 12.345', change: '+8%', period: 'este mês' },
    { title: 'Avaliações', value: '4.8', change: '+0.2', period: 'desde o último mês' },
  ];

  return (
    <Box sx={{ flexGrow: 1 }}>
      <Box sx={{ mb: 4, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Typography variant="h4" component="h1">
          Painel de Controle
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => navigate('/clients/new')}
        >
          Novo Cliente
        </Button>
      </Box>

      {/* Cards de Estatísticas */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        {stats.map((stat, index) => (
          <Grid item xs={12} sm={6} md={3} key={index}>
            <Paper
              sx={{
                p: 3,
                display: 'flex',
                flexDirection: 'column',
                height: '100%',
                borderRadius: 2,
              }}
            >
              <Typography color="text.secondary" variant="subtitle2">
                {stat.title}
              </Typography>
              <Typography variant="h4" sx={{ my: 1 }}>
                {stat.value}
              </Typography>
              <Typography color="success.main" variant="body2">
                {stat.change} {stat.period}
              </Typography>
            </Paper>
          </Grid>
        ))}
      </Grid>

      {/* Seção de Atividades Recentes */}
      <Grid container spacing={3}>
        <Grid item xs={12} md={8}>
          <Paper sx={{ p: 3, borderRadius: 2, height: '100%' }}>
            <Typography variant="h6" gutterBottom>
              Atividades Recentes
            </Typography>
            <Typography color="text.secondary" variant="body2">
              Nenhuma atividade recente.
            </Typography>
          </Paper>
        </Grid>
        
        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 3, borderRadius: 2, height: '100%' }}>
            <Typography variant="h6" gutterBottom>
              Próximos Agendamentos
            </Typography>
            <Typography color="text.secondary" variant="body2">
              Nenhum agendamento próximo.
            </Typography>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Dashboard;
