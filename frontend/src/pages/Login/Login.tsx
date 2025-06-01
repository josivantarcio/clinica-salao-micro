import React, { useState } from 'react';
import { 
  Box, Button, Container, TextField, Typography, Paper, Avatar,
  Grid, Link, InputAdornment, IconButton, CircularProgress
} from '@mui/material';
import { LockOutlined, Visibility, VisibilityOff } from '@mui/icons-material';
import { useAuth } from '../../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
import { useSnackbar } from 'notistack';
import axios from 'axios';

const Login: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();
  const { enqueueSnackbar } = useSnackbar();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);

    try {
      // Em produção, este seria o endpoint real do auth-service
      const response = await axios.post('http://localhost:8080/api/v1/auth/login', {
        email,
        password
      });

      // Para fins de desenvolvimento, vamos simular uma resposta bem-sucedida
      // const response = { data: { token: 'simulated-jwt-token' } };

      login(response.data.token);
      navigate('/dashboard');
      enqueueSnackbar('Login realizado com sucesso!', { variant: 'success' });
    } catch (error) {
      console.error('Erro ao fazer login:', error);
      enqueueSnackbar('Email ou senha inválidos. Tente novamente.', { variant: 'error' });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Container component="main" maxWidth="xs">
      <Paper
        elevation={3}
        sx={{
          marginTop: 8,
          padding: 4,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          borderRadius: 2
        }}
      >
        <Avatar sx={{ m: 1, bgcolor: 'primary.main' }}>
          <LockOutlined />
        </Avatar>
        <Typography component="h1" variant="h5">
          ClinicaSalão
        </Typography>
        <Typography variant="subtitle1" color="text.secondary" sx={{ mt: 1 }}>
          Sistema de Gestão
        </Typography>
        
        <Box component="form" onSubmit={handleSubmit} sx={{ mt: 3, width: '100%' }}>
          <TextField
            margin="normal"
            required
            fullWidth
            id="email"
            label="Email"
            name="email"
            autoComplete="email"
            autoFocus
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            sx={{ mb: 2 }}
          />
          <TextField
            margin="normal"
            required
            fullWidth
            name="password"
            label="Senha"
            type={showPassword ? 'text' : 'password'}
            id="password"
            autoComplete="current-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            InputProps={{
              endAdornment: (
                <InputAdornment position="end">
                  <IconButton
                    aria-label="toggle password visibility"
                    onClick={() => setShowPassword(!showPassword)}
                    edge="end"
                  >
                    {showPassword ? <VisibilityOff /> : <Visibility />}
                  </IconButton>
                </InputAdornment>
              )
            }}
          />
          
          <Button
            type="submit"
            fullWidth
            variant="contained"
            sx={{ mt: 3, mb: 2, py: 1.2 }}
            disabled={isLoading}
          >
            {isLoading ? <CircularProgress size={24} /> : 'Entrar'}
          </Button>
          
          <Grid container justifyContent="flex-end">
            <Grid item>
              <Link href="#" variant="body2">
                Esqueceu a senha?
              </Link>
            </Grid>
          </Grid>
        </Box>
      </Paper>
    </Container>
  );
};

export default Login;
