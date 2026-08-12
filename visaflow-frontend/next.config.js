/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  /* Re-routes API calls to the docker-compose backend container service */
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: 'http://localhost:8080/api/:path*',
      },
    ];
  },
};

module.exports = nextConfig;
