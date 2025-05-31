// Extendendo o tipo Window para incluir propriedades globais
declare namespace NodeJS {
  interface ProcessEnv {
    NODE_ENV: 'development' | 'production' | 'test';
    REACT_APP_API_URL: string;
  }
}

// Adicione aqui outras extensões de tipos globais conforme necessário
