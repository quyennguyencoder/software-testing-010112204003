import type { NextConfig } from "next";


const nextConfig: NextConfig = {
  images: {
    remotePatterns: [
      {
        protocol: 'https',
        hostname: 'cdn.cellphones.com.vn',
      },
      {
        protocol: 'https',
        hostname: 'cdn2.cellphones.com.vn',
      },
      {
        protocol: 'https',
        hostname: 'cellphones.com.vn',
      },
      {
        protocol: 'http',
        hostname: 'localhost',
        port: '8081',
      },
      {
        protocol: 'https',
        hostname: 'example.com',
      },
      {
        protocol: 'https',
        hostname: 'cdn.tgdd.vn',
      },
      {
        protocol: 'https',
        hostname: 'cdnv2.tgdd.vn',
      },
      {
        protocol: 'https',
        hostname: '**', // Allow all HTTPS images
      },
      {
        protocol: 'http',
        hostname: '**', // Allow all HTTP images (for development)
      },
    ],
    unoptimized: true,
  },
};

export default nextConfig;
