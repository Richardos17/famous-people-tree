import nextJest from "next/jest.js";
const createJestConfig = nextJest({
  dir: './',
})

const customJestConfig = {
  testEnvironment: 'jsdom',
  setupFilesAfterEnv: ['<rootDir>/jest.setup.js'],
  testMatch: [
    "<rootDir>/tests/**/*.test.(ts|tsx|js)"
  ],
}

export default createJestConfig(customJestConfig)