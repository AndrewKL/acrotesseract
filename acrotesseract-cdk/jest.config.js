module.exports = {
  displayName: 'acrotesseract-cdk',
  testEnvironment: 'node',
  roots: ['<rootDir>/cdk'],
  testMatch: ['**/*.spec.ts'],
  transform: {
    '^.+\\.ts$': [
      'ts-jest',
      { tsconfig: { module: 'CommonJS', moduleResolution: 'node', isolatedModules: true } },
    ],
  },
};
