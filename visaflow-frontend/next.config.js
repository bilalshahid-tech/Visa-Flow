/** @type {import('next').NextConfig} */
const rawBackendUrl = process.env.BACKEND_URL || process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
const backendUrl = rawBackendUrl.replace(/\/+$/, '').replace(/\/api$/, '');

const nextConfig = {
  reactStrictMode: true,
  /* Re-routes API calls to the backend service (local docker container or production Render deployment) */
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: `${backendUrl}/api/:path*`,
      },
    ];
  },
};

module.exports = nextConfig;

