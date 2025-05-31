import React, { useMemo } from 'react';
import {
  DataGrid as MuiDataGrid,
  DataGridProps as MuiDataGridProps,
  GridColDef,
  GridRowId,
  GridToolbarContainer,
  GridToolbarExport,
  GridToolbarFilterButton,
  GridToolbarDensitySelector,
  GridToolbarColumnsButton,
  GridRowParams,
  GridActionsCellItem,
  GridActionsCellItemProps,
  GridValueFormatterParams,
  GridRenderCellParams,
} from '@mui/x-data-grid';
import { Box, Button, IconButton, Tooltip, Typography, useTheme } from '@mui/material';
import { Refresh as RefreshIcon } from '@mui/icons-material';
import { format } from 'date-fns';
import { ptBR } from 'date-fns/locale';

// Tipos personalizados
export interface DataTableColumn extends Omit<GridColDef, 'field' | 'headerName' | 'type'> {
  field: string;
  headerName: string;
  type?: 'text' | 'number' | 'date' | 'boolean' | 'actions' | 'custom';
  format?: (value: any) => string | React.ReactNode;
  width?: number;
  minWidth?: number;
  maxWidth?: number;
  align?: 'left' | 'center' | 'right';
  headerAlign?: 'left' | 'center' | 'right';
  sortable?: boolean;
  filterable?: boolean;
  hideable?: boolean;
  renderCell?: (params: GridRenderCellParams) => React.ReactNode;
  valueFormatter?: (params: GridValueFormatterParams) => any;
  actionItems?: (params: GridRowParams) => GridActionsCellItemProps[];
}

export interface DataTableProps extends Omit<MuiDataGridProps, 'columns' | 'rows'> {
  columns: DataTableColumn[];
  rows: any[];
  loading?: boolean;
  error?: Error | null;
  onRefresh?: () => void;
  onRowClick?: (params: GridRowParams) => void;
  showToolbar?: boolean;
  showExport?: boolean;
  showFilterButton?: boolean;
  showDensityButton?: boolean;
  showColumnButton?: boolean;
  toolbarActions?: React.ReactNode;
  noRowsLabel?: string;
  noResultsOverlayLabel?: string;
  errorOverlayLabel?: string;
}

// Componente de toolbar personalizado
const CustomToolbar = ({
  onRefresh,
  showExport = true,
  showFilterButton = true,
  showDensityButton = true,
  showColumnButton = true,
  toolbarActions,
}: {
  onRefresh?: () => void;
  showExport?: boolean;
  showFilterButton?: boolean;
  showDensityButton?: boolean;
  showColumnButton?: boolean;
  toolbarActions?: React.ReactNode;
}) => {
  return (
    <GridToolbarContainer
      sx={{
        p: 1,
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        gap: 1,
        flexWrap: 'wrap',
      }}
    >
      <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', alignItems: 'center' }}>
        {onRefresh && (
          <Tooltip title="Atualizar">
            <IconButton size="small" onClick={onRefresh}>
              <RefreshIcon fontSize="small" />
            </IconButton>
          </Tooltip>
        )}
        {showFilterButton && <GridToolbarFilterButton />}
        {showColumnButton && <GridToolbarColumnsButton />}
        {showDensityButton && <GridToolbarDensitySelector />}
        {showExport && (
          <GridToolbarExport
            printOptions={{
              hideFooter: true,
              hideToolbar: true,
            }}
          />
        )}
      </Box>
      {toolbarActions}
    </GridToolbarContainer>
  );
};

// Componente de overlay personalizado para quando não houver dados
const NoRowsOverlay = ({
  message = 'Nenhum registro encontrado',
}: {
  message?: string;
}) => (
  <Box
    sx={{
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      height: '100%',
      p: 2,
    }}
  >
    <Typography variant="body1" color="textSecondary">
      {message}
    </Typography>
  </Box>
);

// Componente de overlay para erros
const ErrorOverlay = ({
  error,
  onRetry,
  message = 'Ocorreu um erro ao carregar os dados',
}: {
  error?: Error | null;
  onRetry?: () => void;
  message?: string;
}) => (
  <Box
    sx={{
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      height: '100%',
      p: 2,
      textAlign: 'center',
    }}
  >
    <Typography variant="body1" color="error" gutterBottom>
      {message}
    </Typography>
    {error && (
      <Typography variant="caption" color="textSecondary" gutterBottom>
        {error.message}
      </Typography>
    )}
    {onRetry && (
      <Button
        variant="outlined"
        color="primary"
        onClick={onRetry}
        startIcon={<RefreshIcon />}
        sx={{ mt: 2 }}
      >
        Tentar novamente
      </Button>
    )}
  </Box>
);

// Componente principal DataTable
const DataTable: React.FC<DataTableProps> = ({
  columns,
  rows,
  loading = false,
  error = null,
  onRefresh,
  onRowClick,
  showToolbar = true,
  showExport = true,
  showFilterButton = true,
  showDensityButton = true,
  showColumnButton = true,
  toolbarActions,
  noRowsLabel = 'Nenhum registro encontrado',
  noResultsOverlayLabel = 'Nenhum resultado encontrado',
  errorOverlayLabel = 'Ocorreu um erro ao carregar os dados',
  ...props
}) => {
  const theme = useTheme();

  // Processa as colunas para adicionar formatações padrão
  const processedColumns = useMemo(() => {
    return columns.map((column) => {
      const col: DataTableColumn = { ...column };

      // Configurações padrão para colunas de data
      if (column.type === 'date' && !col.valueFormatter) {
        col.valueFormatter = (params: GridValueFormatterParams) => {
          if (!params.value) return '-';
          try {
            const date = new Date(params.value);
            return format(date, 'dd/MM/yyyy', { locale: ptBR });
          } catch (error) {
            return params.value;
          }
        };
      }

      // Configurações para colunas booleanas
      if (column.type === 'boolean') {
        col.renderCell = (params: GridRenderCellParams) => (
          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: column.align || 'center',
              width: '100%',
              height: '100%',
            }}
          >
            <Box
              sx={{
                width: 10,
                height: 10,
                borderRadius: '50%',
                bgcolor: params.value ? 'success.main' : 'error.main',
                mr: 1,
              }}
            />
            <Typography variant="body2" color="textSecondary">
              {params.value ? 'Sim' : 'Não'}
            </Typography>
          </Box>
        );
      }

      // Configurações para colunas de ações
      if (column.type === 'actions' && column.actionItems) {
        col.renderCell = (params: GridRenderCellParams) => {
          const actionItems = column.actionItems?.(params) || [];
          return (
            <Box sx={{ display: 'flex', gap: 0.5 }}>
              {actionItems.map((action, index) => (
                <GridActionsCellItem
                  key={index}
                  {...action}
                  sx={{
                    '&:hover': {
                      backgroundColor: 'action.hover',
                    },
                    ...action.sx,
                  }}
                />
              ))}
            </Box>
          );
        };
      }


      // Aplicar formatação personalizada, se fornecida
      if (column.format && !column.renderCell) {
        const originalRenderCell = column.renderCell;
        col.renderCell = (params: GridRenderCellParams) => {
          if (originalRenderCell) {
            return originalRenderCell(params);
          }
          return column.format?.(params.value) || params.value;
        };
      }

      return col;
    });
  }, [columns]);

  // Configurações padrão para a tabela
  const defaultProps: Partial<MuiDataGridProps> = {
    autoHeight: true,
    disableSelectionOnClick: true,
    disableColumnMenu: false,
    disableDensitySelector: !showDensityButton,
    disableColumnSelector: !showColumnButton,
    disableColumnFilter: !showFilterButton,
    disableIgnoreModificationsIfProcessingProps: true,
    pageSize: 10,
    rowsPerPageOptions: [5, 10, 25, 50],
    pagination: true,
    paginationMode: 'server',
    sortingMode: 'server',
    filterMode: 'server',
    components: {
      Toolbar: showToolbar
        ? () => (
            <CustomToolbar
              onRefresh={onRefresh}
              showExport={showExport}
              showFilterButton={showFilterButton}
              showDensityButton={showDensityButton}
              showColumnButton={showColumnButton}
              toolbarActions={toolbarActions}
            />
          )
        : null,
      NoRowsOverlay: () => <NoRowsOverlay message={noRowsLabel} />,
      NoResultsOverlay: () => <NoRowsOverlay message={noResultsOverlayLabel} />,
      ErrorOverlay: () => (
        <ErrorOverlay
          error={error}
          onRetry={onRefresh}
          message={errorOverlayLabel}
        />
      ),
    },
    componentsProps: {
      toolbar: {
        showQuickFilter: true,
      },
    },
    sx: {
      border: 'none',
      '& .MuiDataGrid-columnHeaders': {
        backgroundColor: theme.palette.background.paper,
        borderBottom: `1px solid ${theme.palette.divider}`,
      },
      '& .MuiDataGrid-cell': {
        borderBottom: `1px solid ${theme.palette.divider}`,
      },
      '& .MuiDataGrid-row': {
        '&:hover': {
          backgroundColor: theme.palette.action.hover,
          cursor: onRowClick ? 'pointer' : 'default',
        },
      },
      '& .MuiDataGrid-row.Mui-selected': {
        backgroundColor: theme.palette.action.selected,
        '&:hover': {
          backgroundColor: theme.palette.action.hover,
        },
      },
      ...props.sx,
    },
  };

  // Se houver um erro, exibe o overlay de erro
  if (error) {
    return (
      <Box sx={{ height: 400, width: '100%' }}>
        <MuiDataGrid
          {...defaultProps}
          {...props}
          rows={[]}
          columns={processedColumns}
          loading={false}
          error={error}
        />
      </Box>
    );
  }

  return (
    <Box sx={{ height: '100%', width: '100%' }}>
      <MuiDataGrid
        {...defaultProps}
        {...props}
        rows={rows}
        columns={processedColumns}
        loading={loading}
        onRowClick={onRowClick}
        localeText={{
          toolbarDensity: 'Densidade',
          toolbarDensityLabel: 'Densidade',
          toolbarDensityCompact: 'Compacto',
          toolbarDensityStandard: 'Padrão',
          toolbarDensityComfortable: 'Confortável',
          toolbarExport: 'Exportar',
          toolbarExportCSV: 'Baixar como CSV',
          toolbarExportPrint: 'Imprimir',
          toolbarFilters: 'Filtros',
          toolbarFiltersLabel: 'Mostrar filtros',
          toolbarFiltersTooltipHide: 'Ocultar filtros',
          toolbarFiltersTooltipShow: 'Mostrar filtros',
          filterPanelAddFilter: 'Adicionar filtro',
          filterPanelRemoveAll: 'Remover todos',
          filterPanelDeleteIconLabel: 'Excluir',
          filterPanelLogicOperator: 'Operador lógico',
          filterPanelOperator: 'Operador',
          filterPanelOperatorAnd: 'E',
          filterPanelOperatorOr: 'Ou',
          filterPanelColumns: 'Colunas',
          filterPanelInputLabel: 'Valor',
          filterPanelInputPlaceholder: 'Valor do filtro',
          filterOperatorContains: 'contém',
          filterOperatorEquals: 'igual a',
          filterOperatorStartsWith: 'começa com',
          filterOperatorEndsWith: 'termina com',
          filterOperatorIs: 'é',
          filterOperatorNot: 'não é',
          filterOperatorAfter: 'depois de',
          filterOperatorOnOrAfter: 'em ou depois de',
          filterOperatorBefore: 'antes de',
          filterOperatorOnOrBefore: 'em ou antes de',
          filterOperatorIsEmpty: 'está vazio',
          filterOperatorIsNotEmpty: 'não está vazio',
          filterOperatorIsAnyOf: 'é qualquer um de',
          columnMenuLabel: 'Menu',
          columnMenuShowColumns: 'Mostrar colunas',
          columnMenuFilter: 'Filtrar',
          columnMenuHideColumn: 'Ocultar',
          columnMenuUnsort: 'Desfazer ordenação',
          columnMenuSortAsc: 'Ordenar por ASC',
          columnMenuSortDesc: 'Ordenar por DESC',
          columnsPanelTextFieldLabel: 'Localizar coluna',
          columnsPanelTextFieldPlaceholder: 'Título da coluna',
          columnsPanelDragIconLabel: 'Reordenar coluna',
          columnsPanelShowAllButton: 'Mostrar todas',
          columnsPanelHideAllButton: 'Ocultar todas',
          MuiTablePagination: {
            labelRowsPerPage: 'Linhas por página:',
            labelDisplayedRows: ({ from, to, count }) =>
              `${from}-${to} de ${count !== -1 ? count : `mais de ${to}`}`,
          },
        }}
      />
    </Box>
  );
};

export default DataTable;
