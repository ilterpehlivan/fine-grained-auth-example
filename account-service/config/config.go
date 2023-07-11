package config

import (
	"context"
	"log"

	"github.com/ilyakaznacheev/cleanenv"
)

type (
	// Config -.
	Config struct {
		App      `yaml:"app"`
		HTTP     `yaml:"http"`
		Log      `yaml:"logger"`
		Internal `yaml:"client"`
		ctx      context.Context
	}

	// App -.
	App struct {
		Name    string `yaml:"name"    env:"APP_NAME" env-default:"account-service"`
		Version string `yaml:"version" env:"APP_VERSION" env-default:"0.1"`
	}

	Internal struct {
		SpiceDbUrl   string `yaml:"spiceDbUrl" env:"SPICE_DB_URL" env-default:"localhost:50051"`
		SpiceDbToken string `yaml:"spiceDbToken" env:"SPICE_DB_TOKEN" env-default:"demo-key"`
	}

	// HTTP -.
	HTTP struct {
		Port string `yaml:"port" env:"HTTP_PORT" env-default:"8080"`
	}

	// Log -.
	Log struct {
		Level string `yaml:"log_level"   env:"LOG_LEVEL" env-default:"debug"`
	}
)

func (c Config) Context() context.Context {
	return c.ctx
}

// NewConfig returns app config.
func NewConfig() (*Config, error) {
	cfg := &Config{}

	err := cleanenv.ReadConfig("./config/config.yml", cfg)
	if err != nil {
		//return nil, fmt.Errorf("config app_error: %w", err)
		log.Println("could not read the config file,continue with env variables")
	}

	err = cleanenv.ReadEnv(cfg)
	if err != nil {
		return nil, err
	}

	return cfg, nil
}

// NewConfigWithContext returns app config with context
func NewConfigWithContext(ctx context.Context) (*Config, error) {
	cfg := &Config{}

	err := cleanenv.ReadConfig("./config/config.yml", cfg)
	if err != nil {
		//return nil, fmt.Errorf("config app_error: %w", err)
		log.Println("could not read the config file,continue with env variables")
	}

	err = cleanenv.ReadEnv(cfg)
	if err != nil {
		return nil, err
	}

	//set context
	cfg.ctx = ctx

	return cfg, nil
}
