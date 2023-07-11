package repo

type Persist[T any] interface {
	List() ([]T, error)
	Get(id uint) (T, error)
	Create(entity T) (T, error)
	Update(id uint, entity T) (T, error)
	Delete(id uint) (bool, error)
}
