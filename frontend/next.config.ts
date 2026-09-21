import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  /* config options here */
  images: {
    remotePatterns: [new URL('https://storage.googleapis.com/**'), new URL('http://commons.wikimedia.org/**'), new URL('https://commons.wikimedia.org/**'), new URL('http://localhost:3000')],
  },
};

export default nextConfig;
