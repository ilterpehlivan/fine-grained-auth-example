package controller

import (
	"net/http"

	"github.com/gin-gonic/gin"
	v1 "github.com/ilterpehlivan/fine-grained-auth-example/internal/controller/api/v1"
	"github.com/ilterpehlivan/fine-grained-auth-example/internal/service"
	"github.com/ilterpehlivan/fine-grained-auth-example/pkg/logger"
	"github.com/prometheus/client_golang/prometheus/promhttp"
)

func NewAccountApiController(svc service.AccountService) v1.Controller {
	handler := v1.AccountHandler{
		Svc: svc,
	}
	return handler
}

const AccountRestController = "accountApi"

// NewRouter -.
// @host        localhost:8080
// @BasePath    /v1
func NewRouter(handler *gin.Engine, controllers map[string]v1.Controller) {
	// Options
	//handler.Use(gin.Logger())
	handler.Use(gin.Recovery())

	// K8s probe
	handler.GET("/healthz", func(c *gin.Context) { c.Status(http.StatusOK) })

	// Prometheus metrics
	handler.GET("/metrics", gin.WrapH(promhttp.Handler()))

	// Routers
	h := handler.Group("/v1")
	{
		if restController, ok := controllers[AccountRestController]; ok {
			v1.NewAccountApiRoutes(h, restController)

			//TODO:add here other interfaces if needed
		} else {
			logger.Fatal().Msg("missing parameter for controller")
		}
	}
}
