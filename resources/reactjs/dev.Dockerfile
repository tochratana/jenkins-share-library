# Stage 1: Build
FROM node:latest AS builder
WORKDIR /prod-stack

COPY package*.json ./
RUN npm install

COPY . .
RUN npm run build

# Stage 2: Run
FROM node:latest
WORKDIR /prod-stack

COPY --from=builder /prod-stack ./

ENV NODE_ENV=production
EXPOSE 3000

CMD ["npm", "run", "start"]