package service

import (
	"context"
	"errors"
	"fmt"
	"strconv"

	v1 "github.com/authzed/authzed-go/proto/authzed/api/v1"
	"github.com/authzed/authzed-go/v1"
	"github.com/ilterpehlivan/fine-grained-auth-example/config"
	apperrors "github.com/ilterpehlivan/fine-grained-auth-example/internal/app/app_errors"
	"github.com/ilterpehlivan/fine-grained-auth-example/internal/model/dto"
	"github.com/ilterpehlivan/fine-grained-auth-example/internal/model/entity"
	"github.com/ilterpehlivan/fine-grained-auth-example/internal/repo"
	"github.com/ilterpehlivan/fine-grained-auth-example/pkg/logger"
	"gorm.io/gorm"
)

type key int

var UserKey = key(0)
var GroupKey = key(1)

func NewAccountService(ctx context.Context, cfg *config.Config, spiceCli *authzed.Client, db repo.Persist[entity.Account]) AccountService {
	return AccountService{ctx, cfg, spiceCli, db}
}

type AccountService struct {
	ctx      context.Context
	cfg      *config.Config
	spiceCli *authzed.Client
	db       repo.Persist[entity.Account]
}

func (ac AccountService) CreateAccount(aRequest dto.CreateAccount, ctx context.Context) (entity.Account, error) {
	//first save into repo
	account := entity.Account{
		Model:      gorm.Model{},
		OwnerEmail: aRequest.OwnerEmail,
		Currency:   aRequest.Currency,
		Balance:    0,
	}
	e, err := ac.db.Create(account)
	if err != nil {
		return e, err
	}

	//put the relationship tuples into authzed
	userId, ok := ctx.Value(UserKey).(string)
	if !ok {
		logger.Warn().Msg("no user is in context")
		return e, errors.New("user is missing from the context")
	}
	groupId, ok := ctx.Value(GroupKey).(string)
	if !ok {
		logger.Warn().Msg("no group is in context")
		return e, errors.New("groups is missing from the context")
	}
	erSpice := ac.createAccountRelations(ctx, e.ID, userId, groupId)
	return e, erSpice
}

func (ac AccountService) Get(ctx context.Context, id string) (entity.Account, error) {
	iId, _ := strconv.Atoi(id)
	a, err := ac.db.Get(uint(iId))
	return a, err
}

func (ac AccountService) createAccountRelations(ctx context.Context, acId uint, userId string, groupId string) error {
	acIdStr := fmt.Sprintf("%v", acId)
	logger.Info().Msgf("writing relationship: account:%v#owner@user:%v", acIdStr, userId)
	logger.Info().Msgf("writing relationship: account:%v#agent@group_agent:%v#member", acIdStr, groupId)
	// account:1#owner@user:client1
	// account:1#agent@group_agent:g1#member
	updates := []*v1.RelationshipUpdate{
		{
			Operation: v1.RelationshipUpdate_OPERATION_TOUCH,
			Relationship: &v1.Relationship{
				Resource: &v1.ObjectReference{
					ObjectType: "account",
					ObjectId:   acIdStr,
				},
				Relation: "owner",
				Subject: &v1.SubjectReference{
					Object: &v1.ObjectReference{
						ObjectType: "user",
						ObjectId:   userId,
					},
				},
			},
		}, {
			Operation: v1.RelationshipUpdate_OPERATION_TOUCH,
			Relationship: &v1.Relationship{
				Resource: &v1.ObjectReference{
					ObjectType: "account",
					ObjectId:   acIdStr,
				},
				Relation: "agent",
				Subject: &v1.SubjectReference{
					Object: &v1.ObjectReference{
						ObjectType: "group_agent",
						ObjectId:   groupId,
					},
					OptionalRelation: "member",
				},
			},
		},
	}

	//grant license: set is_activated_wsdm_user to user from postbody, and add relation to product_instance from path
	_, wrErr := ac.spiceCli.WriteRelationships(ctx, &v1.WriteRelationshipsRequest{
		Updates:               updates,
		OptionalPreconditions: nil,
	})

	logger.Info().Msgf("completed writing relationships, result:%v", wrErr)

	return wrErr
}

func (ac AccountService) CanUserReadAccount(ctx context.Context, user string, acId string) error {
	logger.Info().Msgf("checking permission: account:%v#can_read@user:%v", acId, user)
	s, o := createSubjectObjectTuple("user", user, "account", acId)
	//TODO:caveat
	resp, err := ac.spiceCli.CheckPermission(ctx, &v1.CheckPermissionRequest{
		Resource:   o,
		Permission: "can_read",
		Subject:    s,
	})

	if err != nil {
		e := fmt.Errorf("error while reading permissions:%v", err)
		logger.Err(e).Msg("spicedb error")
		return e
	}

	//check permissions: is user not already activated wsdm user?!
	if resp.Permissionship != v1.CheckPermissionResponse_PERMISSIONSHIP_HAS_PERMISSION {
		logger.Warn().Msgf("user %v does not have permission to account %v", user, acId)
		return apperrors.ErrAuthorization
	}

	return nil

}

func createSubjectObjectTuple(subjectType string, subjectValue string, objectType string, objectValue string) (*v1.SubjectReference, *v1.ObjectReference) {
	subject := &v1.SubjectReference{Object: &v1.ObjectReference{
		ObjectType: subjectType,
		ObjectId:   subjectValue,
	}}

	t1 := &v1.ObjectReference{
		ObjectType: objectType,
		ObjectId:   objectValue,
	}
	return subject, t1
}
