import { createTheme, Theme, ThemeOptions } from '@mui/material/styles';
import { PaletteMode } from '@mui/material';

// Cores personalizadas
export const colors = {
  primary: {
    light: '#5e72e4',
    main: '#4f3cc9',
    dark: '#2a2a72',
    contrastText: '#ffffff',
  },
  secondary: {
    light: '#f7fafc',
    main: '#f0f2f5',
    dark: '#e9ecef',
    contrastText: '#2d3748',
  },
  success: {
    light: '#48bb78',
    main: '#38a169',
    dark: '#2f855a',
    contrastText: '#ffffff',
  },
  info: {
    light: '#4299e1',
    main: '#3182ce',
    dark: '#2b6cb0',
    contrastText: '#ffffff',
  },
  warning: {
    light: '#ed8936',
    main: '#dd6b20',
    dark: '#c05621',
    contrastText: '#ffffff',
  },
  error: {
    light: '#f56565',
    main: '#e53e3e',
    dark: '#c53030',
    contrastText: '#ffffff',
  },
  grey: {
    50: '#f8f9fa',
    100: '#f1f3f5',
    200: '#e9ecef',
    300: '#dee2e6',
    400: '#ced4da',
    500: '#adb5bd',
    600: '#868e96',
    700: '#495057',
    800: '#343a40',
    900: '#212529',
  },
  text: {
    primary: '#1a202c',
    secondary: '#4a5568',
    disabled: '#a0aec0',
  },
  background: {
    default: '#f8f9fa',
    paper: '#ffffff',
  },
  divider: '#e2e8f0',
};

// Tipografia
export const typography = {
  fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
  h1: {
    fontWeight: 700,
    fontSize: '2.5rem',
    lineHeight: 1.2,
    letterSpacing: '-0.01562em',
  },
  h2: {
    fontWeight: 700,
    fontSize: '2rem',
    lineHeight: 1.2,
    letterSpacing: '-0.00833em',
  },
  h3: {
    fontWeight: 600,
    fontSize: '1.75rem',
    lineHeight: 1.2,
    letterSpacing: '0em',
  },
  h4: {
    fontWeight: 600,
    fontSize: '1.5rem',
    lineHeight: 1.2,
    letterSpacing: '0.00735em',
  },
  h5: {
    fontWeight: 600,
    fontSize: '1.25rem',
    lineHeight: 1.2,
    letterSpacing: '0em',
  },
  h6: {
    fontWeight: 600,
    fontSize: '1rem',
    lineHeight: 1.2,
    letterSpacing: '0.0075em',
  },
  subtitle1: {
    fontWeight: 500,
    fontSize: '1rem',
    lineHeight: 1.5,
    letterSpacing: '0.00938em',
  },
  subtitle2: {
    fontWeight: 500,
    fontSize: '0.875rem',
    lineHeight: 1.57,
    letterSpacing: '0.00714em',
  },
  body1: {
    fontWeight: 400,
    fontSize: '1rem',
    lineHeight: 1.5,
    letterSpacing: '0.00938em',
  },
  body2: {
    fontWeight: 400,
    fontSize: '0.875rem',
    lineHeight: 1.5,
    letterSpacing: '0.01071em',
  },
  button: {
    fontWeight: 600,
    fontSize: '0.875rem',
    lineHeight: 1.75,
    letterSpacing: '0.02857em',
    textTransform: 'none',
  },
  caption: {
    fontWeight: 400,
    fontSize: '0.75rem',
    lineHeight: 1.66,
    letterSpacing: '0.03333em',
  },
  overline: {
    fontWeight: 400,
    fontSize: '0.75rem',
    lineHeight: 2.66,
    letterSpacing: '0.08333em',
    textTransform: 'uppercase',
  },
};

// Componentes
export const components = {
  MuiCssBaseline: {
    styleOverrides: {
      '*, *::before, *::after': {
        boxSizing: 'border-box',
        margin: 0,
        padding: 0,
      },
      html: {
        WebkitFontSmoothing: 'antialiased',
        MozOsxFontSmoothing: 'grayscale',
        height: '100%',
      },
      body: {
        height: '100%',
        backgroundColor: colors.background.default,
        '& #root': {
          height: '100%',
          display: 'flex',
          flexDirection: 'column',
        },
      },
      a: {
        color: colors.primary.main,
        textDecoration: 'none',
        '&:hover': {
          textDecoration: 'underline',
        },
      },
      'input::-webkit-outer-spin-button, input::-webkit-inner-spin-button': {
        WebkitAppearance: 'none',
        margin: 0,
      },
      'input[type=number]': {
        MozAppearance: 'textfield',
      },
    },
  },
  MuiButton: {
    styleOverrides: {
      root: {
        borderRadius: 8,
        padding: '8px 16px',
        textTransform: 'none',
        fontWeight: 600,
        boxShadow: 'none',
        '&:hover': {
          boxShadow: 'none',
        },
        '&.Mui-disabled': {
          opacity: 0.5,
        },
      },
      sizeLarge: {
        padding: '10px 22px',
        fontSize: '0.9375rem',
      },
      sizeSmall: {
        padding: '6px 12px',
        fontSize: '0.8125rem',
      },
      contained: {
        '&:hover': {
          boxShadow: '0 4px 12px rgba(79, 60, 201, 0.2)',
        },
      },
      outlined: {
        borderWidth: 2,
        '&:hover': {
          borderWidth: 2,
        },
      },
    },
  },
  MuiCard: {
    styleOverrides: {
      root: {
        borderRadius: 12,
        boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)',
        '&.MuiPaper-elevation1': {
          boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1), 0 1px 2px rgba(0, 0, 0, 0.06)',
        },
      },
    },
  },
  MuiCardHeader: {
    styleOverrides: {
      root: {
        padding: '24px 24px 16px',
      },
      title: {
        fontSize: '1.25rem',
        fontWeight: 600,
      },
      subheader: {
        marginTop: 4,
      },
    },
  },
  MuiCardContent: {
    styleOverrides: {
      root: {
        padding: 24,
        '&:last-child': {
          paddingBottom: 24,
        },
      },
    },
  },
  MuiTextField: {
    styleOverrides: {
      root: {
        '& .MuiOutlinedInput-root': {
          borderRadius: 8,
          '& fieldset': {
            borderColor: colors.grey[300],
          },
          '&:hover fieldset': {
            borderColor: colors.primary.light,
          },
          '&.Mui-focused fieldset': {
            borderColor: colors.primary.main,
            borderWidth: 1,
          },
        },
        '& .MuiFormLabel-root.Mui-focused': {
          color: colors.primary.main,
        },
      },
    },
  },
  MuiOutlinedInput: {
    styleOverrides: {
      root: {
        borderRadius: 8,
        '&:hover .MuiOutlinedInput-notchedOutline': {
          borderColor: colors.primary.light,
        },
        '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
          borderColor: colors.primary.main,
          borderWidth: 1,
        },
      },
      input: {
        padding: '12px 14px',
      },
      inputSizeSmall: {
        padding: '8px 14px',
      },
      inputMultiline: {
        padding: 0,
      },
      notchedOutline: {
        borderColor: colors.grey[300],
      },
    },
  },
  MuiInputLabel: {
    styleOverrides: {
      root: {
        color: colors.grey[600],
        '&.Mui-focused': {
          color: colors.primary.main,
        },
      },
      outlined: {
        transform: 'translate(14px, 14px) scale(1)',
        '&.MuiInputLabel-shrink': {
          transform: 'translate(14px, -6px) scale(0.75)',
        },
      },
    },
  },
  MuiFormHelperText: {
    styleOverrides: {
      root: {
        marginLeft: 0,
        marginTop: 8,
        '&.Mui-error': {
          color: colors.error.main,
        },
      },
    },
  },
  MuiSelect: {
    styleOverrides: {
      select: {
        padding: '12px 14px',
        '&:focus': {
          backgroundColor: 'transparent',
        },
      },
    },
  },
  MuiMenuItem: {
    styleOverrides: {
      root: {
        padding: '8px 16px',
        '&.Mui-selected': {
          backgroundColor: 'rgba(79, 60, 201, 0.08)',
          '&:hover': {
            backgroundColor: 'rgba(79, 60, 201, 0.12)',
          },
        },
      },
    },
  },
  MuiTable: {
    styleOverrides: {
      root: {
        borderCollapse: 'separate',
        borderSpacing: 0,
      },
    },
  },
  MuiTableHead: {
    styleOverrides: {
      root: {
        '& .MuiTableCell-head': {
          fontWeight: 600,
          backgroundColor: colors.grey[50],
          color: colors.grey[700],
          borderBottom: `1px solid ${colors.grey[200]}`,
        },
      },
    },
  },
  MuiTableCell: {
    styleOverrides: {
      root: {
        borderBottom: `1px solid ${colors.grey[200]}`,
        padding: '12px 16px',
      },
      head: {
        fontWeight: 600,
      },
    },
  },
  MuiTablePagination: {
    styleOverrides: {
      root: {
        borderTop: `1px solid ${colors.grey[200]}`,
      },
    },
  },
  MuiChip: {
    styleOverrides: {
      root: {
        borderRadius: 6,
        fontWeight: 500,
      },
    },
  },
  MuiDivider: {
    styleOverrides: {
      root: {
        borderColor: colors.grey[200],
      },
    },
  },
  MuiTooltip: {
    styleOverrides: {
      tooltip: {
        backgroundColor: 'rgba(26, 32, 44, 0.9)',
        padding: '6px 12px',
        fontSize: '0.75rem',
        borderRadius: 6,
      },
      arrow: {
        color: 'rgba(26, 32, 44, 0.9)',
      },
    },
  },
  MuiAlert: {
    styleOverrides: {
      root: {
        borderRadius: 8,
        padding: '12px 16px',
        '& .MuiAlert-icon': {
          marginRight: 12,
        },
        '& .MuiAlert-message': {
          padding: '4px 0',
        },
      },
      standardSuccess: {
        backgroundColor: 'rgba(72, 187, 120, 0.1)',
        color: colors.success.dark,
        '& .MuiAlert-icon': {
          color: colors.success.main,
        },
      },
      standardError: {
        backgroundColor: 'rgba(229, 62, 62, 0.1)',
        color: colors.error.dark,
        '& .MuiAlert-icon': {
          color: colors.error.main,
        },
      },
      standardWarning: {
        backgroundColor: 'rgba(221, 107, 32, 0.1)',
        color: colors.warning.dark,
        '& .MuiAlert-icon': {
          color: colors.warning.main,
        },
      },
      standardInfo: {
        backgroundColor: 'rgba(49, 130, 206, 0.1)',
        color: colors.info.dark,
        '& .MuiAlert-icon': {
          color: colors.info.main,
        },
      },
    },
  },
  MuiLinearProgress: {
    styleOverrides: {
      root: {
        height: 6,
        borderRadius: 3,
      },
    },
  },
  MuiSkeleton: {
    styleOverrides: {
      root: {
        backgroundColor: colors.grey[200],
      },
    },
  },
  MuiDrawer: {
    styleOverrides: {
      paper: {
        borderRight: 'none',
      },
    },
  },
  MuiAppBar: {
    styleOverrides: {
      root: {
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
      },
    },
  },
};

// Criar tema base
const createCustomTheme = (mode: PaletteMode): Theme => {
  const isLight = mode === 'light';

  const palette = {
    mode,
    primary: {
      light: colors.primary.light,
      main: colors.primary.main,
      dark: colors.primary.dark,
      contrastText: colors.primary.contrastText,
    },
    secondary: {
      light: colors.secondary.light,
      main: colors.secondary.main,
      dark: colors.secondary.dark,
      contrastText: colors.secondary.contrastText,
    },
    error: {
      light: colors.error.light,
      main: colors.error.main,
      dark: colors.error.dark,
      contrastText: colors.error.contrastText,
    },
    warning: {
      light: colors.warning.light,
      main: colors.warning.main,
      dark: colors.warning.dark,
      contrastText: colors.warning.contrastText,
    },
    info: {
      light: colors.info.light,
      main: colors.info.main,
      dark: colors.info.dark,
      contrastText: colors.info.contrastText,
    },
    success: {
      light: colors.success.light,
      main: colors.success.main,
      dark: colors.success.dark,
      contrastText: colors.success.contrastText,
    },
    grey: colors.grey,
    text: {
      primary: isLight ? colors.grey[900] : colors.grey[100],
      secondary: isLight ? colors.grey[700] : colors.grey[300],
      disabled: isLight ? colors.grey[500] : colors.grey[600],
    },
    divider: isLight ? 'rgba(0, 0, 0, 0.06)' : 'rgba(255, 255, 255, 0.12)',
    background: {
      paper: isLight ? colors.background.paper : colors.grey[800],
      default: isLight ? colors.background.default : colors.grey[900],
    },
    action: {
      active: isLight ? 'rgba(0, 0, 0, 0.54)' : 'rgba(255, 255, 255, 0.5)',
      hover: isLight ? 'rgba(0, 0, 0, 0.04)' : 'rgba(255, 255, 255, 0.08)',
      hoverOpacity: 0.08,
      selected: isLight ? 'rgba(0, 0, 0, 0.08)' : 'rgba(255, 255, 255, 0.16)',
      selectedOpacity: 0.16,
      disabled: isLight ? 'rgba(0, 0, 0, 0.26)' : 'rgba(255, 255, 255, 0.3)',
      disabledBackground: isLight ? 'rgba(0, 0, 0, 0.12)' : 'rgba(255, 255, 255, 0.12)',
      disabledOpacity: 0.38,
      focus: isLight ? 'rgba(0, 0, 0, 0.12)' : 'rgba(255, 255, 255, 0.3)',
      focusOpacity: 0.12,
      activatedOpacity: 0.24,
    },
  };

  const themeOptions: ThemeOptions = {
    palette,
    typography: typography as any,
    shape: {
      borderRadius: 8,
    },
    components: components as any,
    spacing: (factor: number) => `${0.5 * factor}rem`,
  };

  return createTheme(themeOptions);
};

export const lightTheme = createCustomTheme('light');
export const darkTheme = createCustomTheme('dark');

export default createCustomTheme;
