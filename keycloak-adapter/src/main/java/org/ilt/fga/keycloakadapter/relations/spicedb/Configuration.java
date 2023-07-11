package org.ilt.fga.keycloakadapter.relations.spicedb;

import io.grpc.CallCredentials;
import net.devh.boot.grpc.client.autoconfigure.GrpcClientAutoConfiguration;
import net.devh.boot.grpc.client.autoconfigure.GrpcClientSecurityAutoConfiguration;
import net.devh.boot.grpc.client.autoconfigure.GrpcDiscoveryClientAutoConfiguration;
import net.devh.boot.grpc.client.security.CallCredentialsHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;

@ImportAutoConfiguration({
  GrpcClientAutoConfiguration.class,
  GrpcDiscoveryClientAutoConfiguration.class,
  GrpcClientSecurityAutoConfiguration.class
})
@org.springframework.context.annotation.Configuration(proxyBeanMethods = false)
public class Configuration {

  @Value("${spicedb.auth.token}")
  private String bearerToken;

  @Bean
  // Create credentials for username + password.
  CallCredentials grpcCredentials() {
    return CallCredentialsHelper.bearerAuth(bearerToken);
  }
}
