import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  AppBar,
  Toolbar,
  IconButton,
  Typography,
  Box,
  Avatar,
  Menu,
  MenuItem,
  Divider,
  ListItemIcon,
  ListItemText,
  Tooltip,
  Badge,
  useTheme,
  useMediaQuery,
  alpha,
} from '@mui/material';
import {
  Menu as MenuIcon,
  Notifications as NotificationsIcon,
  Settings as SettingsIcon,
  Logout as LogoutIcon,
  Person as PersonIcon,
  Brightness4 as DarkModeIcon,
  Brightness7 as LightModeIcon,
  Search as SearchIcon,
} from '@mui/icons-material';
import { useAuth } from '../../../contexts/AuthContext';

interface HeaderProps {
  onMenuClick: () => void;
  onThemeToggle: () => void;
  isDarkMode: boolean;
}

const Header: React.FC<HeaderProps> = ({ onMenuClick, onThemeToggle, isDarkMode }) => {
  const theme = useTheme();
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  
  // Estados para os menus suspensos
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [notificationsAnchorEl, setNotificationsAnchorEl] = useState<null | HTMLElement>(null);
  
  // Contagem de notificações não lidas
  const notificationCount = 3; // Substitua por um estado real do seu aplicativo

  const handleProfileMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleNotificationsMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setNotificationsAnchorEl(event.currentTarget);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
  };

  const handleNotificationsClose = () => {
    setNotificationsAnchorEl(null);
  };

  const handleLogout = () => {
    handleMenuClose();
    logout();
    navigate('/login');
  };

  const handleProfile = () => {
    handleMenuClose();
    navigate('/profile');
  };

  const handleSettings = () => {
    handleMenuClose();
    navigate('/settings');
  };

  // Dados de notificações de exemplo
  const notifications = [
    { id: 1, text: 'Novo agendamento confirmado', time: 'Há 10 minutos', read: false },
    { id: 2, text: 'Lembrete: Consulta amanhã às 14h', time: 'Há 2 horas', read: false },
    { id: 3, text: 'Promoção especial para clientes', time: 'Ontem', read: true },
  ];

  const unreadCount = notifications.filter(n => !n.read).length;

  return (
    <AppBar
      position="fixed"
      sx={{
        zIndex: theme.zIndex.drawer + 1,
        backdropFilter: 'blur(8px)',
        backgroundColor: alpha(theme.palette.background.paper, 0.8),
        color: theme.palette.text.primary,
        boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
        borderBottom: `1px solid ${theme.palette.divider}`,
      }}
      elevation={0}
    >
      <Toolbar>
        <IconButton
          color="inherit"
          aria-label="abrir menu"
          edge="start"
          onClick={onMenuClick}
          sx={{ mr: 2, display: { md: 'none' } }}
        >
          <MenuIcon />
        </IconButton>

        {/* Logo ou título */}
        <Typography
          variant="h6"
          noWrap
          component="div"
          sx={{
            display: { xs: 'none', sm: 'block' },
            fontWeight: 700,
            background: `linear-gradient(45deg, ${theme.palette.primary.main} 30%, ${theme.palette.secondary.main} 90%})`,
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
            backgroundClip: 'text',
            textFillColor: 'transparent',
            mr: 2,
          }}
        >
          ClinicaSalao
        </Typography>

        <Box sx={{ flexGrow: 1 }} />

        {/* Barra de pesquisa (apenas desktop) */}
        <Box
          sx={{
            display: { xs: 'none', md: 'flex' },
            alignItems: 'center',
            position: 'relative',
            borderRadius: theme.shape.borderRadius,
            backgroundColor: alpha(theme.palette.action.hover, 0.1),
            '&:hover': {
              backgroundColor: alpha(theme.palette.action.hover, 0.15),
            },
            marginRight: 2,
            width: '100%',
            maxWidth: 400,
          }}
        >
          <Box
            sx={{
              padding: theme.spacing(0, 2),
              height: '100%',
              position: 'absolute',
              pointerEvents: 'none',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <SearchIcon color="action" />
          </Box>
          <input
            type="text"
            placeholder="Pesquisar..."
            style={{
              font: 'inherit',
              color: 'currentColor',
              padding: theme.spacing(1, 1, 1, 6),
              border: 'none',
              background: 'transparent',
              width: '100%',
              '&:focus': {
                outline: 'none',
              },
            }}
          />
        </Box>

        {/* Botão de alternar tema */}
        <Tooltip title={isDarkMode ? 'Modo claro' : 'Modo escuro'}>
          <IconButton
            onClick={onThemeToggle}
            color="inherit"
            sx={{ mr: 1 }}
            aria-label="alternar tema"
          >
            {isDarkMode ? <LightModeIcon /> : <DarkModeIcon />}
          </IconButton>
        </Tooltip>

        {/* Notificações */}
        <Tooltip title="Notificações">
          <IconButton
            color="inherit"
            onClick={handleNotificationsMenuOpen}
            aria-label="mostrar notificações"
            sx={{ mr: 1 }}
          >
            <Badge badgeContent={unreadCount} color="error">
              <NotificationsIcon />
            </Badge>
          </IconButton>
        </Tooltip>

        {/* Menu de notificações */}
        <Menu
          anchorEl={notificationsAnchorEl}
          open={Boolean(notificationsAnchorEl)}
          onClose={handleNotificationsClose}
          onClick={handleNotificationsClose}
          PaperProps={{
            elevation: 3,
            sx: {
              width: 360,
              maxWidth: '100%',
              mt: 1.5,
              overflow: 'hidden',
            },
          }}
          transformOrigin={{ horizontal: 'right', vertical: 'top' }}
          anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
        >
          <Box sx={{ p: 2, borderBottom: `1px solid ${theme.palette.divider}` }}>
            <Typography variant="subtitle1" fontWeight={600}>
              Notificações
            </Typography>
            {unreadCount > 0 && (
              <Typography variant="caption" color="primary">
                {unreadCount} não lida{unreadCount > 1 ? 's' : ''}
              </Typography>
            )}
          </Box>
          <Box sx={{ maxHeight: 400, overflowY: 'auto' }}>
            {notifications.length > 0 ? (
              notifications.map((notification) => (
                <MenuItem
                  key={notification.id}
                  sx={{
                    borderLeft: notification.read
                      ? 'none'
                      : `3px solid ${theme.palette.primary.main}`,
                    py: 1.5,
                    '&:hover': {
                      backgroundColor: theme.palette.action.hover,
                    },
                  }}
                >
                  <Box sx={{ width: '100%' }}>
                    <Typography variant="body2">{notification.text}</Typography>
                    <Typography variant="caption" color="textSecondary">
                      {notification.time}
                    </Typography>
                  </Box>
                </MenuItem>
              ))
            ) : (
              <Box sx={{ p: 2, textAlign: 'center' }}>
                <Typography variant="body2" color="textSecondary">
                  Nenhuma notificação
                </Typography>
              </Box>
            )}
          </Box>
          <Divider />
          <Box sx={{ p: 1, textAlign: 'center' }}>
            <Typography
              variant="body2"
              color="primary"
              sx={{ cursor: 'pointer', '&:hover': { textDecoration: 'underline' } }}
            >
              Ver todas as notificações
            </Typography>
          </Box>
        </Menu>

        {/* Avatar e menu do usuário */}
        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          <Box sx={{ textAlign: 'right', mr: 2, display: { xs: 'none', sm: 'block' } }}>
            <Typography variant="subtitle2" noWrap>
              {user?.name || 'Usuário'}
            </Typography>
            <Typography variant="caption" color="textSecondary" noWrap>
              {user?.role || 'Administrador'}
            </Typography>
          </Box>
          <IconButton
            onClick={handleProfileMenuOpen}
            size="small"
            aria-controls="user-menu"
            aria-haspopup="true"
            aria-label="menu do usuário"
          >
            <Avatar
              alt={user?.name || 'Usuário'}
              src={user?.avatar}
              sx={{
                width: 36,
                height: 36,
                bgcolor: theme.palette.primary.main,
                color: theme.palette.primary.contrastText,
              }}
            >
              {(user?.name || 'U').charAt(0).toUpperCase()}
            </Avatar>
          </IconButton>
        </Box>
      </Toolbar>

      {/* Menu do usuário */}
      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleMenuClose}
        onClick={handleMenuClose}
        PaperProps={{
          elevation: 3,
          sx: {
            width: 240,
            mt: 1.5,
            overflow: 'visible',
            '&:before': {
              content: '""',
              display: 'block',
              position: 'absolute',
              top: 0,
              right: 14,
              width: 10,
              height: 10,
              bgcolor: 'background.paper',
              transform: 'translateY(-50%) rotate(45deg)',
              zIndex: 0,
            },
          },
        }}
        transformOrigin={{ horizontal: 'right', vertical: 'top' }}
        anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
      >
        <Box sx={{ p: 2, textAlign: 'center' }}>
          <Avatar
            alt={user?.name || 'Usuário'}
            src={user?.avatar}
            sx={{
              width: 64,
              height: 64,
              mx: 'auto',
              mb: 1,
              bgcolor: theme.palette.primary.main,
              color: theme.palette.primary.contrastText,
            }}
          >
            {(user?.name || 'U').charAt(0).toUpperCase()}
          </Avatar>
          <Typography variant="subtitle1" fontWeight={600}>
            {user?.name || 'Usuário'}
          </Typography>
          <Typography variant="body2" color="textSecondary">
            {user?.email || 'usuario@exemplo.com'}
          </Typography>
        </Box>
        <Divider />
        <MenuItem onClick={handleProfile}>
          <ListItemIcon>
            <PersonIcon fontSize="small" />
          </ListItemIcon>
          <ListItemText>Meu Perfil</ListItemText>
        </MenuItem>
        <MenuItem onClick={handleSettings}>
          <ListItemIcon>
            <SettingsIcon fontSize="small" />
          </ListItemIcon>
          <ListItemText>Configurações</ListItemText>
        </MenuItem>
        <Divider />
        <MenuItem onClick={handleLogout}>
          <ListItemIcon>
            <LogoutIcon fontSize="small" color="error" />
          </ListItemIcon>
          <ListItemText primaryTypographyProps={{ color: 'error' }}>
            Sair
          </ListItemText>
        </MenuItem>
      </Menu>
    </AppBar>
  );
};

export default Header;
