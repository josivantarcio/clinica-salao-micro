import React from 'react';
import { Box, Container, Typography, Button, Paper } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import ErrorOutlineIcon from '@mui/icons-material/ErrorOutline';
import KeyboardBackspaceIcon from '@mui/icons-material/KeyboardBackspace';
import { useAuth } from '../contexts/AuthContext';

const Unauthorized: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuth();

  return (
    <Container maxWidth="md">
      <Paper 
        elevation={3} 
        sx={{ 
          p: 4, 
          mt: 8, 
          textAlign: 'center',
          borderTop: '4px solid #f44336'
        }}
      >
        <ErrorOutlineIcon color="error" sx={{ fontSize: 80, mb: 2 }} />
        
        <Typography variant="h4" gutterBottom>
          Acesso Não Autorizado
        </Typography>
        
        <Typography variant="body1" paragraph sx={{ mb: 3 }}>
          Você não possui permissões suficientes para acessar esta página.
        </Typography>
        
        {user && (
          <Box sx={{ mb: 3 }}>
            <Typography variant="body2" color="textSecondary">
              Usuário: {user.name}
            </Typography>
            <Typography variant="body2" color="textSecondary">
              Perfis disponíveis: {user.roles.join(', ') || 'Nenhum perfil atribuído'}
            </Typography>
          </Box>
        )}
        
        <Typography variant="body2" color="textSecondary" paragraph>
          Se você acredita que deveria ter acesso a esta página, 
          entre em contato com o administrador do sistema.
        </Typography>
        
        <Box sx={{ mt: 4 }}>
          <Button
            variant="contained"
            color="primary"
            startIcon={<KeyboardBackspaceIcon />}
            onClick={() => navigate(-1)}
            sx={{ mr: 2 }}
          >
            Voltar
          </Button>
          
          <Button
            variant="outlined"
            onClick={() => navigate('/dashboard')}
          >
            Ir para o Dashboard
          </Button>
        </Box>
      </Paper>
    </Container>
  );
};

export default Unauthorized;
