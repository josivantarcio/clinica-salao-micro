import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { 
  Box, 
  Button, 
  Card, 
  CardContent, 
  Chip, 
  IconButton, 
  Menu, 
  MenuItem, 
  Paper, 
  Table, 
  TableBody, 
  TableCell, 
  TableContainer, 
  TableHead, 
  TablePagination, 
  TableRow, 
  TextField,
  Tooltip,
  Typography 
} from '@mui/material';
import { 
  Add as AddIcon, 
  MoreVert as MoreVertIcon, 
  CalendarToday as CalendarIcon,
  Search as SearchIcon,
  FilterList as FilterListIcon
} from '@mui/icons-material';
import { format, parseISO } from 'date-fns';
import { ptBR } from 'date-fns/locale';

import Page from '../../components/layout/Page';
import Loading from '../../components/common/Loading';
import ErrorAlert from '../../components/common/ErrorAlert';
import { Appointment, getAppointments } from '../../services/api';

const Appointments: React.FC = () => {
  const navigate = useNavigate();
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [searchTerm, setSearchTerm] = useState('');
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [statusFilter, setStatusFilter] = useState<string>('all');

  // Buscar agendamentos
  const { 
    data: appointmentsData, 
    isLoading, 
    error, 
    refetch 
  } = useQuery({
    queryKey: ['appointments', { page, rowsPerPage, searchTerm, status: statusFilter }],
    queryFn: () => getAppointments({
      page: page + 1,
      limit: rowsPerPage,
      search: searchTerm,
      status: statusFilter !== 'all' ? statusFilter : undefined,
    }),
    keepPreviousData: true,
  });

  const handleChangePage = (event: unknown, newPage: number) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  const handleSearch = (event: React.ChangeEvent<HTMLInputElement>) => {
    setSearchTerm(event.target.value);
    setPage(0);
  };

  const handleFilterClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleFilterClose = () => {
    setAnchorEl(null);
  };

  const handleStatusFilter = (status: string) => {
    setStatusFilter(status);
    setPage(0);
    handleFilterClose();
  };

  const getStatusChip = (status: string) => {
    const statusMap: Record<string, { label: string; color: 'default' | 'primary' | 'secondary' | 'error' | 'info' | 'success' | 'warning' }> = {
      scheduled: { label: 'Agendado', color: 'info' },
      confirmed: { label: 'Confirmado', color: 'success' },
      completed: { label: 'Concluído', color: 'secondary' },
      cancelled: { label: 'Cancelado', color: 'error' },
      no_show: { label: 'Não Compareceu', color: 'warning' },
    };

    const statusInfo = statusMap[status] || { label: status, color: 'default' };
    
    return (
      <Chip 
        label={statusInfo.label} 
        color={statusInfo.color} 
        size="small"
        variant="outlined"
      />
    );
  };

  if (isLoading) return <Loading />;
  if (error) return <ErrorAlert message="Erro ao carregar agendamentos" onRetry={refetch} />;

  return (
    <Page 
      title="Agendamentos"
      subtitle="Gerencie os agendamentos do salão"
      actions={[
        <Button
          key="new-appointment"
          variant="contained"
          color="primary"
          startIcon={<AddIcon />}
          onClick={() => navigate('/appointments/new')}
        >
          Novo Agendamento
        </Button>,
        <Button
          key="calendar-view"
          variant="outlined"
          startIcon={<CalendarIcon />}
          onClick={() => navigate('/appointments/calendar')}
          sx={{ ml: 2 }}
        >
          Visualizar Calendário
        </Button>
      ]}
    >
      <Card>
        <CardContent>
          <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
            <Box display="flex" alignItems="center" width={400}>
              <TextField
                fullWidth
                variant="outlined"
                size="small"
                placeholder="Buscar por cliente, profissional..."
                value={searchTerm}
                onChange={handleSearch}
                InputProps={{
                  startAdornment: <SearchIcon color="action" sx={{ mr: 1 }} />,
                }}
              />
              <Tooltip title="Filtrar">
                <IconButton onClick={handleFilterClick}>
                  <FilterListIcon />
                </IconButton>
              </Tooltip>
              <Menu
                anchorEl={anchorEl}
                open={Boolean(anchorEl)}
                onClose={handleFilterClose}
              >
                <MenuItem 
                  selected={statusFilter === 'all'}
                  onClick={() => handleStatusFilter('all')}
                >
                  Todos os Status
                </MenuItem>
                <MenuItem 
                  selected={statusFilter === 'scheduled'}
                  onClick={() => handleStatusFilter('scheduled')}
                >
                  Agendados
                </MenuItem>
                <MenuItem 
                  selected={statusFilter === 'confirmed'}
                  onClick={() => handleStatusFilter('confirmed')}
                >
                  Confirmados
                </MenuItem>
                <MenuItem 
                  selected={statusFilter === 'completed'}
                  onClick={() => handleStatusFilter('completed')}
                >
                  Concluídos
                </MenuItem>
                <MenuItem 
                  selected={statusFilter === 'cancelled'}
                  onClick={() => handleStatusFilter('cancelled')}
                >
                  Cancelados
                </MenuItem>
              </Menu>
            </Box>
          </Box>

          
          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Cliente</TableCell>
                  <TableCell>Serviço</TableCell>
                  <TableCell>Profissional</TableCell>
                  <TableCell>Data/Hora</TableCell>
                  <TableCell>Duração</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell align="right">Ações</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {appointmentsData?.data.map((appointment: Appointment) => (
                  <TableRow key={appointment.id} hover>
                    <TableCell>
                      <Typography variant="body2">{appointment.client.name}</Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2">{appointment.service.name}</Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2">{appointment.professional.name}</Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2">
                        {format(parseISO(appointment.startTime), "dd/MM/yyyy HH:mm", { locale: ptBR })}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2">{appointment.duration} min</Typography>
                    </TableCell>
                    <TableCell>
                      {getStatusChip(appointment.status)}
                    </TableCell>
                    <TableCell align="right">
                      <IconButton 
                        size="small"
                        onClick={() => navigate(`/appointments/${appointment.id}`)}
                      >
                        <MoreVertIcon />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
          
          <TablePagination
            rowsPerPageOptions={[5, 10, 25]}
            component="div"
            count={appointmentsData?.total || 0}
            rowsPerPage={rowsPerPage}
            page={page}
            onPageChange={handleChangePage}
            onRowsPerPageChange={handleChangeRowsPerPage}
            labelRowsPerPage="Itens por página:"
            labelDisplayedRows={({ from, to, count }) => 
              `${from}-${to} de ${count !== -1 ? count : `mais que ${to}`}`}
          />
        </CardContent>
      </Card>
    </Page>
  );
};

export default Appointments;
