import type { NextConfig } from "next";
import path from "node:path";
import { fileURLToPath } from "node:url";

const nextConfig: NextConfig = {
  turbopack: {
    root: path.dirname(fileURLToPath(import.meta.url)),
  },
  async rewrites() {
    const origin = process.env.ORIONTASK_API_ORIGIN;
    return origin
      ? [{ source: "/api/:path*", destination: `${origin}/api/:path*` }]
      : [];
  },
};

export default nextConfig;
