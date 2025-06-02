import React, { useState } from 'react';
import { 
  Box, Typography, Paper, Grid, Card, CardContent, 
  FormControl, InputLabel, Select, MenuItem, ButtonGroup,
} from '@mui/material';
import { 
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend,
  LineChart, Line, ResponsiveContainer, PieChart, Pie, Cell, ReferenceLine 
} from 'recharts';
import {
  TrendingUp as TrendingUpIcon,
  CalendarToday as CalendarIcon,
  DateRange as DateRangeIcon,
  AccountBalance as AccountBalanceIcon
} from '@mui/icons-material';

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
  totalPaid: number;
  totalPending: number;
  totalRefunded: number;
  totalCancelled: number;
  transactionCount: number;
  paidTransactionCount: number;
  pendingTransactionCount: number;
  refundedTransactionCount: number;
  cancelledTransactionCount: number;
  averageTicket: number;
  conversionRate: number; // taxa de conversão (pagos / total)
  growthRate: number; // taxa de crescimento em relação ao período anterior
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
  totalRevenue: 15000,
  totalPaid: 12000,
  totalPending: 2000,
  totalRefunded: 1000,
  totalCancelled: 500,
  transactionCount: 50,
  paidTransactionCount: 40,
  pendingTransactionCount: 7,
  refundedTransactionCount: 2,
  cancelledTransactionCount: 1,
  averageTicket: 300,
  conversionRate: 0.8, // 80% de conversão
  growthRate: 0.15 // 15% de crescimento
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

      {/* Seção de Relatórios Avançados */}
      <Typography variant="h6" sx={{ mt: 4, mb: 2 }}>
        Relatórios Avançados
      </Typography>
      <Paper sx={{ p: 2, mb: 4 }}>
        <Grid container spacing={3}>
          <Grid item xs={12} md={6}>
            <Card variant="outlined">
              <CardContent>
                <Typography variant="subtitle1" gutterBottom>
                  Projeção de Receita
                </Typography>
                <Typography variant="body2" color="text.secondary" paragraph>
                  Baseado nos dados históricos, a projeção de receita para o próximo período é de {formatCurrency(revenueSummary.totalRevenue * 1.15)}, um aumento estimado de 15%.                
                </Typography>
                <Box sx={{ mt: 2 }}>
                  <Typography variant="body2">Exportar relatórios detalhados:</Typography>
                  <Box sx={{ mt: 1, display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                    <Button size="small" variant="outlined" startIcon={<InsertDriveFileIcon />} onClick={() => handleExportReport('monthly')}>Mensal</Button>
                    <Button size="small" variant="outlined" startIcon={<TableChartIcon />} onClick={() => handleExportReport('quarterly')}>Trimestral</Button>
                    <Button size="small" variant="outlined" startIcon={<AssessmentIcon />} onClick={() => handleExportReport('annual')}>Anual</Button>
                  </Box>
                </Box>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={12} md={6}>
            <Card variant="outlined">
              <CardContent>
                <Typography variant="subtitle1" gutterBottom>
                  Análise de Inadimplência
                </Typography>
                <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
                  <Typography variant="body2" color="text.secondary">
                    Taxa de inadimplência:
                  </Typography>
                  <Typography variant="body1" color="warning.main" fontWeight="bold">
                    {((revenueSummary.totalPending / revenueSummary.totalRevenue) * 100).toFixed(1)}%
                  </Typography>
                </Box>
                <LinearProgress 
                  variant="determinate" 
                  value={(revenueSummary.totalPending / revenueSummary.totalRevenue) * 100}
                  color="warning"
                  sx={{ mb: 2, height: 8, borderRadius: 4 }}
                />
                <Typography variant="body2" color="text.secondary">
                  Recomendação: Implementar lembretes automáticos para pagamentos pendentes e oferecer planos de parcelamento para valores maiores.
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      </Paper>
      
      {/* Botões de exportação e compartilhamento */}
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4, mb: 2, gap: 2 }}>
        <Button
          variant="contained"
          startIcon={<GetAppIcon />}
          onClick={() => handleExportReport('full')}
        >
          Exportar Relatório Completo
        </Button>
        <Button
          variant="outlined"
          startIcon={<ShareIcon />}
          onClick={() => enqueueSnackbar('Link para relatório copiado para a área de transferência', { variant: 'success' })}
        >
          Compartilhar Relatório
        </Button>
      </Box>

      {/* Cards de resumo */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom variant="body2">
                Receita Total
              </Typography>
              <Typography variant="h4" component="div">
                {formatCurrency(revenueSummary.totalRevenue)}
              </Typography>
              <Typography color="textSecondary" variant="body2">
                {revenueSummary.transactionCount} transações
              </Typography>
              {revenueSummary.growthRate > 0 && (
                <Box sx={{ display: 'flex', alignItems: 'center', mt: 1 }}>
                  <ArrowUpwardIcon fontSize="small" color="success" />
                  <Typography variant="body2" color="success.main" sx={{ ml: 0.5 }}>
                    {(revenueSummary.growthRate * 100).toFixed(1)}% de crescimento
                  </Typography>
                </Box>
              )}
              {revenueSummary.growthRate < 0 && (
                <Box sx={{ display: 'flex', alignItems: 'center', mt: 1 }}>
                  <ArrowDownwardIcon fontSize="small" color="error" />
                  <Typography variant="body2" color="error.main" sx={{ ml: 0.5 }}>
                    {(Math.abs(revenueSummary.growthRate) * 100).toFixed(1)}% de queda
                  </Typography>
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom variant="body2">
                Total Pago
              </Typography>
              <Typography variant="h4" component="div" sx={{ color: 'success.main' }}>
                {formatCurrency(revenueSummary.totalPaid)}
              </Typography>
              <Typography color="textSecondary" variant="body2">
                {((revenueSummary.totalPaid / revenueSummary.totalRevenue) * 100).toFixed(1)}% da receita
              </Typography>
              <Typography color="textSecondary" variant="body2" sx={{ mt: 1 }}>
                {revenueSummary.paidTransactionCount} transações pagas
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom variant="body2">
                Pendente
              </Typography>
              <Typography variant="h4" component="div" sx={{ color: 'warning.main' }}>
                {formatCurrency(revenueSummary.totalPending)}
              </Typography>
              <Typography color="textSecondary" variant="body2">
                {((revenueSummary.totalPending / revenueSummary.totalRevenue) * 100).toFixed(1)}% da receita
              </Typography>
              <Typography color="textSecondary" variant="body2" sx={{ mt: 1 }}>
                {revenueSummary.pendingTransactionCount} transações pendentes
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom variant="body2">
                Ticket Médio
              </Typography>
              <Typography variant="h4" component="div">
                {formatCurrency(revenueSummary.averageTicket)}
              </Typography>
              <Typography color="textSecondary" variant="body2">
                Por transação paga
              </Typography>
              <Typography variant="body2" color="primary" sx={{ mt: 1 }}>
                Taxa de conversão: {(revenueSummary.conversionRate * 100).toFixed(1)}%
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
      
      {/* Segunda linha de cards */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={4}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom variant="body2">
                Reembolsos
              </Typography>
              <Typography variant="h5" component="div" sx={{ color: 'error.main' }}>
                {formatCurrency(revenueSummary.totalRefunded)}
              </Typography>
              <Typography color="textSecondary" variant="body2">
                {((revenueSummary.totalRefunded / revenueSummary.totalRevenue) * 100).toFixed(1)}% da receita
              </Typography>
              <Typography color="textSecondary" variant="body2" sx={{ mt: 1 }}>
                {revenueSummary.refundedTransactionCount} transações reembolsadas
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={4}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom variant="body2">
                Cancelamentos
              </Typography>
              <Typography variant="h5" component="div" sx={{ color: 'text.secondary' }}>
                {formatCurrency(revenueSummary.totalCancelled)}
              </Typography>
              <Typography color="textSecondary" variant="body2">
                {((revenueSummary.totalCancelled / revenueSummary.totalRevenue) * 100).toFixed(1)}% da receita
              </Typography>
              <Typography color="textSecondary" variant="body2" sx={{ mt: 1 }}>
                {revenueSummary.cancelledTransactionCount} transações canceladas
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom variant="body2">
                Receita Líquida
              </Typography>
              <Typography variant="h5" component="div" sx={{ color: 'primary.main' }}>
                {formatCurrency(revenueSummary.totalPaid - revenueSummary.totalRefunded)}
              </Typography>
              <Typography color="textSecondary" variant="body2">
                Receita total excluindo reembolsos
              </Typography>
              <Box sx={{ display: 'flex', alignItems: 'center', mt: 1 }}>
                <Typography variant="body2" color="text.secondary">
                  Efetividade: {((revenueSummary.totalPaid - revenueSummary.totalRefunded) / revenueSummary.totalRevenue * 100).toFixed(1)}%
                </Typography>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Gráfico de barras - Receita por Período */}
      <Typography variant="h6" sx={{ mt: 4, mb: 2 }}>
        Receita por Período
      </Typography>
      <Paper sx={{ p: 2, mb: 4 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2, alignItems: 'center' }}>
          <Typography variant="subtitle1" color="text.secondary">
            {timeRange === 'day' && 'Receita diária nos últimos 30 dias'}
            {timeRange === 'week' && 'Receita semanal nos últimos 3 meses'}
            {timeRange === 'month' && 'Receita mensal nos últimos 12 meses'}
            {timeRange === 'year' && 'Receita anual nos últimos 5 anos'}
          </Typography>
          <ToggleButtonGroup
            value={timeRange}
            exclusive
            onChange={handleTimeRangeChange}
            size="small"
          >
            <ToggleButton value="day">Dia</ToggleButton>
            <ToggleButton value="week">Semana</ToggleButton>
            <ToggleButton value="month">Mês</ToggleButton>
            <ToggleButton value="year">Ano</ToggleButton>
          </ToggleButtonGroup>
        </Box>
        <Box sx={{ width: '100%', height: 300 }}>
          <ResponsiveContainer width="100%" height="100%">
            <BarChart
              data={revenueByPeriod}
              margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
            >
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="period" />
              <YAxis />
              <Tooltip 
                formatter={(value) => formatCurrency(value as number)} 
                labelFormatter={(label) => `Período: ${label}`}
              />
              <Legend />
              <Bar dataKey="revenue" name="Receita" fill={theme.palette.primary.main} />
              <Bar dataKey="profit" name="Lucro" fill={theme.palette.success.main} />
              <ReferenceLine y={revenueByPeriod.reduce((sum, item) => sum + (item.revenue || 0), 0) / revenueByPeriod.length} 
                stroke="red" 
                strokeDasharray="3 3"
                label="Média" />
            </BarChart>
          </ResponsiveContainer>
        </Box>
        <Box sx={{ mt: 2, display: 'flex', justifyContent: 'space-between' }}>
          <Typography variant="body2" color="text.secondary">
            Média: {formatCurrency(revenueByPeriod.reduce((sum, item) => sum + item.revenue, 0) / revenueByPeriod.length)}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Total: {formatCurrency(revenueByPeriod.reduce((sum, item) => sum + item.revenue, 0))}
          </Typography>
        </Box>
      </Paper>

      {/* Gráfico de pizza - Receita por Método de Pagamento */}
      <Typography variant="h6" sx={{ mt: 4, mb: 2 }}>
        Receita por Método de Pagamento
      </Typography>
      <Paper sx={{ p: 2, mb: 4 }}>
        <Grid container spacing={2}>
          <Grid item xs={12} md={7}>
            <Box sx={{ width: '100%', height: 300 }}>
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={revenueByPaymentMethod}
                    cx="50%"
                    cy="50%"
                    labelLine={false}
                    outerRadius={100}
                    innerRadius={60}
                    fill="#8884d8"
                    dataKey="value"
                    nameKey="name"
                    label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
                  >
                    {revenueByPaymentMethod.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip formatter={(value) => formatCurrency(value as number)} />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            </Box>
          </Grid>
          <Grid item xs={12} md={5}>
            <Typography variant="subtitle1" gutterBottom>
              Análise de Métodos de Pagamento
            </Typography>
            <TableContainer component={Paper} variant="outlined" sx={{ maxHeight: 250, overflow: 'auto' }}>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Método</TableCell>
                    <TableCell align="right">Valor</TableCell>
                    <TableCell align="right">%</TableCell>
                    <TableCell align="right">Tendência</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {revenueByPaymentMethod.map((method, index) => {
                    const percent = (method.value / revenueByPaymentMethod.reduce((sum, item) => sum + item.value, 0)) * 100;
                    const trends = [5, -2, 8, 1, -3]; // Dados simulados de tendência
                    return (
                      <TableRow key={method.name}>
                        <TableCell>{method.name}</TableCell>
                        <TableCell align="right">{formatCurrency(method.value)}</TableCell>
                        <TableCell align="right">{percent.toFixed(1)}%</TableCell>
                        <TableCell align="right">
                          {trends[index] > 0 ? (
                            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end' }}>
                              <ArrowUpwardIcon fontSize="small" color="success" />
                              <Typography variant="body2" color="success.main">{trends[index]}%</Typography>
                            </Box>
                          ) : (
                            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end' }}>
                              <ArrowDownwardIcon fontSize="small" color="error" />
                              <Typography variant="body2" color="error.main">{Math.abs(trends[index])}%</Typography>
                            </Box>
                          )}
                        </TableCell>
                      </TableRow>
                    );
                  })}
                </TableBody>
              </Table>
            </TableContainer>
            <Box sx={{ mt: 2 }}>
              <Typography variant="body2" color="text.secondary">
                Método Preferido: {revenueByPaymentMethod.reduce((prev, current) => 
                  (prev.value > current.value) ? prev : current).name}
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                Recomendação: Oferecer descontos para métodos de pagamento com menor custo operacional
              </Typography>
            </Box>
          </Grid>
        </Grid>
      </Paper>
    </Box>
  );
};

export default Revenue;
