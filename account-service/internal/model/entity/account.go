package entity

import (
	"gorm.io/gorm"
)

type Account struct {
	gorm.Model
	// Id         int       `json:"id"`
	OwnerEmail string `json:"owner_email"`
	Currency   string `json:"currency"`
	Balance    int    `json:"balance"`
	// CreatedAt  time.Time `json:"created_at"`
	// UpdatedAt  time.Time `json:"updated_at"`
}
