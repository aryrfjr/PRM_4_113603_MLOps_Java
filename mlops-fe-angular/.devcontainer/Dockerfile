FROM node:18

WORKDIR /workspace

RUN npm install -g @angular/cli

COPY package*.json ./
RUN npm install

COPY . .

