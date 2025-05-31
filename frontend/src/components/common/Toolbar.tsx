import React, { ReactNode } from 'react';
import { Box, Button, TextField, InputAdornment, useTheme } from '@mui/material';
import { Search as SearchIcon, FilterList as FilterListIcon } from '@mui/icons-material';

export interface ToolbarAction {
  label: string;
  icon?: ReactNode;
  onClick: () => void;
  color?: 'inherit' | 'primary' | 'secondary' | 'success' | 'error' | 'info' | 'warning';
  variant?: 'text' | 'outlined' | 'contained';
  disabled?: boolean;
}

interface ToolbarProps {
  title?: string;
  searchPlaceholder?: string;
  searchValue?: string;
  onSearchChange?: (value: string) => void;
  actions?: ToolbarAction[];
  showFilters?: boolean;
  onFilterClick?: () => void;
  children?: ReactNode;
  sx?: object;
}

const Toolbar: React.FC<ToolbarProps> = ({
  title,
  searchPlaceholder = 'Pesquisar...',
  searchValue = '',
  onSearchChange,
  actions = [],
  showFilters = false,
  onFilterClick,
  children,
  sx = {},
}) => {
  const theme = useTheme();

  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (onSearchChange) {
      onSearchChange(e.target.value);
    }
  };

  return (
    <Box
      sx={{
        display: 'flex',
        flexDirection: { xs: 'column', sm: 'row' },
        alignItems: { xs: 'stretch', sm: 'center' },
        justifyContent: 'space-between',
        gap: 2,
        mb: 3,
        ...sx,
      }}
    >
      {title && (
        <Typography variant="h6" component="h2" sx={{ fontWeight: 600 }}>
          {title}
        </Typography>
      )}

      <Box
        sx={{
          display: 'flex',
          gap: 2,
          flex: 1,
          maxWidth: { xs: '100%', sm: 600 },
          width: '100%',
        }}
      >
        {onSearchChange && (
          <TextField
            placeholder={searchPlaceholder}
            variant="outlined"
            size="small"
            fullWidth
            value={searchValue}
            onChange={handleSearchChange}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon color="action" />
                </InputAdornment>
              ),
            }}
            sx={{
              backgroundColor: theme.palette.background.paper,
              borderRadius: 1,
              '& .MuiOutlinedInput-root': {
                borderRadius: 1,
              },
            }}
          />
        )}

        {showFilters && onFilterClick && (
          <Button
            variant="outlined"
            startIcon={<FilterListIcon />}
            onClick={onFilterClick}
            sx={{
              whiteSpace: 'nowrap',
              minWidth: 'auto',
              px: 2,
              display: { xs: 'none', sm: 'inline-flex' },
            }}
          >
            Filtros
          </Button>
        )}
      </Box>

      {(actions.length > 0 || children) && (
        <Box
          sx={{
            display: 'flex',
            gap: 1,
            flexWrap: 'wrap',
            justifyContent: { xs: 'flex-start', sm: 'flex-end' },
            '& > *': {
              flexShrink: 0,
            },
          }}
        >
          {actions.map((action, index) => (
            <Button
              key={index}
              variant={action.variant || 'contained'}
              color={action.color || 'primary'}
              startIcon={action.icon}
              onClick={action.onClick}
              disabled={action.disabled}
              sx={{
                whiteSpace: 'nowrap',
                minWidth: 'auto',
              }}
            >
              {action.label}
            </Button>
          ))}
          {children}
        </Box>
      )}
    </Box>
  );
};

export default Toolbar;
