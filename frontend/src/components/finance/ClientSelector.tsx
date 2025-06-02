import React, { useState, useEffect } from 'react';
import { 
  Autocomplete, 
  TextField, 
  CircularProgress, 
  Avatar, 
  Typography, 
  Box,
  Chip,
  debounce
} from '@mui/material';
import { getClientsForSelector } from '../../services/clientService';
import PersonIcon from '@mui/icons-material/Person';

interface ClientOption {
  id: number;
  name: string;
  email: string;
}

interface ClientSelectorProps {
  value: ClientOption | null;
  onChange: (client: ClientOption | null) => void;
  error?: boolean;
  helperText?: string;
  required?: boolean;
  disabled?: boolean;
  label?: string;
}

const ClientSelector: React.FC<ClientSelectorProps> = ({
  value,
  onChange,
  error = false,
  helperText,
  required = false,
  disabled = false,
  label = "Cliente"
}) => {
  const [inputValue, setInputValue] = useState('');
  const [options, setOptions] = useState<ClientOption[]>([]);
  const [loading, setLoading] = useState(false);
  const [open, setOpen] = useState(false);

  // Função para carregar as opções debounced
  const loadOptions = React.useMemo(
    () =>
      debounce(async (searchTerm: string) => {
        setLoading(true);
        try {
          const clients = await getClientsForSelector(searchTerm);
          setOptions(clients);
        } catch (error) {
          console.error('Erro ao buscar clientes:', error);
        } finally {
          setLoading(false);
        }
      }, 500),
    []
  );

  // Efeito para carregar opções quando o input muda
  useEffect(() => {
    if (open) {
      loadOptions(inputValue);
    }
  }, [inputValue, open, loadOptions]);

  // Efeito para carregar opções iniciais quando o componente é montado
  useEffect(() => {
    if (open && options.length === 0 && !loading) {
      loadOptions('');
    }
  }, [open, options.length, loading, loadOptions]);

  return (
    <Autocomplete
      id="client-selector"
      open={open}
      onOpen={() => setOpen(true)}
      onClose={() => setOpen(false)}
      value={value}
      onChange={(event, newValue) => {
        onChange(newValue);
      }}
      inputValue={inputValue}
      onInputChange={(event, newInputValue) => {
        setInputValue(newInputValue);
      }}
      options={options}
      getOptionLabel={(option) => option.name}
      isOptionEqualToValue={(option, value) => option.id === value.id}
      loading={loading}
      disabled={disabled}
      fullWidth
      filterOptions={(x) => x} // Não filtra localmente, já que filtramos no servidor
      renderInput={(params) => (
        <TextField
          {...params}
          label={label}
          required={required}
          error={error}
          helperText={helperText}
          InputProps={{
            ...params.InputProps,
            endAdornment: (
              <React.Fragment>
                {loading ? <CircularProgress color="inherit" size={20} /> : null}
                {params.InputProps.endAdornment}
              </React.Fragment>
            ),
          }}
        />
      )}
      renderOption={(props, option) => (
        <li {...props}>
          <Box sx={{ display: 'flex', alignItems: 'center', width: '100%' }}>
            <Avatar sx={{ bgcolor: 'primary.main', mr: 2, width: 32, height: 32 }}>
              <PersonIcon fontSize="small" />
            </Avatar>
            <Box sx={{ flexGrow: 1, overflow: 'hidden' }}>
              <Typography variant="body1" sx={{ fontWeight: 'medium' }}>
                {option.name}
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ textOverflow: 'ellipsis', overflow: 'hidden' }}>
                {option.email}
              </Typography>
            </Box>
          </Box>
        </li>
      )}
      noOptionsText="Nenhum cliente encontrado"
      loadingText="Carregando..."
      renderTags={(value, getTagProps) => 
        value.map((option, index) => (
          <Chip
            avatar={<Avatar><PersonIcon /></Avatar>}
            label={option.name}
            {...getTagProps({ index })}
          />
        ))
      }
    />
  );
};

export default ClientSelector;
