package app

import (
	"context"
	"os"
	"os/signal"
	"syscall"

	"github.com/gin-gonic/gin"
	"github.com/ilterpehlivan/fine-grained-auth-example/config"
	"github.com/ilterpehlivan/fine-grained-auth-example/internal/controller"
	v1 "github.com/ilterpehlivan/fine-grained-auth-example/internal/controller/api/v1"
	"github.com/ilterpehlivan/fine-grained-auth-example/internal/model/entity"
	"github.com/ilterpehlivan/fine-grained-auth-example/internal/repo/memory"
	"github.com/ilterpehlivan/fine-grained-auth-example/internal/service"
	"github.com/ilterpehlivan/fine-grained-auth-example/pkg/httpserver"
	"github.com/ilterpehlivan/fine-grained-auth-example/pkg/logger"
	"github.com/ilterpehlivan/fine-grained-auth-example/pkg/spicedb"
)

// Run creates objects via constructors.
func Run(cfg *config.Config) {
	//init the logger
	logger.Init(cfg.Log.Level)

	//Service clients
	var ctx context.Context
	if cfg.Context() != nil {
		ctx = cfg.Context()
	} else {
		ctx = context.Background()
	}
	spiceCli, err := spicedb.NewClient(cfg)
	if err != nil {
		logger.Fatal().Err(err).Msg("spicedb client initialization app_error")
	}

	//Repository
	accountRepo := memory.New[entity.Account]()
	//Controllers
	accSvc := service.NewAccountService(ctx, cfg, spiceCli, accountRepo)
	controllers := map[string]v1.Controller{
		controller.AccountRestController: controller.NewAccountApiController(accSvc),
		//TODO:other controllers go here
	}

	// HTTP Server
	handler := gin.New()
	controller.NewRouter(handler, controllers)
	httpServer := httpserver.New(handler, httpserver.Port(cfg.HTTP.Port))

	// Waiting signal
	interrupt := make(chan os.Signal, 1)
	signal.Notify(interrupt, os.Interrupt, syscall.SIGTERM)

	select {
	case s := <-interrupt:
		logger.Info().Str("signal", s.String()).Msg("app - Run")
	case err = <-httpServer.Notify():
		logger.Err(err).Msg("app - Run - httpServer.Notify")

		// Shutdown
		err = httpServer.Shutdown()
		if err != nil {
			logger.Err(err).Msg("app - Run - httpServer.Shutdown")
		}
	}
}
