package main

import (
	"log"

	"github.com/ilterpehlivan/fine-grained-auth-example/config"
	"github.com/ilterpehlivan/fine-grained-auth-example/internal/app"
)

func main() {
	// Configuration
	cfg, err := config.NewConfig()
	if err != nil {
		log.Fatalf("Config app_error: %s", err)
	}

	// Run
	app.Run(cfg)
}
