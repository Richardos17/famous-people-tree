import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  /* config options here */
  images: {
    remotePatterns: [new URL('https://storage.googleapis.com/**'), new URL('https://commons.wikimedia.org/**')],
  },
};

export default nextConfig;
