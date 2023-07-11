package v1

import (
	"github.com/gin-gonic/gin"
)

// Handle  http requests
type Controller interface {
	Create(c *gin.Context)
	GetAll(c *gin.Context)
	GetOne(c *gin.Context)
	Update(c *gin.Context)
	DeleteOne(c *gin.Context)
	//TODO:add other types
}

func NewAccountApiRoutes(handler *gin.RouterGroup, eH Controller) {
	// Routers
	h := handler.Group("/accounts")
	{
		h.POST("", eH.Create)
		h.GET("", eH.GetAll)
		h.GET("/:id", eH.GetOne)
	}

}
