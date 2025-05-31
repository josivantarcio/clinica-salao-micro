import { format, parseISO, isValid } from 'date-fns';
import { ptBR } from 'date-fns/locale';

export const formatDate = (dateString: string | Date, dateFormat = 'dd/MM/yyyy'): string => {
  if (!dateString) return '-';
  
  const date = typeof dateString === 'string' ? parseISO(dateString) : dateString;
  
  if (!isValid(date)) return '-';
  
  return format(date, dateFormat, { locale: ptBR });
};

export const formatDateTime = (dateString: string | Date): string => {
  return formatDate(dateString, "dd/MM/yyyy 'às' HH:mm");
};

export const formatCurrency = (value: number | string): string => {
  const numberValue = typeof value === 'string' ? parseFloat(value) : value;
  
  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  }).format(numberValue);
};

export const formatCPF = (cpf: string): string => {
  if (!cpf) return '';
  
  // Remove qualquer caractere que não seja número
  const cleaned = cpf.replace(/\D/g, '');
  
  // Aplica a máscara: 000.000.000-00
  return cleaned
    .replace(/(\d{3})(\d)/, '$1.$2')
    .replace(/(\d{3})(\d)/, '$1.$2')
    .replace(/(\d{3})(\d{1,2})/, '$1-$2')
    .replace(/(\-\d{2})\d+?$/, '$1');
};

export const formatPhone = (phone: string): string => {
  if (!phone) return '';
  
  // Remove qualquer caractere que não seja número
  const cleaned = phone.replace(/\D/g, '');
  
  // Formatação para celular (XX) XXXXX-XXXX ou telefone fixo (XX) XXXX-XXXX
  if (cleaned.length === 11) {
    return cleaned
      .replace(/(\d{2})(\d{5})(\d{4})/, '($1) $2-$3');
  }
  
  return cleaned
    .replace(/(\d{2})(\d{4})(\d{4})/, '($1) $2-$3');
};
