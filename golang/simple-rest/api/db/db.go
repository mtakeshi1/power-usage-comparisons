package db

import (
	"database/sql"
	"fmt"
	"log"
	"simple-rest/api/utils"
	"time"

	_ "github.com/lib/pq"
)

type DbConnector struct {
	pgHost                     string
	pgPort                     string
	pgUser                     string
	pgPwd                      string
	pgDatabase                 string
	pgMaxIddleConns            int
	pgMaxOpenConns             int
	pgConnMaxLifetimeInMinutes int
}

var (
	dbConnector *DbConnector
	db          *sql.DB
)

func GetDB() *sql.DB {
	if db != nil {
		return db
	}
	db = GetDbConnector().OpenDB()
	return db
}

func GetDbConnector() *DbConnector {
	if dbConnector != nil {
		return dbConnector
	}
	dbConnector = &DbConnector{
		pgHost:                     utils.GetEnvString("PG_HOST"),
		pgPort:                     utils.GetEnvString("PG_PORT"),
		pgUser:                     utils.GetEnvString("PG_USER"),
		pgPwd:                      utils.GetEnvString("PG_PWD"),
		pgDatabase:                 utils.GetEnvString("PG_DATABASE"),
		pgMaxIddleConns:            utils.GetEnvInteger("PG_MAX_IDDLE_CONNS"),
		pgMaxOpenConns:             utils.GetEnvInteger("PG_MAX_OPEN_CONNS"),
		pgConnMaxLifetimeInMinutes: utils.GetEnvInteger("PG_CONN_MAX_LIFETIME_IN_MINUTES"),
	}
	return dbConnector
}

func (d DbConnector) OpenDB() *sql.DB {
	psqlInfo := fmt.Sprintf("host=%s port=%s user=%s password=%s dbname=%s sslmode=disable",
		d.pgHost, d.pgPort, d.pgUser, d.pgPwd, d.pgDatabase)

	db, err := sql.Open("postgres", psqlInfo)
	if err != nil {
		log.Fatal(err)
	}

	db.SetMaxIdleConns(d.pgMaxIddleConns)
	db.SetMaxOpenConns(d.pgMaxOpenConns)
	db.SetConnMaxLifetime(time.Minute * time.Duration(d.pgConnMaxLifetimeInMinutes))

	return db
}

func PingDB() {
	db := GetDB()
	err := db.Ping()
	if err != nil {
		log.Fatal(err)
	}
}
