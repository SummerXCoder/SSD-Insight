# ============================================================
# Dockerfile for Meeting Room Reservation System (Frontend)
# Multi-stage build: builder → runtime
# ============================================================

# -------------------- builder 阶段 --------------------
FROM node:20-alpine3.20 AS builder

WORKDIR /app

# 第一步：仅复制依赖描述文件，利用 Docker 层缓存优化依赖安装
COPY package.json package-lock.json ./

# 使用 npm ci 基于锁文件安装依赖，保证可复现构建
RUN npm ci

# 第二步：复制源码并执行构建
COPY . .

# Vite 构建产物输出到 dist/
RUN npm run build

# -------------------- runtime 阶段 --------------------
FROM nginx:1.27-alpine3.20 AS runtime

# 移除 Nginx 默认站点配置，避免冲突
RUN rm /etc/nginx/conf.d/default.conf

# 注入自定义 Nginx 站点配置
COPY docker/nginx.conf /etc/nginx/conf.d/default.conf

# 从 builder 阶段复制构建产物到 Nginx 静态资源目录
COPY --from=builder /app/dist /usr/share/nginx/html

# 暴露 80 端口
EXPOSE 80

# 使用 exec form 启动 Nginx（前台运行）
CMD ["nginx", "-g", "daemon off;"]
