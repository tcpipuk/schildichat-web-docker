# Build stage
FROM node:19-alpine AS build

# Update and install the required packages
RUN apk --no-cache add \
    bash \
    curl \
    gcc \
    g++ \
    git \
    jq \
    make \
    openssl-dev \
    python3 \
    tcl \
    vim \
    yarn

# Clone the SchildiChat repository
WORKDIR /build
RUN git clone -b sc --recurse-submodules https://github.com/SchildiChat/schildichat-desktop.git

# Setup and build
WORKDIR /build/schildichat-desktop
RUN make setup
RUN make web-release

# Final stage
FROM nginx:mainline-alpine-slim

# Copy the built web files to the Nginx directory
COPY --from=build /build/schildichat-desktop/webapp /usr/share/nginx/html
