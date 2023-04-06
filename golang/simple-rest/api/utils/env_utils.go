package utils

import (
	"fmt"
	"log"
	"os"
	"strconv"

	"github.com/joho/godotenv"
)

func LoadEnvParams() {
	err := godotenv.Load()
	if err != nil {
		log.Fatal(".env file couldn't be loaded")
	}
}

func GetEnvString(key string) string {
	value := os.Getenv(key)
	if value == "" {
		log.Fatal(fmt.Sprintf("Error getting env key: '%s' (It's required) ", key))
	}
	return value
}

func GetEnvInteger(key string) int {
	value := GetEnvString(key)
	i, err := strconv.Atoi(value)
	if err != nil {
		log.Fatal(fmt.Sprintf("Error getting env key: '%s' (It must be integer) ", key), err)
	}
	return i
}
