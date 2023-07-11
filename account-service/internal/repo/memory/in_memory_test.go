package memory

import (
	"testing"

	"github.com/ilterpehlivan/fine-grained-auth-example/internal/model/entity"
	"github.com/ilterpehlivan/fine-grained-auth-example/pkg/logger"
	"github.com/stretchr/testify/assert"
	"gorm.io/gorm"
)

func TestCreateAccount(t *testing.T) {
	logger.Init("info")
	persistMemory := New[entity.Account]()
	account1 := entity.Account{
		Model:      gorm.Model{},
		OwnerEmail: "test1",
		Currency:   "CAD",
		Balance:    0,
	}
	a1, _ := persistMemory.Create(account1)
	account2 := entity.Account{
		Model:      gorm.Model{ID: 2},
		OwnerEmail: "test2",
		Currency:   "CAD",
		Balance:    0,
	}
	a2, e := persistMemory.Create(account2)
	if e != nil {
		t.Fatalf("error while saving %v", e)
	}
	assert.NotNil(t, a1)
	assert.NotNil(t, a2)
	assert.Equal(t, uint(1), a1.ID)
	assert.Equal(t, uint(2), a2.ID)
	logger.Info().Msgf("id1: %v , id2: %v", a1.ID, a2.ID)

	ac, er := persistMemory.Get(1)
	if er != nil {
		t.Fatalf("error while reading %v", e)
	}
	assert.Equal(t, uint(1), ac.ID)

}
