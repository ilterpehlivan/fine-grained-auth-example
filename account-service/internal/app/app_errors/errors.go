package apperrors

import (
	"errors"
)

var (
	ErrInternal        = errors.New("internal app_error")
	ErrAccountNotFound = errors.New("account not found")
	ErrInvalidInput    = errors.New("invalid input")
	ErrAuthorization   = errors.New("not authorized to access resource")
	//TODO:other errors
)
