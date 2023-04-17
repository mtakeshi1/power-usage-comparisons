FROM rust:1.68.2

WORKDIR /usr/src/app

COPY . .

RUN cargo install --path .

EXPOSE 8080

CMD ["axum_diesel"]