function required(name: string): string {
  const value = process.env[name];
  if (!value) {
    throw new Error(`Missing required environment variable: ${name}`);
  }
  return value;
}
function bool(value: string | undefined, fallback = false): boolean {
  if (value === undefined) return fallback;
  return ["true", "1"].includes(value.toLowerCase());
}

export const publicConfig = {
  apiUrl: process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080",
  appName: process.env.NEXT_PUBLIC_APP_NAME ?? "My App",
  mockEnabled: bool(process.env.NEXT_PUBLIC_MOCK_ENABLED) ?? false
} as const;