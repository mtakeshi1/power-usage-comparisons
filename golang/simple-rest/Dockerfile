FROM ubuntu:lunar

WORKDIR /power

RUN apt -y update
RUN apt -y install golang ca-certificates
COPY go.mod go.sum main.go /power/
COPY api/ /power/api/
RUN go build

EXPOSE 8080
COPY .env /power/
ENV GIN_MODE=release
CMD go run /power/main.go
