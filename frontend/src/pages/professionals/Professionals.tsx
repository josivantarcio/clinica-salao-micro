import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  IconButton,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TablePagination,
  TableRow,
  TextField,
  Typography
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Search as SearchIcon,
  FilterList as FilterListIcon
} from '@mui/icons-material';

import Page from '../../components/layout/Page';
import Loading from '../../components/common/Loading';
import ErrorAlert from '../../components/common/ErrorAlert';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import { Professional, getProfessionals, deleteProfessional } from '../../services/professionalService';

const Professionals: React.FC = () => {
  const navigate = useNavigate();
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [searchTerm, setSearchTerm] = useState('');
  const [confirmDialogOpen, setConfirmDialogOpen] = useState(false);
  const [selectedProfessionalId, setSelectedProfessionalId] = useState<number | null>(null);

  // Buscar profissionais
  const {
    data: professionals = [],
    isLoading,
    isError,
    error,
    refetch
  } = useQuery({
    queryKey: ['professionals'],
    queryFn: getProfessionals
  });

  // Funções para paginação
  const handleChangePage = (event: unknown, newPage: number) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  // Função para filtrar profissionais com base no termo de busca
  const filteredProfessionals = professionals.filter((professional) =>
    professional.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    professional.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
    professional.specialties.some(specialty => 
      specialty.toLowerCase().includes(searchTerm.toLowerCase())
    )
  );

  // Paginação dos profissionais filtrados
  const paginatedProfessionals = filteredProfessionals.slice(
    page * rowsPerPage,
    page * rowsPerPage + rowsPerPage
  );

  // Manipulação da exclusão de profissional
  const handleDeleteClick = (id: number) => {
    setSelectedProfessionalId(id);
    setConfirmDialogOpen(true);
  };

  const handleConfirmDelete = async () => {
    if (selectedProfessionalId) {
      try {
        await deleteProfessional(selectedProfessionalId);
        refetch();
      } catch (error) {
        console.error('Erro ao excluir profissional:', error);
      }
    }
    setConfirmDialogOpen(false);
  };

  if (isLoading) return <Loading />;
  if (isError) return <ErrorAlert error={error as Error} />;

  return (
    <Page title="Profissionais">
      <Card>
        <CardContent>
          <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
            <Typography variant="h5" component="h2">
              Gerenciamento de Profissionais
            </Typography>
            <Button
              variant="contained"
              color="primary"
              startIcon={<AddIcon />}
              onClick={() => navigate('/professionals/new')}
            >
              Novo Profissional
            </Button>
          </Box>

          {/* Barra de pesquisa */}
          <Box display="flex" mb={3}>
            <TextField
              fullWidth
              variant="outlined"
              placeholder="Buscar por nome, email ou especialidade..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              InputProps={{
                startAdornment: <SearchIcon color="action" sx={{ mr: 1 }} />,
              }}
              size="small"
            />
            <IconButton color="primary" aria-label="filtrar">
              <FilterListIcon />
            </IconButton>
          </Box>

          {/* Tabela de profissionais */}
          <TableContainer component={Paper} sx={{ mt: 2 }}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Nome</TableCell>
                  <TableCell>Email</TableCell>
                  <TableCell>Telefone</TableCell>
                  <TableCell>Especialidades</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell align="right">Ações</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {paginatedProfessionals.length > 0 ? (
                  paginatedProfessionals.map((professional) => (
                    <TableRow key={professional.id}>
                      <TableCell>{professional.name}</TableCell>
                      <TableCell>{professional.email}</TableCell>
                      <TableCell>{professional.phone}</TableCell>
                      <TableCell>
                        {professional.specialties.map((specialty, index) => (
                          <Chip
                            key={index}
                            label={specialty}
                            size="small"
                            color="primary"
                            variant="outlined"
                            sx={{ mr: 0.5, mb: 0.5 }}
                          />
                        ))}
                      </TableCell>
                      <TableCell>
                        <Chip
                          label={professional.active ? 'Ativo' : 'Inativo'}
                          color={professional.active ? 'success' : 'error'}
                          size="small"
                        />
                      </TableCell>
                      <TableCell align="right">
                        <IconButton
                          color="primary"
                          onClick={() => navigate(`/professionals/${professional.id}/edit`)}
                        >
                          <EditIcon />
                        </IconButton>
                        <IconButton
                          color="error"
                          onClick={() => handleDeleteClick(professional.id)}
                        >
                          <DeleteIcon />
                        </IconButton>
                      </TableCell>
                    </TableRow>
                  ))
                ) : (
                  <TableRow>
                    <TableCell colSpan={6} align="center">
                      Nenhum profissional encontrado
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
            <TablePagination
              rowsPerPageOptions={[5, 10, 25]}
              component="div"
              count={filteredProfessionals.length}
              rowsPerPage={rowsPerPage}
              page={page}
              onPageChange={handleChangePage}
              onRowsPerPageChange={handleChangeRowsPerPage}
              labelRowsPerPage="Itens por página:"
              labelDisplayedRows={({ from, to, count }) => `${from}-${to} de ${count}`}
            />
          </TableContainer>
        </CardContent>
      </Card>

      {/* Diálogo de confirmação para exclusão */}
      <ConfirmDialog
        open={confirmDialogOpen}
        title="Confirmar exclusão"
        content="Tem certeza que deseja excluir este profissional? Esta ação não pode ser desfeita."
        onConfirm={handleConfirmDelete}
        onCancel={() => setConfirmDialogOpen(false)}
      />
    </Page>
  );
};

export default Professionals;
