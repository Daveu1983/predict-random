FROM golang:1.20 AS builder

WORKDIR /app

COPY go.mod ./
RUN go mod download

COPY . .

RUN go build -o random-service main.go

FROM gcr.io/distroless/base

COPY --from=builder /app/random-service /random-service

ENTRYPOINT ["/random-service"]