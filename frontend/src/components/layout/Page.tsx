import React, { ReactNode } from 'react';
import { Box, Container, Paper, Typography, useTheme } from '@mui/material';

interface PageProps {
  title: string;
  subtitle?: string;
  children: ReactNode;
  actions?: ReactNode;
  maxWidth?: 'xs' | 'sm' | 'md' | 'lg' | 'xl' | false;
  disableGutters?: boolean;
  paper?: boolean;
  sx?: object;
}

const Page: React.FC<PageProps> = ({
  title,
  subtitle,
  children,
  actions,
  maxWidth = 'lg',
  disableGutters = false,
  paper = true,
  sx = {},
}) => {
  const theme = useTheme();

  const content = (
    <Box
      sx={{
        flexGrow: 1,
        py: 4,
        ...sx,
      }}
    >
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          mb: 4,
          flexWrap: 'wrap',
          gap: 2,
        }}
      >
        <Box>
          <Typography variant="h4" component="h1" sx={{ mb: 0.5 }}>
            {title}
          </Typography>
          {subtitle && (
            <Typography variant="body1" color="text.secondary">
              {subtitle}
            </Typography>
          )}
        </Box>
        <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
          {actions}
        </Box>
      </Box>
      <Box>{children}</Box>
    </Box>
  );

  const containerContent = paper ? (
    <Paper
      elevation={0}
      sx={{
        borderRadius: 2,
        p: 3,
        backgroundColor: theme.palette.background.paper,
      }}
    >
      {content}
    </Paper>
  ) : (
    content
  );

  return (
    <Container maxWidth={maxWidth} disableGutters={disableGutters}>
      {containerContent}
    </Container>
  );
};

export default Page;
