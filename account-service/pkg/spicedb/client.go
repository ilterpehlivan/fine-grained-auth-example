package spicedb

import (
	"github.com/authzed/authzed-go/v1"
	"github.com/authzed/grpcutil"
	"github.com/ilterpehlivan/fine-grained-auth-example/config"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
)

func NewClient(cfg *config.Config) (*authzed.Client, error) {
	client, err := authzed.NewClient(
		cfg.SpiceDbUrl,
		grpc.WithTransportCredentials(insecure.NewCredentials()),
		grpcutil.WithInsecureBearerToken(cfg.SpiceDbToken),
	)

	return client, err
}
