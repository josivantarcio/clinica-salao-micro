import React, { useState } from 'react';
import {
  Box,
  Button,
  Card,
  CardContent,
  Collapse,
  Divider,
  FormControl,
  Grid,
  IconButton,
  InputAdornment,
  InputLabel,
  MenuItem,
  Select,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material';
import {
  FilterAlt as FilterIcon,
  ExpandMore as ExpandMoreIcon,
  ExpandLess as ExpandLessIcon,
  DateRange as DateRangeIcon,
  Clear as ClearIcon,
  SearchOutlined as SearchIcon,
} from '@mui/icons-material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { ptBR } from 'date-fns/locale';
import ClientSelector from './ClientSelector';

export interface TransactionFilters {
  searchTerm?: string;
  status?: string[];
  paymentMethod?: string[];
  startDate?: Date | null;
  endDate?: Date | null;
  minAmount?: number | null;
  maxAmount?: number | null;
  clientId?: number | null;
}

interface TransactionFiltersProps {
  filters: TransactionFilters;
  onFilterChange: (filters: TransactionFilters) => void;
  onResetFilters: () => void;
}

const PAYMENT_METHODS = [
  { value: 'CREDIT_CARD', label: 'Cartão de Crédito' },
  { value: 'DEBIT_CARD', label: 'Cartão de Débito' },
  { value: 'CASH', label: 'Dinheiro' },
  { value: 'PIX', label: 'PIX' },
  { value: 'BANK_SLIP', label: 'Boleto Bancário' },
];

const TRANSACTION_STATUS = [
  { value: 'PAID', label: 'Pago' },
  { value: 'PENDING', label: 'Pendente' },
  { value: 'REFUNDED', label: 'Reembolsado' },
  { value: 'CANCELLED', label: 'Cancelado' },
];

const TransactionFilters: React.FC<TransactionFiltersProps> = ({
  filters,
  onFilterChange,
  onResetFilters,
}) => {
  const [expanded, setExpanded] = useState(false);
  const [selectedClient, setSelectedClient] = useState<{ id: number; name: string; email: string } | null>(
    filters.clientId ? { id: filters.clientId, name: '', email: '' } : null
  );

  const handleChange = (key: keyof TransactionFilters, value: any) => {
    onFilterChange({ ...filters, [key]: value });
  };

  const handleClientChange = (client: { id: number; name: string; email: string } | null) => {
    setSelectedClient(client);
    handleChange('clientId', client?.id || null);
  };

  const handleToggleExpand = () => {
    setExpanded(!expanded);
  };

  const handleResetFilters = () => {
    setSelectedClient(null);
    onResetFilters();
  };

  return (
    <Card variant="outlined" sx={{ mb: 3 }}>
      <CardContent sx={{ pb: 1 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h6" sx={{ display: 'flex', alignItems: 'center' }}>
            <FilterIcon sx={{ mr: 1 }} />
            Filtros
          </Typography>
          <IconButton onClick={handleToggleExpand} size="small">
            {expanded ? <ExpandLessIcon /> : <ExpandMoreIcon />}
          </IconButton>
        </Box>

        {/* Barra de pesquisa básica sempre visível */}
        <Box sx={{ mb: 2 }}>
          <TextField
            fullWidth
            placeholder="Pesquisar por descrição, referência ou código..."
            value={filters.searchTerm || ''}
            onChange={(e) => handleChange('searchTerm', e.target.value)}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon />
                </InputAdornment>
              ),
              endAdornment: filters.searchTerm ? (
                <InputAdornment position="end">
                  <IconButton
                    size="small"
                    onClick={() => handleChange('searchTerm', '')}
                    edge="end"
                  >
                    <ClearIcon fontSize="small" />
                  </IconButton>
                </InputAdornment>
              ) : null,
            }}
            size="small"
          />
        </Box>

        {/* Filtros avançados colapsáveis */}
        <Collapse in={expanded}>
          <Grid container spacing={2}>
            {/* Status */}
            <Grid item xs={12} md={6}>
              <FormControl fullWidth size="small">
                <InputLabel>Status</InputLabel>
                <Select
                  multiple
                  value={filters.status || []}
                  onChange={(e) => handleChange('status', e.target.value)}
                  label="Status"
                  renderValue={(selected) => (
                    <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                      {(selected as string[]).map((value) => (
                        <Typography variant="body2" key={value} sx={{ mr: 1 }}>
                          {TRANSACTION_STATUS.find((status) => status.value === value)?.label || value}
                        </Typography>
                      ))}
                    </Box>
                  )}
                >
                  {TRANSACTION_STATUS.map((status) => (
                    <MenuItem key={status.value} value={status.value}>
                      {status.label}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>

            {/* Método de Pagamento */}
            <Grid item xs={12} md={6}>
              <FormControl fullWidth size="small">
                <InputLabel>Método de Pagamento</InputLabel>
                <Select
                  multiple
                  value={filters.paymentMethod || []}
                  onChange={(e) => handleChange('paymentMethod', e.target.value)}
                  label="Método de Pagamento"
                  renderValue={(selected) => (
                    <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                      {(selected as string[]).map((value) => (
                        <Typography variant="body2" key={value} sx={{ mr: 1 }}>
                          {PAYMENT_METHODS.find((method) => method.value === value)?.label || value}
                        </Typography>
                      ))}
                    </Box>
                  )}
                >
                  {PAYMENT_METHODS.map((method) => (
                    <MenuItem key={method.value} value={method.value}>
                      {method.label}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>

            {/* Cliente */}
            <Grid item xs={12} md={6}>
              <ClientSelector
                value={selectedClient}
                onChange={handleClientChange}
                label="Filtrar por Cliente"
              />
            </Grid>

            {/* Valor Mínimo e Máximo */}
            <Grid item xs={12} md={6}>
              <Grid container spacing={1}>
                <Grid item xs={6}>
                  <TextField
                    fullWidth
                    label="Valor Mínimo"
                    type="number"
                    size="small"
                    value={filters.minAmount || ''}
                    onChange={(e) => handleChange('minAmount', e.target.value ? Number(e.target.value) : null)}
                    InputProps={{
                      startAdornment: <InputAdornment position="start">R$</InputAdornment>,
                    }}
                  />
                </Grid>
                <Grid item xs={6}>
                  <TextField
                    fullWidth
                    label="Valor Máximo"
                    type="number"
                    size="small"
                    value={filters.maxAmount || ''}
                    onChange={(e) => handleChange('maxAmount', e.target.value ? Number(e.target.value) : null)}
                    InputProps={{
                      startAdornment: <InputAdornment position="start">R$</InputAdornment>,
                    }}
                  />
                </Grid>
              </Grid>
            </Grid>

            {/* Data Inicial e Final */}
            <Grid item xs={12}>
              <LocalizationProvider dateAdapter={AdapterDateFns} adapterLocale={ptBR}>
                <Grid container spacing={2}>
                  <Grid item xs={6}>
                    <DatePicker
                      label="Data Inicial"
                      value={filters.startDate}
                      onChange={(date) => handleChange('startDate', date)}
                      slotProps={{
                        textField: { 
                          fullWidth: true, 
                          size: "small",
                          InputProps: {
                            startAdornment: (
                              <InputAdornment position="start">
                                <DateRangeIcon fontSize="small" />
                              </InputAdornment>
                            ),
                          }
                        }
                      }}
                    />
                  </Grid>
                  <Grid item xs={6}>
                    <DatePicker
                      label="Data Final"
                      value={filters.endDate}
                      onChange={(date) => handleChange('endDate', date)}
                      slotProps={{
                        textField: { 
                          fullWidth: true, 
                          size: "small",
                          InputProps: {
                            startAdornment: (
                              <InputAdornment position="start">
                                <DateRangeIcon fontSize="small" />
                              </InputAdornment>
                            ),
                          }
                        }
                      }}
                    />
                  </Grid>
                </Grid>
              </LocalizationProvider>
            </Grid>
          </Grid>

          <Divider sx={{ my: 2 }} />

          {/* Botões de ação */}
          <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 1 }}>
            <Button variant="outlined" onClick={handleResetFilters}>
              Limpar Filtros
            </Button>
            <Button
              variant="contained"
              startIcon={<FilterIcon />}
              onClick={handleToggleExpand}
            >
              Aplicar Filtros
            </Button>
          </Box>
        </Collapse>

        {/* Chips com filtros ativos */}
        {!expanded && (
          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, mt: 1 }}>
            {filters.status && filters.status.length > 0 && (
              <Tooltip title="Status">
                <Typography variant="caption" sx={{ bgcolor: 'background.paper', p: 0.5, borderRadius: 1, display: 'flex', alignItems: 'center' }}>
                  Status: {filters.status.map(s => TRANSACTION_STATUS.find(status => status.value === s)?.label).join(', ')}
                </Typography>
              </Tooltip>
            )}
            
            {filters.paymentMethod && filters.paymentMethod.length > 0 && (
              <Tooltip title="Método de Pagamento">
                <Typography variant="caption" sx={{ bgcolor: 'background.paper', p: 0.5, borderRadius: 1, display: 'flex', alignItems: 'center' }}>
                  Pagamento: {filters.paymentMethod.map(m => PAYMENT_METHODS.find(method => method.value === m)?.label).join(', ')}
                </Typography>
              </Tooltip>
            )}
            
            {filters.startDate && (
              <Tooltip title="Data Inicial">
                <Typography variant="caption" sx={{ bgcolor: 'background.paper', p: 0.5, borderRadius: 1, display: 'flex', alignItems: 'center' }}>
                  De: {filters.startDate.toLocaleDateString()}
                </Typography>
              </Tooltip>
            )}
            
            {filters.endDate && (
              <Tooltip title="Data Final">
                <Typography variant="caption" sx={{ bgcolor: 'background.paper', p: 0.5, borderRadius: 1, display: 'flex', alignItems: 'center' }}>
                  Até: {filters.endDate.toLocaleDateString()}
                </Typography>
              </Tooltip>
            )}
            
            {filters.minAmount && (
              <Tooltip title="Valor Mínimo">
                <Typography variant="caption" sx={{ bgcolor: 'background.paper', p: 0.5, borderRadius: 1, display: 'flex', alignItems: 'center' }}>
                  Min: R$ {filters.minAmount}
                </Typography>
              </Tooltip>
            )}
            
            {filters.maxAmount && (
              <Tooltip title="Valor Máximo">
                <Typography variant="caption" sx={{ bgcolor: 'background.paper', p: 0.5, borderRadius: 1, display: 'flex', alignItems: 'center' }}>
                  Max: R$ {filters.maxAmount}
                </Typography>
              </Tooltip>
            )}
            
            {selectedClient && (
              <Tooltip title="Cliente">
                <Typography variant="caption" sx={{ bgcolor: 'background.paper', p: 0.5, borderRadius: 1, display: 'flex', alignItems: 'center' }}>
                  Cliente: {selectedClient.name}
                </Typography>
              </Tooltip>
            )}
            
            {(filters.status?.length || filters.paymentMethod?.length || filters.startDate || 
              filters.endDate || filters.minAmount || filters.maxAmount || selectedClient) && (
              <Button 
                size="small" 
                variant="text" 
                color="primary" 
                onClick={handleResetFilters}
                sx={{ ml: 'auto', p: 0, minWidth: 'auto' }}
              >
                Limpar todos
              </Button>
            )}
          </Box>
        )}
      </CardContent>
    </Card>
  );
};

export default TransactionFilters;
