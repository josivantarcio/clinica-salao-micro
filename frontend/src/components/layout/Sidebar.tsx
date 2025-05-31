import React, { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import {
  Box,
  Divider,
  Drawer,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Toolbar,
  Typography,
  Collapse,
  useTheme,
  useMediaQuery,
  IconButton,
  alpha,
} from '@mui/material';
import {
  Dashboard as DashboardIcon,
  People as PeopleIcon,
  CalendarToday as CalendarIcon,
  AttachMoney as MoneyIcon,
  Settings as SettingsIcon,
  Menu as MenuIcon,
  ChevronLeft as ChevronLeftIcon,
  ExpandLess as ExpandLessIcon,
  ExpandMore as ExpandMoreIcon,
  Inventory as InventoryIcon,
  Assessment as AssessmentIcon,
  Receipt as ReceiptIcon,
  Person as PersonIcon,
  Group as GroupIcon,
  Category as CategoryIcon,
  ExpandCircleDown as ExpandCircleDownIcon,
} from '@mui/icons-material';

const drawerWidth = 240;

interface MenuItem {
  title: string;
  path?: string;
  icon: React.ReactNode;
  children?: MenuItem[];
  disabled?: boolean;
}

const menuItems: MenuItem[] = [
  {
    title: 'Dashboard',
    path: '/',
    icon: <DashboardIcon />,
  },
  {
    title: 'Clientes',
    path: '/clients',
    icon: <PeopleIcon />,
  },
  {
    title: 'Agendamentos',
    path: '/appointments',
    icon: <CalendarIcon />,
  },
  {
    title: 'Serviços',
    path: '/services',
    icon: <CategoryIcon />,
  },
  {
    title: 'Profissionais',
    path: '/professionals',
    icon: <GroupIcon />,
  },
  {
    title: 'Financeiro',
    icon: <MoneyIcon />,
    children: [
      {
        title: 'Transações',
        path: '/financial/transactions',
        icon: <ReceiptIcon />,
      },
      {
        title: 'Relatórios',
        path: '/financial/reports',
        icon: <AssessmentIcon />,
      },
    ],
  },
  {
    title: 'Estoque',
    path: '/inventory',
    icon: <InventoryIcon />,
  },
  {
    title: 'Configurações',
    icon: <SettingsIcon />,
    children: [
      {
        title: 'Meu Perfil',
        path: '/settings/profile',
        icon: <PersonIcon />,
      },
      {
        title: 'Preferências',
        path: '/settings/preferences',
        icon: <SettingsIcon />,
      },
    ],
  },
];

interface SidebarProps {
  mobileOpen: boolean;
  handleDrawerToggle: () => void;
}

const Sidebar: React.FC<SidebarProps> = ({ mobileOpen, handleDrawerToggle }) => {
  const theme = useTheme();
  const location = useLocation();
  const navigate = useNavigate();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const [expandedItems, setExpandedItems] = useState<Record<string, boolean>>({});

  const toggleItem = (title: string) => {
    setExpandedItems((prev) => ({
      ...prev,
      [title]: !prev[title],
    }));
  };

  const isActive = (path?: string) => {
    if (!path) return false;
    return location.pathname === path || location.pathname.startsWith(`${path}/`);
  };

  const renderMenuItem = (item: MenuItem, depth = 0) => {
    const hasChildren = item.children && item.children.length > 0;
    const isItemActive = isActive(item.path);
    const isExpanded = expandedItems[item.title] ?? false;

    const listItem = (
      <ListItem disablePadding>
        <ListItemButton
          onClick={() => {
            if (hasChildren) {
              toggleItem(item.title);
            } else if (item.path) {
              navigate(item.path);
              if (isMobile) {
                handleDrawerToggle();
              }
            }
          }}
          disabled={item.disabled}
          sx={{
            pl: 2 + depth * 2,
            py: 1.25,
            borderRadius: 1,
            mx: 1,
            mb: 0.5,
            '&.Mui-selected': {
              backgroundColor: alpha(theme.palette.primary.main, 0.1),
              color: theme.palette.primary.main,
              '&:hover': {
                backgroundColor: alpha(theme.palette.primary.main, 0.15),
              },
              '& .MuiListItemIcon-root': {
                color: theme.palette.primary.main,
              },
            },
            '&:hover': {
              backgroundColor: alpha(theme.palette.primary.main, 0.05),
            },
          }}
          selected={isItemActive && !hasChildren}
        >
          <ListItemIcon sx={{ minWidth: 40 }}>{item.icon}</ListItemIcon>
          <ListItemText
            primary={
              <Typography variant="body2" fontWeight={isItemActive ? 600 : 400}>
                {item.title}
              </Typography>
            }
          />
          {hasChildren && (
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              {isExpanded ? (
                <ExpandLessIcon fontSize="small" />
              ) : (
                <ExpandMoreIcon fontSize="small" />
              )}
            </Box>
          )}
        </ListItemButton>
      </ListItem>
    );

    return (
      <Box key={item.title}>
        {listItem}
        {hasChildren && (
          <Collapse in={isExpanded} timeout="auto" unmountOnExit>
            <List component="div" disablePadding>
              {item.children?.map((child) => renderMenuItem(child, depth + 1))}
            </List>
          </Collapse>
        )}
      </Box>
    );
  };

  const drawer = (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        height: '100%',
        bgcolor: 'background.paper',
        borderRight: `1px solid ${theme.palette.divider}`,
      }}
    >
      {/* Cabeçalho */}
      <Toolbar
        sx={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          px: [2],
          py: 1.5,
        }}
      >
        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          <Box
            component="img"
            src="/logo-small.png"
            alt="Logo"
            sx={{ height: 32, mr: 1 }}
            onError={(e) => {
              const target = e.target as HTMLImageElement;
              target.style.display = 'none';
            }}
          />
          <Typography
            variant="h6"
            noWrap
            component="div"
            sx={{
              fontWeight: 700,
              background: `linear-gradient(45deg, ${theme.palette.primary.main} 30%, ${theme.palette.secondary.main} 90%})`,
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent',
              backgroundClip: 'text',
              textFillColor: 'transparent',
            }}
          >
            ClinicaSalao
          </Typography>
        </Box>
        {!isMobile && (
          <IconButton onClick={handleDrawerToggle} size="small">
            <ChevronLeftIcon />
          </IconButton>
        )}
      </Toolbar>

      <Divider />

      {/* Menu */}
      <Box sx={{ flexGrow: 1, overflowY: 'auto', py: 1, px: 1 }}>
        <List>{menuItems.map((item) => renderMenuItem(item))}</List>
      </Box>

      {/* Rodapé */}
      <Box sx={{ p: 2, borderTop: `1px solid ${theme.palette.divider}` }}>
        <Typography variant="caption" color="text.secondary">
          v{process.env.REACT_APP_VERSION || '1.0.0'}
        </Typography>
      </Box>
    </Box>
  );

  return (
    <Box
      component="nav"
      sx={{
        width: { md: drawerWidth },
        flexShrink: { md: 0 },
      }}
      aria-label="menu de navegação"
    >
      {/* Mobile */}
      <Drawer
        variant="temporary"
        open={mobileOpen}
        onClose={handleDrawerToggle}
        ModalProps={{
          keepMounted: true, // Melhora o desempenho no mobile
        }}
        sx={{
          display: { xs: 'block', md: 'none' },
          '& .MuiDrawer-paper': {
            boxSizing: 'border-box',
            width: drawerWidth,
            borderRight: 'none',
            boxShadow: theme.shadows[8],
          },
        }}
      >
        {drawer}
      </Drawer>

      {/* Desktop */}
      <Drawer
        variant="permanent"
        sx={{
          display: { xs: 'none', md: 'block' },
          '& .MuiDrawer-paper': {
            boxSizing: 'border-box',
            width: drawerWidth,
            borderRight: 'none',
            boxShadow: theme.shadows[1],
          },
        }}
        open
      >
        {drawer}
      </Drawer>
    </Box>
  );
};

export default Sidebar;
