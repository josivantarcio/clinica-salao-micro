import React from 'react';
import { Alert, AlertTitle, Box, Button, Typography } from '@mui/material';
import { Refresh as RefreshIcon } from '@mui/icons-material';

interface ErrorAlertProps {
  error?: Error | string | null;
  onRetry?: () => void;
  title?: string;
  showIcon?: boolean;
  fullWidth?: boolean;
  sx?: object;
}

const ErrorAlert: React.FC<ErrorAlertProps> = ({
  error,
  onRetry,
  title = 'Ocorreu um erro',
  showIcon = true,
  fullWidth = true,
  sx = {},
}) => {
  if (!error) return null;

  const errorMessage = typeof error === 'string' ? error : error.message || 'Ocorreu um erro inesperado';

  return (
    <Box 
      sx={{
        width: fullWidth ? '100%' : 'auto',
        ...sx,
      }}
    >
      <Alert
        severity="error"
        sx={{
          '& .MuiAlert-message': {
            width: '100%',
          },
        }}
        icon={showIcon ? undefined : false}
      >
        <Box>
          <AlertTitle>{title}</AlertTitle>
          <Typography variant="body2" component="div">
            {errorMessage}
          </Typography>
          {onRetry && (
            <Box sx={{ mt: 2, textAlign: 'right' }}>
              <Button
                color="error"
                size="small"
                onClick={onRetry}
                startIcon={<RefreshIcon />}
                variant="outlined"
              >
                Tentar novamente
              </Button>
            </Box>
          )}
        </Box>
      </Alert>
    </Box>
  );
};

export default ErrorAlert;
