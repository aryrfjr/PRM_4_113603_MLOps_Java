# Production Dockerfile (used by Compose/Nginx)

###############################
#
# Stage 1: build Angular app
#
###############################

FROM node:18 AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build --prod

###############################
#
# Stage 2: serve with Nginx
#
###############################

FROM nginx:stable-alpine
COPY --from=build /app/dist/mlops-fe-angular /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf

