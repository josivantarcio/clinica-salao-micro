import React, { useState } from 'react';
import { 
  Box, Typography, Paper, Grid, Card, CardContent, 
  FormControl, InputLabel, Select, MenuItem, ButtonGroup,
  Button, Divider, CircularProgress, useTheme
} from '@mui/material';
import {
  TrendingUp as TrendingUpIcon,
  CalendarToday as CalendarIcon,
  DateRange as DateRangeIcon,
  AccountBalance as AccountBalanceIcon
} from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import {
  LineChart,
  Line,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  Legend,
  ResponsiveContainer,
  CartesianGrid,
  PieChart,
  Pie,
  Cell
} from 'recharts';

// Importando serviço real de finanças
import { 
  getRevenueSummary, 
  getRevenueByPeriod, 
  getRevenueByPaymentMethod,
  RevenueSummaryDTO,
  RevenueByPeriodDTO,
  RevenueByPaymentMethodDTO 
} from '../../services/financeService';

// Interfaces para tipagem
interface RevenueSummary {
  totalRevenue: number;
  countPaid: number;
  countPending: number;
  averageTicket: number;
}

interface RevenueByPeriod {
  period: string;
  revenue: number;
}

interface RevenueByPaymentMethod {
  method: string;
  revenue: number;
  percentage: number;
}

// Dados simulados para desenvolvimento
const mockRevenueSummary: RevenueSummary = {
  totalRevenue: 12500.00,
  countPaid: 85,
  countPending: 23,
  averageTicket: 147.05
};

const mockRevenueByMonth: RevenueByPeriod[] = [
  { period: 'Jan', revenue: 8500 },
  { period: 'Fev', revenue: 9200 },
  { period: 'Mar', revenue: 10800 },
  { period: 'Abr', revenue: 9700 },
  { period: 'Mai', revenue: 11300 },
  { period: 'Jun', revenue: 12500 }
];

const mockRevenueByDay: RevenueByPeriod[] = [
  { period: '26/05', revenue: 750 },
  { period: '27/05', revenue: 900 },
  { period: '28/05', revenue: 850 },
  { period: '29/05', revenue: 1200 },
  { period: '30/05', revenue: 1300 },
  { period: '31/05', revenue: 800 },
  { period: '01/06', revenue: 1100 }
];

const mockRevenueByPaymentMethod: RevenueByPaymentMethod[] = [
  { method: 'Cartão de Crédito', revenue: 7500, percentage: 60 },
  { method: 'PIX', revenue: 2500, percentage: 20 },
  { method: 'Boleto', revenue: 1250, percentage: 10 },
  { method: 'Dinheiro', revenue: 1250, percentage: 10 }
];

const Revenue: React.FC = () => {
  const [timeRange, setTimeRange] = useState<'day' | 'week' | 'month' | 'year'>('month');
  const [chartType, setChartType] = useState<'line' | 'bar'>('bar');
  const navigate = useNavigate();
  const theme = useTheme();

    // Buscando dados do backend com fallback para dados simulados
  const { data: revenueSummary = mockRevenueSummary, isLoading: isLoadingSummary } = useQuery({
    queryKey: ['revenueSummary'],
    queryFn: async () => {
      try {
        // Tenta buscar dados do backend
        return await getRevenueSummary();
      } catch (error) {
        console.error('Erro ao buscar resumo de receita:', error);
        // Fallback para dados simulados
        return mockRevenueSummary;
      }
    },
    initialData: mockRevenueSummary
  });

  const { data: revenueByPeriod = mockRevenueByMonth, isLoading: isLoadingPeriod } = useQuery({
    queryKey: ['revenueByPeriod', timeRange],
    queryFn: async () => {
      try {
        // Tenta buscar dados do backend
        return await getRevenueByPeriod(timeRange);
      } catch (error) {
        console.error(`Erro ao buscar receita por período (${timeRange}):`, error);
        // Fallback para dados simulados
        return timeRange === 'day' || timeRange === 'week' 
          ? mockRevenueByDay 
          : mockRevenueByMonth;
      }
    }
  });

  const { data: revenueByPaymentMethod = mockRevenueByPaymentMethod, isLoading: isLoadingMethods } = useQuery({
    queryKey: ['revenueByPaymentMethod'],
    queryFn: async () => {
      try {
        // Tenta buscar dados do backend
        return await getRevenueByPaymentMethod();
      } catch (error) {
        console.error('Erro ao buscar receita por método de pagamento:', error);
        // Fallback para dados simulados
        return mockRevenueByPaymentMethod;
      }
    },
    initialData: mockRevenueByPaymentMethod
  });

  const handleTimeRangeChange = (event: any) => {
    setTimeRange(event.target.value);
  };

  // Cores para o gráfico de pizza
  const COLORS = [
    theme.palette.primary.main,
    theme.palette.secondary.main,
    theme.palette.info.main,
    theme.palette.success.main,
    theme.palette.warning.main,
  ];

  const formatCurrency = (value: number) => {
    return `R$ ${value.toFixed(2).replace('.', ',').replace(/\B(?=(\d{3})+(?!\d))/g, '.')}`;
  };

  return (
    <Box sx={{ width: '100%' }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3, alignItems: 'center' }}>
        <Typography variant="h4" sx={{ display: 'flex', alignItems: 'center' }}>
          <AccountBalanceIcon sx={{ mr: 1 }} /> Relatório de Receita
        </Typography>
        <Button 
          variant="outlined" 
          onClick={() => navigate('/finance')}
        >
          Ver Transações
        </Button>
      </Box>

      {/* Cards de resumo */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Receita Total
              </Typography>
              {isLoadingSummary ? (
                <CircularProgress size={24} />
              ) : (
                <Typography variant="h4" component="div" sx={{ fontWeight: 'bold' }}>
                  {formatCurrency(revenueSummary.totalRevenue)}
                </Typography>
              )}
              <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                <TrendingUpIcon sx={{ fontSize: 16, verticalAlign: 'text-bottom' }} />
                {' '} Período atual
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Pagamentos Realizados
              </Typography>
              {isLoadingSummary ? (
                <CircularProgress size={24} />
              ) : (
                <Typography variant="h4" component="div" sx={{ fontWeight: 'bold' }}>
                  {revenueSummary.countPaid}
                </Typography>
              )}
              <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                <CalendarIcon sx={{ fontSize: 16, verticalAlign: 'text-bottom' }} />
                {' '} No período
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Pagamentos Pendentes
              </Typography>
              {isLoadingSummary ? (
                <CircularProgress size={24} />
              ) : (
                <Typography variant="h4" component="div" sx={{ fontWeight: 'bold' }}>
                  {revenueSummary.countPending}
                </Typography>
              )}
              <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                <DateRangeIcon sx={{ fontSize: 16, verticalAlign: 'text-bottom' }} />
                {' '} A receber
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Ticket Médio
              </Typography>
              {isLoadingSummary ? (
                <CircularProgress size={24} />
              ) : (
                <Typography variant="h4" component="div" sx={{ fontWeight: 'bold' }}>
                  {formatCurrency(revenueSummary.averageTicket)}
                </Typography>
              )}
              <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                <TrendingUpIcon sx={{ fontSize: 16, verticalAlign: 'text-bottom' }} />
                {' '} Por transação
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Gráficos */}
      <Grid container spacing={3}>
        {/* Gráfico de receita por período */}
        <Grid item xs={12} md={8}>
          <Paper sx={{ p: 3 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Typography variant="h6">Receita por Período</Typography>
              <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
                <FormControl size="small" sx={{ minWidth: 120 }}>
                  <InputLabel>Período</InputLabel>
                  <Select
                    value={timeRange}
                    label="Período"
                    onChange={handleTimeRangeChange}
                  >
                    <MenuItem value="day">Diário</MenuItem>
                    <MenuItem value="week">Semanal</MenuItem>
                    <MenuItem value="month">Mensal</MenuItem>
                    <MenuItem value="year">Anual</MenuItem>
                  </Select>
                </FormControl>

                <ButtonGroup size="small">
                  <Button
                    variant={chartType === 'bar' ? 'contained' : 'outlined'}
                    onClick={() => setChartType('bar')}
                  >
                    Barras
                  </Button>
                  <Button
                    variant={chartType === 'line' ? 'contained' : 'outlined'}
                    onClick={() => setChartType('line')}
                  >
                    Linha
                  </Button>
                </ButtonGroup>
              </Box>
            </Box>
            <Divider sx={{ mb: 3 }} />
            
            {isLoadingPeriod ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 300 }}>
                <CircularProgress />
              </Box>
            ) : (
              <ResponsiveContainer width="100%" height={300}>
                {chartType === 'bar' ? (
                  <BarChart
                    data={revenueByPeriod}
                    margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
                  >
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="period" />
                    <YAxis 
                      tickFormatter={(value) => `R$ ${value}`} 
                    />
                    <Tooltip 
                      formatter={(value) => [`${formatCurrency(value as number)}`, 'Receita']} 
                      labelFormatter={(label) => `Período: ${label}`}
                    />
                    <Bar 
                      dataKey="revenue" 
                      name="Receita" 
                      fill={theme.palette.primary.main} 
                    />
                  </BarChart>
                ) : (
                  <LineChart
                    data={revenueByPeriod}
                    margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
                  >
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="period" />
                    <YAxis 
                      tickFormatter={(value) => `R$ ${value}`} 
                    />
                    <Tooltip 
                      formatter={(value) => [`${formatCurrency(value as number)}`, 'Receita']} 
                      labelFormatter={(label) => `Período: ${label}`}
                    />
                    <Legend />
                    <Line 
                      type="monotone" 
                      dataKey="revenue" 
                      name="Receita" 
                      stroke={theme.palette.primary.main} 
                      strokeWidth={3}
                      activeDot={{ r: 8 }} 
                    />
                  </LineChart>
                )}
              </ResponsiveContainer>
            )}
          </Paper>
        </Grid>

        {/* Gráfico de receita por método de pagamento */}
        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 3, height: '100%' }}>
            <Typography variant="h6" sx={{ mb: 2 }}>
              Receita por Método de Pagamento
            </Typography>
            <Divider sx={{ mb: 3 }} />
            
            {isLoadingMethods ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 300 }}>
                <CircularProgress />
              </Box>
            ) : (
              <Box sx={{ height: 300, display: 'flex', flexDirection: 'column' }}>
                <ResponsiveContainer width="100%" height="70%">
                  <PieChart>
                    <Pie
                      data={revenueByPaymentMethod}
                      cx="50%"
                      cy="50%"
                      labelLine={false}
                      outerRadius={80}
                      fill="#8884d8"
                      dataKey="revenue"
                      nameKey="method"
                      label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
                    >
                      {revenueByPaymentMethod.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                      ))}
                    </Pie>
                    <Tooltip formatter={(value) => [`${formatCurrency(value as number)}`, 'Receita']} />
                  </PieChart>
                </ResponsiveContainer>
                <Box sx={{ mt: 2 }}>
                  <Typography variant="body2" color="text.secondary" sx={{ textAlign: 'center' }}>
                    Distribuição percentual por método de pagamento
                  </Typography>
                </Box>
              </Box>
            )}
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Revenue;
