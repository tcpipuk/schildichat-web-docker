# SchildiChat Web Docker Image

SchildiChat is an Element-based Matrix client that allows users to log in to a compatible homeserver using a web browser.

This repository provides a Docker container that repackages the current release of SchildiChat Desktop with Nginx for convenient deployment by homeserver administrators. All credits for the actual application go to the developers of [SchildiChat](https://github.com/SchildiChat/schildichat-desktop).

## Running with Docker

### Directly with Docker:

1. Pull the Docker image:
   ```bash
   docker pull tcpipuk/schildichat-web:latest
   ```

2. Download the default `config.json`:
   ```bash
   wget https://raw.githubusercontent.com/SchildiChat/schildichat-desktop/master/configs/sc/config.json
   ```

3. Edit the `config.json` to match the configuration of your homeserver.

4. Run the Docker container, mapping the `config.json` into the container:
   ```bash
   docker run -d -p 58008:80 -v $(pwd)/config.json:/usr/share/nginx/html/config.json tcpipuk/schildichat-web:latest
   ```

### With Docker Compose:

1. Create a `docker-compose.yml` file with the following content:

   ```yaml
   version: '3'

   services:
     schildichat-web:
       image: tcpipuk/schildichat-web:latest
       ports:
         - "58008:80"
       volumes:
         - ./config.json:/usr/share/nginx/html/config.json
   ```

2. Download the default `config.json`:
   ```bash
   wget https://raw.githubusercontent.com/SchildiChat/schildichat-desktop/master/configs/sc/config.json
   ```

3. Edit the `config.json` to match the configuration of your homeserver.

4. Start the service with Docker Compose:
   ```bash
   docker compose up -d
   ```

## HTTPS Configuration

The examples above serve SchildiChat over HTTP on port 58008 by default. If you wish to use HTTPS, it's recommended to place this container behind a reverse-proxy like Nginx. Below is an example Nginx configuration:

```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        proxy_pass http://localhost:58008;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # SSL configuration
    # listen 443 ssl;
    # ssl_certificate /path/to/your/certificate.crt;
    # ssl_certificate_key /path/to/your/private/key.key;
    # ... other SSL settings ...
}
```
