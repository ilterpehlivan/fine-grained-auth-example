package v1

import (
	"context"
	"errors"
	"net/http"
	"regexp"
	"strconv"
	"strings"

	"github.com/gin-gonic/gin"
	apperrors "github.com/ilterpehlivan/fine-grained-auth-example/internal/app/app_errors"
	"github.com/ilterpehlivan/fine-grained-auth-example/internal/model/dto"
	"github.com/ilterpehlivan/fine-grained-auth-example/internal/service"
	"github.com/ilterpehlivan/fine-grained-auth-example/pkg/logger"
)

// Handle account http requests
type AccountHandler struct {
	Svc service.AccountService
}

func (aH AccountHandler) Create(c *gin.Context) {
	account := &dto.CreateAccount{}
	err := c.Bind(account)
	if err != nil {
		logger.Err(err).Msg("CreateAccount:could not bind to request params")
		return
	}
	userId := c.GetHeader("X_USER_ID")
	groupStr := c.GetHeader("X_USER_GROUPS") //splitted by comma
	logger.Info().Str("user", userId).Str("groups", groupStr).Msgf("received create account request %v", account)
	if len(userId) == 0 || len(groupStr) == 0 {
		logger.Warn().Msg("X_USER_ID or X_USER_GROUPS headers cannot be empty")
		handleError(apperrors.ErrInvalidInput, c)
		return
	}

	ctx := getCtxWithHeaderValues(groupStr, c, userId)

	//now start processing event
	resp, err := aH.Svc.CreateAccount(*account, ctx)
	if err != nil {
		handleError(err, c)
		return
	}
	c.JSON(http.StatusOK, resp)
}

func (aH AccountHandler) GetAll(c *gin.Context) {
	panic("not implemented") // TODO: Implement
}

func (aH AccountHandler) GetOne(c *gin.Context) {
	id := c.Param("id")
	acId, _ := strconv.Atoi(id)
	if acId == 0 {
		logger.Warn().Msgf("request param id:%v should not be empty", id)
		handleError(apperrors.ErrInvalidInput, c)
		return
	}
	userId := c.GetHeader("X_USER_ID")
	if len(userId) == 0 {
		logger.Warn().Msg("X_USER_ID headers cannot be empty")
		handleError(apperrors.ErrInvalidInput, c)
		return
	}
	logger.Info().Str("user", userId).Str("accountId", id).Msg("received get account request")
	//check if user is authorized
	if err := aH.Svc.CanUserReadAccount(c, userId, id); err != nil {
		handleError(err, c)
		return
	}

	resp, err := aH.Svc.Get(c, id)
	if err != nil {
		handleError(err, c)
		return
	}
	c.JSON(http.StatusOK, resp)

}

func (aH AccountHandler) Update(c *gin.Context) {
	panic("not implemented") // TODO: Implement
}

func (aH AccountHandler) DeleteOne(c *gin.Context) {
	panic("not implemented") // TODO: Implement
}

func getCtxWithHeaderValues(groupStr string, c *gin.Context, userId string) context.Context {
	re := regexp.MustCompile(`/(branch.*)/(.+)`)
	var groupId string
	for _, s := range strings.Split(groupStr, ",") {
		if re.MatchString(s) {
			//g := re.FindStringSubmatch("/branch-1/group-2")
			groupId = strings.Split(s, ":")[1]
		}

	}
	ctxWithUserId := context.WithValue(c, service.UserKey, userId)
	ctx := context.WithValue(ctxWithUserId, service.GroupKey, groupId)
	return ctx
}

func handleError(err error, c *gin.Context) {
	if errors.Is(err, apperrors.ErrInternal) {
		c.AbortWithStatus(http.StatusInternalServerError)
		return
	}
	if errors.Is(err, apperrors.ErrAccountNotFound) {
		c.AbortWithStatus(http.StatusNotFound)
		return
	}
	if errors.Is(err, apperrors.ErrInvalidInput) {
		c.AbortWithStatus(http.StatusBadRequest)
		return
	}
	if errors.Is(err, apperrors.ErrAuthorization) {
		c.AbortWithStatus(http.StatusForbidden)
		return
	}
	c.AbortWithStatus(http.StatusInternalServerError)
}
