import React from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import {
  Box,
  Typography,
  Paper,
  Button,
  Divider,
  Chip,
  Grid,
  Card,
  CardContent,
  CardHeader,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Avatar,
  IconButton,
  useTheme,
} from '@mui/material';
import {
  Edit as EditIcon,
  ArrowBack as ArrowBackIcon,
  Person as PersonIcon,
  Email as EmailIcon,
  Phone as PhoneIcon,
  Cake as CakeIcon,
  Home as HomeIcon,
  Notes as NotesIcon,
  Event as EventIcon,
} from '@mui/icons-material';
import { format, parseISO } from 'date-fns';
import { ptBR } from 'date-fns/locale';
import { useQuery } from '@tanstack/react-query';
import { getClientById } from '../../../services/clientService';

const ClientDetails: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const theme = useTheme();

  const { data: client, isLoading, isError } = useQuery(
    ['client', id],
    () => getClientById(Number(id)),
    { enabled: !!id, staleTime: 0 }
  );

  if (isLoading) {
    return <div>Carregando...</div>;
  }

  if (isError || !client) {
    return <div>Erro ao carregar os dados do cliente</div>;
  }

  const formatDate = (dateString?: string) => {
    if (!dateString) return 'Não informado';
    return format(parseISO(dateString), 'dd/MM/yyyy', { locale: ptBR });
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 3, gap: 2 }}>
        <IconButton onClick={() => navigate(-1)}>
          <ArrowBackIcon />
        </IconButton>
        <Box sx={{ flexGrow: 1 }}>
          <Typography variant="h4" component="h1">
            {client.name}
            <Chip
              label={client.active ? 'Ativo' : 'Inativo'}
              color={client.active ? 'success' : 'default'}
              size="small"
              sx={{ ml: 2, verticalAlign: 'middle' }}
            />
          </Typography>
          <Typography variant="subtitle1" color="text.secondary">
            Cliente desde {formatDate(client.createdAt)}
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<EditIcon />}
          onClick={() => navigate(`/clients/${id}/edit`)}
        >
          Editar
        </Button>
      </Box>

      <Grid container spacing={3}>
        <Grid item xs={12} md={8}>
          <Card>
            <CardHeader title="Informações Pessoais" />
            <Divider />
            <CardContent>
              <List>
                <ListItem>
                  <ListItemIcon>
                    <PersonIcon />
                  </ListItemIcon>
                  <ListItemText 
                    primary="CPF" 
                    secondary={client.cpf || 'Não informado'} 
                  />
                </ListItem>
                <Divider component="li" />
                
                <ListItem>
                  <ListItemIcon>
                    <EmailIcon />
                  </ListItemIcon>
                  <ListItemText 
                    primary="E-mail" 
                    secondary={
                      <Link to={`mailto:${client.email}`} style={{ color: theme.palette.primary.main }}>
                        {client.email}
                      </Link>
                    } 
                  />
                </ListItem>
                <Divider component="li" />
                
                <ListItem>
                  <ListItemIcon>
                    <PhoneIcon />
                  </ListItemIcon>
                  <ListItemText 
                    primary="Telefone" 
                    secondary={
                      <Link to={`tel:${client.phone}`} style={{ color: theme.palette.primary.main }}>
                        {client.phone}
                      </Link>
                    } 
                  />
                </ListItem>
                <Divider component="li" />
                
                <ListItem>
                  <ListItemIcon>
                    <CakeIcon />
                  </ListItemIcon>
                  <ListItemText 
                    primary="Data de Nascimento" 
                    secondary={client.birthDate ? formatDate(client.birthDate) : 'Não informada'} 
                  />
                </ListItem>
                
                {client.address && (
                  <>
                    <Divider component="li" />
                    <ListItem>
                      <ListItemIcon>
                        <HomeIcon />
                      </ListItemIcon>
                      <ListItemText 
                        primary="Endereço" 
                        secondary={client.address} 
                      />
                    </ListItem>
                  </>
                )}
                
                {client.notes && (
                  <>
                    <Divider component="li" />
                    <ListItem>
                      <ListItemIcon>
                        <NotesIcon />
                      </ListItemIcon>
                      <ListItemText 
                        primary="Observações" 
                        secondary={client.notes} 
                      />
                    </ListItem>
                  </>
                )}
              </List>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid item xs={12} md={4}>
          <Card>
            <CardHeader title="Atividades Recentes" />
            <Divider />
            <CardContent>
              <List>
                <ListItem>
                  <ListItemIcon>
                    <EventIcon color="action" />
                  </ListItemIcon>
                  <ListItemText 
                    primary="Cadastrado em" 
                    secondary={format(parseISO(client.createdAt), "dd 'de' MMMM 'de' yyyy 'às' HH:mm", { locale: ptBR })} 
                  />
                </ListItem>
                <Divider component="li" />
                <ListItem>
                  <ListItemIcon>
                    <EventIcon color="action" />
                  </ListItemIcon>
                  <ListItemText 
                    primary="Última atualização" 
                    secondary={format(parseISO(client.updatedAt), "dd 'de' MMMM 'de' yyyy 'às' HH:mm", { locale: ptBR })} 
                  />
                </ListItem>
              </List>
            </CardContent>
          </Card>
          
          <Box sx={{ mt: 3, display: 'flex', gap: 2, flexDirection: 'column' }}>
            <Button
              variant="outlined"
              fullWidth
              onClick={() => navigate(`/clients/${id}/edit`)}
              startIcon={<EditIcon />}
            >
              Editar Dados
            </Button>
            <Button
              variant="outlined"
              fullWidth
              color={client.active ? 'error' : 'success'}
              // Aqui você implementaria a lógica para ativar/desativar o cliente
              // onClick={handleToggleStatus}
              disabled
            >
              {client.active ? 'Desativar Cliente' : 'Ativar Cliente'}
            </Button>
          </Box>
        </Grid>
      </Grid>
    </Box>
  );
};

export default ClientDetails;
