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

# Install Rust
RUN curl -sSf https://sh.rustup.rs | sh -s -- -y \
    && echo 'export PATH="$PATH:$HOME/.cargo/bin"' >> ~/.bashrc \
    && source ~/.bashrc

# Clone the SchildiChat repository
WORKDIR /build
RUN git clone -b sc --recurse-submodules https://github.com/SchildiChat/schildichat-desktop.git

# Handle matrix-js-sdk
WORKDIR /build/schildichat-desktop/matrix-js-sdk
RUN yarnpkg unlink &>/dev/null || true \
    && yarnpkg link \
    && yarnpkg install

# Handle matrix-react-sdk
WORKDIR /build/schildichat-desktop/matrix-react-sdk
RUN yarnpkg link matrix-js-sdk \
    && yarnpkg unlink &>/dev/null || true \
    && yarnpkg link \
    && yarnpkg install

# Handle element-web
WORKDIR /build/schildichat-desktop/element-web
RUN yarnpkg link matrix-js-sdk \
    && yarnpkg link matrix-react-sdk \
    && yarnpkg install

# Handle element-desktop
WORKDIR /build/schildichat-desktop/element-desktop
RUN yarnpkg install \
    && ln -s ../element-web/webapp ./ || true

# Build SchildiChat directly into /web-output
WORKDIR /build/schildichat-desktop
RUN cp configs/sc/config.json element-web/ \
    && yarnpkg --cwd element-web dist \
    && echo "$(grep version element-desktop/package.json | sed 's|.*: \"\(.*\)\",|\1|')" > element-web/webapp/version \
    && mkdir -p /schildichat-web \
    && tar -xzf element-web/dist/schildichat-web-*.tar.gz --strip-components=1 -C /schildichat-web \
    && cp configs/sc/config.json /schildichat-web/

# Final stage
FROM nginx:mainline-alpine-slim

# Copy the built web files to the Nginx directory
COPY --from=build /schildichat-web /usr/share/nginx/html
