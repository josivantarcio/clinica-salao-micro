import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import {
  Box,
  Button,
  Card,
  CardContent,
  Divider,
  FormControl,
  Grid,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  TextField,
  Typography
} from '@mui/material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import {
  Download as DownloadIcon,
  FileCopy as FileCopyIcon,
  Print as PrintIcon
} from '@mui/icons-material';
import { ptBR } from 'date-fns/locale';
import { format, startOfMonth, endOfMonth, subMonths } from 'date-fns';

import Page from '../../components/layout/Page';
import Loading from '../../components/common/Loading';
import ErrorAlert from '../../components/common/ErrorAlert';
import {
  ReportFilter,
  getFinancialSummary,
  getRevenueTrends,
  getServicePopularity,
  getProfessionalPerformance,
  exportFinancialReport
} from '../../services/reportService';

// Componente para filtros de relatório
const ReportFilters: React.FC<{
  filters: ReportFilter;
  onFilterChange: (filters: ReportFilter) => void;
}> = ({ filters, onFilterChange }) => {
  // Função para lidar com mudanças nos filtros
  const handleFilterChange = (field: keyof ReportFilter) => (
    event: React.ChangeEvent<HTMLInputElement | { name?: string; value: unknown }>
  ) => {
    onFilterChange({
      ...filters,
      [field]: event.target.value
    });
  };

  // Função para lidar com mudanças nas datas
  const handleDateChange = (field: 'startDate' | 'endDate') => (date: Date | null) => {
    onFilterChange({
      ...filters,
      [field]: date ? format(date, 'yyyy-MM-dd') : undefined
    });
  };

  // Aplicar filtro para um período específico
  const applyPeriodFilter = (months: number) => {
    const endDate = new Date();
    const startDate = subMonths(endDate, months);

    onFilterChange({
      ...filters,
      startDate: format(startDate, 'yyyy-MM-dd'),
      endDate: format(endDate, 'yyyy-MM-dd')
    });
  };

  return (
    <LocalizationProvider dateAdapter={AdapterDateFns} adapterLocale={ptBR}>
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Typography variant="h6" sx={{ mb: 2 }}>Filtros</Typography>
          <Grid container spacing={2}>
            <Grid item xs={12} md={6} lg={3}>
              <DatePicker
                label="Data Inicial"
                value={filters.startDate ? new Date(filters.startDate) : null}
                onChange={handleDateChange('startDate')}
                slotProps={{ textField: { fullWidth: true, variant: 'outlined' } }}
              />
            </Grid>
            <Grid item xs={12} md={6} lg={3}>
              <DatePicker
                label="Data Final"
                value={filters.endDate ? new Date(filters.endDate) : null}
                onChange={handleDateChange('endDate')}
                slotProps={{ textField: { fullWidth: true, variant: 'outlined' } }}
              />
            </Grid>
            <Grid item xs={12} md={6} lg={3}>
              <FormControl fullWidth>
                <InputLabel>Tipo de Serviço</InputLabel>
                <Select
                  value={filters.serviceType || ''}
                  onChange={handleFilterChange('serviceType')}
                  label="Tipo de Serviço"
                >
                  <MenuItem value="">Todos</MenuItem>
                  <MenuItem value="HAIRCUT">Corte de Cabelo</MenuItem>
                  <MenuItem value="HAIR_COLORING">Coloração</MenuItem>
                  <MenuItem value="MANICURE">Manicure</MenuItem>
                  <MenuItem value="PEDICURE">Pedicure</MenuItem>
                  <MenuItem value="FACIAL">Tratamento Facial</MenuItem>
                  <MenuItem value="MASSAGE">Massagem</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} md={6} lg={3}>
              <FormControl fullWidth>
                <InputLabel>Status</InputLabel>
                <Select
                  value={filters.status || ''}
                  onChange={handleFilterChange('status')}
                  label="Status"
                >
                  <MenuItem value="">Todos</MenuItem>
                  <MenuItem value="COMPLETED">Concluído</MenuItem>
                  <MenuItem value="CANCELED">Cancelado</MenuItem>
                  <MenuItem value="PENDING">Pendente</MenuItem>
                </Select>
              </FormControl>
            </Grid>
          </Grid>

          <Box display="flex" gap={1} mt={2}>
            <Button variant="outlined" size="small" onClick={() => applyPeriodFilter(1)}>
              Último Mês
            </Button>
            <Button variant="outlined" size="small" onClick={() => applyPeriodFilter(3)}>
              Últimos 3 Meses
            </Button>
            <Button variant="outlined" size="small" onClick={() => applyPeriodFilter(6)}>
              Últimos 6 Meses
            </Button>
            <Button variant="outlined" size="small" onClick={() => applyPeriodFilter(12)}>
              Último Ano
            </Button>
            <Button
              variant="outlined"
              size="small"
              onClick={() => {
                const today = new Date();
                onFilterChange({
                  ...filters,
                  startDate: format(startOfMonth(today), 'yyyy-MM-dd'),
                  endDate: format(endOfMonth(today), 'yyyy-MM-dd')
                });
              }}
            >
              Este Mês
            </Button>
          </Box>
        </CardContent>
      </Card>
    </LocalizationProvider>
  );
};

// Componente para card de métricas
interface MetricCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  icon?: React.ReactNode;
  color?: string;
}

const MetricCard: React.FC<MetricCardProps> = ({ title, value, subtitle, icon, color = 'primary.main' }) => {
  return (
    <Card>
      <CardContent>
        <Box display="flex" justifyContent="space-between" alignItems="center">
          <Box>
            <Typography variant="body2" color="text.secondary">
              {title}
            </Typography>
            <Typography variant="h5" sx={{ mt: 1, color }}>
              {value}
            </Typography>
            {subtitle && (
              <Typography variant="caption" color="text.secondary">
                {subtitle}
              </Typography>
            )}
          </Box>
          {icon && (
            <Box sx={{ color: 'action.active' }}>
              {icon}
            </Box>
          )}
        </Box>
      </CardContent>
    </Card>
  );
};

const Reports: React.FC = () => {
  // Estado para filtros
  const [filters, setFilters] = useState<ReportFilter>({
    startDate: format(subMonths(new Date(), 1), 'yyyy-MM-dd'),
    endDate: format(new Date(), 'yyyy-MM-dd')
  });

  // Buscar resumo financeiro
  const {
    data: financialSummary,
    isLoading: isLoadingFinancial,
    isError: isErrorFinancial,
    error: errorFinancial
  } = useQuery({
    queryKey: ['financialSummary', filters],
    queryFn: () => getFinancialSummary(filters),
  });

  // Buscar tendências de receita
  const {
    data: revenueTrends,
    isLoading: isLoadingTrends,
    isError: isErrorTrends,
    error: errorTrends
  } = useQuery({
    queryKey: ['revenueTrends', filters],
    queryFn: () => getRevenueTrends(filters),
  });

  // Buscar popularidade dos serviços
  const {
    data: servicePopularity,
    isLoading: isLoadingServices,
    isError: isErrorServices,
    error: errorServices
  } = useQuery({
    queryKey: ['servicePopularity', filters],
    queryFn: () => getServicePopularity(filters),
  });

  // Buscar desempenho dos profissionais
  const {
    data: professionalPerformance,
    isLoading: isLoadingProfessionals,
    isError: isErrorProfessionals,
    error: errorProfessionals
  } = useQuery({
    queryKey: ['professionalPerformance', filters],
    queryFn: () => getProfessionalPerformance(filters),
  });

  // Função para formatar valores monetários
  const formatCurrency = (value: number): string => {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(value);
  };

  // Função para exportar relatório financeiro
  const handleExportFinancial = async () => {
    try {
      const blob = await exportFinancialReport(filters);
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `relatorio-financeiro-${format(new Date(), 'yyyy-MM-dd')}.xlsx`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Erro ao exportar relatório:', error);
    }
  };

  const isLoading =
    isLoadingFinancial || isLoadingTrends || isLoadingServices || isLoadingProfessionals;

  const isError = isErrorFinancial || isErrorTrends || isErrorServices || isErrorProfessionals;
  const error = errorFinancial || errorTrends || errorServices || errorProfessionals;

  if (isLoading) return <Loading />;
  if (isError) return <ErrorAlert error={error as Error} />;

  return (
    <Page title="Relatórios">
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4">Relatórios</Typography>
        <Box display="flex" gap={1}>
          <Button
            variant="outlined"
            startIcon={<DownloadIcon />}
            onClick={handleExportFinancial}
          >
            Exportar
          </Button>
          <Button
            variant="outlined"
            startIcon={<PrintIcon />}
          >
            Imprimir
          </Button>
        </Box>
      </Box>

      <ReportFilters filters={filters} onFilterChange={setFilters} />

      <Typography variant="h5" mb={2}>Resumo Financeiro</Typography>
      <Grid container spacing={3} mb={4}>
        <Grid item xs={12} md={6} lg={3}>
          <MetricCard
            title="Receita Total"
            value={formatCurrency(financialSummary?.totalRevenue || 0)}
            color="success.main"
          />
        </Grid>
        <Grid item xs={12} md={6} lg={3}>
          <MetricCard
            title="Despesas"
            value={formatCurrency(financialSummary?.totalExpenses || 0)}
            color="error.main"
          />
        </Grid>
        <Grid item xs={12} md={6} lg={3}>
          <MetricCard
            title="Lucro Líquido"
            value={formatCurrency(financialSummary?.netRevenue || 0)}
            color={financialSummary?.netRevenue && financialSummary.netRevenue > 0 ? 'success.main' : 'error.main'}
          />
        </Grid>
        <Grid item xs={12} md={6} lg={3}>
          <MetricCard
            title="Ticket Médio"
            value={formatCurrency(financialSummary?.averageTicket || 0)}
          />
        </Grid>
      </Grid>

      <Grid container spacing={3}>
        <Grid item xs={12} lg={8}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Typography variant="h6" mb={2}>Tendência de Receita</Typography>
              <Box sx={{ height: 300 }}>
                {/* Aqui seria inserido um gráfico com os dados de revenueTrends */}
                {/* Usando biblioteca de gráficos como Recharts ou Chart.js */}
                <Typography variant="body2" color="text.secondary" align="center">
                  Gráfico de tendência será inserido aqui utilizando os dados de revenueTrends
                </Typography>
              </Box>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} lg={4}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Typography variant="h6" mb={2}>Serviços Mais Populares</Typography>
              <Box>
                {servicePopularity?.slice(0, 5).map((service, index) => (
                  <Box key={index} mb={2}>
                    <Box display="flex" justifyContent="space-between">
                      <Typography variant="body2">{service.serviceName}</Typography>
                      <Typography variant="body2">{formatCurrency(service.revenue)}</Typography>
                    </Box>
                    <Box display="flex" alignItems="center">
                      <Box
                        sx={{
                          width: `${service.percentage}%`,
                          bgcolor: 'primary.main',
                          height: 8,
                          borderRadius: 1,
                          mr: 1
                        }}
                      />
                      <Typography variant="caption">{service.percentage}%</Typography>
                    </Box>
                  </Box>
                ))}
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Typography variant="h5" mt={4} mb={2}>Desempenho de Profissionais</Typography>
      <Grid container spacing={3}>
        {professionalPerformance?.map((professional) => (
          <Grid item xs={12} md={6} lg={4} key={professional.professionalId}>
            <Card>
              <CardContent>
                <Typography variant="h6">{professional.professionalName}</Typography>
                <Divider sx={{ my: 1 }} />
                <Grid container spacing={2}>
                  <Grid item xs={6}>
                    <Typography variant="body2" color="text.secondary">Atendimentos</Typography>
                    <Typography variant="h6">{professional.totalAppointments}</Typography>
                  </Grid>
                  <Grid item xs={6}>
                    <Typography variant="body2" color="text.secondary">Receita</Typography>
                    <Typography variant="h6">{formatCurrency(professional.totalRevenue)}</Typography>
                  </Grid>
                  <Grid item xs={6}>
                    <Typography variant="body2" color="text.secondary">Taxa de Conclusão</Typography>
                    <Typography variant="h6">
                      {((professional.completedAppointments / professional.totalAppointments) * 100).toFixed(1)}%
                    </Typography>
                  </Grid>
                  <Grid item xs={6}>
                    <Typography variant="body2" color="text.secondary">Satisfação</Typography>
                    <Typography variant="h6">{professional.clientSatisfaction.toFixed(1)}/5.0</Typography>
                  </Grid>
                </Grid>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Page>
  );
};

export default Reports;
