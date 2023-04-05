package db

import (
	"database/sql"
	"fmt"
	"os"

	_ "github.com/lib/pq"
)

type DbConnector struct {
	pgHost     string
	pgPort     string
	pgUser     string
	pgPwd      string
	pgDatabase string
}

var (
	dbConnector *DbConnector
)

func GetDbConnector() *DbConnector {
	if dbConnector != nil {
		return dbConnector
	}
	dbConnector = &DbConnector{
		pgHost:     os.Getenv("PG_HOST"),
		pgPort:     os.Getenv("PG_PORT"),
		pgUser:     os.Getenv("PG_USER"),
		pgPwd:      os.Getenv("PG_PWD"),
		pgDatabase: os.Getenv("PG_DATABASE"),
	}
	return dbConnector
}

func (d DbConnector) OpenDBConnection() *sql.DB {
	psqlInfo := fmt.Sprintf("host=%s port=%s user=%s password=%s dbname=%s sslmode=disable",
		d.pgHost, d.pgPort, d.pgUser, d.pgPwd, d.pgDatabase)

	db, err := sql.Open("postgres", psqlInfo)
	if err != nil {
		panic(err)
	}

	err = db.Ping()
	if err != nil {
		panic(err)
	}

	return db
}
