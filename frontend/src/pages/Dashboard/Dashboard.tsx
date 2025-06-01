import React, { useState, useEffect } from 'react';
import { 
  Box, Grid, Card, CardContent, Typography, Paper, Divider,
  List, ListItem, ListItemText, CircularProgress, useTheme,
  ListItemAvatar, Avatar, Chip
} from '@mui/material';
import {
  Today as TodayIcon,
  People as PeopleIcon,
  AttachMoney as MoneyIcon,
  TrendingUp as TrendingUpIcon,
  EventAvailable as EventAvailableIcon
} from '@mui/icons-material';
import { useSnackbar } from 'notistack';
import axios from 'axios';

// Interface para tipagem dos dados do dashboard
interface DashboardData {
  appointmentsToday: number;
  appointmentsPending: number;
  totalClients: number;
  activeClients: number;
  revenue: {
    today: number;
    week: number;
    month: number;
  };
  upcomingAppointments: {
    id: string;
    clientName: string;
    serviceName: string;
    time: string;
    professionalName: string;
  }[];
}

// Dados simulados para desenvolvimento
const mockDashboardData: DashboardData = {
  appointmentsToday: 12,
  appointmentsPending: 3,
  totalClients: 245,
  activeClients: 178,
  revenue: {
    today: 1250.00,
    week: 8750.50,
    month: 32450.75
  },
  upcomingAppointments: [
    {
      id: '1',
      clientName: 'Maria Silva',
      serviceName: 'Corte de Cabelo',
      time: '14:30',
      professionalName: 'Carlos Oliveira'
    },
    {
      id: '2',
      clientName: 'João Pereira',
      serviceName: 'Barba',
      time: '15:00',
      professionalName: 'Ana Souza'
    },
    {
      id: '3',
      clientName: 'Fernanda Lima',
      serviceName: 'Manicure',
      time: '15:30',
      professionalName: 'Paula Santos'
    },
    {
      id: '4',
      clientName: 'Roberto Alves',
      serviceName: 'Hidratação',
      time: '16:15',
      professionalName: 'Carlos Oliveira'
    }
  ]
};

const Dashboard: React.FC = () => {
  const [data, setData] = useState<DashboardData | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const theme = useTheme();
  const { enqueueSnackbar } = useSnackbar();

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        // Em produção, esta seria a chamada real à API
        // const response = await axios.get('http://localhost:8080/api/v1/dashboard');
        // setData(response.data);

        // Para desenvolvimento, usamos os dados simulados
        setTimeout(() => {
          setData(mockDashboardData);
          setIsLoading(false);
        }, 1000); // Simula tempo de carregamento
      } catch (error) {
        console.error('Erro ao buscar dados do dashboard:', error);
        enqueueSnackbar('Não foi possível carregar os dados do dashboard', { variant: 'error' });
        setIsLoading(false);
      }
    };

    fetchDashboardData();
  }, [enqueueSnackbar]);

  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <CircularProgress />
      </Box>
    );
  }

  if (!data) {
    return <Typography color="error">Erro ao carregar dados</Typography>;
  }

  return (
    <Box sx={{ flexGrow: 1 }}>
      <Typography variant="h4" gutterBottom fontWeight="600">
        Dashboard
      </Typography>
      <Typography variant="body1" color="text.secondary" paragraph>
        Bem-vindo ao sistema ClinicaSalão. Confira o resumo do dia:
      </Typography>

      {/* Cards com métricas principais */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card 
            elevation={1} 
            sx={{ 
              height: '100%',
              borderLeft: `4px solid ${theme.palette.primary.main}`
            }}
          >
            <CardContent>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Agendamentos Hoje
                  </Typography>
                  <Typography variant="h4" fontWeight="600">
                    {data.appointmentsToday}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    {data.appointmentsPending} pendentes
                  </Typography>
                </Box>
                <Avatar sx={{ bgcolor: 'primary.light', width: 40, height: 40 }}>
                  <TodayIcon />
                </Avatar>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card 
            elevation={1} 
            sx={{ 
              height: '100%',
              borderLeft: `4px solid ${theme.palette.secondary.main}`
            }}
          >
            <CardContent>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Clientes
                  </Typography>
                  <Typography variant="h4" fontWeight="600">
                    {data.activeClients}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    de {data.totalClients} cadastrados
                  </Typography>
                </Box>
                <Avatar sx={{ bgcolor: 'secondary.light', width: 40, height: 40 }}>
                  <PeopleIcon />
                </Avatar>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card 
            elevation={1} 
            sx={{ 
              height: '100%',
              borderLeft: `4px solid ${theme.palette.success.main}`
            }}
          >
            <CardContent>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Faturamento Hoje
                  </Typography>
                  <Typography variant="h4" fontWeight="600">
                    R$ {data.revenue.today.toFixed(2)}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    R$ {data.revenue.week.toFixed(2)} esta semana
                  </Typography>
                </Box>
                <Avatar sx={{ bgcolor: 'success.light', width: 40, height: 40 }}>
                  <MoneyIcon />
                </Avatar>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card 
            elevation={1} 
            sx={{ 
              height: '100%',
              borderLeft: `4px solid ${theme.palette.info.main}`
            }}
          >
            <CardContent>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Faturamento Mensal
                  </Typography>
                  <Typography variant="h4" fontWeight="600">
                    R$ {data.revenue.month.toFixed(2)}
                  </Typography>
                  <Typography variant="caption" color="success.main" sx={{ display: 'flex', alignItems: 'center' }}>
                    <TrendingUpIcon fontSize="small" sx={{ mr: 0.5 }} />
                    +12% que mês anterior
                  </Typography>
                </Box>
                <Avatar sx={{ bgcolor: 'info.light', width: 40, height: 40 }}>
                  <TrendingUpIcon />
                </Avatar>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Próximos agendamentos */}
      <Grid container spacing={3}>
        <Grid item xs={12}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="h6" gutterBottom>
              Próximos Agendamentos
            </Typography>
            <Divider sx={{ mb: 2 }} />
            <List>
              {data.upcomingAppointments.map((appointment) => (
                <React.Fragment key={appointment.id}>
                  <ListItem alignItems="flex-start">
                    <ListItemAvatar>
                      <Avatar sx={{ bgcolor: 'primary.main' }}>
                        <EventAvailableIcon />
                      </Avatar>
                    </ListItemAvatar>
                    <ListItemText
                      primary={
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                          <Typography variant="subtitle1" fontWeight="500">
                            {appointment.serviceName}
                          </Typography>
                          <Chip 
                            size="small" 
                            label={appointment.time} 
                            color="primary" 
                            variant="outlined" 
                          />
                        </Box>
                      }
                      secondary={
                        <>
                          <Typography component="span" variant="body2" color="text.primary">
                            Cliente: {appointment.clientName}
                          </Typography>
                          <Typography component="span" variant="body2" display="block" color="text.secondary">
                            Profissional: {appointment.professionalName}
                          </Typography>
                        </>
                      }
                    />
                  </ListItem>
                  <Divider variant="inset" component="li" />
                </React.Fragment>
              ))}
            </List>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Dashboard;
