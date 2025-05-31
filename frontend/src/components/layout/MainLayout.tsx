import {
  Box,
  CssBaseline,
  Toolbar,
  AppBar,
  Typography,
  IconButton,
  Avatar,
  Tooltip,
  Menu,
  MenuItem,
  Divider,
  Drawer,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  ListItemButton,
  Paper,
  Badge,
  useTheme,
  useMediaQuery,
} from '@mui/material';
import { useState, useEffect } from 'react';
import {
  Menu as MenuIcon,
  Dashboard as DashboardIcon,
  CalendarMonth as CalendarIcon,
  People as ClientsIcon,
  Person as ProfessionalsIcon,
  Redeem as LoyaltyIcon,
  BarChart as ReportsIcon,
  WhatsApp as WhatsAppIcon,
  Settings as SettingsIcon,
  Notifications as NotificationsIcon,
  Brightness4 as DarkModeIcon,
  Brightness7 as LightModeIcon,
  ChevronLeft as ChevronLeftIcon,
  Help as HelpIcon,
} from '@mui/icons-material';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';

const drawerWidth = 260;

interface MainLayoutProps {
  children: React.ReactNode;
}

interface NavItem {
  title: string;
  path: string;
  icon: React.ReactNode;
  badge?: number;
}

const MainLayout = ({ children }: MainLayoutProps) => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));
  const [mobileOpen, setMobileOpen] = useState(false);
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [notificationEl, setNotificationEl] = useState<null | HTMLElement>(null);
  const { isAuthenticated, logout, user } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [darkMode, setDarkMode] = useState(false);

  // Fechar drawer quando mudar de rota em dispositivos móveis
  useEffect(() => {
    if (isMobile) {
      setMobileOpen(false);
    }
  }, [location.pathname, isMobile]);

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  const handleMenu = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleNotificationMenu = (event: React.MouseEvent<HTMLElement>) => {
    setNotificationEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleNotificationClose = () => {
    setNotificationEl(null);
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
    handleClose();
  };

  const handleThemeToggle = () => {
    setDarkMode(!darkMode);
  };

  const navItems: NavItem[] = [
    { title: 'Dashboard', path: '/dashboard', icon: <DashboardIcon /> },
    { title: 'Agendamentos', path: '/agendamentos', icon: <CalendarIcon />, badge: 5 },
    { title: 'Clientes', path: '/clientes', icon: <ClientsIcon /> },
    { title: 'Profissionais', path: '/profissionais', icon: <ProfessionalsIcon /> },
    { title: 'Fidelidade', path: '/fidelidade', icon: <LoyaltyIcon /> },
    { title: 'Relatórios', path: '/relatorios', icon: <ReportsIcon /> },
    { title: 'WhatsApp Bot', path: '/whatsapp', icon: <WhatsAppIcon />, badge: 3 },
  ];

  const bottomNavItems: NavItem[] = [
    { title: 'Configurações', path: '/configuracoes', icon: <SettingsIcon /> },
    { title: 'Ajuda', path: '/ajuda', icon: <HelpIcon /> },
  ];

  const drawer = (
    <div>
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          p: 2,
          height: 64,
          borderBottom: `1px solid ${theme.palette.divider}`,
        }}
      >
        <Typography variant="h6" noWrap component="div" sx={{ fontWeight: 'bold' }}>
          ClinicaSalão
        </Typography>
        {isMobile && (
          <IconButton onClick={handleDrawerToggle}>
            <ChevronLeftIcon />
          </IconButton>
        )}
      </Box>
      <Box sx={{ p: 2 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
          <Avatar
            sx={{ width: 40, height: 40, mr: 2, bgcolor: theme.palette.primary.main }}
            alt={user?.name || 'Usuário'}
            src={user?.avatar || ''}
          >
            {user?.name?.charAt(0) || 'U'}
          </Avatar>
          <Box>
            <Typography variant="subtitle1" sx={{ fontWeight: 'bold' }}>
              {user?.name || 'Usuário'}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {user?.role || 'Administrador'}
            </Typography>
          </Box>
        </Box>
      </Box>
      <Divider />
      <List sx={{ pt: 1 }}>
        {navItems.map((item) => (
          <ListItem key={item.path} disablePadding>
            <ListItemButton
              component={Link}
              to={item.path}
              selected={location.pathname === item.path}
              sx={{
                borderRadius: '8px',
                mx: 1,
                mb: 0.5,
                '&.Mui-selected': {
                  backgroundColor: theme.palette.primary.main + '20',
                  '&:hover': {
                    backgroundColor: theme.palette.primary.main + '30',
                  },
                },
              }}
            >
              <ListItemIcon
                sx={{
                  minWidth: 40,
                  color: location.pathname === item.path ? theme.palette.primary.main : 'inherit',
                }}
              >
                {item.badge ? (
                  <Badge badgeContent={item.badge} color="error">
                    {item.icon}
                  </Badge>
                ) : (
                  item.icon
                )}
              </ListItemIcon>
              <ListItemText
                primary={item.title}
                primaryTypographyProps={{
                  fontSize: 14,
                  fontWeight: location.pathname === item.path ? 'bold' : 'normal',
                }}
              />
            </ListItemButton>
          </ListItem>
        ))}
      </List>
      <Divider sx={{ my: 2 }} />
      <List>
        {bottomNavItems.map((item) => (
          <ListItem key={item.path} disablePadding>
            <ListItemButton
              component={Link}
              to={item.path}
              selected={location.pathname === item.path}
              sx={{
                borderRadius: '8px',
                mx: 1,
                mb: 0.5,
                '&.Mui-selected': {
                  backgroundColor: theme.palette.primary.main + '20',
                  '&:hover': {
                    backgroundColor: theme.palette.primary.main + '30',
                  },
                },
              }}
            >
              <ListItemIcon
                sx={{
                  minWidth: 40,
                  color: location.pathname === item.path ? theme.palette.primary.main : 'inherit',
                }}
              >
                {item.icon}
              </ListItemIcon>
              <ListItemText
                primary={item.title}
                primaryTypographyProps={{
                  fontSize: 14,
                  fontWeight: location.pathname === item.path ? 'bold' : 'normal',
                }}
              />
            </ListItemButton>
          </ListItem>
        ))}
      </List>
    </div>
  );

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh' }}>
      <CssBaseline />
      
      {/* AppBar */}
      <AppBar
        position="fixed"
        elevation={0}
        sx={{
          width: { sm: `calc(100% - ${drawerWidth}px)` },
          ml: { sm: `${drawerWidth}px` },
          bgcolor: 'background.paper',
          color: 'text.primary',
          borderBottom: `1px solid ${theme.palette.divider}`,
        }}
      >
        <Toolbar>
          <IconButton
            color="inherit"
            aria-label="open drawer"
            edge="start"
            onClick={handleDrawerToggle}
            sx={{ mr: 2, display: { sm: 'none' } }}
          >
            <MenuIcon />
          </IconButton>
          
          <Typography variant="h6" noWrap component="div" sx={{ flexGrow: 1, display: { xs: 'none', sm: 'block' } }}>
            {navItems.find(item => item.path === location.pathname)?.title || 'Dashboard'}
          </Typography>
          
          {isAuthenticated && (
            <Box sx={{ display: 'flex' }}>
              <IconButton color="inherit" onClick={handleThemeToggle}>
                {darkMode ? <LightModeIcon /> : <DarkModeIcon />}
              </IconButton>
              
              <IconButton color="inherit" onClick={handleNotificationMenu}>
                <Badge badgeContent={3} color="error">
                  <NotificationsIcon />
                </Badge>
              </IconButton>
              <Menu
                anchorEl={notificationEl}
                anchorOrigin={{
                  vertical: 'bottom',
                  horizontal: 'right',
                }}
                keepMounted
                transformOrigin={{
                  vertical: 'top',
                  horizontal: 'right',
                }}
                open={Boolean(notificationEl)}
                onClose={handleNotificationClose}
                PaperProps={{
                  elevation: 0,
                  sx: {
                    overflow: 'visible',
                    filter: 'drop-shadow(0px 2px 8px rgba(0,0,0,0.1))',
                    width: 320,
                    maxHeight: 400,
                  },
                }}
              >
                <Typography variant="subtitle1" sx={{ p: 2, fontWeight: 'bold' }}>
                  Notificações
                </Typography>
                <Divider />
                <MenuItem sx={{ py: 2 }} onClick={handleNotificationClose}>
                  <Box>
                    <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                      Novo agendamento
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      Maria Silva agendou para 15:00h
                    </Typography>
                  </Box>
                </MenuItem>
                <MenuItem sx={{ py: 2 }} onClick={handleNotificationClose}>
                  <Box>
                    <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                      Cancelamento
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      João Costa cancelou para hoje às 13:30h
                    </Typography>
                  </Box>
                </MenuItem>
                <Divider />
                <MenuItem onClick={handleNotificationClose}>
                  <Typography variant="body2" color="primary" sx={{ width: '100%', textAlign: 'center' }}>
                    Ver todas
                  </Typography>
                </MenuItem>
              </Menu>
              
              <Tooltip title="Conta">
                <IconButton
                  size="large"
                  onClick={handleMenu}
                  color="inherit"
                  sx={{ ml: 1 }}
                >
                  <Avatar 
                    sx={{ width: 32, height: 32, bgcolor: theme.palette.primary.main }}
                    alt={user?.name || 'Usuário'}
                    src={user?.avatar || ''}
                  >
                    {user?.name?.charAt(0) || 'U'}
                  </Avatar>
                </IconButton>
              </Tooltip>
              <Menu
                anchorEl={anchorEl}
                anchorOrigin={{
                  vertical: 'bottom',
                  horizontal: 'right',
                }}
                keepMounted
                transformOrigin={{
                  vertical: 'top',
                  horizontal: 'right',
                }}
                open={Boolean(anchorEl)}
                onClose={handleClose}
                PaperProps={{
                  elevation: 0,
                  sx: {
                    overflow: 'visible',
                    filter: 'drop-shadow(0px 2px 8px rgba(0,0,0,0.1))',
                    mt: 1.5,
                    width: 200,
                  },
                }}
              >
                <MenuItem onClick={() => { navigate('/perfil'); handleClose(); }}>
                  Meu Perfil
                </MenuItem>
                <MenuItem onClick={() => { navigate('/configuracoes'); handleClose(); }}>
                  Configurações
                </MenuItem>
                <Divider />
                <MenuItem onClick={handleLogout}>Sair</MenuItem>
              </Menu>
            </Box>
          )}
        </Toolbar>
      </AppBar>
      
      {/* Sidebar */}
      <Box
        component="nav"
        sx={{ width: { sm: drawerWidth }, flexShrink: { sm: 0 } }}
        aria-label="mailbox folders"
      >
        {/* Mobile drawer */}
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={handleDrawerToggle}
          ModalProps={{
            keepMounted: true, // Better performance on mobile
          }}
          sx={{
            display: { xs: 'block', sm: 'none' },
            '& .MuiDrawer-paper': { 
              boxSizing: 'border-box', 
              width: drawerWidth,
              borderRight: `1px solid ${theme.palette.divider}`,
            },
          }}
        >
          {drawer}
        </Drawer>
        
        {/* Desktop drawer */}
        <Drawer
          variant="permanent"
          sx={{
            display: { xs: 'none', sm: 'block' },
            '& .MuiDrawer-paper': { 
              boxSizing: 'border-box', 
              width: drawerWidth,
              borderRight: `1px solid ${theme.palette.divider}`,
            },
          }}
          open
        >
          {drawer}
        </Drawer>
      </Box>
      
      {/* Main content */}
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          p: 3,
          width: { sm: `calc(100% - ${drawerWidth}px)` },
          backgroundColor: theme.palette.background.default,
          minHeight: '100vh',
        }}
      >
        <Toolbar />
        <Paper 
          elevation={0} 
          sx={{ 
            p: 3, 
            borderRadius: 2, 
            bgcolor: 'background.paper',
            boxShadow: '0px 2px 8px rgba(0,0,0,0.05)'
          }}
        >
          {children}
        </Paper>
      </Box>
    </Box>
  );
};

export default MainLayout;
