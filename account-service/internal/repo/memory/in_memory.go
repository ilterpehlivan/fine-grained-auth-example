package memory

import (
	"reflect"
	"sync"

	apperrors "github.com/ilterpehlivan/fine-grained-auth-example/internal/app/app_errors"
	"github.com/ilterpehlivan/fine-grained-auth-example/pkg/logger"
	"gorm.io/gorm/schema"
)

type PersistMemory[T any] struct {
	entities map[uint]T
}

func New[T any]() *PersistMemory[T] {
	return &PersistMemory[T]{
		entities: make(map[uint]T),
	}
}

func (pM *PersistMemory[T]) List() ([]T, error) {
	panic("not implemented") // TODO: Implement
}

func (pM *PersistMemory[T]) Get(id uint) (T, error) {
	logger.Info().Msgf("finding entity for id: %v from memory", id)
	if e, ok := pM.entities[id]; ok {
		return e, nil
	}

	var resp T
	return resp, apperrors.ErrAccountNotFound

}

func (pM *PersistMemory[T]) Create(entity T) (T, error) {
	logger.Info().Msgf("saving entity:%v into memory", entity)
	e, err := schema.Parse(&entity, &sync.Map{}, schema.NamingStrategy{})
	if err != nil {
		logger.Err(err).Msgf("failed to parse entity, got error")
		var n T
		return n, err
	}
	//This is a hack to find the ID value and set it if empty
	r := reflect.ValueOf(&entity)
	f := r.Elem().FieldByName(e.PrioritizedPrimaryField.Name)
	x := f.Uint()
	if x == 0 {
		x = uint64(len(pM.entities) + 1)
		f.SetUint(x)
	}
	pM.entities[uint(x)] = entity
	return entity, nil

}

func (pM *PersistMemory[T]) Update(id uint, entity T) (T, error) {
	panic("not implemented") // TODO: Implement
}

func (pM *PersistMemory[T]) Delete(id uint) (bool, error) {
	panic("not implemented") // TODO: Implement
}
